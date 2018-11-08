package yooco.uchain.uchainwallet.utils.task.runnable;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import yooco.uchain.uchainwallet.data.bean.BalanceBean;
import yooco.uchain.uchainwallet.data.bean.request.RequestGetAccountState;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetAccountState;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.IGetAccountStateCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;

/**
 * Created by SteelCabbage on 2018/5/17 0017.
 */

public class GetAccountState implements Runnable, INetCallback {

    private static final String TAG = GetAccountState.class.getSimpleName();

    private String mAddress;
    private IGetAccountStateCallback mIGetAccountStateCallback;

    public GetAccountState(String account, IGetAccountStateCallback iGetAccountStateCallback) {
        mAddress = account;
        mIGetAccountStateCallback = iGetAccountStateCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mAddress) || null == mIGetAccountStateCallback) {
            UChainLog.e(TAG, "run() -> mAccount or mIGetAccountStateCallback is null！");
            return;
        }

        RequestGetAccountState requestGetAccountState = new RequestGetAccountState();
        requestGetAccountState.setJsonrpc("2.0");
        requestGetAccountState.setMethod("getaccountstate");
        requestGetAccountState.setId(1);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(mAddress);
        requestGetAccountState.setParams(arrayList);

        OkHttpClientManager.getInstance().postJson(Constant.URL_CLI_NEO, GsonUtils.toJsonStr(requestGetAccountState), this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        ResponseGetAccountState responseGetAccountState = GsonUtils.json2Bean(result, ResponseGetAccountState.class);
        if (null == responseGetAccountState) {
            UChainLog.e(TAG, "responseGetAccountState is null!");
            mIGetAccountStateCallback.getNeoGlobalAssetBalance(null);
            return;
        }

        ResponseGetAccountState.ResultBean resultBean = responseGetAccountState.getResult();
        if (null == resultBean) {
            UChainLog.e(TAG, "resultBean is null!");
            mIGetAccountStateCallback.getNeoGlobalAssetBalance(null);
            return;
        }

        List<ResponseGetAccountState.ResultBean.BalancesBean> balances = resultBean.getBalances();
        if (null == balances || balances.isEmpty()) {
            UChainLog.w(TAG, "balances is null or empty!");
            mIGetAccountStateCallback.getNeoGlobalAssetBalance(null);
            return;
        }

        HashMap<String, BalanceBean> balanceBeans = new HashMap<>();
        for (ResponseGetAccountState.ResultBean.BalancesBean balance : balances) {
            if (null == balance) {
                UChainLog.e(TAG, "balance is null!");
                continue;
            }

            BalanceBean balanceBean = new BalanceBean();
            balanceBean.setMapState(Constant.MAP_STATE_UNFINISHED);
            balanceBean.setWalletType(Constant.WALLET_TYPE_NEO);
            balanceBean.setAssetsID(balance.getAsset());
            balanceBean.setAssetType(Constant.ASSET_TYPE_GLOBAL);
            balanceBean.setAssetDecimal(0);
            balanceBean.setAssetsValue(balance.getValue());
            balanceBeans.put(balance.getAsset(), balanceBean);
        }

        mIGetAccountStateCallback.getNeoGlobalAssetBalance(balanceBeans);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIGetAccountStateCallback.getNeoGlobalAssetBalance(null);
    }
}
