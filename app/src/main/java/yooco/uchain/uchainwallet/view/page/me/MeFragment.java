package yooco.uchain.uchainwallet.view.page.me;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.view.adapter.MeFunctionRecyclerViewAdapter;
import yooco.uchain.uchainwallet.view.adapter.SpacesItemDecorationTopBottom;
import yooco.uchain.uchainwallet.base.BaseFragment;
import yooco.uchain.uchainwallet.data.bean.MeFunction;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.DensityUtil;
import yooco.uchain.uchainwallet.view.page.me.portrait.MePortraitActivity;
import yooco.uchain.uchainwallet.view.page.me.portrait.MePortraitEmptyActivity;

/**
 * Created by SteelCabbage on 2018/7/11 0011 16:48.
 * E-Mail：liuyi_61@163.com
 */

public class MeFragment extends BaseFragment implements View.OnClickListener,
        MeFunctionRecyclerViewAdapter.OnItemClickListener {

    public static final String TAG = MeFragment.class.getSimpleName();

    private MeFunctionRecyclerViewAdapter mMeFunctionRecyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_me, container, false);
    }

    @Override
    protected void init(View view) {
        super.init(view);

        initView(view);
    }

    private void initView(View view) {
        ImageButton ib_me_manage_wallet = view.findViewById(R.id.ib_me_manage_wallet);
        TextView tv_me_manage_wallet = view.findViewById(R.id.tv_me_manage_wallet);
        ImageButton ib_me_tx_records = view.findViewById(R.id.ib_me_tx_records);
        TextView tv_me_tx_records = view.findViewById(R.id.tv_me_tx_records);
        RecyclerView rv_me = view.findViewById(R.id.rv_me);
        rv_me.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        int space = DensityUtil.dip2px(getActivity(), 15);
        rv_me.addItemDecoration(new SpacesItemDecorationTopBottom(space));

        mMeFunctionRecyclerViewAdapter = new MeFunctionRecyclerViewAdapter(getAssetsMenus());
        mMeFunctionRecyclerViewAdapter.setOnItemClickListener(this);
        rv_me.setAdapter(mMeFunctionRecyclerViewAdapter);

        ib_me_manage_wallet.setOnClickListener(this);
        tv_me_manage_wallet.setOnClickListener(this);
        ib_me_tx_records.setOnClickListener(this);
        tv_me_tx_records.setOnClickListener(this);
    }

    private List<MeFunction> getAssetsMenus() {
        ArrayList<MeFunction> meFunctions = new ArrayList<>();
        //drawable数组要用TypedArray获取
        TypedArray ar = getResources().obtainTypedArray(R.array.me_function_icons);
        String[] menuTexts = getResources().getStringArray(R.array.me_function_texts);

        for (int i = 0; i < ar.length(); i++) {
            MeFunction meFunction = new MeFunction();
            meFunction.setFunctionIcon(ar.getResourceId(i, 0));
            meFunction.setFunctionText(menuTexts[i]);
            meFunctions.add(meFunction);
        }
        ar.recycle();
        return meFunctions;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_me_manage_wallet:
            case R.id.tv_me_manage_wallet:
                startActivityStringExtra(Me2Activity.class, false, Constant.ME_2_SHOULD_BE_SHOW, Constant
                        .ME_2_SHOULD_BE_SHOW_MANAGE_WALLET);
                break;
            case R.id.ib_me_tx_records:
            case R.id.tv_me_tx_records:
                startActivityStringExtra(Me2Activity.class, false, Constant.ME_2_SHOULD_BE_SHOW, Constant
                        .ME_2_SHOULD_BE_SHOW_TX_RECORDS);
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        switch (position) {
            case 0:
                // 个人画像
                skip2Portrait();
                break;
            case 1:
                // 语言设置
                startActivity(MeLanguageSettingsActivity.class, false);
                break;
            case 2:
                // 关于我们
                break;
            default:
                break;
        }
    }

    private void skip2Portrait() {
        WalletBean rewardWallet = getRewardWallet();
        if (null == rewardWallet) {
            startActivity(MePortraitEmptyActivity.class, false);
        } else {
            startActivity(MePortraitActivity.class, false);
        }

    }

    private WalletBean getRewardWallet() {
        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return null;
        }

        List<WalletBean> walletBeans = uChainWalletDbDao.queryWallets(Constant.TABLE_NEO_WALLET);
        if (null == walletBeans || walletBeans.isEmpty()) {
            UChainLog.e(TAG, "neoWallets is null or empty!");
            return null;
        }

        return walletBeans.get(0);
    }

}
