package yooco.uchain.uchainwallet.utils.task.runnable;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.ILoadTransactionRecordCallback;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;

/**
 * Created by SteelCabbage on 2018/6/28 0028 10:21.
 * E-Mail：liuyi_61@163.com
 */

public class LoadTransactionRecord implements Runnable {

    private static final String TAG = LoadTransactionRecord.class.getSimpleName();

    private int mWalletType;
    private String mAddress;
    private ILoadTransactionRecordCallback mILoadTransactionRecordCallback;

    public LoadTransactionRecord(int walletType, String address, ILoadTransactionRecordCallback ILoadTransactionRecordCallback) {
        mWalletType = walletType;
        mAddress = address;
        mILoadTransactionRecordCallback = ILoadTransactionRecordCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mAddress) || null == mILoadTransactionRecordCallback) {
            UChainLog.e(TAG, "mAddress or mILoadTransactionRecordCallback is null!");
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            mILoadTransactionRecordCallback.loadTransactionRecord(null);
            return;
        }

        String txCacheTableName = null;
        String txTableName = null;
        switch (mWalletType) {
            case Constant.WALLET_TYPE_NEO:
                txCacheTableName = Constant.TABLE_NEO_TX_CACHE;
                txTableName = Constant.TABLE_NEO_TRANSACTION_RECORD;
                break;
            case Constant.WALLET_TYPE_ETH:
                txCacheTableName = Constant.TABLE_ETH_TX_CACHE;
                txTableName = Constant.TABLE_ETH_TRANSACTION_RECORD;
                break;
            case Constant.WALLET_TYPE_CPX:

                break;
            default:
                UChainLog.e(TAG, "Illegal wallet type!");
                break;
        }

        List<TransactionRecord> finalTxs = new ArrayList<>();

        List<TransactionRecord> txCacheRecords = uChainWalletDbDao.queryTxByAddress(txCacheTableName, mAddress);
        if (null != txCacheRecords && !txCacheRecords.isEmpty()) {
            Collections.reverse(txCacheRecords);
            finalTxs.addAll(txCacheRecords);
        }

        List<TransactionRecord> txRecords = uChainWalletDbDao.queryTxByAddress(txTableName, mAddress);
        if (null != txRecords && !txRecords.isEmpty()) {
            Collections.reverse(txRecords);
            finalTxs.addAll(txRecords);
        }

        mILoadTransactionRecordCallback.loadTransactionRecord(finalTxs);
    }
}
