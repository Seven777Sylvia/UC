package yooco.uchain.uchainwallet.utils.task.runnable;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetNeoTransactionHistory;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.IGetNeoTransactionHistoryCallback;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import yooco.uchain.uchainwallet.utils.SharedPreferencesUtils;

/**
 * Created by SteelCabbage on 2018/6/22 0022 11:42.
 * E-Mail：liuyi_61@163.com
 */

public class GetNeoTransactionHistory implements Runnable, INetCallback {

    private static final String TAG = GetNeoTransactionHistory.class.getSimpleName();

    private String mAddress;
    private IGetNeoTransactionHistoryCallback mIGetNeoTransactionHistoryCallback;
    private long mRecentTime;

    public GetNeoTransactionHistory(String address, IGetNeoTransactionHistoryCallback IGetNeoTransactionHistoryCallback) {
        mAddress = address;
        mIGetNeoTransactionHistoryCallback = IGetNeoTransactionHistoryCallback;
    }

    @Override
    public void run() {
        if (null == mIGetNeoTransactionHistoryCallback || TextUtils.isEmpty(mAddress)) {
            UChainLog.e(TAG, "mIGetNeoTransactionHistoryCallback or mAddress is null!");
            return;
        }

        mRecentTime = (long) SharedPreferencesUtils.getParam(UChainWalletApplication.getInstance(), mAddress, 0L);
        UChainLog.i(TAG, "mRecentTime:" + mRecentTime);

        String url = Constant.URL_NEO_TRANSACTION_HISTORY + mAddress + "&beginTime=" + mRecentTime;
        OkHttpClientManager.getInstance().get(url, this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        if (TextUtils.isEmpty(result)) {
            UChainLog.e(TAG, "result is null!");
            mIGetNeoTransactionHistoryCallback.getNeoTransactionHistory(null);
            return;
        }

        ResponseGetNeoTransactionHistory responseGetNeoTransactionHistory = GsonUtils.json2Bean(result,
                ResponseGetNeoTransactionHistory.class);
        if (null == responseGetNeoTransactionHistory) {
            UChainLog.e(TAG, "responseGetNeoTransactionHistory is null!");
            mIGetNeoTransactionHistoryCallback.getNeoTransactionHistory(null);
            return;
        }

        List<ResponseGetNeoTransactionHistory.DataBean> dataBeans = responseGetNeoTransactionHistory.getData();
        if (null == dataBeans || dataBeans.isEmpty()) {
            UChainLog.w(TAG, "dataBeans is null or empty!");
            mIGetNeoTransactionHistoryCallback.getNeoTransactionHistory(null);
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            mIGetNeoTransactionHistoryCallback.getNeoTransactionHistory(null);
            return;
        }

        HashMap<String, TransactionRecord> txCacheByAddress = uChainWalletDbDao.queryTxCacheByAddress(
                Constant.TABLE_NEO_TX_CACHE, mAddress);

        if (null == txCacheByAddress) {
            UChainLog.e(TAG, "txCacheByAddress is null!");
            mIGetNeoTransactionHistoryCallback.getNeoTransactionHistory(null);
            return;
        }

        // 记录该地址的最近更新时间
        SharedPreferencesUtils.putParam(UChainWalletApplication.getInstance(), mAddress, dataBeans.get(dataBeans.size() - 1)
                .getTime());

        List<TransactionRecord> transactionRecords = new ArrayList<>();

        for (ResponseGetNeoTransactionHistory.DataBean dataBean : dataBeans) {
            if (null == dataBean) {
                UChainLog.e(TAG, "dataBean is null!");
                continue;
            }

            TransactionRecord transactionRecord = new TransactionRecord();

            // 如果缓存中包含相同txid，删除缓存中该地址对应的该条txid，并写入正式表，状态为确认中
            String txID = dataBean.getTxid();
            String txType = dataBean.getType();
            long txTime = dataBean.getTime();
            String assetId = dataBean.getAssetId();
            if (txCacheByAddress.containsKey(txID)) {
                transactionRecord.setTxState(Constant.TRANSACTION_STATE_CONFIRMING);
                UChainListeners.getInstance().notifyTxStateUpdate(txID, Constant.TRANSACTION_STATE_CONFIRMING, txTime);
                uChainWalletDbDao.delCacheByTxIDAndAddr(Constant.TABLE_NEO_TX_CACHE, txID, mAddress);
            } else {
                switch (txType) {
                    case Constant.ASSET_TYPE_NEP5:
                        String vmstate = dataBean.getVmstate();
                        if (!TextUtils.isEmpty(vmstate) && !vmstate.contains("FAULT")) {
                            transactionRecord.setTxState(Constant.TRANSACTION_STATE_SUCCESS);
                        } else {
                            transactionRecord.setTxState(Constant.TRANSACTION_STATE_FAIL);
                        }
                        break;
                    default:
                        transactionRecord.setTxState(Constant.TRANSACTION_STATE_SUCCESS);
                        break;
                }

            }
            transactionRecord.setWalletAddress(mAddress);
            transactionRecord.setTxType(txType);
            transactionRecord.setTxID(txID);
            transactionRecord.setTxAmount(dataBean.getValue());
            transactionRecord.setTxFrom(dataBean.getFrom());
            transactionRecord.setTxTo(dataBean.getTo());
            transactionRecord.setGasConsumed(dataBean.getGas_consumed());
            transactionRecord.setAssetID(assetId);
            switch (assetId) {
                case Constant.ASSETS_NEO_GAS:
                    transactionRecord.setAssetSymbol(Constant.SYMBOL_NEO_GAS);
                    break;
                default:
                    transactionRecord.setAssetSymbol(dataBean.getSymbol());
                    break;

            }
            transactionRecord.setAssetLogoUrl(dataBean.getImageURL());
            transactionRecord.setAssetDecimal(TextUtils.isEmpty(dataBean.getDecimal()) ?
                    0 : Integer.valueOf(dataBean.getDecimal()));
            transactionRecord.setTxTime(txTime);

            List<TransactionRecord> txsByTxIdAndAddress = uChainWalletDbDao.queryTxByTxIdAndAddress
                    (Constant.TABLE_NEO_TRANSACTION_RECORD, txID, mAddress);
            if (null == txsByTxIdAndAddress || txsByTxIdAndAddress.isEmpty()) {
                uChainWalletDbDao.insertTxRecord(Constant.TABLE_NEO_TRANSACTION_RECORD, transactionRecord);
                transactionRecords.add(transactionRecord);
            }
        }

        mIGetNeoTransactionHistoryCallback.getNeoTransactionHistory(transactionRecords);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "getNeoTransactionHistory net onFailed!");
        mIGetNeoTransactionHistoryCallback.getNeoTransactionHistory(null);
    }
}
