package yooco.uchain.uchainwallet.view.page.wallet;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.view.adapter.viewpager.FragmentUpdateAdapter;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.base.BaseFragment;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.FragmentFactory;

public class ImportWalletActivity extends BaseActivity {

    private static final String TAG = ImportWalletActivity.class.getSimpleName();

    private TabLayout mTl_import_wallet;
    private ViewPager mVp_import_wallet;
    private FragmentUpdateAdapter mFragmentUpdateAdapter;

    private List<BaseFragment> mBaseFragments;
    private List<String> mTitles;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_import_wallet);
    }

    @Override
    protected void init() {
        super.init();

        initView();
        initData();
    }

    private void initView() {
        mTl_import_wallet = (TabLayout) findViewById(R.id.tl_import_wallet);
        mVp_import_wallet = (ViewPager) findViewById(R.id.vp_import_wallet);
    }

    private void initData() {
        mTl_import_wallet.setupWithViewPager(mVp_import_wallet);

        mBaseFragments = new ArrayList<>();
        ImportMnemonicFragment importMnemonicFragment = (ImportMnemonicFragment) FragmentFactory.getFragment(Constant
                .FRAGMENT_TAG_IMPORT_MNEMONIC);
        mBaseFragments.add(importMnemonicFragment);
        ImportKeystoreFragment importKeystoreFragment = (ImportKeystoreFragment) FragmentFactory.getFragment(Constant
                .FRAGMENT_TAG_IMPORT_KEYSTORE);
        mBaseFragments.add(importKeystoreFragment);
        mTitles = Arrays.asList(getResources().getStringArray(R.array.Wallet_import_method));

        mFragmentUpdateAdapter = new FragmentUpdateAdapter(getFragmentManager(), mBaseFragments, mTitles);
        mVp_import_wallet.setAdapter(mFragmentUpdateAdapter);
    }

}
