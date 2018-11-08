package yooco.uchain.uchainwallet.view.page.me;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.text.TextUtils;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.base.BaseFragment;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.changelistener.UChainListeners;
import yooco.uchain.uchainwallet.changelistener.OnWalletDeleteListener;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.FragmentFactory;

public class Me3Activity extends BaseActivity implements OnWalletDeleteListener {

    private static final String TAG = Me3Activity.class.getSimpleName();

    private WalletBean mWalletBean;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_me_skip);
    }

    @Override
    protected void init() {
        super.init();

        initData();
    }

    private void initData() {
        UChainListeners.getInstance().addOnItemDeleteListener(this);

        Intent intent = getIntent();
        if (null == intent) {
            UChainLog.e(TAG, "intent is null!");
            return;
        }

        mWalletBean = intent.getParcelableExtra(Constant.PARCELABLE_WALLET_BEAN_MANAGE_DETAIL);
        String fragmentTag = intent.getStringExtra(Constant.ME_SKIP_ACTIVITY_FRAGMENT_TAG);
        initFragment(fragmentTag);
    }

    private void initFragment(String fragmentTag) {
        if (TextUtils.isEmpty(fragmentTag)) {
            UChainLog.e(TAG, "fragmentTag is null!");
            return;
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        BaseFragment fragment = FragmentFactory.getFragment(fragmentTag);
        if (!fragment.isAdded()) {
            fragmentTransaction.add(R.id.fl_me3, fragment, fragmentTag);
        }
        fragmentTransaction.show(fragment).commit();
    }

    public WalletBean getWalletBean() {
        return mWalletBean;
    }

    @Override
    public void onWalletDelete(WalletBean walletBean) {
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UChainListeners.getInstance().removeOnItemDeleteListener(this);
    }
}
