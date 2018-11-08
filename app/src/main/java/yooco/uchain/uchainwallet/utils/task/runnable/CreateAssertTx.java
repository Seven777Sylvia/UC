package yooco.uchain.uchainwallet.utils.task.runnable;

import yooco.uchain.uchainwallet.data.bean.AssertTxBean;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.ICreateAssertTxCallback;
import neomobile.Tx;
import neomobile.Wallet;

/**
 * Created by SteelCabbage on 2018/6/8 01:08
 * E-Mail：liuyi_61@163.com
 */
public class CreateAssertTx implements Runnable {

    private static final String TAG = CreateAssertTx.class.getSimpleName();

    private Wallet mWallet;
    private AssertTxBean mAssertTxBean;
    private ICreateAssertTxCallback mICreateAssertTxCallback;

    public CreateAssertTx(Wallet wallet, AssertTxBean assertTxBean, ICreateAssertTxCallback ICreateAssertTxCallback) {
        mWallet = wallet;
        mAssertTxBean = assertTxBean;
        mICreateAssertTxCallback = ICreateAssertTxCallback;
    }

    @Override
    public void run() {
        if (null == mWallet
                || null == mAssertTxBean
                || null == mICreateAssertTxCallback) {
            UChainLog.e(TAG, "mWallet or mAssertTxBean or mICreateAssertTxCallback is null!");
            return;
        }

        Tx tx = null;
        try {
            tx = mWallet.createAssertTx(mAssertTxBean.getAssetsID()
                    , mAssertTxBean.getAddrFrom()
                    , mAssertTxBean.getAddrTo()
                    , mAssertTxBean.getTransferAmount()
                    , mAssertTxBean.getUtxos());
        } catch (Exception e) {
            UChainLog.e(TAG, "createAssertTx exception: " + e.getMessage());
        }
        mICreateAssertTxCallback.createAssertTx(tx);
    }
}
