package yooco.uchain.uchainwallet.utils;

import android.widget.Toast;

import yooco.uchain.uchainwallet.global.UChainWalletApplication;

/**
 * Created by SteelCabbage on 2018/7/4 0004 14:48.
 * E-Mail：liuyi_61@163.com
 */

public class ToastUtils {
    private Toast mToast;
    private static ToastUtils mToastUtils;

    private ToastUtils() {
        mToast = Toast.makeText(UChainWalletApplication.getInstance(), "", Toast.LENGTH_SHORT);
    }

    public static ToastUtils getInstance() {
        if (null == mToastUtils) {
            synchronized (ToastUtils.class) {
                if (null == mToastUtils) {
                    mToastUtils = new ToastUtils();
                }
            }
        }

        return mToastUtils;
    }

    public void showToast(String toastMsg) {
        mToast.setText(toastMsg);
        mToast.show();
    }



}
