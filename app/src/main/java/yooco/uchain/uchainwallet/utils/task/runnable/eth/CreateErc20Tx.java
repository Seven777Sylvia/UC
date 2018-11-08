package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import yooco.uchain.uchainwallet.data.bean.tx.EthTxBean;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.ICreateErc20TxCallback;
import ethmobile.Wallet;

/**
 * Created by SteelCabbage on 2018/9/7 0007 14:16.
 * E-Mail：liuyi_61@163.com
 */
public class CreateErc20Tx implements Runnable {
    private static final String TAG = CreateErc20Tx.class.getSimpleName();
    private EthTxBean mEthTxBean;
    private ICreateErc20TxCallback mICreateErc20TxCallback;

    public CreateErc20Tx(EthTxBean ethTxBean, ICreateErc20TxCallback ICreateErc20TxCallback) {
        mEthTxBean = ethTxBean;
        mICreateErc20TxCallback = ICreateErc20TxCallback;
    }

    @Override
    public void run() {
        if (null == mICreateErc20TxCallback) {
            UChainLog.e(TAG, "mICreateErc20TxCallback is null！");
            return;
        }

        if (null == mEthTxBean) {
            UChainLog.e(TAG, "mEthTxBean is null！");
            mICreateErc20TxCallback.createErc20Tx(null);
            return;
        }

        Wallet ethWallet = mEthTxBean.getWallet();
        if (null == ethWallet) {
            UChainLog.e(TAG, "ethWallet is null!");
            mICreateErc20TxCallback.createErc20Tx(null);
            return;
        }

        try {
            String data = ethWallet.transferERC20(mEthTxBean.getAssetID(),
                    mEthTxBean.getNonce(),
                    mEthTxBean.getToAddress(),
                    mEthTxBean.getAmount(),
                    mEthTxBean.getGasPrice(),
                    mEthTxBean.getGasLimit());
            mICreateErc20TxCallback.createErc20Tx("0x" + data);
        } catch (Exception e) {
            UChainLog.e(TAG, "ethWallet.transferERC20 exception:" + e.getMessage());
            mICreateErc20TxCallback.createErc20Tx(null);
        }
    }
}
