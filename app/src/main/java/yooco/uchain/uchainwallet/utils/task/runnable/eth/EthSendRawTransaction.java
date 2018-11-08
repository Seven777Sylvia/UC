package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import java.util.ArrayList;

import yooco.uchain.uchainwallet.data.bean.request.RequestGetEthRpc;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetEthRpcResult;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IEthSendRawTransactionCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;

/**
 * Created by SteelCabbage on 2018/9/7 0007 16:26.
 * E-Mail：liuyi_61@163.com
 */
public class EthSendRawTransaction implements Runnable, INetCallback {

    private static final String TAG = EthSendRawTransaction.class.getSimpleName();

    private String mEthTxData;
    private IEthSendRawTransactionCallback mIEthSendRawTransactionCallback;

    public EthSendRawTransaction(String ethTxData, IEthSendRawTransactionCallback IEthSendRawTransactionCallback) {
        mEthTxData = ethTxData;
        mIEthSendRawTransactionCallback = IEthSendRawTransactionCallback;
    }

    @Override
    public void run() {
        if (null == mIEthSendRawTransactionCallback) {
            UChainLog.e(TAG, "mIEthSendRawTransactionCallback is null!");
            return;
        }

        if (TextUtils.isEmpty(mEthTxData)) {
            UChainLog.e(TAG, "mEthTxData is null!");
            mIEthSendRawTransactionCallback.ethSendRawTransaction(false, null);
            return;
        }

        RequestGetEthRpc requestGetEthRpc = new RequestGetEthRpc();
        requestGetEthRpc.setJsonrpc("2.0");
        requestGetEthRpc.setMethod("eth_sendRawTransaction");
        requestGetEthRpc.setId(1);
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add(mEthTxData);
        requestGetEthRpc.setParams(arrayList);

        OkHttpClientManager.getInstance().postJsonByAuth(Constant.URL_CLI_ETH, GsonUtils.toJsonStr(requestGetEthRpc), this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        UChainLog.i(TAG, "EthSendRawTransaction onSuccess:" + result);
        ResponseGetEthRpcResult responseGetEthRpcResult = GsonUtils.json2Bean(result, ResponseGetEthRpcResult.class);
        if (null == responseGetEthRpcResult) {
            UChainLog.e(TAG, "responseGetEthRpcResult is null!");
            mIEthSendRawTransactionCallback.ethSendRawTransaction(true, null);
            return;
        }

        String txId = responseGetEthRpcResult.getResult();
        if (TextUtils.isEmpty(txId)) {
            UChainLog.e(TAG, "txId is null!");
            mIEthSendRawTransactionCallback.ethSendRawTransaction(true, null);
            return;
        }

        UChainLog.i(TAG, "txId is:" + txId);
        mIEthSendRawTransactionCallback.ethSendRawTransaction(true, txId);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIEthSendRawTransactionCallback.ethSendRawTransaction(false, null);
    }
}
