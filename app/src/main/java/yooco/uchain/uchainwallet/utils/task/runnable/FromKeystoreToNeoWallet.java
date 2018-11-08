package yooco.uchain.uchainwallet.utils.task.runnable;

import android.text.TextUtils;

import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.IFromKeystoreToNeoWalletCallback;
import neomobile.Neomobile;
import neomobile.Wallet;

/**
 * Created by SteelCabbage on 2018/6/8 00:34
 * E-Mail：liuyi_61@163.com
 */
public class FromKeystoreToNeoWallet implements Runnable {

    private static final String TAG = FromKeystoreToNeoWallet.class.getSimpleName();

    private String mKeystore;
    private String mPwd;
    private IFromKeystoreToNeoWalletCallback mIFromKeystoreToNeoWalletCallback;

    public FromKeystoreToNeoWallet(String keystore, String pwd, IFromKeystoreToNeoWalletCallback
            IFromKeystoreToNeoWalletCallback) {
        mKeystore = keystore;
        mPwd = pwd;
        mIFromKeystoreToNeoWalletCallback = IFromKeystoreToNeoWalletCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mKeystore)
                || TextUtils.isEmpty(mPwd)
                || null == mIFromKeystoreToNeoWalletCallback) {
            UChainLog.e(TAG, "mKeystore or mPwd or mIFromKeystoreToNeoWalletCallback is null！");
            return;
        }

        Wallet wallet = null;
        try {
            wallet = Neomobile.fromKeyStore(mKeystore, mPwd);
            UChainLog.i(TAG, "wallet address:" + wallet.address());
        } catch (Exception e) {
            UChainLog.e(TAG, "fromKeyStore exception:" + e.getMessage());
        }
        mIFromKeystoreToNeoWalletCallback.fromKeystoreToNeoWallet(wallet);
    }
}
