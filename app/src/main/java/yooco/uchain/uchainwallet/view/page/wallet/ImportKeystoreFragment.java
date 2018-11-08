package yooco.uchain.uchainwallet.view.page.wallet;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseFragment;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.data.bean.eth.EthWallet;
import yooco.uchain.uchainwallet.data.bean.neo.NeoWallet;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.IFromKeystoreToNeoWalletCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IFromKeystoreToEthWalletCallback;
import yooco.uchain.uchainwallet.utils.task.runnable.FromKeystoreToNeoWallet;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.FromKeystoreToEthWallet;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.DensityUtil;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import yooco.uchain.uchainwallet.utils.SharedPreferencesUtils;
import yooco.uchain.uchainwallet.utils.ToastUtils;
import yooco.uchain.uchainwallet.view.page.MainActivity;
import cn.qqtheme.framework.picker.OptionPicker;
import cn.qqtheme.framework.widget.WheelView;
import neomobile.Wallet;

/**
 * Created by SteelCabbage on 2018/6/10 22:31
 * E-Mail：liuyi_61@163.com
 */
public class ImportKeystoreFragment extends BaseFragment implements View.OnClickListener,
        IFromKeystoreToNeoWalletCallback, IFromKeystoreToEthWalletCallback {

    private static final String TAG = ImportKeystoreFragment.class.getSimpleName();

    private EditText mEt_import_wallet_keystore;
    private ImageButton mIb_import_wallet_keystore_privacy_point;
    private Button mBt_import_wallet_keystore;
    private TextInputEditText mEt_import_wallet_keystore_pwd;
    private TextInputLayout mTl_import_wallet_keystore;
    private TextView mTv_import_wallet_keystore_privacy;
    private TextView mTv_import_wallet_keystore_type;
    private ImageView mIv_import_wallet_keystore_arrows;

    private boolean mIsSelectedPrivacy;
    private boolean mIsAgreePrivacy;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_import_keystore, container, false);
    }

    @Override
    protected void init(View view) {
        super.init(view);

        initView(view);
        initData();
    }

    private void initView(View view) {
        mTv_import_wallet_keystore_type = view.findViewById(R.id.tv_import_wallet_keystore_type);
        mIv_import_wallet_keystore_arrows = view.findViewById(R.id.iv_import_wallet_keystore_arrows);
        mEt_import_wallet_keystore = view.findViewById(R.id.et_import_wallet_keystore);
        mEt_import_wallet_keystore_pwd = view.findViewById(R.id.et_import_wallet_keystore_pwd);
        mIb_import_wallet_keystore_privacy_point = view.findViewById(R.id.ib_import_wallet_keystore_privacy_point);
        mTv_import_wallet_keystore_privacy = view.findViewById(R.id.tv_import_wallet_keystore_privacy);
        mBt_import_wallet_keystore = view.findViewById(R.id.bt_import_wallet_keystore);
        mTl_import_wallet_keystore = view.findViewById(R.id.tl_import_wallet_keystore);

        mTv_import_wallet_keystore_type.setOnClickListener(this);
        mIv_import_wallet_keystore_arrows.setOnClickListener(this);
        mIb_import_wallet_keystore_privacy_point.setOnClickListener(this);
        mBt_import_wallet_keystore.setOnClickListener(this);
        mTv_import_wallet_keystore_privacy.setOnClickListener(this);
    }

    private void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_import_wallet_keystore_arrows:
            case R.id.tv_import_wallet_keystore_type:
                showOptionPicker();
                break;
            case R.id.ib_import_wallet_keystore_privacy_point:
                if (mIsSelectedPrivacy) {
                    mIsSelectedPrivacy = false;
                    mIb_import_wallet_keystore_privacy_point.setImageResource(R.drawable
                            .icon_privacy_def);
                    mBt_import_wallet_keystore.setBackgroundResource(R.drawable
                            .shape_import_wallet_bt_bg_def);
                    mBt_import_wallet_keystore.setTextColor(getResources().getColor(R.color
                            .c_666666));
                    mIsAgreePrivacy = false;
                } else {
                    mIsSelectedPrivacy = true;
                    mIb_import_wallet_keystore_privacy_point.setImageResource(R.drawable
                            .icon_privacy);
                    mBt_import_wallet_keystore.setBackgroundResource(R.drawable
                            .shape_new_visitor_bt_bg);
                    mBt_import_wallet_keystore.setTextColor(Color.WHITE);
                    mIsAgreePrivacy = true;
                }
                break;
            case R.id.tv_import_wallet_keystore_privacy:
                startActivity(PrivacyActivity.class, false);
                break;
            case R.id.bt_import_wallet_keystore:
                if (!checkInput()) {
                    UChainLog.w(TAG, "checkInput is false!");
                    return;
                }

                String keystore = mEt_import_wallet_keystore.getText().toString().trim();
                String keystorePwd = mEt_import_wallet_keystore_pwd.getText().toString().trim();
                String walletType = mTv_import_wallet_keystore_type.getText().toString().trim();
                if (TextUtils.isEmpty(walletType)) {
                    UChainLog.e(TAG, "walletType is null!");
                    ToastUtils.getInstance().showToast(getString(R.string.select_wallet_type));
                    return;
                }

                switch (walletType) {
                    case "NEO":
                        TaskController.getInstance().submit(new FromKeystoreToNeoWallet(keystore,
                                keystorePwd, this));
                        break;
                    case "ETH":
                        TaskController.getInstance().submit(new FromKeystoreToEthWallet(keystore,
                                keystorePwd, this));
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    private boolean checkInput() {
        String keystore = mEt_import_wallet_keystore.getText().toString().trim();
        String pwd = mEt_import_wallet_keystore_pwd.getText().toString().trim();

        if (TextUtils.isEmpty(keystore)) {
            ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance().getResources()
                    .getString(R.string
                            .keystore_can_not_be_empty));
            UChainLog.w(TAG, "keystore is null!");
            return false;
        }

        if (TextUtils.isEmpty(pwd)) {
            ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance().getResources()
                    .getString(R.string.pwd_can_not_be_empty));
            UChainLog.w(TAG, "pwd is null!");
            return false;
        }

        if (!mIsAgreePrivacy) {
            ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance().getResources()
                    .getString(R.string.read_privacy_policy_first));
            return false;
        }

        return true;
    }

    @Override
    public void fromKeystoreToNeoWallet(Wallet wallet) {
        if (null == wallet) {
            UChainLog.e(TAG, "fromKeystoreToNeoWallet() -> wallet is null!");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance()
                            .getResources().getString(R.string.keystore_import_failed));
                }
            });
            return;
        }

        String neoAddress = wallet.address();
        if (TextUtils.isEmpty(neoAddress)) {
            UChainLog.e(TAG, "fromKeystoreToNeoWallet() -> neoAddress is null!");
            return;
        }

        if (!neoAddress.startsWith(Constant.NEO_ADDRESS_START_WITH) || neoAddress.length() != 34) {
            UChainLog.e(TAG, "the address is not Neo type!");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.getInstance().showToast(getString(R.string.select_correct_import_type));
                }
            });
            return;
        }

        keystoreToWallet(wallet.address(), Constant.WALLET_TYPE_NEO);
    }

    @Override
    public void fromKeystoreToEthWallet(ethmobile.Wallet wallet) {
        if (null == wallet) {
            UChainLog.e(TAG, "fromKeystoreToEthWallet() -> wallet is null!");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance()
                            .getResources().getString(R.string.keystore_import_failed));
                }
            });
            return;
        }

        String ethAddress = wallet.address();
        if (TextUtils.isEmpty(ethAddress)) {
            UChainLog.e(TAG, "fromKeystoreToEthWallet() -> ethAddress is null!");
            return;
        }

        if (!ethAddress.startsWith(Constant.ETH_ADDRESS_START_WITH) || ethAddress.length() != 42) {
            UChainLog.e(TAG, "the address is not Eth type!");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.getInstance().showToast(getString(R.string.select_correct_import_type));
                }
            });
            return;
        }

        keystoreToWallet(wallet.address(), Constant.WALLET_TYPE_ETH);
    }

    private void keystoreToWallet(String walletAddress, int walletType) {
        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication
                .getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        String tableName = null;
        switch (walletType) {
            case Constant.WALLET_TYPE_NEO:
                tableName = Constant.TABLE_NEO_WALLET;
                break;
            case Constant.WALLET_TYPE_ETH:
                tableName = Constant.TABLE_ETH_WALLET;
                break;
            case Constant.WALLET_TYPE_CPX:
                break;
            default:
                break;
        }

        WalletBean walletBeanTmp = uChainWalletDbDao.queryByWalletAddress(tableName, walletAddress);
        if (null != walletBeanTmp) {
            UChainLog.e(TAG, "this walletBeanTmp from keystore has existed!");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance()
                            .getResources().getString(R.string
                                    .wallet_exist));
                }
            });
            return;
        }

        String keystore = mEt_import_wallet_keystore.getText().toString().trim();

        WalletBean walletBean = null;
        ArrayList<String> assets = new ArrayList<>();
        ArrayList<String> colorAsset = new ArrayList<>();
        switch (walletType) {
            case Constant.WALLET_TYPE_NEO:
                walletBean = new NeoWallet();
                assets.add(Constant.ASSETS_NEO_GAS);
                assets.add(Constant.ASSETS_NEO);
                colorAsset.add(Constant.ASSETS_CPX);
                walletBean.setWalletType(Constant.WALLET_TYPE_NEO);
                break;
            case Constant.WALLET_TYPE_ETH:
                walletBean = new EthWallet();
                assets.add(Constant.ASSETS_ETH);
                colorAsset.add(Constant.ASSETS_ERC20_NMB);
                walletBean.setWalletType(Constant.WALLET_TYPE_ETH);
                break;
            case Constant.WALLET_TYPE_CPX:
                break;
            default:
                break;
        }

        if (null == walletBean) {
            UChainLog.e(TAG, "keystoreToWallet() -> walletBean is null!");
            return;
        }

        walletBean.setName(Constant.WALLET_NAME_IMPORT_DEFAULT);
        walletBean.setAddress(walletAddress);
        walletBean.setBackupState(Constant.BACKUP_UNFINISHED);
        walletBean.setKeyStore(keystore);
        walletBean.setAssetJson(GsonUtils.toJsonStr(assets));
        walletBean.setColorAssetJson(GsonUtils.toJsonStr(colorAsset));

        uChainWalletDbDao.insert(tableName, walletBean);
        UChainListeners.getInstance().notifyWalletAdd(walletBean);

        isFirstEnter();
    }

    private void isFirstEnter() {
        boolean isFirstExport = (boolean) SharedPreferencesUtils.getParam(UChainWalletApplication.getInstance(),
                Constant.IS_FIRST_ENTER_MAIN, true);
        if (isFirstExport) {
            SharedPreferencesUtils.putParam(UChainWalletApplication.getInstance(), Constant.IS_FIRST_ENTER_MAIN, false);
            startActivity(MainActivity.class, true);
        } else {
            getActivity().finish();
        }
    }

    private void showOptionPicker() {
        OptionPicker picker = new OptionPicker(this.getActivity(), getWalletTypes());
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
                mTv_import_wallet_keystore_type.setText(item);
            }
        });

        picker.show();
    }

    private List<String> getWalletTypes() {
        String[] menuTexts = getResources().getStringArray(R.array.create_wallet_type);
        return Arrays.asList(menuTexts);
    }


}
