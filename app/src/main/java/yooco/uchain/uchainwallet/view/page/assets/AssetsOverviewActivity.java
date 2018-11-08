package yooco.uchain.uchainwallet.view.page.assets;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.view.adapter.AssetsOverviewRecyclerViewAdapter;
import yooco.uchain.uchainwallet.view.adapter.SpacesItemDecoration;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.data.bean.AssetBean;
import yooco.uchain.uchainwallet.data.bean.BalanceBean;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.presenter.balance.GetBalancePresenter;
import yooco.uchain.uchainwallet.presenter.balance.IGetBalancePresenter;
import yooco.uchain.uchainwallet.utils.DensityUtil;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import yooco.uchain.uchainwallet.utils.PhoneUtils;
import yooco.uchain.uchainwallet.utils.ToastUtils;
import yooco.uchain.uchainwallet.view.dialog.AddAssetsDialog;

public class AssetsOverviewActivity extends BaseActivity implements AssetsOverviewRecyclerViewAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, AddAssetsDialog.onCheckedAssetsListener, IGetBalanceView {

    private static final String TAG = AssetsOverviewActivity.class.getSimpleName();

    private IGetBalancePresenter mIGetBalancePresenter;

    private TextView mTv_assets_overview_wallet_name;
    private TextView mTv_assets_overview_wallet_address;
    private RecyclerView mRv_assets_overview;
    private SwipeRefreshLayout mSl_assets_overview_rv;
    private ImageButton mIb_assets_overview_ellipsis;
    private ImageButton mIb_assets_address_copy;
    private AssetsOverviewRecyclerViewAdapter mAssetsOverviewRecyclerViewAdapter;

    private WalletBean mWalletBean;
    private List<BalanceBean> mBalanceBeans;
    private List<String> mCurrentAssets;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_assets_overview);
    }

    @Override
    protected void init() {
        super.init();

        initView();
        initData();
    }

    private void initView() {
        mTv_assets_overview_wallet_name = (TextView) findViewById(R.id.tv_assets_overview_wallet_name);
        mTv_assets_overview_wallet_address = (TextView) findViewById(R.id.tv_assets_overview_wallet_address);
        mIb_assets_address_copy = (ImageButton) findViewById(R.id.ib_assets_address_copy);
        mIb_assets_overview_ellipsis = (ImageButton) findViewById(R.id.ib_assets_overview_ellipsis);

        mRv_assets_overview = (RecyclerView) findViewById(R.id.rv_assets_overview);
        mSl_assets_overview_rv = (SwipeRefreshLayout) findViewById(R.id.sl_assets_overview_rv);

        mIb_assets_overview_ellipsis.setOnClickListener(this);
        mSl_assets_overview_rv.setColorSchemeColors(this.getResources().getColor(R.color.c_1253BF));
        mSl_assets_overview_rv.setOnRefreshListener(this);

        // 复制地址
        mTv_assets_overview_wallet_address.setOnClickListener(this);
        mIb_assets_address_copy.setOnClickListener(this);
    }

    private void initData() {
        Intent intent = getIntent();
        if (null == intent) {
            UChainLog.e(TAG, "intent is null!");
            return;
        }

        mWalletBean = intent.getParcelableExtra(Constant.WALLET_BEAN);
        if (null == mWalletBean) {
            UChainLog.e(TAG, "initData() -> mWalletBean is null!");
            return;
        }

        mTv_assets_overview_wallet_name.setText(mWalletBean.getName());
        mTv_assets_overview_wallet_address.setText(mWalletBean.getAddress());

        mRv_assets_overview.setLayoutManager(new LinearLayoutManager(UChainWalletApplication.getInstance(), LinearLayoutManager
                .VERTICAL, false));
        mCurrentAssets = new ArrayList<>();
        mBalanceBeans = new ArrayList<>();
        getDefaultAssets();
        mAssetsOverviewRecyclerViewAdapter = new AssetsOverviewRecyclerViewAdapter(mBalanceBeans);
        mAssetsOverviewRecyclerViewAdapter.setOnItemClickListener(this);

        int space = DensityUtil.dip2px(this, 8);
        mRv_assets_overview.addItemDecoration(new SpacesItemDecoration(space));
        mRv_assets_overview.setAdapter(mAssetsOverviewRecyclerViewAdapter);

        getBalance();
    }

    private void getBalance() {
        mIGetBalancePresenter = new GetBalancePresenter(this);
        mIGetBalancePresenter.init(mWalletBean.getWalletType());
        mIGetBalancePresenter.getGlobalAssetBalance(mWalletBean);
        mIGetBalancePresenter.getColorAssetBalance(mWalletBean);
    }

    @Override
    public void onItemClick(int position) {
        BalanceBean balanceBean = mBalanceBeans.get(position);
        if (null == balanceBean) {
            UChainLog.e(TAG, "balanceBean is null!");
            return;
        }

        Intent intent = new Intent(UChainWalletApplication.getInstance(), BalanceDetailActivity.class);
        intent.putExtra(Constant.WALLET_BEAN, mWalletBean);
        intent.putExtra(Constant.BALANCE_BEAN, balanceBean);
        startActivity(intent);
    }

    // 设置默认添加的资产
    private void getDefaultAssets() {
        if (null == mBalanceBeans) {
            UChainLog.e(TAG, "mBalanceBeans is null!");
            return;
        }

        List<BalanceBean> globalAssets = getGlobalAssets();
        if (null != globalAssets && !globalAssets.isEmpty()) {
            for (BalanceBean globalAssetBalanceBean : globalAssets) {
                if (null == globalAssetBalanceBean) {
                    UChainLog.e(TAG, "globalAssetBalanceBean is null!");
                    continue;
                }

                if (Constant.ASSETS_NEO.equals(globalAssetBalanceBean.getAssetsID())
                        || Constant.ASSETS_NEO_GAS.equals(globalAssetBalanceBean.getAssetsID())) {
                    mBalanceBeans.add(0, globalAssetBalanceBean);
                } else {
                    mBalanceBeans.add(globalAssetBalanceBean);
                }
            }
        }

        List<BalanceBean> colorAssets = getColorAssets();
        if (null != colorAssets && !colorAssets.isEmpty()) {
            for (BalanceBean colorAssetBalanceBean : colorAssets) {
                if (null == colorAssetBalanceBean) {
                    UChainLog.e(TAG, "colorAssetBalanceBean is null!");
                    continue;
                }

                if (Constant.ASSETS_CPX.equals(colorAssetBalanceBean.getAssetsID())) {
                    mBalanceBeans.add(0, colorAssetBalanceBean);
                } else {
                    mBalanceBeans.add(colorAssetBalanceBean);
                }
            }
        }
    }

    private List<BalanceBean> getColorAssets() {
        if (null == mWalletBean) {
            UChainLog.e(TAG, "getColorAssets() -> mWalletBean is null!");
            return null;
        }

        String colorAssetJson = mWalletBean.getColorAssetJson();
        List<String> colorAssets = GsonUtils.json2List(colorAssetJson, String.class);
        if (null == colorAssets || colorAssets.isEmpty()) {
            UChainLog.e(TAG, "colorAssets is null or empty!");
            return null;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null");
            return null;
        }

        String tableName = null;
        String assetType = null;
        switch (mWalletBean.getWalletType()) {
            case Constant.WALLET_TYPE_NEO:
                tableName = Constant.TABLE_NEO_ASSETS;
                assetType = Constant.ASSET_TYPE_NEP5;
                break;
            case Constant.WALLET_TYPE_ETH:
                tableName = Constant.TABLE_ETH_ASSETS;
                assetType = Constant.ASSET_TYPE_ERC20;
                break;
            case Constant.WALLET_TYPE_CPX:
                tableName = Constant.TABLE_CPX_ASSETS;
                break;
            default:
                UChainLog.e(TAG, "unknown wallet type!");
                break;
        }

        if (TextUtils.isEmpty(tableName) || TextUtils.isEmpty(assetType)) {
            UChainLog.e(TAG, "getColorAssets() -> tableName or assetType is null!");
            return null;
        }

        ArrayList<BalanceBean> balanceBeans = new ArrayList<>();
        for (String colorAsset : colorAssets) {
            AssetBean assetBean = uChainWalletDbDao.queryAssetByHash(tableName, colorAsset);
            if (null == assetBean) {
                UChainLog.e(TAG, "getColorAssets() -> assetBean is null!");
                continue;
            }

            BalanceBean balanceBean = new BalanceBean();
            balanceBean.setMapState(Constant.MAP_STATE_UNFINISHED);
            balanceBean.setWalletType(mWalletBean.getWalletType());
            balanceBean.setAssetsID(colorAsset);
            balanceBean.setAssetSymbol(assetBean.getSymbol());
            balanceBean.setAssetType(assetType);
            balanceBean.setAssetDecimal(Integer.valueOf(assetBean.getPrecision()));
            balanceBean.setAssetsValue("0");
            balanceBeans.add(balanceBean);
            mCurrentAssets.add(colorAsset);
        }
        return balanceBeans;
    }

    private List<BalanceBean> getGlobalAssets() {
        if (null == mWalletBean) {
            UChainLog.e(TAG, "getGlobalAssets() -> mWalletBean is null!");
            return null;
        }

        String assetJson = mWalletBean.getAssetJson();
        List<String> globalAssets = GsonUtils.json2List(assetJson, String.class);
        if (null == globalAssets || globalAssets.isEmpty()) {
            UChainLog.e(TAG, "globalAssets is null or empty!");
            return null;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication
                .getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null");
            return null;
        }

        String tableName = null;
        String assetType = null;
        switch (mWalletBean.getWalletType()) {
            case Constant.WALLET_TYPE_NEO:
                tableName = Constant.TABLE_NEO_ASSETS;
                assetType = Constant.ASSET_TYPE_GLOBAL;
                break;
            case Constant.WALLET_TYPE_ETH:
                tableName = Constant.TABLE_ETH_ASSETS;
                assetType = Constant.ASSET_TYPE_ETH;
                break;
            case Constant.WALLET_TYPE_CPX:
                tableName = Constant.TABLE_CPX_ASSETS;
                break;
            default:
                break;
        }

        if (TextUtils.isEmpty(tableName) || TextUtils.isEmpty(assetType)) {
            UChainLog.e(TAG, "getGlobalAssets() -> tableName or assetType is null!");
            return null;
        }

        ArrayList<BalanceBean> balanceBeans = new ArrayList<>();
        for (String globalAsset : globalAssets) {
            AssetBean assetBean = uChainWalletDbDao.queryAssetByHash(tableName, globalAsset);
            if (null == assetBean) {
                UChainLog.e(TAG, "getGlobalAssets() -> assetBean is null!");
                continue;
            }

            BalanceBean balanceBean = new BalanceBean();
            balanceBean.setMapState(Constant.MAP_STATE_UNFINISHED);
            balanceBean.setWalletType(mWalletBean.getWalletType());
            balanceBean.setAssetsID(globalAsset);
            balanceBean.setAssetSymbol(assetBean.getSymbol());
            balanceBean.setAssetType(assetType);
            balanceBean.setAssetDecimal(Integer.valueOf(assetBean.getPrecision()));
            balanceBean.setAssetsValue("0");
            balanceBeans.add(balanceBean);
            if (mCurrentAssets.size() >= 1) {
                mCurrentAssets.add(1, globalAsset);
            } else {
                mCurrentAssets.add(globalAsset);
            }
        }
        return balanceBeans;
    }

    @Override
    public void onRefresh() {
        mIGetBalancePresenter.getGlobalAssetBalance(mWalletBean);
        mIGetBalancePresenter.getColorAssetBalance(mWalletBean);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_assets_overview_ellipsis:
                showAddAssetsDialog();
                break;
            case R.id.tv_assets_overview_wallet_address:
            case R.id.ib_assets_address_copy:
                String copyAddr = mTv_assets_overview_wallet_address.getText().toString().trim();
                PhoneUtils.copy2Clipboard(UChainWalletApplication.getInstance(), copyAddr);
                ToastUtils.getInstance().showToast(UChainWalletApplication.getInstance()
                        .getResources().getString(R.string.wallet_address_copied));
            default:
                break;
        }
    }

    public void showAddAssetsDialog() {
        AddAssetsDialog addAssetsDialog = AddAssetsDialog.newInstance();
        addAssetsDialog.setOnCheckedAssetsListener(this);
        addAssetsDialog.setWalletType(mWalletBean.getWalletType());
        addAssetsDialog.setCurrentAssets(mCurrentAssets);
        addAssetsDialog.show(getFragmentManager(), "AddAssetsDialog");
    }

    @Override
    public void onCheckedAssets(List<String> checkedAssets) {
        if (null == checkedAssets || checkedAssets.isEmpty()) {
            UChainLog.w(TAG, "checkedAssets is null or empty!");
            return;
        }

        mCurrentAssets.clear();
        mCurrentAssets.addAll(checkedAssets);

        List<String> colorAssets = new ArrayList<>();
        List<String> globalAssets = new ArrayList<>();

        for (String checkedAsset : checkedAssets) {
            if (TextUtils.isEmpty(checkedAsset)) {
                UChainLog.e(TAG, "checkedAsset is null!");
                continue;
            }

            if (Constant.ASSETS_NEO.equals(checkedAsset)
                    || Constant.ASSETS_NEO_GAS.equals(checkedAsset)
                    || Constant.ASSETS_ETH.equals(checkedAsset)) {
                globalAssets.add(checkedAsset);
            } else {
                colorAssets.add(checkedAsset);
            }
        }

        mWalletBean.setAssetJson(GsonUtils.toJsonStr(globalAssets));
        mWalletBean.setColorAssetJson(GsonUtils.toJsonStr(colorAssets));
        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        switch (mWalletBean.getWalletType()) {
            case Constant.WALLET_TYPE_NEO:
                uChainWalletDbDao.updateCheckedAssets(Constant.TABLE_NEO_WALLET, mWalletBean);
                break;
            case Constant.WALLET_TYPE_ETH:
                uChainWalletDbDao.updateCheckedAssets(Constant.TABLE_ETH_WALLET, mWalletBean);
                break;
             default:
                 break;
        }

        UChainListeners.getInstance().notifyAssetJsonUpdate(mWalletBean);
        updateAssets();
    }

    private void updateAssets() {
        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        if (null == mBalanceBeans) {
            UChainLog.e(TAG, "mBalanceBeans is null!");
            return;
        }

        Iterator<BalanceBean> iterator = mBalanceBeans.iterator();
        while (iterator.hasNext()) {
            BalanceBean balanceBeanTmp = iterator.next();
            if (null == balanceBeanTmp) {
                UChainLog.e(TAG, "balanceBeanTmp is null!");
                continue;
            }

            if (!mCurrentAssets.contains(balanceBeanTmp.getAssetsID())) {
                iterator.remove();
            }
        }

        String tableName = null;
        switch (mWalletBean.getWalletType()) {
            case Constant.WALLET_TYPE_NEO:
                tableName = Constant.TABLE_NEO_ASSETS;
                break;
            case Constant.WALLET_TYPE_ETH:
                tableName = Constant.TABLE_ETH_ASSETS;
                break;
            case Constant.WALLET_TYPE_CPX:
                tableName = Constant.TABLE_CPX_ASSETS;
                break;
        }

        a:
        for (String currentAsset : mCurrentAssets) {
            if (TextUtils.isEmpty(currentAsset)) {
                UChainLog.e(TAG, "currentAsset is null!");
                continue;
            }

            for (BalanceBean balanceBean : mBalanceBeans) {
                if (null == balanceBean) {
                    UChainLog.e(TAG, "balanceBean is null!");
                    continue;
                }

                if (currentAsset.equals(balanceBean.getAssetsID())) {
                    continue a;
                }
            }

            AssetBean assetBean = uChainWalletDbDao.queryAssetByHash(tableName, currentAsset);
            if (null == assetBean) {
                UChainLog.e(TAG, "assetBean is null!");
                continue;
            }

            BalanceBean balanceBeanNew = new BalanceBean();
            balanceBeanNew.setMapState(Constant.MAP_STATE_UNFINISHED);
            balanceBeanNew.setWalletType(mWalletBean.getWalletType());
            balanceBeanNew.setAssetsID(currentAsset);
            balanceBeanNew.setAssetSymbol(assetBean.getSymbol());
            balanceBeanNew.setAssetType(assetBean.getType());
            balanceBeanNew.setAssetDecimal(Integer.valueOf(assetBean.getPrecision()));
            balanceBeanNew.setAssetsValue("0");
            mBalanceBeans.add(balanceBeanNew);
        }

        mAssetsOverviewRecyclerViewAdapter.notifyDataSetChanged();

        mIGetBalancePresenter.getGlobalAssetBalance(mWalletBean);
        mIGetBalancePresenter.getColorAssetBalance(mWalletBean);
    }

    @Override
    public void getGlobalAssetBalance(HashMap<String, BalanceBean> balanceBeans) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSl_assets_overview_rv.isRefreshing()) {
                    mSl_assets_overview_rv.setRefreshing(false);
                }
            }
        });

        if (null == mBalanceBeans || mBalanceBeans.isEmpty()) {
            UChainLog.e(TAG, "getGlobalAssetBalance() -> mBalanceBeans is null or empty!");
            return;
        }

        if (null == balanceBeans || balanceBeans.isEmpty()) {
            UChainLog.e(TAG, "getGlobalAssetBalance() -> balanceBeans is null or empty!");
            return;
        }

        for (BalanceBean balanceBean : mBalanceBeans) {
            if (null == balanceBean) {
                UChainLog.e(TAG, "getGlobalAssetBalance() -> balanceBean is null or empty!");
                continue;
            }

            String assetsID = balanceBean.getAssetsID();
            if (balanceBeans.containsKey(assetsID)) {
                BalanceBean balanceBeanTmp = balanceBeans.get(assetsID);
                if (null == balanceBeanTmp) {
                    UChainLog.e(TAG, "getGlobalAssetBalance() -> balanceBeanTmp is null or empty!");
                    continue;
                }

                balanceBean.setAssetsValue(balanceBeanTmp.getAssetsValue());
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAssetsOverviewRecyclerViewAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void getColorAssetBalance(HashMap<String, BalanceBean> balanceBeans) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mSl_assets_overview_rv.isRefreshing()) {
                    mSl_assets_overview_rv.setRefreshing(false);
                }
            }
        });

        if (null == mBalanceBeans || mBalanceBeans.isEmpty()) {
            UChainLog.e(TAG, "getColorAssetBalance() -> mBalanceBeans is null or empty!");
            return;
        }

        if (null == balanceBeans || balanceBeans.isEmpty()) {
            UChainLog.e(TAG, "getColorAssetBalance() -> balanceBeans is null or empty!");
            return;
        }

        for (BalanceBean balanceBean : mBalanceBeans) {
            if (null == balanceBean) {
                UChainLog.e(TAG, "getColorAssetBalance() -> balanceBean is null or empty!");
                continue;
            }

            String assetsID = balanceBean.getAssetsID();
            if (balanceBeans.containsKey(assetsID)) {
                BalanceBean balanceBeanTmp = balanceBeans.get(assetsID);
                if (null == balanceBeanTmp) {
                    UChainLog.e(TAG, "getColorAssetBalance() -> balanceBeanTmp is null or empty!");
                    continue;
                }

                balanceBean.setAssetsValue(balanceBeanTmp.getAssetsValue());
            }
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAssetsOverviewRecyclerViewAdapter.notifyDataSetChanged();
            }
        });

    }
}
