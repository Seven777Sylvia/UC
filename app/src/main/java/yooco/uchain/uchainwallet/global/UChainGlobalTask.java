package yooco.uchain.uchainwallet.global;

import android.text.TextUtils;

import java.util.List;
import java.util.concurrent.ScheduledFuture;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.ICheckIsUpdateNeoAssetsCallback;
import yooco.uchain.uchainwallet.utils.task.callback.ICheckIsUpdateNeoTxStateCallback;
import yooco.uchain.uchainwallet.utils.task.callback.IGetNeoAssetsCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.ICheckIsUpdateEthAssetsCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.ICheckIsUpdateEthTxStateCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthAssetsCallback;
import yooco.uchain.uchainwallet.utils.task.runnable.CheckIsUpdateNeoAssets;
import yooco.uchain.uchainwallet.utils.task.runnable.CheckIsUpdateNeoTxState;
import yooco.uchain.uchainwallet.utils.task.runnable.GetNeoAssets;
import yooco.uchain.uchainwallet.utils.task.runnable.GetRawTransaction;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.CheckIsUpdateEthAssets;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.CheckIsUpdateEthTxState;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.GetEthAssets;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.GetEthTransactionReceipt;

/**
 * Created by SteelCabbage on 2018/6/10 15:23
 * E-Mail：liuyi_61@163.com
 */
public class UChainGlobalTask implements ICheckIsUpdateNeoAssetsCallback, IGetNeoAssetsCallback,
        ICheckIsUpdateNeoTxStateCallback, ICheckIsUpdateEthAssetsCallback, IGetEthAssetsCallback,
        ICheckIsUpdateEthTxStateCallback {

    private static final String TAG = UChainGlobalTask.class.getSimpleName();

    private ScheduledFuture mCheckIsUpdateNeoAssetsSF;
    private ScheduledFuture mCheckIsUpdateEthAssetsSF;

    private UChainGlobalTask() {

    }

    private static class ApexCacheHolder {
        private static final UChainGlobalTask sApexGlobalTask = new UChainGlobalTask();
    }

    public static UChainGlobalTask getInstance() {
        return ApexCacheHolder.sApexGlobalTask;
    }

    public void doInit() {
        // check assets
        TaskController.getInstance().submit(new CheckIsUpdateNeoAssets(this));
        TaskController.getInstance().submit(new CheckIsUpdateEthAssets(this));

        // check tx state
        TaskController.getInstance().submit(new CheckIsUpdateNeoTxState(this));
        TaskController.getInstance().submit(new CheckIsUpdateEthTxState(this));
    }

    @Override
    public void checkIsUpdateNeoAssets(boolean isUpdate) {
        if (isUpdate) {
            UChainLog.i(TAG, "need to update neo assets!");
            mCheckIsUpdateEthAssetsSF = TaskController.getInstance().schedule(new GetNeoAssets(this), 0, Constant
                    .ASSETS_POLLING_TIME);
        }
    }

    @Override
    public void checkIsUpdateEthAssets(boolean isUpdate) {
        if (isUpdate) {
            UChainLog.i(TAG, "need to update eth assets!");
            mCheckIsUpdateNeoAssetsSF = TaskController.getInstance().schedule(new GetEthAssets(this), 0, Constant
                    .ASSETS_POLLING_TIME);
        }
    }

    @Override
    public void getNeoAssets(String msg) {
        if (TextUtils.isEmpty(msg)) {
            UChainLog.e(TAG, "getNeoAssets() -> msg is null!");
            return;
        }

        if (Constant.UPDATE_ASSETS_OK.equals(msg)) {
            UChainLog.i(TAG, "update neo assets ok!");
            mCheckIsUpdateNeoAssetsSF.cancel(true);
        }
    }

    @Override
    public void getEthAssets(String msg) {
        if (TextUtils.isEmpty(msg)) {
            UChainLog.e(TAG, "getEthAssets() -> msg is null!");
            return;
        }

        if (Constant.UPDATE_ASSETS_OK.equals(msg)) {
            UChainLog.i(TAG, "update eth assets ok!");
            mCheckIsUpdateEthAssetsSF.cancel(true);
        }
    }

    @Override
    public void checkIsUpdateNeoTxState(List<TransactionRecord> transactionRecords) {
        if (null == transactionRecords || transactionRecords.isEmpty()) {
            UChainLog.i(TAG, "checkIsUpdateNeoTxState() -> no need to update neo tx state!");
            return;
        }

        for (TransactionRecord transactionRecord : transactionRecords) {
            if (null == transactionRecord) {
                UChainLog.e(TAG, "checkIsUpdateNeoTxState() -> transactionRecord is null!");
                continue;
            }

            startNeoPolling(transactionRecord.getTxID(), transactionRecord.getWalletAddress());
            UChainLog.i(TAG, "checkIsUpdateNeoTxState() -> restart neo polling for txId:" + transactionRecord.getTxID());
        }
    }

    @Override
    public void checkIsUpdateEthTxState(List<TransactionRecord> transactionRecords) {
        if (null == transactionRecords || transactionRecords.isEmpty()) {
            UChainLog.i(TAG, "checkIsUpdateEthTxState() -> no need to update eth tx state!");
            return;
        }

        for (TransactionRecord transactionRecord : transactionRecords) {
            if (null == transactionRecord) {
                UChainLog.e(TAG, "checkIsUpdateEthTxState() -> transactionRecord is null!");
                continue;
            }

            startEthPolling(transactionRecord.getTxID(), transactionRecord.getWalletAddress());
            UChainLog.i(TAG, "checkIsUpdateEthTxState() -> restart eth polling for txId:" + transactionRecord.getTxID());
        }
    }

    public void startNeoPolling(String txId, String walletAddress) {
        if (TextUtils.isEmpty(txId) || TextUtils.isEmpty(walletAddress)) {
            UChainLog.e(TAG, "startNeoPolling() -> txId or walletAddress is null!");
            return;
        }

        UpdateNeoTxState updateNeoTxState = new UpdateNeoTxState(txId);
        ScheduledFuture updateNeoTxStateSF = TaskController.getInstance().schedule(
                new GetRawTransaction(txId, walletAddress, updateNeoTxState), 0, Constant.TX_NEO_POLLING_TIME);
        updateNeoTxState.setScheduledFuture(updateNeoTxStateSF);
    }

    public void startEthPolling(String txId, String walletAddress) {
        if (TextUtils.isEmpty(txId) || TextUtils.isEmpty(walletAddress)) {
            UChainLog.e(TAG, "startEthPolling() -> txId or walletAddress is null!");
            return;
        }

        UpdateEthTxState updateEthTxState = new UpdateEthTxState(txId);
        ScheduledFuture updateEthTxStateSF = TaskController.getInstance().schedule(
                new GetEthTransactionReceipt(txId, walletAddress, updateEthTxState), 0, Constant.TX_ETH_POLLING_TIME);
        updateEthTxState.setGetTxReceiptSF(updateEthTxStateSF);
    }

}
