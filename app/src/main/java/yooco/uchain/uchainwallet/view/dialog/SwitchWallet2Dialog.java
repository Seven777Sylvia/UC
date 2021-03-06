package yooco.uchain.uchainwallet.view.dialog;

import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.view.adapter.SwitchWallet2RecyclerViewAdapter;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.global.Constant;

/**
 * Created by SteelCabbage on 2018/5/31 0031.
 */

public class SwitchWallet2Dialog extends DialogFragment implements View.OnClickListener,
        DialogInterface.OnKeyListener, SwitchWallet2RecyclerViewAdapter.OnItemClickListener {

    private static final String TAG = SwitchWallet2Dialog.class.getSimpleName();

    private ImageButton mIb_switch_wallet2_back;
    private RecyclerView mRv_dialog_switch_wallet2;
    private onSelectedWalletListener mOnSelectedWalletListener;
    private SwitchWallet2RecyclerViewAdapter mSwitchWallet2RecyclerViewAdapter;

    private List<WalletBean> mWalletBeans;
    private WalletBean mCurrentWalletBean;

    public interface onSelectedWalletListener {
        void onSelectedWallet(WalletBean walletBean);
    }

    public void setOnSelectedWalletListener(onSelectedWalletListener onSelectedWalletListener) {
        mOnSelectedWalletListener = onSelectedWalletListener;
    }

    public void setCurrentWallet(WalletBean currentWallet) {
        mCurrentWalletBean = currentWallet;
    }

    public static SwitchWallet2Dialog newInstance() {
        return new SwitchWallet2Dialog();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        // 去掉边框
        Window window = getDialog().getWindow();
        if (null == window) {
            UChainLog.e(TAG, "window is null!");
            return null;
        }
        window.setBackgroundDrawable(new ColorDrawable(0));

        // 点击空白区域不可取消
        setCancelable(false);

        // 设置style
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BottomDialog);

        // 可设置dialog的位置
        window.setGravity(Gravity.BOTTOM);

        // 消除边距
        window.getDecorView().setPadding(0, 0, 0, 0);

        // 设置全屏
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        lp.windowAnimations = R.style.BottomDialogAnim;
        window.setAttributes(lp);

        // 沉浸式状态栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        // 监听返回键回调
        this.getDialog().setOnKeyListener(this);

        return inflater.inflate(R.layout.dialog_switch_wallet2, container, false);
    }

    // 返回键回调
    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (null != mOnSelectedWalletListener) {
                mOnSelectedWalletListener.onSelectedWallet(mCurrentWalletBean);
            }

            dismiss();
            return true;
        }
        return false;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);
        initData();
    }

    private void initView(View view) {
        mIb_switch_wallet2_back = view.findViewById(R.id.ib_switch_wallet2_back);
        mRv_dialog_switch_wallet2 = view.findViewById(R.id.rv_dialog_switch_wallet2);

        mIb_switch_wallet2_back.setOnClickListener(this);
    }

    private void initData() {
        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        mWalletBeans = new ArrayList<>();
        List<WalletBean> neoWalletBeans = uChainWalletDbDao.queryWallets(Constant.TABLE_NEO_WALLET);
        if (null != neoWalletBeans && !neoWalletBeans.isEmpty()) {
            mWalletBeans.addAll(neoWalletBeans);
        }

        List<WalletBean> ethWalletBeans = uChainWalletDbDao.queryWallets(Constant.TABLE_ETH_WALLET);
        if (null != ethWalletBeans && !ethWalletBeans.isEmpty()) {
            mWalletBeans.addAll(ethWalletBeans);
        }

        for (WalletBean walletBean : mWalletBeans) {
            if (null == walletBean) {
                UChainLog.e(TAG, "walletBean is null!");
                continue;
            }

            if (walletBean.equals(mCurrentWalletBean)) {
                walletBean.setSelected(true);
                break;
            }
        }

        mSwitchWallet2RecyclerViewAdapter = new SwitchWallet2RecyclerViewAdapter(mWalletBeans);
        mSwitchWallet2RecyclerViewAdapter.setOnItemClickListener(this);
        mRv_dialog_switch_wallet2.setLayoutManager(new LinearLayoutManager(UChainWalletApplication
                .getInstance(), LinearLayoutManager.VERTICAL, false));
        mRv_dialog_switch_wallet2.setAdapter(mSwitchWallet2RecyclerViewAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_switch_wallet2_back:
                if (null != mOnSelectedWalletListener) {
                    mOnSelectedWalletListener.onSelectedWallet(mCurrentWalletBean);
                }

                dismiss();
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        if (null == mWalletBeans || mWalletBeans.isEmpty()) {
            UChainLog.e(TAG, "mWalletBeans is null or empty!");
            return;
        }

        WalletBean walletBean = mWalletBeans.get(position);
        if (null == walletBean) {
            UChainLog.e(TAG, "walletBean is null!");
            return;
        }

        mCurrentWalletBean = walletBean;
    }

}
