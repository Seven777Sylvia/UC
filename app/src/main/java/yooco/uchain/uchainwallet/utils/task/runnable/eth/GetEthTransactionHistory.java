package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetEthTransactionHistory;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthTransactionHistoryCallback;
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

public class GetEthTransactionHistory implements Runnable, INetCallback {

    private static final String TAG = GetEthTransactionHistory.class.getSimpleName();

    private String mAddress;
    private IGetEthTransactionHistoryCallback mIGetEthTransactionHistoryCallback;

    public GetEthTransactionHistory(String address, IGetEthTransactionHistoryCallback IGetEthTransactionHistoryCallback) {
        mAddress = address;
        mIGetEthTransactionHistoryCallback = IGetEthTransactionHistoryCallback;
    }

    @Override
    public void run() {
        if (null == mIGetEthTransactionHistoryCallback || TextUtils.isEmpty(mAddress)) {
            UChainLog.e(TAG, "mIGetEthTransactionHistoryCallback or mAddress is null!");
            return;
        }

        long startBlock;
        try {
            String startBlockSP = (String) SharedPreferencesUtils.getParam(UChainWalletApplication.getInstance(), mAddress, "0");
            startBlock = Long.valueOf(startBlockSP);
        } catch (NumberFormatException e) {
            UChainLog.e(TAG, "GetEthTransactionHistory NumberFormatException:" + e.getMessage());
            return;
        }

        UChainLog.i(TAG, "startBlock:" + startBlock);

        String url = Constant.URL_ETH_TRANSACTION_HISTORY
                + mAddress
                + "&startblock=" + (startBlock + 1);
        OkHttpClientManager.getInstance().get(url, this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        if (TextUtils.isEmpty(result)) {
            UChainLog.e(TAG, "result is null!");
            mIGetEthTransactionHistoryCallback.getEthTransactionHistory(null);
            return;
        }

        ResponseGetEthTransactionHistory responseGetEthTransactionHistory = GsonUtils.json2Bean(result,
                ResponseGetEthTransactionHistory.class);
        if (null == responseGetEthTransactionHistory) {
            UChainLog.e(TAG, "responseGetEthTransactionHistory is null!");
            mIGetEthTransactionHistoryCallback.getEthTransactionHistory(null);
            return;
        }

        List<ResponseGetEthTransactionHistory.DataBean> dataBeans = responseGetEthTransactionHistory.getData();
        if (null == dataBeans || dataBeans.isEmpty()) {
            UChainLog.w(TAG, "dataBeans is null or empty!");
            mIGetEthTransactionHistoryCallback.getEthTransactionHistory(null);
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            mIGetEthTransactionHistoryCallback.getEthTransactionHistory(null);
            return;
        }

        HashMap<String, TransactionRecord> txCacheByAddress = uChainWalletDbDao.queryTxCacheByAddress(Constant
                .TABLE_ETH_TX_CACHE, mAddress);

        if (null == txCacheByAddress) {
            UChainLog.e(TAG, "txCacheByAddress is null!");
            mIGetEthTransactionHistoryCallback.getEthTransactionHistory(null);
            return;
        }

        // 记录该地址的最新交易所在区块
        SharedPreferencesUtils.putParam(UChainWalletApplication.getInstance(), mAddress, dataBeans.get(dataBeans.size() - 1)
                .getBlock_number());

        List<TransactionRecord> transactionRecords = new ArrayList<>();

        for (ResponseGetEthTransactionHistory.DataBean dataBean : dataBeans) {
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
                uChainWalletDbDao.delCacheByTxIDAndAddr(Constant.TABLE_ETH_TX_CACHE, txID, mAddress);
            } else {
                String vmstate = dataBean.getVmstate();
                if (TextUtils.isEmpty(vmstate)) {
                    transactionRecord.setTxState(Constant.TRANSACTION_STATE_SUCCESS);
                } else {
                    switch (vmstate) {
                        case "0":
                            transactionRecord.setTxState(Constant.TRANSACTION_STATE_SUCCESS);
                            break;
                        case "1":
                            transactionRecord.setTxState(Constant.TRANSACTION_STATE_FAIL);
                            break;
                        default:
                            break;
                    }
                }
            }
            transactionRecord.setWalletAddress(mAddress);
            transactionRecord.setTxType(txType);
            transactionRecord.setTxID(txID);
            transactionRecord.setTxAmount(dataBean.getValue());
            transactionRecord.setTxFrom(dataBean.getFrom());
            transactionRecord.setTxTo(dataBean.getTo());
            transactionRecord.setGasConsumed(null == dataBean.getGas_consumed() ? "0" : dataBean.getGas_consumed());
            transactionRecord.setAssetID(assetId);
            transactionRecord.setAssetSymbol(dataBean.getSymbol());
            transactionRecord.setAssetLogoUrl(dataBean.getImageURL());
            transactionRecord.setAssetDecimal(TextUtils.isEmpty(dataBean.getDecimal()) ?
                    0 : Integer.valueOf(dataBean.getDecimal()));
            transactionRecord.setGasPrice(dataBean.getGas_price());
            transactionRecord.setBlockNumber(dataBean.getBlock_number());
            transactionRecord.setGasFee(dataBean.getGas_fee());
            transactionRecord.setTxTime(txTime);

            List<TransactionRecord> txsByTxIdAndAddress = uChainWalletDbDao.queryTxByTxIdAndAddress
                    (Constant.TABLE_ETH_TRANSACTION_RECORD, txID, mAddress);
            if (null == txsByTxIdAndAddress || txsByTxIdAndAddress.isEmpty()) {
                uChainWalletDbDao.insertTxRecord(Constant.TABLE_ETH_TRANSACTION_RECORD, transactionRecord);
                transactionRecords.add(transactionRecord);
            }
        }

        mIGetEthTransactionHistoryCallback.getEthTransactionHistory(transactionRecords);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "GetEthTransactionHistory net onFailed!");
        mIGetEthTransactionHistoryCallback.getEthTransactionHistory(null);
    }
}
