package yooco.uchain.uchainwallet.view.page.wallet;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.PhoneUtils;
import yooco.uchain.uchainwallet.utils.ToastUtils;

public class ExportKeystoreActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = ExportKeystoreActivity.class.getSimpleName();

    private TextView mTv_export_wallet_keystore;
    private Button mBt_export_wallet_keystore;

    private String mKeystore;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_export_keystore);
    }

    @Override
    protected void init() {
        super.init();

        initView();
        initData();
    }

    private void initView() {
        mTv_export_wallet_keystore = (TextView) findViewById(R.id.tv_export_wallet_keystore);
        mBt_export_wallet_keystore = (Button) findViewById(R.id.bt_export_wallet_keystore);

        mBt_export_wallet_keystore.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (null == intent) {
            UChainLog.e(TAG, "intent is null!");
            return;
        }

        mKeystore = intent.getStringExtra(Constant.BACKUP_KEYSTORE);
        if (TextUtils.isEmpty(mKeystore)) {
            UChainLog.e(TAG, "keystore is null!");
            return;
        }

        mTv_export_wallet_keystore.setText(mKeystore);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_export_wallet_keystore:
                PhoneUtils.copy2Clipboard(UChainWalletApplication.getInstance(), mKeystore);
                ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance()
                        .getResources().getString(R.string.keystore_copied));
                break;
            default:
                break;
        }
    }
}
