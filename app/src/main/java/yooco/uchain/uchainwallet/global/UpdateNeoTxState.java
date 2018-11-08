package yooco.uchain.uchainwallet.global;

import android.text.TextUtils;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.IGetNeoTransactionHistoryCallback;
import yooco.uchain.uchainwallet.utils.task.callback.IGetRawTransactionCallback;
import yooco.uchain.uchainwallet.utils.task.runnable.GetNeoTransactionHistory;

/**
 * Created by SteelCabbage on 2018/7/13 0013 11:55.
 * E-Mail：liuyi_61@163.com
 */

public class UpdateNeoTxState implements IGetRawTransactionCallback, IGetNeoTransactionHistoryCallback {

    private static final String TAG = UpdateNeoTxState.class.getSimpleName();

    private String mTxId;
    private ScheduledFuture mScheduledFuture;
    private long mConfirmations;

    public UpdateNeoTxState(String txId) {
        mTxId = txId;
    }

    public void setScheduledFuture(ScheduledFuture scheduledFuture) {
        mScheduledFuture = scheduledFuture;
    }

    @Override
    public void getRawTransaction(String txId, String walletAddress, long confirmations) {
        if (null == mScheduledFuture
                || TextUtils.isEmpty(mTxId)
                || TextUtils.isEmpty(walletAddress)) {
            UChainLog.e(TAG, "mUpdateTxStateSF or txId or walletAddress is null!");
            return;
        }

        if (Constant.TX_CONFIRM_EXCEPTION == confirmations) {
            UChainLog.e(TAG, "TX_CONFIRM_EXCEPTION");
            return;
        }

        if (Constant.TX_UN_CONFIRM == confirmations) {
            UChainLog.w(TAG, "TX_UN_CONFIRM");
            return;
        }

        if (Constant.TX_NEO_CONFIRM_OK <= confirmations) {
            UChainLog.i(TAG, "TX_NEO_CONFIRM_OK");
            mScheduledFuture.cancel(false);
        }

        mConfirmations = confirmations;
        TaskController.getInstance().submit(new GetNeoTransactionHistory(walletAddress, this));
    }

    @Override
    public void getNeoTransactionHistory(List<TransactionRecord> transactionRecords) {
        if (Constant.TX_NEO_CONFIRM_OK > mConfirmations) {
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

        List<TransactionRecord> cacheTxs = uChainWalletDbDao.queryTxCacheByTxId(Constant.TABLE_NEO_TX_CACHE, mTxId);
        if (null == cacheTxs || cacheTxs.isEmpty()) {
            uChainWalletDbDao.updateTxState(Constant.TABLE_NEO_TRANSACTION_RECORD, mTxId, Constant.TRANSACTION_STATE_SUCCESS);
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
            uChainWalletDbDao.insertTxRecord(Constant.TABLE_NEO_TRANSACTION_RECORD, cacheTx);
        }
        UChainListeners.getInstance().notifyTxStateUpdate(mTxId, Constant.TRANSACTION_STATE_FAIL, Constant.NO_NEED_MODIFY_TX_TIME);
        uChainWalletDbDao.delCacheByTxId(Constant.TABLE_NEO_TX_CACHE, mTxId);
    }
}
