package yooco.uchain.uchainwallet.view.page.assets;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.google.zxing.common.BitmapUtils;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.PhoneUtils;
import yooco.uchain.uchainwallet.utils.ToastUtils;

public class GatheringActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = GatheringActivity.class.getSimpleName();

    private TextView mTv_gathering_wallet_name;
    private TextView mTv_gathering_wallet_addr;
    private Button mBt_gathering_copy_addr;
    private ImageView mIv_gathering_qr_code;

    private WalletBean mWalletBean;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_gathering);

    }

    @Override
    protected void init() {
        super.init();

        initView();
        initData();
    }

    private void initView() {
        mTv_gathering_wallet_name = (TextView) findViewById(R.id.tv_gathering_wallet_name);
        mTv_gathering_wallet_addr = (TextView) findViewById(R.id.tv_gathering_wallet_addr);
        mBt_gathering_copy_addr = (Button) findViewById(R.id.bt_gathering_copy_addr);
        mIv_gathering_qr_code = (ImageView) findViewById(R.id.iv_gathering_qr_code);

        mBt_gathering_copy_addr.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (null == intent) {
            UChainLog.e(TAG, "intent is null!");
            return;
        }

        mWalletBean = intent.getParcelableExtra(Constant.PARCELABLE_WALLET_BEAN_GATHERING);
        if (null == mWalletBean) {
            UChainLog.e(TAG, "mWalletBean is null!");
            return;
        }
        mTv_gathering_wallet_name.setText(mWalletBean.getName());
        mTv_gathering_wallet_addr.setText(mWalletBean.getAddress());

        //生成二维码
        String walletAddr = mWalletBean.getAddress();
        Bitmap bitmap;
        try {
            bitmap = BitmapUtils.create2DCode(walletAddr);
            mIv_gathering_qr_code.setImageBitmap(bitmap);
        } catch (WriterException e) {
            UChainLog.e(TAG, "qrCode exception:" + e.getMessage());
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_gathering_copy_addr:
                UChainLog.i(TAG, "bt_gathering_copy_addr is click！");
                PhoneUtils.copy2Clipboard(UChainWalletApplication.getInstance(), mWalletBean.getAddress());
                ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance()
                        .getResources().getString(R.string.wallet_copied_share));
                break;
            default:
                break;
        }
    }

}
