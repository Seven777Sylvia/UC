package yooco.uchain.uchainwallet.view.dialog;

import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;

import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.view.adapter.SwitchTransactionRecyclerViewAdapter;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.data.bean.neo.NeoWallet;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.DensityUtil;

/**
 * Created by SteelCabbage on 2018/5/31 0031.
 */

public class SwitchWalletDialog extends DialogFragment implements View.OnClickListener,
        SwitchTransactionRecyclerViewAdapter.OnItemClickListener {

    private static final String TAG = SwitchWalletDialog.class.getSimpleName();

    private ImageButton mIb_switch_wallet_close;
    private RecyclerView mRv_me_switch_wallet;
    private SwitchTransactionRecyclerViewAdapter mSwitchTransactionRecyclerViewAdapter;
    private onItemSelectedListener mOnItemSelectedListener;

    private int mPreIndex;
    private int mCurrentIndex;
    private NeoWallet mCurrentNeoWallet;
    private List<WalletBean> mWalletBeans;

    public static SwitchWalletDialog newInstance() {
        return new SwitchWalletDialog();
    }

    public void setCurrentNeoWallet(NeoWallet currentNeoWallet) {
        mCurrentNeoWallet = currentNeoWallet;
    }

    public interface onItemSelectedListener {
        void onItemSelected(WalletBean walletBean);
    }

    public void setOnItemSelectedListener(onItemSelectedListener onItemSelectedListener) {
        mOnItemSelectedListener = onItemSelectedListener;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        //去掉边框
        Window window = getDialog().getWindow();
        if (null != window) {
            window.setBackgroundDrawable(new ColorDrawable(0));
        }
        return inflater.inflate(R.layout.dialog_switch_wallet, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initData();
        initView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(DensityUtil.dip2px(getActivity(), 264), DensityUtil
                .dip2px(getActivity(), 436));
    }

    private void initData() {
        if (null == mCurrentNeoWallet) {
            UChainLog.e(TAG, "mCurrentNeoWallet is null!");
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication
                .getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }


        mWalletBeans = uChainWalletDbDao.queryWallets(Constant.TABLE_NEO_WALLET);
        for (WalletBean walletBean : mWalletBeans) {
            if (null == walletBean) {
                UChainLog.e(TAG, "walletBean is null!");
                continue;
            }

            if (mCurrentNeoWallet.equals(walletBean)) {
                walletBean.setSelected(true);
                mCurrentIndex = mWalletBeans.indexOf(walletBean);
            }
        }
    }

    private void initView(View view) {
        mIb_switch_wallet_close = view.findViewById(R.id.ib_switch_wallet_close);
        mIb_switch_wallet_close.setOnClickListener(this);
        mRv_me_switch_wallet = view.findViewById(R.id.rv_me_switch_wallet);


        mRv_me_switch_wallet.setLayoutManager(new LinearLayoutManager(UChainWalletApplication
                .getInstance(), LinearLayoutManager.VERTICAL, false));
        mSwitchTransactionRecyclerViewAdapter = new SwitchTransactionRecyclerViewAdapter(mWalletBeans);
        mSwitchTransactionRecyclerViewAdapter.setOnItemClickListener(this);
        mRv_me_switch_wallet.setAdapter(mSwitchTransactionRecyclerViewAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_switch_wallet_close:
                dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        WalletBean walletBean = mWalletBeans.get(position);
        if (null == walletBean) {
            UChainLog.e(TAG, "walletBean is null!");
            return;
        }

        mOnItemSelectedListener.onItemSelected(walletBean);
        walletBean.setSelected(true);

        mPreIndex = mCurrentIndex;
        mCurrentIndex = mWalletBeans.indexOf(walletBean);

        //当前所选与上次不同
        if (!walletBean.equals(mWalletBeans.get(mPreIndex))) {
            mWalletBeans.get(mPreIndex).setSelected(false);
        }

        mSwitchTransactionRecyclerViewAdapter.notifyDataSetChanged();
    }
}
