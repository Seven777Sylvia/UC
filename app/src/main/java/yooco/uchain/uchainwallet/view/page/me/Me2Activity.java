package yooco.uchain.uchainwallet.view.page.me;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbHelper;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.view.adapter.MeRecyclerViewAdapter;
import yooco.uchain.uchainwallet.view.adapter.SpacesItemDecoration;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.changelistener.OnWalletBackupStateUpdateListener;
import yooco.uchain.uchainwallet.changelistener.OnWalletDeleteListener;
import yooco.uchain.uchainwallet.changelistener.OnWalletNameUpdateListener;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.DensityUtil;

public class Me2Activity extends BaseActivity implements MeRecyclerViewAdapter
        .OnItemClickListener, OnWalletNameUpdateListener, OnWalletBackupStateUpdateListener,
        OnWalletDeleteListener {

    private static final String TAG = Me2Activity.class.getSimpleName();

    private RecyclerView mRv_me2;
    private TextView mTv_me2_title;
    private MeRecyclerViewAdapter mMeRecyclerViewAdapter;

    private WalletBean mCurrentClickedWallet;
    private List<WalletBean> mWalletBeans;
    private String mShowTag;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_me2);
    }

    @Override
    protected void init() {
        super.init();

        initView();
        initData();
    }

    private void initView() {
        mRv_me2 = (RecyclerView) findViewById(R.id.rv_me2);
        mTv_me2_title = (TextView) findViewById(R.id.tv_me2_title);
    }

    private void initData() {
        Intent intent = getIntent();
        if (null == intent) {
            UChainLog.e(TAG, "intent is null!");
            return;
        }

        mShowTag = intent.getStringExtra(Constant.ME_2_SHOULD_BE_SHOW);
        if (TextUtils.isEmpty(mShowTag)) {
            UChainLog.e(TAG, "showTag is null!");
            return;
        }

        switch (mShowTag) {
            case Constant.ME_2_SHOULD_BE_SHOW_MANAGE_WALLET:
                mTv_me2_title.setText(UChainWalletApplication.getInstance().getResources()
                        .getString(R.string.manage_wallets));
                break;
            case Constant.ME_2_SHOULD_BE_SHOW_TX_RECORDS:
                mTv_me2_title.setText(UChainWalletApplication.getInstance().getResources()
                        .getString(R.string.transaction_records));
                break;
            default:
                break;
        }

        mWalletBeans = initWalletBeans();
        mRv_me2.setLayoutManager(new LinearLayoutManager(UChainWalletApplication.getInstance(),
                LinearLayoutManager.VERTICAL, false));
        int space = DensityUtil.dip2px(UChainWalletApplication.getInstance(), 8);
        mRv_me2.addItemDecoration(new SpacesItemDecoration(space));
        mMeRecyclerViewAdapter = new MeRecyclerViewAdapter(mWalletBeans);
        mMeRecyclerViewAdapter.setOnItemClickListener(this);
        mRv_me2.setAdapter(mMeRecyclerViewAdapter);

        UChainListeners.getInstance().addOnItemStateUpdateListener(this);
        UChainListeners.getInstance().addOnItemDeleteListener(this);
        UChainListeners.getInstance().addOnItemNameUpdateListener(this);
    }

    private List<WalletBean> initWalletBeans() {
        List<WalletBean> walletBeans = new ArrayList<>();
        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return walletBeans;
        }

        walletBeans.addAll(uChainWalletDbDao.queryWallets(Constant.TABLE_NEO_WALLET));
        walletBeans.addAll(uChainWalletDbDao.queryWallets(Constant.TABLE_ETH_WALLET));
        if (walletBeans.isEmpty()) {
            UChainLog.w(TAG, "local have no wallet");
            return walletBeans;
        }

        for (WalletBean walletBean : walletBeans) {
            if (null == walletBean) {
                UChainLog.e(TAG, "walletBean is null!");
                continue;
            }

            switch (mShowTag) {
                case Constant.ME_2_SHOULD_BE_SHOW_MANAGE_WALLET:
                    walletBean.setSelectedTag(Constant.SELECTED_TAG_MANAGER_WALLET);
                    break;
                case Constant.ME_2_SHOULD_BE_SHOW_TX_RECORDS:
                    walletBean.setSelectedTag(Constant.SELECTED_TAG_TRANSACTION_RECORED);
                    break;
                default:
                    break;
            }
        }

        return walletBeans;
    }

    @Override
    public void onItemClick(int position) {
        mCurrentClickedWallet = mWalletBeans.get(position);
        if (null == mCurrentClickedWallet) {
            UChainLog.e(TAG, "mCurrentClickedWallet is null!");
            return;
        }

        switch (mShowTag) {
            case Constant.ME_2_SHOULD_BE_SHOW_MANAGE_WALLET:
                toMeManagerDetailFragment();
                break;
            case Constant.ME_2_SHOULD_BE_SHOW_TX_RECORDS:
                toMeTransactionRecordFragment();
                break;
            default:
                break;
        }
    }

    private void toMeManagerDetailFragment() {
        Intent intent = new Intent(UChainWalletApplication.getInstance(), Me3Activity.class);
        intent.putExtra(Constant.ME_SKIP_ACTIVITY_FRAGMENT_TAG, Constant.FRAGMENT_TAG_ME_MANAGE_DETAIL);
        intent.putExtra(Constant.PARCELABLE_WALLET_BEAN_MANAGE_DETAIL, mCurrentClickedWallet);
        startActivity(intent);
    }

    private void toMeTransactionRecordFragment() {
        Intent intent = new Intent(UChainWalletApplication.getInstance(), Me3Activity.class);
        intent.putExtra(Constant.ME_SKIP_ACTIVITY_FRAGMENT_TAG, Constant.FRAGMENT_TAG_ME_TRANSACTION_RECORD);
        intent.putExtra(Constant.PARCELABLE_WALLET_BEAN_MANAGE_DETAIL, mCurrentClickedWallet);
        startActivity(intent);
    }

    // 备份钱包后回调
    @Override
    public void onWalletBackupStateUpdate(WalletBean walletBean) {
        if (null == walletBean) {
            UChainLog.e(TAG, "walletBean is null!");
            return;
        }

        for (WalletBean walletBeanTmp : mWalletBeans) {
            if (null == walletBeanTmp) {
                UChainLog.e(TAG, "walletBeanTmp is null!");
                continue;
            }

            if (walletBeanTmp.getAddress().equals(walletBean.getAddress())) {
                walletBeanTmp.setBackupState(walletBean.getBackupState());
            }
        }

        mMeRecyclerViewAdapter.notifyDataSetChanged();
    }

    // 删除钱包时回调
    @Override
    public void onWalletDelete(WalletBean walletBean) {
        if (null == walletBean) {
            UChainLog.e(TAG, "onWalletDelete() -> neoWallet is null!");
            return;
        }

        if (!mWalletBeans.contains(walletBean)) {
            UChainLog.e(TAG, "onWalletDelete() -> this wallet not exist!");
            return;
        }

        mWalletBeans.remove(walletBean);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMeRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    // 修改钱包名称回调
    @Override
    public void OnWalletNameUpdate(WalletBean walletBean) {
        if (null == walletBean) {
            UChainLog.e(TAG, "neoWallet is null!");
            return;
        }

        for (WalletBean walletBeanTmp : mWalletBeans) {
            if (null == walletBeanTmp) {
                UChainLog.e(TAG, "walletBeanTmp is null!");
                continue;
            }

            if (walletBeanTmp.getAddress().equals(walletBean.getAddress())) {
                walletBeanTmp.setName(walletBean.getName());
            }
        }

        mMeRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UChainListeners.getInstance().removeOnItemStateUpdateListener(this);
        UChainListeners.getInstance().removeOnItemDeleteListener(this);
        UChainListeners.getInstance().removeOnItemNameUpdateListener(this);
    }
}
