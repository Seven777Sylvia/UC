package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IFromMnemonicToEthWalletCallback;
import ethmobile.Ethmobile;
import ethmobile.Wallet;


/**
 * Created by SteelCabbage on 2018/6/11 0011.
 */

public class FromMnemonicToEthWallet implements Runnable {

    private static final String TAG = FromMnemonicToEthWallet.class.getSimpleName();

    private String mMnemonic;
    private String mMnemonicType;
    private IFromMnemonicToEthWalletCallback mIFromMnemonicToEthWalletCallback;

    public FromMnemonicToEthWallet(String mnemonic, String mnemonicType, IFromMnemonicToEthWalletCallback
            iFromMnemonicToEthWalletCallback) {
        mMnemonic = mnemonic;
        mMnemonicType = mnemonicType;
        mIFromMnemonicToEthWalletCallback = iFromMnemonicToEthWalletCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mMnemonic) || null == mIFromMnemonicToEthWalletCallback) {
            UChainLog.e(TAG, "mMnemonic or mPwd or mIFromMnemonicToEthWalletCallback is null!");
            return;
        }

        Wallet wallet = null;
        try {
            wallet = Ethmobile.fromMnemonic(mMnemonic, mMnemonicType);
        } catch (Exception e) {
            UChainLog.e(TAG, "fromMnemonic exception:" + e.getMessage());
        }

        mIFromMnemonicToEthWalletCallback.fromMnemonicToEthWallet(wallet);
    }
}
