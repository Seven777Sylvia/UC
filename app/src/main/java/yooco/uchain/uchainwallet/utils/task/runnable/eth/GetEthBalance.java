package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

import yooco.uchain.uchainwallet.data.bean.AssetBean;
import yooco.uchain.uchainwallet.data.bean.BalanceBean;
import yooco.uchain.uchainwallet.data.bean.request.RequestGetEthRpc;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetEthRpcResult;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthBalanceCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import yooco.uchain.uchainwallet.utils.WalletUtils;

/**
 * Created by SteelCabbage on 2018/5/17 0017.
 */

public class GetEthBalance implements Runnable, INetCallback {

    private static final String TAG = GetEthBalance.class.getSimpleName();

    private String mAddress;
    private IGetEthBalanceCallback mIGetEthBalanceCallback;

    public GetEthBalance(String address, IGetEthBalanceCallback IGetEthBalanceCallback) {
        mAddress = address;
        mIGetEthBalanceCallback = IGetEthBalanceCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mAddress) || null == mIGetEthBalanceCallback) {
            UChainLog.e(TAG, "run() -> mAddress or mIGetEthBalanceCallback is null！");
            return;
        }

        RequestGetEthRpc requestGetEthRpc = new RequestGetEthRpc();
        requestGetEthRpc.setJsonrpc("2.0");
        requestGetEthRpc.setMethod("eth_getBalance");
        requestGetEthRpc.setId(1);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(mAddress);
        arrayList.add("latest");
        requestGetEthRpc.setParams(arrayList);

        OkHttpClientManager.getInstance().postJsonByAuth(Constant.URL_CLI_ETH, GsonUtils.toJsonStr(requestGetEthRpc), this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        ResponseGetEthRpcResult responseGetEthRpcResult = GsonUtils.json2Bean(result, ResponseGetEthRpcResult.class);
        if (null == responseGetEthRpcResult) {
            UChainLog.e(TAG, "responseGetEthRpcResult is null!");
            mIGetEthBalanceCallback.getEthBalance(null);
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            mIGetEthBalanceCallback.getEthBalance(null);
            return;
        }

        AssetBean assetBean = uChainWalletDbDao.queryAssetByHash(Constant.TABLE_ETH_ASSETS, Constant.ASSETS_ETH);
        if (null == assetBean) {
            UChainLog.e(TAG, "assetBean is null!");
            mIGetEthBalanceCallback.getEthBalance(null);
            return;
        }

        String ethBalance = WalletUtils.toDecString(responseGetEthRpcResult.getResult(), assetBean.getPrecision());
        if (TextUtils.isEmpty(ethBalance)) {
            UChainLog.e(TAG, "ethBalance is null or empty!");
            mIGetEthBalanceCallback.getEthBalance(null);
            return;
        }

        HashMap<String, BalanceBean> balanceBeans = new HashMap<>();
        BalanceBean balanceBean = new BalanceBean();
        balanceBean.setMapState(Constant.MAP_STATE_UNFINISHED);
        balanceBean.setWalletType(Constant.WALLET_TYPE_ETH);
        balanceBean.setAssetsID(Constant.ASSETS_ETH);
        balanceBean.setAssetSymbol(assetBean.getSymbol());
        balanceBean.setAssetType(Constant.ASSET_TYPE_ETH);
        balanceBean.setAssetDecimal(Integer.valueOf(assetBean.getPrecision()));
        balanceBean.setAssetsValue(ethBalance);
        balanceBeans.put(Constant.ASSETS_ETH, balanceBean);

        mIGetEthBalanceCallback.getEthBalance(balanceBeans);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIGetEthBalanceCallback.getEthBalance(null);
    }
}
