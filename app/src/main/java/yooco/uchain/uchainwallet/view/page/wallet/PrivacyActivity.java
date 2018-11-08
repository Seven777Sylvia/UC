package yooco.uchain.uchainwallet.view.page.wallet;

import android.webkit.WebView;

import java.util.Locale;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.utils.PhoneUtils;

public class PrivacyActivity extends BaseActivity {

    private static final String TAG = PrivacyActivity.class.getSimpleName();

    private WebView mWv_privacy;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_privacy);
    }

    @Override
    protected void init() {
        super.init();

        initView();
        initData();
    }

    private void initView() {
        mWv_privacy = (WebView) findViewById(R.id.wv_privacy);
    }

    private void initData() {
        String appLanguage = PhoneUtils.getAppLanguage();
        String url;
        if (appLanguage.contains(Locale.CHINA.toString())) {
            url = "file:///android_asset/web/userProtocol.html";
        } else if (appLanguage.contains(Locale.ENGLISH.toString())) {
            url = "file:///android_asset/web/userProtocol_en.html";
        } else {
            url = "file:///android_asset/web/userProtocol_en.html";
        }

        mWv_privacy.loadUrl(url);
    }
}
