package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import java.util.ArrayList;

import yooco.uchain.uchainwallet.data.bean.request.RequestGetEthRpc;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetEthRpcResult;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthBlockNumberCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import yooco.uchain.uchainwallet.utils.WalletUtils;

/**
 * Created by SteelCabbage on 2018/9/20 0020 13:29.
 * E-Mail：liuyi_61@163.com
 */
public class GetEthBlockNumber implements Runnable, INetCallback {
    private static final String TAG = GetEthBlockNumber.class.getSimpleName();
    private IGetEthBlockNumberCallback mIGetEthBlockNumberCallback;

    public GetEthBlockNumber(IGetEthBlockNumberCallback IGetEthBlockNumberCallback) {
        mIGetEthBlockNumberCallback = IGetEthBlockNumberCallback;
    }

    @Override
    public void run() {
        if (null == mIGetEthBlockNumberCallback) {
            UChainLog.e(TAG, "mIGetEthBlockNumberCallback is null!");
            return;
        }

        RequestGetEthRpc requestGetEthRpc = new RequestGetEthRpc();
        requestGetEthRpc.setJsonrpc("2.0");
        requestGetEthRpc.setMethod("eth_blockNumber");
        requestGetEthRpc.setId(83);
        requestGetEthRpc.setParams(new ArrayList<String>());

        OkHttpClientManager.getInstance().postJsonByAuth(Constant.URL_CLI_ETH, GsonUtils.toJsonStr(requestGetEthRpc), this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        UChainLog.i(TAG, "onSuccess,result:" + result);
        if (TextUtils.isEmpty(result)) {
            UChainLog.e(TAG, "result is null!");
            mIGetEthBlockNumberCallback.getEthBlockNumber(null);
            return;
        }

        ResponseGetEthRpcResult responseGetEthRpcResult = GsonUtils.json2Bean(result, ResponseGetEthRpcResult.class);
        if (null == responseGetEthRpcResult) {
            UChainLog.e(TAG, "responseGetEthRpcResult is null!");
            mIGetEthBlockNumberCallback.getEthBlockNumber(null);
            return;
        }

        String blockNumber = WalletUtils.toDecString(responseGetEthRpcResult.getResult(), "0");
        UChainLog.i(TAG, "blockNumber:" + blockNumber);
        mIGetEthBlockNumberCallback.getEthBlockNumber(blockNumber);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.i(TAG, "onFailed,msg:" + msg);
        mIGetEthBlockNumberCallback.getEthBlockNumber(null);
    }
}
