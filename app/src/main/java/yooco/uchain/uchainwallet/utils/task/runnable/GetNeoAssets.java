package yooco.uchain.uchainwallet.utils.task.runnable;

import java.util.List;

import yooco.uchain.uchainwallet.data.bean.AssetBean;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetNeoAssets;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.task.callback.IGetNeoAssetsCallback;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.GsonUtils;

/**
 * Created by SteelCabbage on 2018/7/8 13:57
 * E-Mail：liuyi_61@163.com
 */
public class GetNeoAssets implements Runnable, INetCallback {

    private static final String TAG = GetNeoAssets.class.getSimpleName();

    private IGetNeoAssetsCallback mIGetNeoAssetsCallback;

    public GetNeoAssets(IGetNeoAssetsCallback IGetNeoAssetsCallback) {
        mIGetNeoAssetsCallback = IGetNeoAssetsCallback;
    }

    @Override
    public void run() {
        if (null == mIGetNeoAssetsCallback) {
            UChainLog.e(TAG, "mIGetNeoAssetsCallback is null!");
            return;
        }

        OkHttpClientManager.getInstance().get(Constant.URL_ASSETS_NEO, this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        ResponseGetNeoAssets responseGetNeoAssets = GsonUtils.json2Bean(result, ResponseGetNeoAssets.class);
        if (null == responseGetNeoAssets) {
            UChainLog.e(TAG, "responseGetNeoAssets is null!");
            mIGetNeoAssetsCallback.getNeoAssets(null);
            return;
        }

        List<ResponseGetNeoAssets.DataBean> dataBeans = responseGetNeoAssets.getData();
        if (null == dataBeans || dataBeans.isEmpty()) {
            UChainLog.e(TAG, "dataBeans is null or empty!");
            mIGetNeoAssetsCallback.getNeoAssets(null);
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            mIGetNeoAssetsCallback.getNeoAssets(null);
            return;
        }

        for (ResponseGetNeoAssets.DataBean dataBean : dataBeans) {
            if (null == dataBean) {
                UChainLog.e(TAG, "dataBean is null!");
                continue;
            }

            AssetBean assetBean = new AssetBean();
            String assetType = dataBean.getType();
            if (Constant.ASSET_TYPE_GOVERNING.equals(assetType) || Constant.ASSET_TYPE_UTILITY.equals(assetType)) {
                assetBean.setType(Constant.ASSET_TYPE_GLOBAL);
            } else {
                assetBean.setType(assetType);
            }

            assetBean.setSymbol(dataBean.getSymbol());
            assetBean.setPrecision(dataBean.getPrecision());
            assetBean.setName(dataBean.getName());
            assetBean.setImageUrl(dataBean.getImage_url());
            assetBean.setHexHash(dataBean.getHex_hash());
            assetBean.setHash(dataBean.getHash());

            uChainWalletDbDao.insertAsset(Constant.TABLE_NEO_ASSETS, assetBean);
        }

        mIGetNeoAssetsCallback.getNeoAssets(Constant.UPDATE_ASSETS_OK);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIGetNeoAssetsCallback.getNeoAssets(null);
    }
}
