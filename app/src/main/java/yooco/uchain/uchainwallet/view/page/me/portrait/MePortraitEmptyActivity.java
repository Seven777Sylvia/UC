package yooco.uchain.uchainwallet.view.page.me.portrait;

import android.view.View;
import android.widget.Button;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.view.page.wallet.CreateWalletActivity;

/**
 * Created by SteelCabbage on 2018/7/23 0023 18:11.
 * E-Mail：liuyi_61@163.com
 */

public class MePortraitEmptyActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = MePortraitEmptyActivity.class.getSimpleName();

    private Button mBt_portrait_create_wallet;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_me_portrait_empty);
    }

    @Override
    protected void init() {
        super.init();

        initView();
        initData();
    }

    private void initView() {
        mBt_portrait_create_wallet = findViewById(R.id.bt_portrait_create_wallet);
        mBt_portrait_create_wallet.setOnClickListener(this);
    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_portrait_create_wallet:
                startActivity(CreateWalletActivity.class, true);
                break;
            default:
                break;
        }
    }
}
