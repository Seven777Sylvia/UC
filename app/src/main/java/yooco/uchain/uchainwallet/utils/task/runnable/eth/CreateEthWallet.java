package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import java.util.ArrayList;

import yooco.uchain.uchainwallet.data.bean.eth.EthWallet;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.task.callback.eth.ICreateEthWalletCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import ethmobile.Ethmobile;
import ethmobile.Wallet;


/**
 * Created by SteelCabbage on 2018/8/13 0013 18:03.
 * E-Mail：liuyi_61@163.com
 */

public class CreateEthWallet implements Runnable {

    private static final String TAG = CreateEthWallet.class.getSimpleName();

    private String mName;
    private String mPwd;
    private ICreateEthWalletCallback mICreateEthWalletCallback;

    public CreateEthWallet(String name, String pwd, ICreateEthWalletCallback
            ICreateEthWalletCallback) {
        mName = name;
        mPwd = pwd;
        mICreateEthWalletCallback = ICreateEthWalletCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mName)
                || TextUtils.isEmpty(mPwd)
                || null == mICreateEthWalletCallback) {
            UChainLog.e(TAG, "eth mName or mPwd or mICreateEthWalletCallback is null!");
            return;
        }

        Wallet walletFirst = null;
        try {
            walletFirst = Ethmobile.new_();
        } catch (Exception e) {
            UChainLog.e(TAG, "eth new_() exception:" + e.getMessage());
        }

        if (null == walletFirst) {
            UChainLog.e(TAG, "eth walletFirst is null!");
            return;
        }

        Wallet walletChecked = checkMnemonic(walletFirst);
        if (null == walletChecked) {
            UChainLog.e(TAG, "eth walletChecked is null!");
            return;
        }

        String toKeyStore = null;
        try {
            toKeyStore = walletChecked.toKeyStore(mPwd);
        } catch (Exception e) {
            UChainLog.e(TAG, "eth toKeyStore exception:" + e.getMessage());
        }

        if (TextUtils.isEmpty(toKeyStore)) {
            UChainLog.e(TAG, "eth toKeyStore is null！");
            return;
        }

        ArrayList<String> assets = new ArrayList<>();
        assets.add(Constant.ASSETS_ETH);

        ArrayList<String> colorAssets = new ArrayList<>();
        colorAssets.add(Constant.ASSETS_ERC20_NMB);

        EthWallet ethWallet = new EthWallet();
        ethWallet.setWalletType(Constant.WALLET_TYPE_ETH);
        ethWallet.setName(mName);
        ethWallet.setAddress(walletChecked.address());
        ethWallet.setBackupState(Constant.BACKUP_UNFINISHED);
        ethWallet.setKeyStore(toKeyStore);
        ethWallet.setAssetJson(GsonUtils.toJsonStr(assets));
        ethWallet.setColorAssetJson(GsonUtils.toJsonStr(colorAssets));

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        uChainWalletDbDao.insert(Constant.TABLE_ETH_WALLET, ethWallet);
        UChainListeners.getInstance().notifyWalletAdd(ethWallet);
        mICreateEthWalletCallback.createEthWallet(walletChecked);
    }

    private Wallet checkMnemonic(Wallet wallet) {
        Wallet walletFinal = null;
        try {
            String mnemonic = wallet.mnemonic("en_US");
            walletFinal = Ethmobile.fromMnemonic(mnemonic, "en_US");
        } catch (Exception e) {
            UChainLog.e(TAG, "eth checkMnemonic fromMnemonic Exception:" + e.getMessage());
            try {
                walletFinal = checkMnemonic(Ethmobile.new_());
            } catch (Exception e1) {
                UChainLog.e(TAG, "eth checkMnemonic new_() Exception:" + e.getMessage());
            }
        }

        return walletFinal;
    }
}
