package yooco.uchain.uchainwallet.view.page.wallet;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.Collections;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.view.adapter.BackupClickMnemonicAdapter;
import yooco.uchain.uchainwallet.view.adapter.BackupShowMnemonicAdapter;
import yooco.uchain.uchainwallet.view.adapter.SpacesItemDecorationHorizontal;
import yooco.uchain.uchainwallet.base.BaseFragment;
import yooco.uchain.uchainwallet.data.bean.MnemonicState;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.data.bean.eth.EthWallet;
import yooco.uchain.uchainwallet.data.bean.neo.NeoWallet;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.DensityUtil;
import yooco.uchain.uchainwallet.utils.SharedPreferencesUtils;
import yooco.uchain.uchainwallet.utils.ToastUtils;
import yooco.uchain.uchainwallet.view.page.MainActivity;

/**
 * Created by SteelCabbage on 2018/5/28 0028.
 */

public class ConfirmMnemonicFragment extends BaseFragment implements View.OnClickListener,
        BackupClickMnemonicAdapter.OnItemClickListener, BackupShowMnemonicAdapter
                .OnItemClickShowListener {

    private static final String TAG = ConfirmMnemonicFragment.class.getSimpleName();

    private Button mBt_confirm_mnemonic_confirm;
    private RecyclerView mRv_confirm_mnemonic_show;
    private RecyclerView mRv_confirm_mnemonic_click;
    private BackupClickMnemonicAdapter mBackupClickMnemonicAdapter;
    private BackupShowMnemonicAdapter mBackupShowMnemonicAdapter;

    private ArrayList<MnemonicState> mMnemonicStatesShow;
    private ArrayList<MnemonicState> mMnemonicStatesClick;
    private ArrayList<MnemonicState> mFinalRightMnemonics;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_confirm_mnemonic, container, false);
    }

    @Override
    protected void init(View view) {
        super.init(view);

        initView(view);
        initData();
    }

    private void initView(View view) {
        mBt_confirm_mnemonic_confirm = view.findViewById(R.id.bt_confirm_mnemonic_confirm);
        mRv_confirm_mnemonic_show = view.findViewById(R.id.rv_confirm_mnemonic_show);
        mRv_confirm_mnemonic_click = view.findViewById(R.id.rv_confirm_mnemonic_click);

        mBt_confirm_mnemonic_confirm.setOnClickListener(this);
    }

    private void initData() {
        BackupWalletActivity backupWalletActivity = (BackupWalletActivity) getActivity();
        if (null == backupWalletActivity) {
            UChainLog.e(TAG, "backupWalletActivity is null!");
            return;
        }

        // 设置点击的助记词
        String backupMnemonic = backupWalletActivity.getBackupMnemonic();
        if (TextUtils.isEmpty(backupMnemonic)) {
            UChainLog.e(TAG, "backupMnemonic is null!");
            return;
        }
        String[] backupMnemonics = backupMnemonic.split(" ");
        mMnemonicStatesClick = new ArrayList<>();
        mFinalRightMnemonics = new ArrayList<>();
        for (int i = 0; i < backupMnemonics.length; i++) {
            MnemonicState mnemonicState = new MnemonicState();
            mnemonicState.setMnemonic(backupMnemonics[i]);
            mnemonicState.setSelected(false);
            mFinalRightMnemonics.add(mnemonicState);
            mMnemonicStatesClick.add(mnemonicState);
        }

        // 打乱助记词
        Collections.shuffle(mMnemonicStatesClick);
        mBackupClickMnemonicAdapter = new BackupClickMnemonicAdapter(mMnemonicStatesClick);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(UChainWalletApplication
                .getInstance());
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        mRv_confirm_mnemonic_click.setLayoutManager(layoutManager);
        mRv_confirm_mnemonic_click.setAdapter(mBackupClickMnemonicAdapter);
        int space = DensityUtil.dip2px(getActivity(), 4);
        mRv_confirm_mnemonic_click.addItemDecoration(new SpacesItemDecorationHorizontal(space));
        mBackupClickMnemonicAdapter.setOnItemClickListener(this);

        // 设置展示的助记词
        mMnemonicStatesShow = new ArrayList<>();
        mBackupShowMnemonicAdapter = new BackupShowMnemonicAdapter(mMnemonicStatesShow);
        FlexboxLayoutManager layoutManagerShow = new FlexboxLayoutManager(UChainWalletApplication
                .getInstance());
        layoutManagerShow.setFlexDirection(FlexDirection.ROW);
        layoutManagerShow.setJustifyContent(JustifyContent.FLEX_START);
        layoutManager.setFlexWrap(FlexWrap.WRAP);
        mRv_confirm_mnemonic_show.setLayoutManager(layoutManagerShow);
        mRv_confirm_mnemonic_show.setAdapter(mBackupShowMnemonicAdapter);
        mRv_confirm_mnemonic_show.addItemDecoration(new SpacesItemDecorationHorizontal(space));
        mBackupShowMnemonicAdapter.setOnItemClickShowListener(this);

    }

    //展示助记词的回调
    @Override
    public void onItemClickShow(int position) {
        MnemonicState mnemonicStateShow = mMnemonicStatesShow.get(position);
        for (MnemonicState stateClick : mMnemonicStatesClick) {
            if (mnemonicStateShow.getMnemonic().equals(stateClick.getMnemonic())) {
                stateClick.setSelected(false);
            }
        }
        mMnemonicStatesShow.remove(mnemonicStateShow);
        mBackupClickMnemonicAdapter.notifyDataSetChanged();
        mBackupShowMnemonicAdapter.notifyDataSetChanged();
    }

    //点击助记词的回调
    @Override
    public void onItemClick(int position) {
        MnemonicState mnemonicState = mMnemonicStatesClick.get(position);
        if (mnemonicState.isSelected()) {
            mMnemonicStatesShow.add(mnemonicState);
        } else {
            mMnemonicStatesShow.remove(mnemonicState);
        }
        mBackupShowMnemonicAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_confirm_mnemonic_confirm:
                if (!checkMnemonicIsRight()) {
                    UChainLog.w(TAG, "checkMnemonicIsRight is false!");
                    ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance()
                            .getResources().getString(R.string.mnemonic_incorrect));
                    return;
                }

                updateWalletBackupState();
                isFirstEnter();
                break;
            default:
                break;
        }
    }

    private boolean checkMnemonicIsRight() {
        if (null == mFinalRightMnemonics || mFinalRightMnemonics.isEmpty()) {
            UChainLog.w(TAG, "mFinalRightMnemonics is null or empty!");
            return false;
        }

        if (null == mMnemonicStatesShow || mMnemonicStatesShow.isEmpty()) {
            UChainLog.w(TAG, "mMnemonicStatesShow is null or empty!");
            return false;
        }

        if (mFinalRightMnemonics.size() != mMnemonicStatesShow.size()) {
            UChainLog.w(TAG, "mFinalRightMnemonics and mMnemonicStatesShow size is not same!");
            return false;
        }

        for (int i = 0; i < mFinalRightMnemonics.size(); i++) {
            MnemonicState mnemonicFinal = mFinalRightMnemonics.get(i);
            MnemonicState mnemonicShow = mMnemonicStatesShow.get(i);
            if (!mnemonicFinal.equals(mnemonicShow)) {
                UChainLog.w(TAG, "mnemonics out of order!");
                return false;
            }
        }

        return true;
    }

    private void updateWalletBackupState() {
        BackupWalletActivity backupWalletActivity = (BackupWalletActivity) getActivity();
        if (null == backupWalletActivity) {
            UChainLog.e(TAG, "updateWalletBackupState() -> backupWalletActivity is null!");
            return;
        }

        WalletBean walletBean = backupWalletActivity.getWalletBean();
        if (null == walletBean) {
            UChainLog.e(TAG, "walletBean is null!");
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication
                .getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        switch (walletBean.getClass().getSimpleName()) {
            case "NeoWallet":
                NeoWallet neoWallet = (NeoWallet) walletBean;
                uChainWalletDbDao.updateBackupState(Constant.TABLE_NEO_WALLET, neoWallet.getAddress(), Constant.BACKUP_FINISH);
                neoWallet.setBackupState(Constant.BACKUP_FINISH);
                UChainListeners.getInstance().notifyWalletBackupStateUpdate(neoWallet);
                break;
            case "EthWallet":
                EthWallet ethWallet = (EthWallet) walletBean;
                uChainWalletDbDao.updateBackupState(Constant.TABLE_ETH_WALLET, ethWallet.getAddress(), Constant.BACKUP_FINISH);
                ethWallet.setBackupState(Constant.BACKUP_FINISH);
                UChainListeners.getInstance().notifyWalletBackupStateUpdate(ethWallet);
                break;
            default:
                UChainLog.e(TAG, "unknown wallet type!");
                break;
        }
    }

    private void isFirstEnter() {
        boolean isFirstExport = (boolean) SharedPreferencesUtils.getParam(UChainWalletApplication
                .getInstance(), Constant.IS_FIRST_ENTER_MAIN, true);
        if (isFirstExport) {
            SharedPreferencesUtils.putParam(UChainWalletApplication.getInstance(), Constant
                    .IS_FIRST_ENTER_MAIN, false);
            startActivity(MainActivity.class, true);
        } else {
            getActivity().finish();
        }
    }

}
