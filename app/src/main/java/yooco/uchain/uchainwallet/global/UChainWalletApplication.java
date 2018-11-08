package yooco.uchain.uchainwallet.global;

import android.app.Application;
import android.content.res.Configuration;

import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.PhoneUtils;

/**
 * Created by SteelCabbage on 2018/5/21 0021.
 */

public class UChainWalletApplication extends Application {

    private static final String TAG = UChainWalletApplication.class.getSimpleName();

    private static UChainWalletApplication sApexWalletApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        UChainLog.i(TAG, "UChainWalletApplication start!");
        sApexWalletApplication = this;
        TaskController.getInstance().doInit();
        UChainListeners.getInstance().doInit();
        UChainGlobalTask.getInstance().doInit();
        PhoneUtils.setLanguage();
    }

    public static UChainWalletApplication getInstance() {
        return sApexWalletApplication;
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        PhoneUtils.setLanguage();
    }
}