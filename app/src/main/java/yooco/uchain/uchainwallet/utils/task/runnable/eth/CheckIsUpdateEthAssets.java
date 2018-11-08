package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import java.util.List;

import yooco.uchain.uchainwallet.data.bean.AssetBean;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.task.callback.eth.ICheckIsUpdateEthAssetsCallback;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.utils.UChainLog;

/**
 * Created by SteelCabbage on 2018/7/8 14:49
 * E-Mail：liuyi_61@163.com
 */
public class CheckIsUpdateEthAssets implements Runnable {

    private static final String TAG = CheckIsUpdateEthAssets.class.getSimpleName();

    private ICheckIsUpdateEthAssetsCallback mICheckIsUpdateEthAssetsCallback;

    public CheckIsUpdateEthAssets(ICheckIsUpdateEthAssetsCallback ICheckIsUpdateEthAssetsCallback) {
        mICheckIsUpdateEthAssetsCallback = ICheckIsUpdateEthAssetsCallback;
    }

    @Override
    public void run() {
        if (null == mICheckIsUpdateEthAssetsCallback) {
            UChainLog.e(TAG, "mICheckIsUpdateEthAssetsCallback is null!");
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        List<AssetBean> assetBeans = uChainWalletDbDao.queryAssetsByType(Constant.TABLE_ETH_ASSETS, Constant.ASSET_TYPE_ERC20);
        if (null == assetBeans || assetBeans.isEmpty()) {
            mICheckIsUpdateEthAssetsCallback.checkIsUpdateEthAssets(true);
            return;
        }

        UChainLog.i(TAG, "no need to update eth assets!");
        mICheckIsUpdateEthAssetsCallback.checkIsUpdateEthAssets(false);
    }
}
