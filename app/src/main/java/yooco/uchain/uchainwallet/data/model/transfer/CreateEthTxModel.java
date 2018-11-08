package yooco.uchain.uchainwallet.data.model.transfer;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.Map;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.bean.AssetBean;
import yooco.uchain.uchainwallet.data.bean.BalanceBean;
import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.data.bean.gasfee.EthTxFee;
import yooco.uchain.uchainwallet.data.bean.gasfee.ITxFee;
import yooco.uchain.uchainwallet.data.bean.tx.EthTxBean;
import yooco.uchain.uchainwallet.data.bean.tx.ITxBean;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainGlobalTask;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.eth.ICreateErc20TxCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.ICreateEthTxCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IEthSendRawTransactionCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthBalanceCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthNonceCallback;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.CreateErc20Tx;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.CreateEthTx;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.EthSendRawTransaction;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.GetEthBalance;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.GetEthNonce;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.SharedPreferencesUtils;
import yooco.uchain.uchainwallet.utils.WalletUtils;

/**
 * Created by SteelCabbage on 2018/8/24 0024 15:56.
 * E-Mail：liuyi_61@163.com
 */
public class CreateEthTxModel implements ICreateTxModel, ICreateEthTxCallback, IEthSendRawTransactionCallback,
        IGetEthNonceCallback, ICreateErc20TxCallback, IGetEthBalanceCallback {

    private static final String TAG = CreateEthTxModel.class.getSimpleName();

    private ICreateTxModelCallback mICreateTxModelCallback;
    private EthTxFee mEthTxFee;
    private EthTxBean mEthTxBean;

    public CreateEthTxModel(ICreateTxModelCallback ICreateTxModelCallback) {
        mICreateTxModelCallback = ICreateTxModelCallback;
    }

    @Override
    public void checkTxFee(ITxFee iTxFee) {
        if (null == mICreateTxModelCallback) {
            UChainLog.e(TAG, "mICreateTxModelCallback is null!");
            return;
        }

        if (null == iTxFee) {
            UChainLog.e(TAG, "iTxFee is null!");
            return;
        }

        if (iTxFee instanceof EthTxFee) {
            mEthTxFee = (EthTxFee) iTxFee;
        }

        if (null == mEthTxFee) {
            UChainLog.e(TAG, "mEthTxFee is null!");
            return;
        }

        UChainLog.i(TAG, "mEthTxFee:" + mEthTxFee.toString());

        TaskController.getInstance().submit(new GetEthBalance(mEthTxFee.getAddress(), this));
    }

    @Override
    public void getEthBalance(Map<String, BalanceBean> balanceBeans) {
        if (null == balanceBeans || balanceBeans.isEmpty()) {
            UChainLog.e(TAG, "balanceBeans is null or empty!");
            mICreateTxModelCallback.checkTxFee(false, UChainWalletApplication.getInstance().getResources().getString(R.string
                    .insufficient_balance));
            return;
        }

        BalanceBean balanceBean = balanceBeans.get(Constant.ASSETS_ETH);
        if (null == balanceBean) {
            UChainLog.e(TAG, "balanceBean is null!");
            mICreateTxModelCallback.checkTxFee(false, UChainWalletApplication.getInstance().getResources().getString(R.string
                    .insufficient_balance));
            return;
        }

        String assetType = mEthTxFee.getAssetType();
        if (TextUtils.isEmpty(assetType)) {
            UChainLog.e(TAG, "assetType is null or empty!");
            mICreateTxModelCallback.checkTxFee(false, UChainWalletApplication.getInstance().getResources().getString(R.string
                    .no_this_type_asset));
            return;
        }

        switch (assetType) {
            case Constant.ASSET_TYPE_ETH:
                checkEthIsEnough();
                break;
            case Constant.ASSET_TYPE_ERC20:
                checkErc20IsEnough(balanceBean.getAssetsValue());
                break;
            default:
                mICreateTxModelCallback.checkTxFee(false, UChainWalletApplication.getInstance().getResources().getString(R.string
                        .no_this_type_asset));
                break;
        }
    }

    private void checkEthIsEnough() {
        try {
            BigDecimal balance = new BigDecimal(mEthTxFee.getBalance());
            BigDecimal amount = new BigDecimal(mEthTxFee.getAmount());
            BigDecimal gasPrice = new BigDecimal(mEthTxFee.getGasPrice()).divide(new BigDecimal(10).pow(9));
            BigDecimal gasLimit = new BigDecimal(mEthTxFee.getGasLimit());

            BigDecimal bigDecimal = gasPrice.multiply(gasLimit).add(amount).subtract(balance);

            int compareTo = BigDecimal.ZERO.compareTo(bigDecimal);
            if (compareTo == 1 || compareTo == 0) {
                mICreateTxModelCallback.checkTxFee(true, null);
                return;
            }

            mICreateTxModelCallback.checkTxFee(false, UChainWalletApplication.getInstance().getResources().getString(R.string
                    .insufficient_balance));
        } catch (Exception e) {
            mICreateTxModelCallback.checkTxFee(false, UChainWalletApplication.getInstance().getResources().getString(R.string
                    .illegal_input));
            UChainLog.e(TAG, "checkTxFee Exception:" + e.getMessage());
        }
    }

    private void checkErc20IsEnough(String ethBalance) {
        try {
            BigDecimal ethBalanceFee = new BigDecimal(ethBalance);
            BigDecimal balance = new BigDecimal(mEthTxFee.getBalance());
            BigDecimal amount = new BigDecimal(mEthTxFee.getAmount());
            BigDecimal gasPrice = new BigDecimal(mEthTxFee.getGasPrice()).divide(new BigDecimal(10).pow(9));
            BigDecimal gasLimit = new BigDecimal(mEthTxFee.getGasLimit());

            if (amount.compareTo(balance) == 1) {
                mICreateTxModelCallback.checkTxFee(false, UChainWalletApplication.getInstance()
                        .getResources().getString(R.string.insufficient_balance));
                return;
            }

            BigDecimal gasFee = gasPrice.multiply(gasLimit);

            if (gasFee.compareTo(ethBalanceFee) == 1) {
                mICreateTxModelCallback.checkTxFee(false, UChainWalletApplication.getInstance()
                        .getResources().getString(R.string.insufficient_gas));
                return;
            }

            mICreateTxModelCallback.checkTxFee(true, null);
        } catch (Exception e) {
            mICreateTxModelCallback.checkTxFee(false, UChainWalletApplication.getInstance().getResources().getString(R.string
                    .illegal_input));
            UChainLog.e(TAG, "checkTxFee Exception:" + e.getMessage());
        }
    }

    @Override
    public void createGlobalTx(ITxBean iTxBean) {
        if (null == mICreateTxModelCallback) {
            UChainLog.e(TAG, "mICreateTxModelCallback is null!");
            return;
        }

        if (null == iTxBean) {
            UChainLog.e(TAG, "iTxBean is null!");
            return;
        }

        if (iTxBean instanceof EthTxBean) {
            mEthTxBean = (EthTxBean) iTxBean;
        }

        if (null == mEthTxBean) {
            UChainLog.e(TAG, "mEthTxBean is null!");
            return;
        }

        TaskController.getInstance().submit(new GetEthNonce(mEthTxBean.getFromAddress(), this));
    }

    @Override
    public void createColorTx(ITxBean iTxBean) {
        if (null == mICreateTxModelCallback) {
            UChainLog.e(TAG, "mICreateTxModelCallback is null!");
            return;
        }

        if (null == iTxBean) {
            UChainLog.e(TAG, "iTxBean is null!");
            return;
        }

        if (iTxBean instanceof EthTxBean) {
            mEthTxBean = (EthTxBean) iTxBean;
        }

        if (null == mEthTxBean) {
            UChainLog.e(TAG, "mEthTxBean is null!");
            return;
        }

        TaskController.getInstance().submit(new GetEthNonce(mEthTxBean.getFromAddress(), this));
    }

    @Override
    public void getEthNonce(String nonce) {
        if (TextUtils.isEmpty(nonce)) {
            UChainLog.e(TAG, "nonce is null!");
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .eth_nonce_null), false);
            return;
        }

        mEthTxBean.setNonce(nonce);

        String assetType = mEthTxBean.getAssetType();
        if (TextUtils.isEmpty(assetType)) {
            UChainLog.e(TAG, "assetType is null!");
            return;
        }

        switch (assetType) {
            case Constant.ASSET_TYPE_ETH:
                TaskController.getInstance().submit(new CreateEthTx(mEthTxBean, this));
                break;
            case Constant.ASSET_TYPE_ERC20:
                TaskController.getInstance().submit(new CreateErc20Tx(mEthTxBean, this));
                break;
            default:
                UChainLog.w(TAG, "illegal asset");
                break;
        }
    }

    @Override
    public void createEthTx(String data) {
        if (TextUtils.isEmpty(data)) {
            UChainLog.e(TAG, "createEthTx() -> data is null!");
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .eth_data_null), false);
            return;
        }

        UChainLog.i(TAG, "createEthTx() -> data:" + data);
        TaskController.getInstance().submit(new EthSendRawTransaction(data, this));
    }

    @Override
    public void createErc20Tx(String data) {
        if (TextUtils.isEmpty(data)) {
            UChainLog.e(TAG, "createErc20Tx() -> data is null!");
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .eth_data_null), false);
            return;
        }

        UChainLog.i(TAG, "createErc20Tx() -> data:" + data);
        TaskController.getInstance().submit(new EthSendRawTransaction(data, this));
    }

    @Override
    public void ethSendRawTransaction(Boolean isSendSuccess, String txId) {
        if (TextUtils.isEmpty(txId)) {
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .server_err_txId_null), false);
            return;
        }

        // write db
        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .db_exception), true);
            return;
        }

        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setWalletAddress(mEthTxBean.getFromAddress());
        transactionRecord.setTxType(mEthTxBean.getAssetType());
        transactionRecord.setTxID(txId);
        String amountDec = WalletUtils.toDecString(mEthTxBean.getAmount(), String.valueOf(mEthTxBean.getAssetDecimal()));
        transactionRecord.setTxAmount("-" + amountDec);
        transactionRecord.setTxFrom(mEthTxBean.getFromAddress());
        transactionRecord.setTxTo(mEthTxBean.getToAddress());
        transactionRecord.setTxTime(0);

        AssetBean assetBean = uChainWalletDbDao.queryAssetByHash(Constant.TABLE_ETH_ASSETS, mEthTxBean.getAssetID());
        if (null == assetBean) {
            UChainLog.e(TAG, "assetBean is null!");
            return;
        }

        transactionRecord.setAssetID(mEthTxBean.getAssetID());
        transactionRecord.setAssetLogoUrl(assetBean.getImageUrl());
        transactionRecord.setAssetSymbol(assetBean.getSymbol());

        if (isSendSuccess) {
            transactionRecord.setTxState(Constant.TRANSACTION_STATE_PACKAGING);
            uChainWalletDbDao.insertTxRecord(Constant.TABLE_ETH_TX_CACHE, transactionRecord);
        } else {
            transactionRecord.setTxState(Constant.TRANSACTION_STATE_FAIL);
            uChainWalletDbDao.insertTxRecord(Constant.TABLE_ETH_TRANSACTION_RECORD, transactionRecord);
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .transaction_broadcast_failed), true);
        }

        // start polling
        SharedPreferencesUtils.putParam(UChainWalletApplication.getInstance(), txId, System.currentTimeMillis());
        SharedPreferencesUtils.putParam(UChainWalletApplication.getInstance(), Constant.TX_ETH_NONCE + txId,
                mEthTxBean.getNonce());
        UChainGlobalTask.getInstance().startEthPolling(txId, mEthTxBean.getFromAddress());
        mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                .transaction_broadcast_successful), true);
    }

}
