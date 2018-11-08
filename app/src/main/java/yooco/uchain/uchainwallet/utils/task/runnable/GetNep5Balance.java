package yooco.uchain.uchainwallet.utils.task.runnable;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import yooco.uchain.uchainwallet.data.bean.AssetBean;
import yooco.uchain.uchainwallet.data.bean.BalanceBean;
import yooco.uchain.uchainwallet.data.bean.request.RequestGetNep5Balance;
import yooco.uchain.uchainwallet.data.bean.request.RequestGetNep5BalanceSub;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetNep5Balance;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.IGetNep5BalanceCallback;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import yooco.uchain.uchainwallet.utils.PhoneUtils;
import neomobile.Neomobile;

/**
 * Created by SteelCabbage on 2018/5/17 0017.
 */

public class GetNep5Balance implements Runnable, INetCallback {

    private static final String TAG = GetNep5Balance.class.getSimpleName();

    private String mAssetID;
    private String mAddress;
    private IGetNep5BalanceCallback mIGetNep5BalanceCallback;

    public GetNep5Balance(String assetID, String address, IGetNep5BalanceCallback
            IGetNep5BalanceCallback) {
        mAssetID = assetID;
        mAddress = address;
        mIGetNep5BalanceCallback = IGetNep5BalanceCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mAssetID)
                || TextUtils.isEmpty(mAddress)
                || null == mIGetNep5BalanceCallback) {
            UChainLog.e(TAG, "mAssetID or mAddress or mIGetNep5BalanceCallback is null！");
            return;
        }

        String decodeAddress = null;
        try {
            decodeAddress = Neomobile.decodeAddress(mAddress);
        } catch (Exception e) {
            UChainLog.e(TAG, "decodeAddress exception: " + e.getMessage());
        }

        if (TextUtils.isEmpty(decodeAddress)) {
            UChainLog.e(TAG, "decodeAddress is null or empty!");
            return;
        }

        RequestGetNep5Balance requestGetNep5Balance = new RequestGetNep5Balance();
        requestGetNep5Balance.setJsonrpc("2.0");
        requestGetNep5Balance.setMethod("invokefunction");
        requestGetNep5Balance.setId(3);

        ArrayList<Object> params = new ArrayList<>();
        params.add(mAssetID);
        params.add("balanceOf");
        ArrayList<RequestGetNep5BalanceSub> paramsSub = new ArrayList<>();
        RequestGetNep5BalanceSub requestGetNep5BalanceSub = new RequestGetNep5BalanceSub();
        requestGetNep5BalanceSub.setType("Hash160");
        requestGetNep5BalanceSub.setValue(decodeAddress);
        paramsSub.add(requestGetNep5BalanceSub);
        params.add(paramsSub);

        requestGetNep5Balance.setParams(params);

        OkHttpClientManager.getInstance().postJson(Constant.URL_CLI_NEO, GsonUtils.toJsonStr(requestGetNep5Balance), this);

    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        HashMap<String, BalanceBean> balanceBeans = new HashMap<>();

        ResponseGetNep5Balance responseGetNep5Balance = GsonUtils.json2Bean(result, ResponseGetNep5Balance.class);
        if (null == responseGetNep5Balance) {
            UChainLog.e(TAG, "responseGetNep5Balance is null!");
            balanceBeans.put(mAssetID, null);
            mIGetNep5BalanceCallback.getNep5Balance(balanceBeans);
            return;
        }

        ResponseGetNep5Balance.ResultBean resultBean = responseGetNep5Balance.getResult();
        if (null == resultBean) {
            UChainLog.e(TAG, "resultBean is null!");
            balanceBeans.put(mAssetID, null);
            mIGetNep5BalanceCallback.getNep5Balance(balanceBeans);
            return;
        }

        List<ResponseGetNep5Balance.ResultBean.StackBean> stackBeans = resultBean.getStack();
        if (null == stackBeans || stackBeans.isEmpty()) {
            UChainLog.e(TAG, "stackBeans is null or empty!");
            balanceBeans.put(mAssetID, null);
            mIGetNep5BalanceCallback.getNep5Balance(balanceBeans);
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication.getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            balanceBeans.put(mAssetID, null);
            mIGetNep5BalanceCallback.getNep5Balance(balanceBeans);
            return;
        }

        AssetBean assetBean = uChainWalletDbDao.queryAssetByHash(Constant.TABLE_NEO_ASSETS, mAssetID);
        if (null == assetBean) {
            UChainLog.e(TAG, "assetBean is null!");
            balanceBeans.put(mAssetID, null);
            mIGetNep5BalanceCallback.getNep5Balance(balanceBeans);
            return;
        }

        for (ResponseGetNep5Balance.ResultBean.StackBean stackBean : stackBeans) {
            if (null == stackBean) {
                UChainLog.e(TAG, "stackBean is null!");
                continue;
            }

            String stackBeanValue = stackBean.getValue();
            if (TextUtils.isEmpty(stackBeanValue)) {
                UChainLog.w(TAG, "stackBeanValue is null!");
                stackBeanValue = "0";
            }

            BigInteger reverseArray = new BigInteger(PhoneUtils.reverseArray(stackBeanValue));
            BigDecimal pow = new BigDecimal(10).pow(Integer.parseInt("8"));
            BigDecimal value = null;
            try {
                value = new BigDecimal(reverseArray).divide(pow);
            } catch (Exception e) {
                UChainLog.e(TAG, e.getMessage());
            }

            BalanceBean balanceBean = new BalanceBean();
            balanceBean.setMapState(Constant.MAP_STATE_UNFINISHED);
            balanceBean.setWalletType(Constant.WALLET_TYPE_NEO);
            balanceBean.setAssetsID(mAssetID);
            balanceBean.setAssetSymbol(assetBean.getSymbol());
            balanceBean.setAssetType(Constant.ASSET_TYPE_NEP5);
            balanceBean.setAssetDecimal(Integer.valueOf(assetBean.getPrecision()));
            balanceBean.setAssetsValue(null == value ? "0" : value.toPlainString());
            balanceBeans.put(mAssetID, balanceBean);
        }

        mIGetNep5BalanceCallback.getNep5Balance(balanceBeans);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        HashMap<String, BalanceBean> balanceBeans = new HashMap<>();
        balanceBeans.put(mAssetID, null);
        mIGetNep5BalanceCallback.getNep5Balance(balanceBeans);
    }
}
