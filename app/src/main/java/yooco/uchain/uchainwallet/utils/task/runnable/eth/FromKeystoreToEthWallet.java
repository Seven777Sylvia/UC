package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IFromKeystoreToEthWalletCallback;
import ethmobile.Ethmobile;
import ethmobile.Wallet;

/**
 * Created by SteelCabbage on 2018/6/8 00:34
 * E-Mail：liuyi_61@163.com
 */
public class FromKeystoreToEthWallet implements Runnable {

    private static final String TAG = FromKeystoreToEthWallet.class.getSimpleName();

    private String mKeystore;
    private String mPwd;
    private IFromKeystoreToEthWalletCallback mIFromKeystoreToEthWalletCallback;

    public FromKeystoreToEthWallet(String keystore, String pwd, IFromKeystoreToEthWalletCallback
            iFromKeystoreToEthWalletCallback) {
        mKeystore = keystore;
        mPwd = pwd;
        mIFromKeystoreToEthWalletCallback = iFromKeystoreToEthWalletCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mKeystore)
                || TextUtils.isEmpty(mPwd)
                || null == mIFromKeystoreToEthWalletCallback) {
            UChainLog.e(TAG, "mKeystore or mPwd or mIFromKeystoreToEthWalletCallback is null！");
            return;
        }

        Wallet wallet = null;
        try {
            wallet = Ethmobile.fromKeyStore(mKeystore, mPwd);
        } catch (Exception e) {
            UChainLog.e(TAG, "fromKeyStore exception:" + e.getMessage());
        }
        mIFromKeystoreToEthWalletCallback.fromKeystoreToEthWallet(wallet);
    }
}
