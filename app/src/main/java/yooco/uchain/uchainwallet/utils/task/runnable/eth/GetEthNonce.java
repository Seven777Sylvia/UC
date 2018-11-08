package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import java.util.ArrayList;

import yooco.uchain.uchainwallet.data.bean.request.RequestGetEthRpc;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetEthRpcResult;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthNonceCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;

/**
 * Created by SteelCabbage on 2018/9/6 0006 17:30.
 * E-Mail：liuyi_61@163.com
 */
public class GetEthNonce implements Runnable, INetCallback {

    private static final String TAG = GetEthNonce.class.getSimpleName();

    private String mAddress;
    private IGetEthNonceCallback mIGetEthNonceCallback;

    public GetEthNonce(String address, IGetEthNonceCallback IGetEthNonceCallback) {
        mAddress = address;
        mIGetEthNonceCallback = IGetEthNonceCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mAddress) || null == mIGetEthNonceCallback) {
            UChainLog.e(TAG, "mAddress or mIGetEthNonceCallback is null!");
            return;
        }

        RequestGetEthRpc requestGetEthRpc = new RequestGetEthRpc();
        requestGetEthRpc.setJsonrpc("2.0");
        requestGetEthRpc.setMethod("eth_getTransactionCount");
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
            mIGetEthNonceCallback.getEthNonce(null);
            return;
        }

        String nonce = responseGetEthRpcResult.getResult();
        if (TextUtils.isEmpty(nonce)) {
            UChainLog.e(TAG, "nonce is null!");
            mIGetEthNonceCallback.getEthNonce(null);
            return;
        }

        UChainLog.i(TAG, "nonce is:" + nonce);
        mIGetEthNonceCallback.getEthNonce(nonce);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIGetEthNonceCallback.getEthNonce(null);
    }
}
