package yooco.uchain.uchainwallet.view.page.wallet;

import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.data.bean.eth.EthWallet;
import yooco.uchain.uchainwallet.data.bean.neo.NeoWallet;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.ICreateWalletCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.ICreateEthWalletCallback;
import yooco.uchain.uchainwallet.utils.task.runnable.CreateNeoWallet;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.CreateEthWallet;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.DensityUtil;
import yooco.uchain.uchainwallet.utils.ToastUtils;
import cn.qqtheme.framework.picker.OptionPicker;
import cn.qqtheme.framework.widget.WheelView;
import neomobile.Wallet;

public class CreateWalletActivity extends BaseActivity implements View.OnClickListener,
        ICreateWalletCallback, ICreateEthWalletCallback {

    private static final String TAG = CreateWalletActivity.class.getSimpleName();

    private Button mBt_create_wallet_confirm;
    private TextInputEditText mEt_create_wallet_name;
    private TextInputEditText mEt_create_wallet_pwd;
    private TextInputEditText mEt_create_wallet_repeat_pwd;
    private ImageButton mIb_create_wallet_privacy_point;
    private TextInputLayout mTl_create_wallet_name;
    private TextInputLayout mTl_create_wallet_pwd;
    private TextInputLayout mTl_create_wallet_repeat_pwd;
    private Button mBt_create_wallet_import;
    private TextView mTv_create_wallet_privacy;
    private TextView mTv_create_wallet_type;
    private ImageView mIv_create_wallet_arrows;

    private boolean mIsSelectedPrivacy;
    private boolean mIsAgreePrivacy;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_create_wallet);
    }

    @Override
    protected void init() {
        super.init();

        initView();
    }

    private void initView() {
        mTv_create_wallet_type = (TextView) findViewById(R.id.tv_create_wallet_type);
        mIv_create_wallet_arrows = (ImageView) findViewById(R.id.iv_create_wallet_arrows);
        mEt_create_wallet_name = (TextInputEditText) findViewById(R.id.et_create_wallet_name);
        mEt_create_wallet_pwd = (TextInputEditText) findViewById(R.id.et_create_wallet_pwd);
        mEt_create_wallet_repeat_pwd = (TextInputEditText) findViewById(R.id.et_create_wallet_repeat_pwd);
        mBt_create_wallet_confirm = (Button) findViewById(R.id.bt_create_wallet_confirm);
        mBt_create_wallet_import = (Button) findViewById(R.id.bt_create_wallet_import);
        mIb_create_wallet_privacy_point = findViewById(R.id.ib_create_wallet_privacy_point);
        mTv_create_wallet_privacy = (TextView) findViewById(R.id.tv_create_wallet_privacy);
        mTl_create_wallet_name = (TextInputLayout) findViewById(R.id.tl_create_wallet_name);
        mTl_create_wallet_pwd = (TextInputLayout) findViewById(R.id.tl_create_wallet_pwd);
        mTl_create_wallet_repeat_pwd = (TextInputLayout) findViewById(R.id.tl_create_wallet_repeat_pwd);

        mTv_create_wallet_type.setOnClickListener(this);
        mIv_create_wallet_arrows.setOnClickListener(this);
        mBt_create_wallet_confirm.setOnClickListener(this);
        mIb_create_wallet_privacy_point.setOnClickListener(this);
        mBt_create_wallet_import.setOnClickListener(this);
        mTv_create_wallet_privacy.setOnClickListener(this);

        mEt_create_wallet_name.addTextChangedListener(new MyTextWatcher(mEt_create_wallet_name) {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    //设置错误提示信息
                    showError(mTl_create_wallet_name, UChainWalletApplication.getInstance()
                            .getResources().getString(R.string.wallet_name_can_not_be_empty));
                } else {
                    //关闭错误提示
                    mTl_create_wallet_name.setErrorEnabled(false);
                }
            }
        });

        mEt_create_wallet_pwd.addTextChangedListener(new MyTextWatcher(mEt_create_wallet_pwd) {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() < 6) {
                    //设置错误提示信息
                    showError(mTl_create_wallet_pwd, UChainWalletApplication.getInstance()
                            .getResources().getString(R.string.pwd_must_not_be_less_than_6_bits));
                } else {
                    //关闭错误提示
                    mTl_create_wallet_pwd.setErrorEnabled(false);
                }
            }
        });

        mEt_create_wallet_repeat_pwd.addTextChangedListener(new MyTextWatcher
                (mEt_create_wallet_repeat_pwd) {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pwd = mEt_create_wallet_pwd.getText().toString().trim();
                String repeatPwd = mEt_create_wallet_repeat_pwd.getText().toString().trim();

                if (s.length() < 6) {
                    //设置错误提示信息
                    showError(mTl_create_wallet_repeat_pwd, UChainWalletApplication.getInstance()
                            .getResources().getString(R.string.pwd_must_not_be_less_than_6_bits));
                } else if (TextUtils.isEmpty(repeatPwd) || !repeatPwd.equals(pwd)) {
                    showError(mTl_create_wallet_repeat_pwd, UChainWalletApplication.getInstance()
                            .getResources().getString(R.string.inconsistent_password));
                } else {
                    //关闭错误提示
                    mTl_create_wallet_repeat_pwd.setErrorEnabled(false);
                }

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_create_wallet_arrows:
            case R.id.tv_create_wallet_type:
                showOptionPicker();
                break;
            case R.id.bt_create_wallet_confirm:
                if (!checkInput()) {
                    UChainLog.w(TAG, "checkInput is false!");
                    return;
                }

                String walletName = mEt_create_wallet_name.getText().toString().trim();
                String walletPwd = mEt_create_wallet_pwd.getText().toString().trim();
                String walletType = mTv_create_wallet_type.getText().toString().trim();
                if (TextUtils.isEmpty(walletType)) {
                    UChainLog.e(TAG, "walletType is null!");
                    ToastUtils.getInstance().showToast(getString(R.string.select_wallet_type));
                    return;
                }

                switch (walletType) {
                    case "NEO":
                        TaskController.getInstance().submit(new CreateNeoWallet(walletName, walletPwd, this));
                        break;
                    case "ETH":
                        TaskController.getInstance().submit(new CreateEthWallet(walletName, walletPwd, this));
                        break;
                    default:
                        break;

                }
                break;
            case R.id.bt_create_wallet_import:
                startActivity(ImportWalletActivity.class, true);
                break;
            case R.id.ib_create_wallet_privacy_point:
                if (mIsSelectedPrivacy) {
                    mIsSelectedPrivacy = false;
                    mIb_create_wallet_privacy_point.setImageResource(R.drawable.icon_privacy_def);
                    mBt_create_wallet_confirm.setBackgroundResource(R.drawable.shape_gray_bt_bg);
                    mBt_create_wallet_confirm.setTextColor(getResources().getColor(R.color.c_979797));
                    mIsAgreePrivacy = false;
                } else {
                    mIsSelectedPrivacy = true;
                    mIb_create_wallet_privacy_point.setImageResource(R.drawable.icon_privacy);
                    mBt_create_wallet_confirm.setBackgroundResource(R.drawable.shape_new_visitor_bt_bg);
                    mBt_create_wallet_confirm.setTextColor(Color.WHITE);
                    mIsAgreePrivacy = true;
                }
                break;
            case R.id.tv_create_wallet_privacy:
                startActivity(PrivacyActivity.class, false);
                break;
            default:
                break;
        }
    }

    private boolean checkInput() {
        String wallet_name = mEt_create_wallet_name.getText().toString().trim();
        String wallet_pwd = mEt_create_wallet_pwd.getText().toString().trim();
        String repeat_pwd = mEt_create_wallet_repeat_pwd.getText().toString().trim();

        if (TextUtils.isEmpty(wallet_name)) {
            ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance().getResources()
                    .getString(R.string.wallet_name_can_not_be_empty));
            UChainLog.w(TAG, "wallet_name is null!");
            return false;
        }

        if (TextUtils.isEmpty(wallet_pwd) || TextUtils.isEmpty(repeat_pwd)) {
            ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance().getResources()
                    .getString(R.string.pwd_can_not_be_empty));
            UChainLog.w(TAG, "wallet_pwd or repeat_pwd is null!");
            return false;
        }

        if (!wallet_pwd.equals(repeat_pwd)) {
            ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance().getResources()
                    .getString(R.string.inconsistent_password));
            UChainLog.w(TAG, "wallet_pwd and repeat_pwd is not same!");
            return false;
        }

        if (repeat_pwd.length() < 6) {
            ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance().getResources()
                    .getString(R.string.pwd_must_not_be_less_than_6_bits));
            UChainLog.w(TAG, "repeat_pwd.length() < 6!");
            return false;
        }

        if (!mIsAgreePrivacy) {
            ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance().getResources()
                    .getString(R.string.read_privacy_policy_first));
            return false;
        }

        return true;
    }

    private void showError(TextInputLayout textInputLayout, String error) {
        textInputLayout.setError(error);
        EditText editText = textInputLayout.getEditText();
        if (null == editText) {
            UChainLog.e(TAG, "editText is null!");
            return;
        }

        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
    }

    @Override
    public void newWallet(Wallet wallet) {
        if (null == wallet) {
            UChainLog.e(TAG, "wallet is null！");
            return;
        }

        String mnemonicEnUs = null;
        try {
            mnemonicEnUs = wallet.mnemonic("en_US");
        } catch (Exception e) {
            UChainLog.e(TAG, "mnemonicEnUs exception:" + e.getMessage());
        }

        if (TextUtils.isEmpty(mnemonicEnUs)) {
            UChainLog.e(TAG, "mnemonicEnUs is null！");
            return;
        }

        NeoWallet neoWallet = new NeoWallet();
        neoWallet.setAddress(wallet.address());
        neoWallet.setBackupState(Constant.BACKUP_UNFINISHED);
        Intent intent = new Intent(this, BackupWalletActivity.class);
        intent.putExtra(Constant.BACKUP_MNEMONIC, mnemonicEnUs);
        intent.putExtra(Constant.WALLET_BEAN, neoWallet);
        startActivity(intent);
        finish();
    }

    @Override
    public void createEthWallet(ethmobile.Wallet wallet) {
        if (null == wallet) {
            UChainLog.e(TAG, "wallet is null！");
            return;
        }

        String mnemonicEnUs = null;
        try {
            mnemonicEnUs = wallet.mnemonic("en_US");
        } catch (Exception e) {
            UChainLog.e(TAG, "mnemonicEnUs exception:" + e.getMessage());
        }

        if (TextUtils.isEmpty(mnemonicEnUs)) {
            UChainLog.e(TAG, "mnemonicEnUs is null！");
            return;
        }

        EthWallet ethWallet = new EthWallet();
        ethWallet.setAddress(wallet.address());
        ethWallet.setBackupState(Constant.BACKUP_UNFINISHED);
        Intent intent = new Intent(this, BackupWalletActivity.class);
        intent.putExtra(Constant.BACKUP_MNEMONIC, mnemonicEnUs);
        intent.putExtra(Constant.WALLET_BEAN, ethWallet);
        startActivity(intent);
        finish();
    }

    private List<String> getWalletTypes() {
        String[] menuTexts = getResources().getStringArray(R.array.create_wallet_type);
        return Arrays.asList(menuTexts);
    }

    private void showOptionPicker() {
        OptionPicker picker = new OptionPicker(this, getWalletTypes());
        picker.setOffset(2);
        picker.setDividerRatio(WheelView.DividerConfig.FILL);
        picker.setHeight(DensityUtil.dip2px(UChainWalletApplication.getInstance(), 200));
        picker.setTopHeight(40);
        picker.setDividerColor(UChainWalletApplication.getInstance().getResources().getColor(R.color.c_DDDDDD));
        picker.setTopLineColor(UChainWalletApplication.getInstance().getResources().getColor(R.color.c_DDDDDD));
        picker.setTextColor(Color.BLACK, UChainWalletApplication.getInstance().getResources().getColor(R.color.c_999999));
        picker.setSelectedIndex(1);
        picker.setTextSize(16);

        // set cancel
        picker.setCancelText(UChainWalletApplication.getInstance().getResources().getString(R
                .string.cancel));
        picker.setCancelTextColor(UChainWalletApplication.getInstance().getResources().getColor(R.color.c_1253BF));
        picker.setCancelTextSize(14);

        // set confirm
        picker.setSubmitText(UChainWalletApplication.getInstance().getResources().getString(R
                .string.confirm));
        picker.setSubmitTextColor(UChainWalletApplication.getInstance().getResources().getColor(R.color.c_1253BF));
        picker.setSubmitTextSize(14);

        picker.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
            @Override
            public void onOptionPicked(int index, String item) {
                UChainLog.i(TAG, "index:" + index + ",item:" + item);
                mTv_create_wallet_type.setText(item);
            }
        });

        picker.show();
    }

}
