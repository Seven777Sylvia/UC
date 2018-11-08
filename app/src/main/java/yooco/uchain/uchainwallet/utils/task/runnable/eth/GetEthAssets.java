package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import java.util.List;

import yooco.uchain.uchainwallet.data.bean.AssetBean;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetEthAssets;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthAssetsCallback;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;

/**
 * Created by SteelCabbage on 2018/7/8 13:57
 * E-Mail：liuyi_61@163.com
 */
public class GetEthAssets implements Runnable, INetCallback {

    private static final String TAG = GetEthAssets.class.getSimpleName();

    private IGetEthAssetsCallback mIGetEthAssetsCallback;

    public GetEthAssets(IGetEthAssetsCallback IGetEthAssetsCallback) {
        mIGetEthAssetsCallback = IGetEthAssetsCallback;
    }

    @Override
    public void run() {
        if (null == mIGetEthAssetsCallback) {
            UChainLog.e(TAG, "mIGetEthAssetsCallback is null!");
            return;
        }

        OkHttpClientManager.getInstance().get(Constant.URL_ASSETS_ETH, this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        ResponseGetEthAssets responseGetEthAssets = GsonUtils.json2Bean(result, ResponseGetEthAssets.class);
        if (null == responseGetEthAssets) {
            UChainLog.e(TAG, "responseGetEthAssets is null!");
            mIGetEthAssetsCallback.getEthAssets(null);
            return;
        }

        List<ResponseGetEthAssets.DataBean> data = responseGetEthAssets.getData();
        if (null == data || data.isEmpty()) {
            UChainLog.e(TAG, "data is null or empty!");
            mIGetEthAssetsCallback.getEthAssets(null);
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            mIGetEthAssetsCallback.getEthAssets(null);
            return;
        }


        /**
         *  erc20 NMB(Test)==========================================================
         */
//        AssetBean assetBeanErc20 = new AssetBean();
//        assetBeanErc20.setType(Constant.ASSET_TYPE_ERC20);
//        assetBeanErc20.setSymbol("NMB");
//        assetBeanErc20.setPrecision(18 + "");
//        assetBeanErc20.setName("NMB");
//        assetBeanErc20.setImageUrl("");
//        assetBeanErc20.setHexHash(Constant.ASSETS_ERC20_NMB);
//        assetBeanErc20.setHash(Constant.ASSETS_ERC20_NMB);
//
//        uChainWalletDbDao.insertAsset(Constant.TABLE_ETH_ASSETS, assetBeanErc20);
        /**
         *  erc20 NMB(Test)==========================================================
         */


        for (ResponseGetEthAssets.DataBean dataBean : data) {
            if (null == dataBean) {
                UChainLog.e(TAG, "resultBean is null!");
                continue;
            }

            AssetBean assetBean = new AssetBean();
            assetBean.setType(dataBean.getType());
            assetBean.setSymbol(dataBean.getSymbol());
            assetBean.setPrecision(dataBean.getPrecision());
            assetBean.setName(dataBean.getName());
            assetBean.setImageUrl(dataBean.getImage_url());
            assetBean.setHexHash(dataBean.getHex_hash());
            assetBean.setHash(dataBean.getHash());

            uChainWalletDbDao.insertAsset(Constant.TABLE_ETH_ASSETS, assetBean);
        }

        mIGetEthAssetsCallback.getEthAssets(Constant.UPDATE_ASSETS_OK);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIGetEthAssetsCallback.getEthAssets(null);
    }

}
