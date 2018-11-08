package yooco.uchain.uchainwallet.view.page;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Build;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.SharedPreferencesUtils;

public class BootPageActivity extends BaseActivity {

    private static final String TAG = BootPageActivity.class.getSimpleName();

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_boot_page);
    }

    @Override
    protected void init() {
        super.init();

        initView();
    }

    private void initView() {
        ImageView iv_boot_page = (ImageView) findViewById(R.id.iv_boot_page);
        ObjectAnimator animator = ObjectAnimator.ofFloat(iv_boot_page, "alpha", 0, 1, 1);
        animator.setDuration(3000);
        animator.start();
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isFirstEnter();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        // 设置透明导航键
        setNavigationBarColorTransparent();
    }

    private void setNavigationBarColorTransparent() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }

    }

    private void isFirstEnter() {
        boolean isFirstEnter = (boolean) SharedPreferencesUtils.getParam(UChainWalletApplication
                .getInstance(), Constant.IS_FIRST_ENTER_APP, true);
        if (isFirstEnter) {
            UChainLog.i(TAG, "this is first enter!");
            SharedPreferencesUtils.putParam(UChainWalletApplication.getInstance(), Constant
                    .IS_FIRST_ENTER_APP, false);
            startActivity(NewVisitorActivity.class, true);
        } else {
            startActivity(MainActivity.class, true);
        }
    }

}
