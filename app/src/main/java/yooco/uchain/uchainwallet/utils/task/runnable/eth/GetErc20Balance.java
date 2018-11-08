package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;

import yooco.uchain.uchainwallet.data.bean.AssetBean;
import yooco.uchain.uchainwallet.data.bean.BalanceBean;
import yooco.uchain.uchainwallet.data.bean.request.RequestErc20Params;
import yooco.uchain.uchainwallet.data.bean.request.RequestGetErc20Balance;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetEthRpcResult;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetErc20BalanceCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import yooco.uchain.uchainwallet.utils.WalletUtils;
import ethmobile.EthCall;

/**
 * Created by SteelCabbage on 2018/5/17 0017.
 */

public class GetErc20Balance implements Runnable, INetCallback {

    private static final String TAG = GetErc20Balance.class.getSimpleName();

    private String mAssetID;
    private String mAddress;
    private IGetErc20BalanceCallback mIGetErc20BalanceCallback;

    public GetErc20Balance(String assetID, String address, IGetErc20BalanceCallback IGetErc20BalanceCallback) {
        mAssetID = assetID;
        mAddress = address;
        mIGetErc20BalanceCallback = IGetErc20BalanceCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mAssetID) || TextUtils.isEmpty(mAddress) || null == mIGetErc20BalanceCallback) {
            UChainLog.e(TAG, "mAssetID or mAddress or mIGetErc20BalanceCallback is null!");
            return;
        }

        EthCall ethCall = new EthCall();
        String data = null;
        try {
            data = ethCall.balanceOf(mAssetID, mAddress);
        } catch (Exception e) {
            UChainLog.e(TAG, "ethCall.balanceOf exception:" + e.getMessage());
        }

        if (TextUtils.isEmpty(data)) {
            UChainLog.e(TAG, "ethCall.balanceOf data is null!");
            mIGetErc20BalanceCallback.getErc20Balance(null);
            return;
        }

        RequestGetErc20Balance requestGetErc20Balance = new RequestGetErc20Balance();
        requestGetErc20Balance.setJsonrpc("2.0");
        requestGetErc20Balance.setMethod("eth_call");
        requestGetErc20Balance.setId(1);
        ArrayList<Object> arrayList = new ArrayList<>();
        RequestErc20Params requestErc20Params = GsonUtils.json2Bean(data, RequestErc20Params.class);
        arrayList.add(requestErc20Params);
        arrayList.add("latest");
        requestGetErc20Balance.setParams(arrayList);

        OkHttpClientManager.getInstance().postJsonByAuth(Constant.URL_CLI_ETH, GsonUtils.toJsonStr(requestGetErc20Balance), this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        ResponseGetEthRpcResult responseGetErc20Balance = GsonUtils.json2Bean(result, ResponseGetEthRpcResult.class);
        if (null == responseGetErc20Balance) {
            UChainLog.e(TAG, "responseGetErc20Balance is null!");
            mIGetErc20BalanceCallback.getErc20Balance(null);
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            mIGetErc20BalanceCallback.getErc20Balance(null);
            return;
        }

        AssetBean assetBean = uChainWalletDbDao.queryAssetByHash(Constant.TABLE_ETH_ASSETS, mAssetID);
        if (null == assetBean) {
            UChainLog.e(TAG, "assetBean is null!");
            mIGetErc20BalanceCallback.getErc20Balance(null);
            return;
        }

        String erc20BalanceValue = WalletUtils.toDecString(responseGetErc20Balance.getResult(), assetBean.getPrecision());
        if (TextUtils.isEmpty(erc20BalanceValue)) {
            UChainLog.e(TAG, "erc20Balance is null or empty!");
            mIGetErc20BalanceCallback.getErc20Balance(null);
            return;
        }

        HashMap<String, BalanceBean> erc20Balance = new HashMap<>();
        BalanceBean balanceBean = new BalanceBean();
        balanceBean.setMapState(Constant.MAP_STATE_UNFINISHED);
        balanceBean.setWalletType(Constant.WALLET_TYPE_ETH);
        balanceBean.setAssetsID(mAssetID);
        balanceBean.setAssetSymbol(assetBean.getSymbol());
        balanceBean.setAssetType(Constant.ASSET_TYPE_ERC20);
        balanceBean.setAssetDecimal(Integer.valueOf(assetBean.getPrecision()));
        balanceBean.setAssetsValue(erc20BalanceValue);
        erc20Balance.put(mAssetID, balanceBean);

        mIGetErc20BalanceCallback.getErc20Balance(erc20Balance);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIGetErc20BalanceCallback.getErc20Balance(null);
    }
}
