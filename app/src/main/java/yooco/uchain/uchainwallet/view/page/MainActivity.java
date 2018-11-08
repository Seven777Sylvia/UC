package yooco.uchain.uchainwallet.view.page;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.os.Build;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import yooco.uchain.uchainwallet.R;
import yooco.uchain.uchainwallet.base.BaseActivity;
import yooco.uchain.uchainwallet.base.BaseFragment;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.FragmentFactory;
import yooco.uchain.uchainwallet.utils.SharedPreferencesUtils;

public class MainActivity extends BaseActivity implements BottomNavigationBar
        .OnTabSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSION = 201;

    private BottomNavigationBar mBottomNavigationBar;

    private String[] mBnItemTitles;

    @Override
    protected void setContentView() {
        super.setContentView();

        setContentView(R.layout.activity_main);
    }

    @Override
    protected void init() {
        super.init();

        initData();
        initView();

        SharedPreferencesUtils.putParam(UChainWalletApplication.getInstance(), Constant.IS_FIRST_ENTER_MAIN, false);
        checkPermission();
    }

    private void initData() {
        mBnItemTitles = getResources().getStringArray(R.array.main_navibar_item_title);
    }

    private void initView() {
        mBottomNavigationBar = (BottomNavigationBar) findViewById(R.id.navibar_main);

        initBottomNavigationBar();
        initFragment();
    }

    private void initBottomNavigationBar() {
      /*  mBottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.btn_main_navibar_item_assets, mBnItemTitles[0]));
        mBottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.btn_main_navibar_item_news, mBnItemTitles[1]));
        mBottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.btn_main_navibar_item_me, mBnItemTitles[2]));  */

        mBottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.btn_main_navibar_item_assets, null));
        mBottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.btn_main_navibar_item_news, null));
        mBottomNavigationBar.addItem(new BottomNavigationItem(R.drawable.btn_main_navibar_item_me, null));
/*        mBottomNavigationBar.setActiveColor(R.color.c_1253BF);
        mBottomNavigationBar.setInActiveColor(R.color.c_979797);*/
        mBottomNavigationBar.initialise();
        mBottomNavigationBar.setTabSelectedListener(this);
    }

    private void initFragment() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        for (int i = 0; i < mBnItemTitles.length; i++) {
            Fragment fragment = fragmentManager.findFragmentByTag(i + "");
            if (null != fragment) {
                fragmentTransaction.remove(fragment);
            }
        }
        fragmentTransaction.add(R.id.fl_main, FragmentFactory.getFragment(0), "0");
        fragmentTransaction.commit();
    }

    @Override
    public void onTabSelected(int position) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        BaseFragment fragment = FragmentFactory.getFragment(position);
        if (!fragment.isAdded()) {
            fragmentTransaction.add(R.id.fl_main, fragment, "" + position);
        }
        fragmentTransaction.show(fragment).commit();
    }

    @Override
    public void onTabUnselected(int position) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.hide(FragmentFactory.getFragment(position));
        fragmentTransaction.commit();
    }

    @Override
    public void onTabReselected(int position) {

    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        REQUEST_PERMISSION);
            }
        }
    }

}
