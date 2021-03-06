package yooco.uchain.uchainwallet.view.page.excitation;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.view.adapter.ExcitationAdapter;
import yooco.uchain.uchainwallet.view.adapter.SpacesItemDecorationBottom;
import yooco.uchain.uchainwallet.base.BaseFragment;
import yooco.uchain.uchainwallet.data.bean.ExcitationBean;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.DensityUtil;
import yooco.uchain.uchainwallet.utils.ToastUtils;
import yooco.uchain.uchainwallet.view.page.excitation.detail.ExcitationDetailActivity;

/**
 * Created by SteelCabbage on 2018/5/21 0021.
 */

public class ExcitationFragment extends BaseFragment implements ExcitationAdapter
        .OnItemClickListener, SwipeRefreshLayout.OnRefreshListener, IGetExcitationView {

    private static final String TAG = ExcitationFragment.class.getSimpleName();

    private IGetExcitationPresenter mIGetExcitationPresenter;

    private RecyclerView mExcitationEvent;
    private SwipeRefreshLayout mExcitationRefresh;
    private ExcitationAdapter mAdapter;

    private List<ExcitationBean> mList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle
            savedInstanceState) {

        return inflater.inflate(R.layout.fragment_excitation, container, false);
    }

    @Override
    protected void init(View view) {
        super.init(view);

        initView(view);
        initData();
    }

    private void initView(View view) {
        mExcitationEvent = (RecyclerView) view.findViewById(R.id.new_event);
        mExcitationRefresh = (SwipeRefreshLayout) view.findViewById(R.id.srl_event_refresh);

        mExcitationEvent.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.VERTICAL, false));
        mList = new ArrayList<>();
        mAdapter = new ExcitationAdapter(mList);
        mAdapter.setOnItemClickListener(this);
        mExcitationEvent.setAdapter(mAdapter);

        int space = DensityUtil.dip2px(getActivity(), 15);
        int bottomsSpace = DensityUtil.dip2px(getActivity(), 34);
        mExcitationEvent.addItemDecoration(new SpacesItemDecorationBottom(space));
        mExcitationRefresh.setColorSchemeColors(UChainWalletApplication.getInstance().getResources
                ().getColor(R.color.c_1253BF));
        mExcitationRefresh.setOnRefreshListener(this);

        CollapsingToolbarLayout ctlTitle = view.findViewById(R.id.collapsing_toolbar_layout);
        ctlTitle.setScrimAnimationDuration(50);
        ctlTitle.setExpandedTitleMarginStart(space);
        ctlTitle.setExpandedTitleMarginBottom(space + bottomsSpace);

    }

    private void initData() {
        mIGetExcitationPresenter = new GetExcitationPresenter(this);
        mIGetExcitationPresenter.getExcitation();
    }

    @Override
    public void onItemClick(int position) {
        ExcitationBean excitationBean = mList.get(position);
        if (null == excitationBean) {
            UChainLog.e(TAG, "excitationBean is null!");
            return;
        }

        if (excitationBean.getNewEventStatus() == Constant.EXCITATION_EXCITATION_AOUBT_TO_BEGIN) {
            ToastUtils.getInstance().showToast(getActivity().getResources().getString(R.string.excitation_about_to_begin_toast));
            return;
        }
        if (excitationBean.getNewEventStatus() == Constant.EXCITATION_EXCITATION_CLOSED) {
            ToastUtils.getInstance().showToast(getActivity().getResources().getString(R.string.excitation_closed));
            return;
        }


        Intent intent = new Intent(getActivity(), ExcitationDetailActivity.class);
        intent.putExtra(Constant.EXCITATION_GAS_LIMIT, excitationBean.getGasLimit());
        intent.putExtra(Constant.EXCITATION_ACTIVITY_ID, excitationBean.getActivityId());
        startActivity(intent);
    }

    @Override
    public void onRefresh() {
        mIGetExcitationPresenter.getExcitation();
    }

    @Override
    public void getExcitation(List<ExcitationBean> excitationBeans) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mExcitationRefresh.isRefreshing()) {
                    mExcitationRefresh.setRefreshing(false);
                }
            }
        });

        if (null == excitationBeans) {
            UChainLog.e(TAG, "excitationBeans is null");
            return;
        }

        mList.clear();
        mList.addAll(excitationBeans);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.notifyDataSetChanged();
            }
        });
    }
}
