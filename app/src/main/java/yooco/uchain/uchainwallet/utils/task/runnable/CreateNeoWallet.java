package yooco.uchain.uchainwallet.utils.task.runnable;


import android.text.TextUtils;

import java.util.ArrayList;

import yooco.uchain.uchainwallet.data.bean.neo.NeoWallet;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.task.callback.ICreateWalletCallback;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import neomobile.Neomobile;
import neomobile.Wallet;

/**
 * Created by SteelCabbage on 2018/6/1 0001.
 */

public class CreateNeoWallet implements Runnable {

    private static final String TAG = CreateNeoWallet.class.getSimpleName();

    private String mWalletName;
    private String mPwd;
    private ICreateWalletCallback mICreateWalletCallback;

    public CreateNeoWallet(String walletName, String pwd, ICreateWalletCallback
            iCreateWalletCallback) {
        mWalletName = walletName;
        mPwd = pwd;
        mICreateWalletCallback = iCreateWalletCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mWalletName)
                || TextUtils.isEmpty(mPwd)
                || null == mICreateWalletCallback) {
            UChainLog.e(TAG, "neo mWalletName or mPwd or mICreateWalletCallback is null!");
            return;
        }

        Wallet walletFirst = null;
        try {
            walletFirst = Neomobile.new_();
        } catch (Exception e) {
            UChainLog.e(TAG, "neo new_() exception:" + e.getMessage());
        }

        if (null == walletFirst) {
            UChainLog.e(TAG, "neo walletFirst is null!");
            return;
        }

        Wallet walletChecked = checkMnemonic(walletFirst);
        if (null == walletChecked) {
            UChainLog.e(TAG, "neo walletChecked is null!");
            return;
        }

        String toKeyStore = null;
        try {
            toKeyStore = walletChecked.toKeyStore(mPwd);
        } catch (Exception e) {
            UChainLog.e(TAG, "neo toKeyStore exception:" + e.getMessage());
        }

        if (TextUtils.isEmpty(toKeyStore)) {
            UChainLog.e(TAG, "neo toKeyStore is null！");
            return;
        }

        ArrayList<String> assets = new ArrayList<>();
        assets.add(Constant.ASSETS_NEO_GAS);
        assets.add(Constant.ASSETS_NEO);

        ArrayList<String> assetsNep5 = new ArrayList<>();
        assetsNep5.add(Constant.ASSETS_CPX);

        NeoWallet neoWallet = new NeoWallet();
        neoWallet.setWalletType(Constant.WALLET_TYPE_NEO);
        neoWallet.setName(mWalletName);
        neoWallet.setAddress(walletChecked.address());
        neoWallet.setBackupState(Constant.BACKUP_UNFINISHED);
        neoWallet.setKeyStore(toKeyStore);
        neoWallet.setAssetJson(GsonUtils.toJsonStr(assets));
        neoWallet.setColorAssetJson(GsonUtils.toJsonStr(assetsNep5));

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        uChainWalletDbDao.insert(Constant.TABLE_NEO_WALLET, neoWallet);
        UChainListeners.getInstance().notifyWalletAdd(neoWallet);
        mICreateWalletCallback.newWallet(walletChecked);
    }

    private Wallet checkMnemonic(Wallet wallet) {
        Wallet walletFinal = null;
        try {
            String mnemonic = wallet.mnemonic("en_US");
            walletFinal = Neomobile.fromMnemonic(mnemonic, "en_US");
        } catch (Exception e) {
            UChainLog.e(TAG, "neo checkMnemonic fromMnemonic Exception:" + e.getMessage());
            try {
                walletFinal = checkMnemonic(Neomobile.new_());
            } catch (Exception e1) {
                UChainLog.e(TAG, "neo checkMnemonic new_() Exception:" + e.getMessage());
            }
        }

        return walletFinal;
    }
}
