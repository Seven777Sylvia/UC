package yooco.uchain.uchainwallet.data.model.transfer;

import android.text.TextUtils;

import java.math.BigDecimal;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.bean.AssertTxBean;
import yooco.uchain.uchainwallet.data.bean.AssetBean;
import yooco.uchain.uchainwallet.data.bean.Nep5TxBean;
import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.data.bean.gasfee.ITxFee;
import yooco.uchain.uchainwallet.data.bean.gasfee.NeoTxFee;
import yooco.uchain.uchainwallet.data.bean.tx.ITxBean;
import yooco.uchain.uchainwallet.data.bean.tx.NeoTxBean;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainGlobalTask;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.ICreateAssertTxCallback;
import yooco.uchain.uchainwallet.utils.task.callback.ICreateNep5TxCallback;
import yooco.uchain.uchainwallet.utils.task.callback.IGetUtxosCallback;
import yooco.uchain.uchainwallet.utils.task.callback.ISendRawTransactionCallback;
import yooco.uchain.uchainwallet.utils.task.runnable.CreateAssertTx;
import yooco.uchain.uchainwallet.utils.task.runnable.CreateNep5Tx;
import yooco.uchain.uchainwallet.utils.task.runnable.GetUtxos;
import yooco.uchain.uchainwallet.utils.task.runnable.SendRawTransaction;
import yooco.uchain.uchainwallet.global.Constant;
import neomobile.Tx;
import neomobile.Wallet;

/**
 * Created by SteelCabbage on 2018/8/24 0024 15:48.
 * E-Mail：liuyi_61@163.com
 */
public class CreateNeoTxModel implements ICreateTxModel, IGetUtxosCallback, ICreateAssertTxCallback,
        ISendRawTransactionCallback, ICreateNep5TxCallback {

    private static final String TAG = CreateNeoTxModel.class.getSimpleName();

    private ICreateTxModelCallback mICreateTxModelCallback;
    private NeoTxFee mNeoTxFee;
    private NeoTxBean mNeoTxBean;
    private String mOrder;

    public CreateNeoTxModel(ICreateTxModelCallback ICreateTxModelCallback) {
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

        if (iTxFee instanceof NeoTxFee) {
            mNeoTxFee = (NeoTxFee) iTxFee;
        }

        if (null == mNeoTxFee) {
            UChainLog.e(TAG, "mNeoTxFee is null!");
            return;
        }

        UChainLog.i(TAG, "mNeoTxFee:" + mNeoTxFee.toString());

        try {
            BigDecimal balance = new BigDecimal(mNeoTxFee.getBalance());
            BigDecimal amount = new BigDecimal(mNeoTxFee.getAmount());

            if (amount.compareTo(balance) == 1) {
                mICreateTxModelCallback.checkTxFee(false, UChainWalletApplication.getInstance()
                        .getResources().getString(R.string.insufficient_balance));
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

        if (iTxBean instanceof NeoTxBean) {
            mNeoTxBean = (NeoTxBean) iTxBean;
        }

        if (null == mNeoTxBean) {
            UChainLog.e(TAG, "mNeoTxBean is null!");
            return;
        }

        Wallet wallet = mNeoTxBean.getWallet();
        if (null == wallet) {
            UChainLog.e(TAG, "neo wallet is null!");
            return;
        }

        TaskController.getInstance().submit(new GetUtxos(wallet.address(), this));
    }

    @Override
    public void getUtxos(String utxos) {
        if (TextUtils.isEmpty(utxos) || "[]".equals(utxos)) {
            UChainLog.e(TAG, "utxos is null or []!");
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .generate_utxo_failed), false);
            return;
        }

        AssertTxBean assertTxBean = new AssertTxBean();
        assertTxBean.setAssetsID(mNeoTxBean.getAssetID());
        assertTxBean.setAddrFrom(mNeoTxBean.getFromAddress());
        assertTxBean.setAddrTo(mNeoTxBean.getToAddress());
        assertTxBean.setTransferAmount(Double.valueOf(mNeoTxBean.getAmount()));
        assertTxBean.setUtxos(utxos);

        TaskController.getInstance().submit(new CreateAssertTx(mNeoTxBean.getWallet(), assertTxBean, this));
    }

    @Override
    public void createAssertTx(Tx tx) {
        if (null == tx) {
            UChainLog.e(TAG, "createAssertTx() -> tx is null！");
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .transaction_creation_failed), false);
            return;
        }

        mOrder = "0x" + tx.getID();
        UChainLog.i(TAG, "createAssertTx order:" + mOrder);
        TaskController.getInstance().submit(new SendRawTransaction(tx.getData(), this));
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

        if (iTxBean instanceof NeoTxBean) {
            mNeoTxBean = (NeoTxBean) iTxBean;
        }

        if (null == mNeoTxBean) {
            UChainLog.e(TAG, "mNeoTxBean is null!");
            return;
        }

        Nep5TxBean nep5TxBean = new Nep5TxBean();
        nep5TxBean.setAssetID(mNeoTxBean.getAssetID());
        nep5TxBean.setAssetDecimal(mNeoTxBean.getAssetDecimal());
        nep5TxBean.setAddrFrom(mNeoTxBean.getFromAddress());
        nep5TxBean.setAddrTo(mNeoTxBean.getToAddress());
        nep5TxBean.setTransferAmount(mNeoTxBean.getAmount());
        nep5TxBean.setUtxos("[]");

        TaskController.getInstance().submit(new CreateNep5Tx(mNeoTxBean.getWallet(), nep5TxBean, this));
    }

    @Override
    public void createNep5Tx(Tx tx) {
        if (null == tx) {
            UChainLog.e(TAG, "createNep5Tx() -> tx is null！");
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .transaction_creation_failed), false);
            return;
        }

        mOrder = "0x" + tx.getID();
        UChainLog.i(TAG, "createNep5Tx order:" + mOrder);

        TaskController.getInstance().submit(new SendRawTransaction(tx.getData(), this));
    }

    @Override
    public void sendTxData(Boolean isSuccess) {
        // write db
        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .db_exception), true);
            return;
        }

        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setWalletAddress(mNeoTxBean.getFromAddress());
        transactionRecord.setTxType(mNeoTxBean.getAssetType());
        transactionRecord.setTxAmount("-" + mNeoTxBean.getAmount());
        transactionRecord.setTxFrom(mNeoTxBean.getFromAddress());
        transactionRecord.setTxTo(mNeoTxBean.getToAddress());
        transactionRecord.setTxTime(0);
        transactionRecord.setTxID(mOrder);

        AssetBean assetBean = uChainWalletDbDao.queryAssetByHash(Constant.TABLE_NEO_ASSETS, mNeoTxBean.getAssetID());
        if (null == assetBean) {
            UChainLog.e(TAG, "assetBean is null!");
            return;
        }

        transactionRecord.setAssetID(mNeoTxBean.getAssetID());
        transactionRecord.setAssetLogoUrl(assetBean.getImageUrl());
        transactionRecord.setAssetSymbol(assetBean.getSymbol());

        if (isSuccess) {
            transactionRecord.setTxState(Constant.TRANSACTION_STATE_PACKAGING);
            uChainWalletDbDao.insertTxRecord(Constant.TABLE_NEO_TX_CACHE, transactionRecord);
        } else {
            transactionRecord.setTxState(Constant.TRANSACTION_STATE_FAIL);
            uChainWalletDbDao.insertTxRecord(Constant.TABLE_NEO_TRANSACTION_RECORD, transactionRecord);
            mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                    .transaction_broadcast_failed), true);
            return;
        }

        // start polling
        UChainGlobalTask.getInstance().startNeoPolling(mOrder, mNeoTxBean.getFromAddress());
        mICreateTxModelCallback.CreateTxModel(UChainWalletApplication.getInstance().getResources().getString(R.string
                .transaction_broadcast_successful), true);
    }

}
