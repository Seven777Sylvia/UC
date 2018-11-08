package yooco.uchain.uchainwallet.utils.task.runnable;

import android.text.TextUtils;

import yooco.uchain.uchainwallet.utils.task.callback.IFromMnemonicToNeoWalletCallback;
import yooco.uchain.uchainwallet.utils.UChainLog;
import neomobile.Neomobile;
import neomobile.Wallet;

/**
 * Created by SteelCabbage on 2018/6/11 0011.
 */

public class FromMnemonicToNeoWallet implements java.lang.Runnable {

    private static final String TAG = FromMnemonicToNeoWallet.class.getSimpleName();

    private String mMnemonic;
    private String mMnemonicType;
    private IFromMnemonicToNeoWalletCallback mIFromMnemonicToNeoWalletCallback;

    public FromMnemonicToNeoWallet(String mnemonic, String mnemonicType,
                                   IFromMnemonicToNeoWalletCallback IFromMnemonicToNeoWalletCallback) {
        mMnemonic = mnemonic;
        mMnemonicType = mnemonicType;
        mIFromMnemonicToNeoWalletCallback = IFromMnemonicToNeoWalletCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mMnemonic)
                || null == mIFromMnemonicToNeoWalletCallback) {
            UChainLog.e(TAG, "mMnemonic or mPwd or mIFromMnemonicToNeoWalletCallback is null!");
            return;
        }

        Wallet wallet = null;
        try {
            wallet = Neomobile.fromMnemonic(mMnemonic, mMnemonicType);
        } catch (Exception e) {
            UChainLog.e(TAG, "fromMnemonic exception:" + e.getMessage());
        }

        mIFromMnemonicToNeoWalletCallback.fromMnemonicToNeoWallet(wallet);
    }
}
