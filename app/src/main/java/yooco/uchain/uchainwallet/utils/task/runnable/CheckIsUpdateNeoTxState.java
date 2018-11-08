package yooco.uchain.uchainwallet.utils.task.runnable;

import java.util.ArrayList;
import java.util.List;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.ICheckIsUpdateNeoTxStateCallback;
import yooco.uchain.uchainwallet.global.Constant;

/**
 * Created by SteelCabbage on 2018/7/13 0013 13:10.
 * E-Mail：liuyi_61@163.com
 */

public class CheckIsUpdateNeoTxState implements Runnable {

    private static final String TAG = CheckIsUpdateNeoTxState.class.getSimpleName();

    private ICheckIsUpdateNeoTxStateCallback mICheckIsUpdateNeoTxStateCallback;

    public CheckIsUpdateNeoTxState(ICheckIsUpdateNeoTxStateCallback ICheckIsUpdateNeoTxStateCallback) {
        mICheckIsUpdateNeoTxStateCallback = ICheckIsUpdateNeoTxStateCallback;
    }

    @Override
    public void run() {
        if (null == mICheckIsUpdateNeoTxStateCallback) {
            UChainLog.e(TAG, "mICheckIsUpdateNeoTxStateCallback is null!");
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication
                .getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        List<TransactionRecord> needUpdateStateTxs = new ArrayList<>();

        List<TransactionRecord> packagingTxs = uChainWalletDbDao.queryTxByState(Constant
                .TABLE_NEO_TX_CACHE, Constant.TRANSACTION_STATE_PACKAGING);
        if (null != packagingTxs && !packagingTxs.isEmpty()) {
            needUpdateStateTxs.addAll(packagingTxs);
        }

        List<TransactionRecord> confirmingTxs = uChainWalletDbDao.queryTxByState(Constant
                .TABLE_NEO_TRANSACTION_RECORD, Constant.TRANSACTION_STATE_CONFIRMING);
        if (null != confirmingTxs && !confirmingTxs.isEmpty()) {
            needUpdateStateTxs.addAll(confirmingTxs);
        }

        mICheckIsUpdateNeoTxStateCallback.checkIsUpdateNeoTxState(needUpdateStateTxs);
    }
}
