package yooco.uchain.uchainwallet.utils.task.runnable;

import java.util.List;

import yooco.uchain.uchainwallet.data.bean.AssetBean;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.ICheckIsUpdateNeoAssetsCallback;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;

/**
 * Created by SteelCabbage on 2018/7/8 14:49
 * E-Mail：liuyi_61@163.com
 */
public class CheckIsUpdateNeoAssets implements Runnable {

    private static final String TAG = CheckIsUpdateNeoAssets.class.getSimpleName();

    private ICheckIsUpdateNeoAssetsCallback mICheckIsUpdateNeoAssetsCallback;

    public CheckIsUpdateNeoAssets(ICheckIsUpdateNeoAssetsCallback ICheckIsUpdateNeoAssetsCallback) {
        mICheckIsUpdateNeoAssetsCallback = ICheckIsUpdateNeoAssetsCallback;
    }

    @Override
    public void run() {
        if (null == mICheckIsUpdateNeoAssetsCallback) {
            UChainLog.e(TAG, "mICheckIsUpdateNeoAssetsCallback is null!");
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication
                .getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        List<AssetBean> assetBeans = uChainWalletDbDao.queryAssetsByType(Constant.TABLE_NEO_ASSETS, Constant.ASSET_TYPE_NEP5);
        if (null == assetBeans || assetBeans.isEmpty()) {
            mICheckIsUpdateNeoAssetsCallback.checkIsUpdateNeoAssets(true);
            return;
        }

        UChainLog.i(TAG, "no need to update neo assets!");
        mICheckIsUpdateNeoAssetsCallback.checkIsUpdateNeoAssets(false);
    }
}
