package yooco.uchain.uchainwallet.view.dialog;

import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.IFromKeystoreToNeoWalletCallback;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IFromKeystoreToEthWalletCallback;
import yooco.uchain.uchainwallet.utils.task.runnable.FromKeystoreToNeoWallet;
import yooco.uchain.uchainwallet.utils.task.runnable.eth.FromKeystoreToEthWallet;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.DensityUtil;
import yooco.uchain.uchainwallet.utils.ToastUtils;
import neomobile.Wallet;

/**
 * Created by SteelCabbage on 2018/5/31 0031.
 */

public class TransferPwdDialog extends DialogFragment implements View.OnClickListener,
        IFromKeystoreToNeoWalletCallback, IFromKeystoreToEthWalletCallback {

    private static final String TAG = TransferPwdDialog.class.getSimpleName();

    private Button mBt_dialog_pwd_transfer_cancel;
    private Button mBt_dialog_pwd_transfer_confirm;
    private EditText mEt_dialog_pwd_transfer;
    private AppCompatTextView mTv_dialog_amount;
    private TextView mTv_dialog_unit;
    private OnCheckPwdListener mOnCheckPwdListener;

    private WalletBean mCurrentWalletBean;
    private String mTransferAmount;
    private String mTransferUnit;

    public static TransferPwdDialog newInstance() {
        return new TransferPwdDialog();
    }

    public interface OnCheckPwdListener {
        void onCheckPwd(Wallet wallet);

        void onCheckEthPwd(ethmobile.Wallet wallet);
    }

    public void setOnCheckPwdListener(OnCheckPwdListener onCheckPwdListener) {
        mOnCheckPwdListener = onCheckPwdListener;
    }


    public void setCurrentWallet(WalletBean currentWalletBean) {
        mCurrentWalletBean = currentWalletBean;
    }

    public void setTransferAmount(String transferAmount) {
        mTransferAmount = transferAmount;
    }

    public void setTransferUnit(String transferUnit) {
        mTransferUnit = transferUnit;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {

        // 去掉边框
        Window window = getDialog().getWindow();
        if (null != window) {
            window.setBackgroundDrawable(new ColorDrawable(0));
        }

        // 点击空白区域不可取消
        setCancelable(false);

        return inflater.inflate(R.layout.dialog_transfer_pwd, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(DensityUtil.dip2px(getActivity(), 257), DensityUtil
                .dip2px(getActivity(), 159));
    }

    private void initView(View view) {
        mBt_dialog_pwd_transfer_cancel = view.findViewById(R.id.bt_dialog_pwd_transfer_cancel);
        mBt_dialog_pwd_transfer_confirm = view.findViewById(R.id.bt_dialog_pwd_transfer_confirm);
        mEt_dialog_pwd_transfer = view.findViewById(R.id.et_dialog_pwd_transfer);
        mTv_dialog_amount = view.findViewById(R.id.tv_dialog_amount);
        mTv_dialog_unit = view.findViewById(R.id.tv_dialog_unit);

        mBt_dialog_pwd_transfer_cancel.setOnClickListener(this);
        mBt_dialog_pwd_transfer_confirm.setOnClickListener(this);
    }

    private void initData() {
        mTv_dialog_amount.setText(mTransferAmount);
        mTv_dialog_unit.setText(mTransferUnit);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_dialog_pwd_transfer_cancel:
                dismiss();
                break;
            case R.id.bt_dialog_pwd_transfer_confirm:
                String pwd = mEt_dialog_pwd_transfer.getText().toString().trim();
                int walletType = mCurrentWalletBean.getWalletType();
                switch (walletType) {
                    case Constant.WALLET_TYPE_NEO:
                        TaskController.getInstance().submit(new FromKeystoreToNeoWallet(mCurrentWalletBean.getKeyStore(), pwd,
                                this));
                        break;
                    case Constant.WALLET_TYPE_ETH:
                        TaskController.getInstance().submit(new FromKeystoreToEthWallet(mCurrentWalletBean.getKeyStore(), pwd,
                                this));
                        break;
                    case Constant.WALLET_TYPE_CPX:
                        break;
                    default:
                        UChainLog.e(TAG, "unknown wallet type!");
                        return;
                }
                break;
        }
    }

    @Override
    public void fromKeystoreToNeoWallet(Wallet wallet) {
        if (null == mOnCheckPwdListener) {
            UChainLog.e(TAG, "fromKeystoreToNeoWallet() -> mOnCheckPwdListener is null!");
            return;
        }

        if (null == wallet) {
            UChainLog.e(TAG, "pwd is not match keystore");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance()
                            .getResources().getString(R.string.password_incorrect));
                }
            });
            return;
        }

        mOnCheckPwdListener.onCheckPwd(wallet);
        dismiss();
    }

    @Override
    public void fromKeystoreToEthWallet(ethmobile.Wallet wallet) {
        if (null == mOnCheckPwdListener) {
            UChainLog.e(TAG, "fromKeystoreToEthWallet() -> mOnCheckPwdListener is null!");
            return;
        }

        if (null == wallet) {
            UChainLog.e(TAG, "eth pwd is not match keystore");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance()
                            .getResources().getString(R.string.password_incorrect));
                }
            });
            return;
        }

        mOnCheckPwdListener.onCheckEthPwd(wallet);
        dismiss();
    }

}
