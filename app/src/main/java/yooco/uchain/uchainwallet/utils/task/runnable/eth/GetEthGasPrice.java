package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import java.util.ArrayList;

import yooco.uchain.uchainwallet.data.bean.request.RequestGetEthRpc;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetEthRpcResult;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthGasPriceCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import yooco.uchain.uchainwallet.utils.WalletUtils;

/**
 * Created by SteelCabbage on 2018/8/29 0029 15:07.
 * E-Mail：liuyi_61@163.com
 */
public class GetEthGasPrice implements Runnable, INetCallback {

    private static final String TAG = GetEthGasPrice.class.getSimpleName();

    private IGetEthGasPriceCallback mIGetEthGasPriceCallback;

    public GetEthGasPrice(IGetEthGasPriceCallback IGetEthGasPriceCallback) {
        mIGetEthGasPriceCallback = IGetEthGasPriceCallback;
    }

    @Override
    public void run() {
        if (null == mIGetEthGasPriceCallback) {
            UChainLog.e(TAG, "mIGetEthGasPriceCallback is null!");
            return;
        }

        RequestGetEthRpc requestGetEthGasPrice = new RequestGetEthRpc();
        requestGetEthGasPrice.setJsonrpc("2.0");
        requestGetEthGasPrice.setMethod("eth_gasPrice");
        requestGetEthGasPrice.setId(73);
        ArrayList<String> arrayList = new ArrayList<>();
        requestGetEthGasPrice.setParams(arrayList);

        OkHttpClientManager.getInstance().postJsonByAuth(Constant.URL_CLI_ETH, GsonUtils.toJsonStr(requestGetEthGasPrice), this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        ResponseGetEthRpcResult responseGetEthGasPrice = GsonUtils.json2Bean(result, ResponseGetEthRpcResult.class);
        if (null == responseGetEthGasPrice) {
            UChainLog.e(TAG, "responseGetEthGasPrice is null!");
            mIGetEthGasPriceCallback.getEthGasPrice(null);
            return;
        }

        String gwei = WalletUtils.toDecString(responseGetEthGasPrice.getResult(), "9");
        if (TextUtils.isEmpty(gwei)) {
            UChainLog.e(TAG, "gwei is null or empty!");
            mIGetEthGasPriceCallback.getEthGasPrice(null);
            return;
        }

        mIGetEthGasPriceCallback.getEthGasPrice(gwei);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIGetEthGasPriceCallback.getEthGasPrice(null);
    }
}
