package yooco.uchain.uchainwallet.global;

import android.text.TextUtils;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthBlockNumberCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthNonceCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthTransactionHistoryCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthTransactionReceiptCallback;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.GetEthBlockNumber;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.GetEthNonce;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.GetEthTransactionHistory;
import yooco.uchain.uchainwallet.utils.SharedPreferencesUtils;
import yooco.uchain.uchainwallet.utils.WalletUtils;

/**
 * Created by SteelCabbage on 2018/7/13 0013 11:55.
 * E-Mail：liuyi_61@163.com
 */

public class UpdateEthTxState implements IGetEthTransactionReceiptCallback, IGetEthTransactionHistoryCallback,
        IGetEthBlockNumberCallback, IGetEthNonceCallback {

    private static final String TAG = UpdateEthTxState.class.getSimpleName();

    private String mTxId;
    private ScheduledFuture mGetTxReceiptSF;
    private ScheduledFuture mUpdateTxRecordsSF;
    private ScheduledFuture mGetBlockNumberSF;
    private long mTxOfBlockNum;
    private long mCurrentBlockNum;
    private long mStartPollingTime;
    private String mHexNonce;

    public UpdateEthTxState(String txId) {
        mTxId = txId;
        mStartPollingTime = (long) SharedPreferencesUtils.getParam(UChainWalletApplication.getInstance(), mTxId, 0L);
        UChainLog.i(TAG, "startPollingTime:" + mStartPollingTime);
    }

    public void setGetTxReceiptSF(ScheduledFuture getTxReceiptSF) {
        mGetTxReceiptSF = getTxReceiptSF;
    }

    @Override
    public void getEthTransactionReceipt(String walletAddress, String blockNumber, boolean isSuccess) {
        if (TextUtils.isEmpty(mTxId) || TextUtils.isEmpty(walletAddress) || null == mGetTxReceiptSF) {
            UChainLog.e(TAG, "getEthTransactionReceipt() -> mTxId or walletAddress or mGetTxReceiptSF is null!");
            return;
        }

        if (!TextUtils.isEmpty(blockNumber)) {
            handleBlockNum(walletAddress, blockNumber);
            return;
        }

        if (System.currentTimeMillis() - mStartPollingTime > Constant.TX_ETH_EXCEPTION_TIME) {
            UChainLog.w(TAG, "over 5 minutes,handle failed tx");
            mHexNonce = (String) SharedPreferencesUtils.getParam(UChainWalletApplication.getInstance(), Constant.TX_ETH_NONCE
                    + mTxId, "");
            TaskController.getInstance().submit(new GetEthNonce(walletAddress, this));
        }
    }

    private void handleBlockNum(String walletAddress, String txOfblockNumber) {
        if (TextUtils.isEmpty(txOfblockNumber)) {
            UChainLog.w(TAG, "handleBlockNum()-> txOfblockNumber is null,this tx is not in block!");
            return;
        }

        try {
            mTxOfBlockNum = Long.valueOf(txOfblockNumber);
        } catch (NumberFormatException e) {
            UChainLog.e(TAG, "getEthTransactionReceipt NumberFormatException:" + e.getMessage());
            return;
        }

        mGetTxReceiptSF.cancel(true);
        mGetBlockNumberSF = TaskController.getInstance().schedule(new GetEthBlockNumber(this), 0, Constant.TX_ETH_POLLING_TIME);
        mUpdateTxRecordsSF = TaskController.getInstance().schedule(new GetEthTransactionHistory(walletAddress, this), 0,
                Constant.TX_ETH_POLLING_TIME);
    }

    @Override
    public void getEthBlockNumber(String blockNumber) {
        if (TextUtils.isEmpty(blockNumber)) {
            UChainLog.e(TAG, "getEthBlockNumber() -> blockNumber is null!");
            return;
        }

        try {
            mCurrentBlockNum = Long.valueOf(blockNumber);
        } catch (NumberFormatException e) {
            UChainLog.e(TAG, "getEthBlockNumber NumberFormatException:" + e.getMessage());
            return;
        }

        if (mCurrentBlockNum - mTxOfBlockNum >= Constant.TX_ETH_CONFIRM_OK) {
            UChainLog.i(TAG, "TX_ETH_CONFIRM_OK");
            mUpdateTxRecordsSF.cancel(false);
            mGetBlockNumberSF.cancel(false);
            SharedPreferencesUtils.remove(UChainWalletApplication.getInstance(), mTxId);
            SharedPreferencesUtils.remove(UChainWalletApplication.getInstance(), Constant.TX_ETH_NONCE + mTxId);
        }
    }

    @Override
    public void getEthTransactionHistory(List<TransactionRecord> transactionRecords) {
        if (Constant.TX_ETH_CONFIRM_OK > mCurrentBlockNum - mTxOfBlockNum) {
            return;
        }

        handleTx();
    }

    private void handleTx() {
        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        List<TransactionRecord> cacheTxs = uChainWalletDbDao.queryTxCacheByTxId(Constant.TABLE_ETH_TX_CACHE, mTxId);
        if (null == cacheTxs || cacheTxs.isEmpty()) {
            uChainWalletDbDao.updateTxState(Constant.TABLE_ETH_TRANSACTION_RECORD, mTxId, Constant.TRANSACTION_STATE_SUCCESS);
            UChainListeners.getInstance().notifyTxStateUpdate(mTxId, Constant.TRANSACTION_STATE_SUCCESS, Constant
                    .NO_NEED_MODIFY_TX_TIME);
            return;
        }

        for (TransactionRecord cacheTx : cacheTxs) {
            if (null == cacheTx) {
                UChainLog.e(TAG, "cacheTx is null!");
                continue;
            }

            cacheTx.setTxState(Constant.TRANSACTION_STATE_FAIL);
            uChainWalletDbDao.insertTxRecord(Constant.TABLE_ETH_TRANSACTION_RECORD, cacheTx);
        }
        UChainListeners.getInstance().notifyTxStateUpdate(mTxId, Constant.TRANSACTION_STATE_FAIL, Constant.NO_NEED_MODIFY_TX_TIME);
        uChainWalletDbDao.delCacheByTxId(Constant.TABLE_ETH_TX_CACHE, mTxId);
    }

    @Override
    public void getEthNonce(String nonce) {
        if (TextUtils.isEmpty(nonce)) {
            UChainLog.e(TAG, "nonce is null!");
            return;
        }

        try {
            String currentNonce = WalletUtils.toDecString(nonce, "0");
            String txNonce = WalletUtils.toDecString(mHexNonce, "0");
            UChainLog.i(TAG, "currentNonce:" + currentNonce);
            UChainLog.i(TAG, "txNonce:" + txNonce);

            BigInteger subtract = new BigInteger(currentNonce).subtract(new BigInteger(txNonce));
            if (BigInteger.ZERO.compareTo(subtract) == 0 || BigInteger.ZERO.compareTo(subtract) == 1) {
                UChainLog.w(TAG, "this txNonce is valid!");
                return;
            }
        } catch (Exception e) {
            UChainLog.e(TAG, "UpdateEthTxState getEthNonce Exception:" + e.getMessage());
            return;
        }

        mGetTxReceiptSF.cancel(true);
        handleTx();
        SharedPreferencesUtils.remove(UChainWalletApplication.getInstance(), mTxId);
        SharedPreferencesUtils.remove(UChainWalletApplication.getInstance(), Constant.TX_ETH_NONCE + mTxId);
    }
}
