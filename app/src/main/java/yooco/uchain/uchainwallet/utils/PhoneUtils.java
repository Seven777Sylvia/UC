package yooco.uchain.uchainwallet.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;

/**
 * Created by SteelCabbage on 2018/5/24 0024.
 */

public class PhoneUtils {

    private static final String TAG = PhoneUtils.class.getSimpleName();
    private static SimpleDateFormat sSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取顶部状态栏高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int statusBarHeight = -1;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId);

        }
        UChainLog.v(TAG, "getStatusBarHeight:" + statusBarHeight);
        return statusBarHeight;
    }

    /**
     * 获取底部虚拟按键高度
     *
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        int navigationBarHeight = -1;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = resources.getDimensionPixelSize(resourceId);
        }
        UChainLog.v(TAG, "getNavigationBarHeight:" + navigationBarHeight);
        return navigationBarHeight;
    }

    public static void logDisplayMetrics(Activity activity) {
        DisplayMetrics metric = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metric);
        int widthPixels = metric.widthPixels;
        int heightPixels = metric.heightPixels;
        float density = metric.density;
        int densityDpi = metric.densityDpi;
        float xdpi = metric.xdpi;
        float ydpi = metric.ydpi;
        UChainLog.i(TAG, "widthPixels:" + widthPixels);
        UChainLog.i(TAG, "heightPixels:" + heightPixels);
        UChainLog.i(TAG, "density:" + density);
        UChainLog.i(TAG, "densityDpi:" + densityDpi);
        UChainLog.i(TAG, "xdpi:" + xdpi);
        UChainLog.i(TAG, "ydpi:" + ydpi);
    }

    /**
     * 复制内容到剪切板
     *
     * @param copyContent
     */
    public static void copy2Clipboard(Context context, String copyContent) {
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context
                .CLIPBOARD_SERVICE);
        if (null == clipboardManager) {
            UChainLog.e(TAG, "clipboardManager is null!");
            return;
        }

        ClipData clipData = ClipData.newPlainText("text", copyContent);
        clipboardManager.setPrimaryClip(clipData);
    }


    public static String getFormatTime(long time) {
        String formatTime = "";
        if (time == 0) {
            return formatTime;
        }

        try {
            formatTime = sSimpleDateFormat.format(new Date(time * 1000));
        } catch (Exception e) {
            UChainLog.e(TAG, "getFormatTime exception:" + e.getMessage());
        }
        return formatTime;
    }

    public static byte[] reverseArray(String string) {
        if ("0".equals(string)) {
            byte[] zero = new byte[1];
            return zero;
        }
        byte[] array = hexStringToBytes(string);
        byte[] array_list = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            array_list[i] = (array[array.length - i - 1]);
        }
        return array_list;
    }

    private static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    public static String getAppLanguage() {
        String defLanguage = Locale.getDefault().toString();
        return (String) SharedPreferencesUtils.getParam(UChainWalletApplication.getInstance(),
                Constant.CURRENT_LANGUAGE, defLanguage);
    }

    // update language
    public static void updateLocale(Locale newUserLocale) {
        Configuration _Configuration = UChainWalletApplication.getInstance().getResources()
                .getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            _Configuration.setLocale(newUserLocale);
        } else {
            _Configuration.locale = newUserLocale;
        }
        DisplayMetrics _DisplayMetrics = UChainWalletApplication.getInstance().getResources()
                .getDisplayMetrics();

        UChainWalletApplication.getInstance().getResources().updateConfiguration
                (_Configuration, _DisplayMetrics);

        SharedPreferencesUtils.putParam(UChainWalletApplication.getInstance(), Constant
                .CURRENT_LANGUAGE, newUserLocale.toString());
    }

    public static Context attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 8.0需要使用createConfigurationContext处理
            return updateResources(context);
        } else {
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private static Context updateResources(Context context) {
        Resources resources = context.getResources();
        Locale locale = getUserLocale();// getSetLocale方法是获取新设置的语言

        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        configuration.setLocales(new LocaleList(locale));
        return context.createConfigurationContext(configuration);
    }

    public static Locale getUserLocale() {
        String defLanguage = Locale.getDefault().toString();
        String spLanguage = (String) SharedPreferencesUtils.getParam(UChainWalletApplication
                .getInstance(), Constant.CURRENT_LANGUAGE, defLanguage);

        if (TextUtils.isEmpty(spLanguage)) {
            UChainLog.e(TAG, "languageValue is null or empty!");
            return Locale.ENGLISH;
        }

        if (spLanguage.contains(Locale.CHINA.toString())) {
            return Locale.SIMPLIFIED_CHINESE;
        } else if (spLanguage.contains(Locale.ENGLISH.toString())) {
            return Locale.US;
        } else {
            return Locale.getDefault();
        }
    }

    public static void setLanguage() {
        Locale userLocale = PhoneUtils.getUserLocale();
        PhoneUtils.updateLocale(userLocale);
    }
}
