package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import yooco.uchain.uchainwallet.data.bean.tx.EthTxBean;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.ICreateEthTxCallback;
import ethmobile.Wallet;

/**
 * Created by SteelCabbage on 2018/9/7 0007 14:16.
 * E-Mail：liuyi_61@163.com
 */
public class CreateEthTx implements Runnable {

    private static final String TAG = CreateEthTx.class.getSimpleName();

    private EthTxBean mEthTxBean;
    private ICreateEthTxCallback mICreateEthTxCallback;

    public CreateEthTx(EthTxBean ethTxBean, ICreateEthTxCallback ICreateEthTxCallback) {
        mEthTxBean = ethTxBean;
        mICreateEthTxCallback = ICreateEthTxCallback;
    }

    @Override
    public void run() {
        if (null == mICreateEthTxCallback) {
            UChainLog.e(TAG, "mICreateEthTxCallback is null！");
            return;
        }

        if (null == mEthTxBean) {
            UChainLog.e(TAG, "mEthTxBean is null！");
            mICreateEthTxCallback.createEthTx(null);
            return;
        }

        Wallet ethWallet = mEthTxBean.getWallet();
        if (null == ethWallet) {
            UChainLog.e(TAG, "ethWallet is null!");
            mICreateEthTxCallback.createEthTx(null);
            return;
        }

        try {
            String data = ethWallet.transfer(mEthTxBean.getNonce(),
                    mEthTxBean.getToAddress(),
                    mEthTxBean.getAmount(),
                    mEthTxBean.getGasPrice(),
                    mEthTxBean.getGasLimit());
            mICreateEthTxCallback.createEthTx("0x" + data);
        } catch (Exception e) {
            UChainLog.e(TAG, "ethWallet.transfer exception:" + e.getMessage());
            mICreateEthTxCallback.createEthTx(null);
        }
    }
}
