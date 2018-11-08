package yooco.uchain.uchainwallet.utils.task.runnable.eth;

import android.text.TextUtils;

import java.util.ArrayList;

import yooco.uchain.uchainwallet.data.bean.request.RequestGetEthRpc;
import yooco.uchain.uchainwallet.data.bean.response.ResponseEthTxByHash;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.eth.IGetEthTransactionByHashCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;
import yooco.uchain.uchainwallet.utils.WalletUtils;

/**
 * Created by SteelCabbage on 2018/9/20 0020 13:29.
 * E-Mail：liuyi_61@163.com
 */
public class GetEthTransactionByHash implements Runnable, INetCallback {
    private static final String TAG = GetEthTransactionByHash.class.getSimpleName();

    private String mTxId;
    private String mWalletAddress;
    private IGetEthTransactionByHashCallback mIGetEthTransactionByHashCallback;

    public GetEthTransactionByHash(String txId, String walletAddress, IGetEthTransactionByHashCallback
            IGetEthTransactionByHashCallback) {
        mTxId = txId;
        mWalletAddress = walletAddress;
        mIGetEthTransactionByHashCallback = IGetEthTransactionByHashCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mTxId) || TextUtils.isEmpty(mWalletAddress) || null == mIGetEthTransactionByHashCallback) {
            UChainLog.e(TAG, "mTxId or mTxId or mIGetEthTransactionByHashCallback is null!");
            return;
        }

        RequestGetEthRpc requestGetEthRpc = new RequestGetEthRpc();
        requestGetEthRpc.setJsonrpc("2.0");
        requestGetEthRpc.setMethod("eth_getTransactionByHash");
        requestGetEthRpc.setId(1);
        ArrayList<String> txHash = new ArrayList<>();
        txHash.add(mTxId);
        requestGetEthRpc.setParams(txHash);

        OkHttpClientManager.getInstance().postJsonByAuth(Constant.URL_CLI_ETH, GsonUtils.toJsonStr(requestGetEthRpc), this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        UChainLog.i(TAG, "onSuccess,result:" + result);
        if (TextUtils.isEmpty(result)) {
            UChainLog.e(TAG, "result is null!");
            mIGetEthTransactionByHashCallback.getEthTransactionByHash(mWalletAddress, null);
            return;
        }

        ResponseEthTxByHash responseEthTxByHash = GsonUtils.json2Bean(result, ResponseEthTxByHash.class);
        if (null == responseEthTxByHash) {
            UChainLog.e(TAG, "responseEthTxByHash is null!");
            mIGetEthTransactionByHashCallback.getEthTransactionByHash(mWalletAddress, null);
            return;
        }

        ResponseEthTxByHash.ResultBean resultBean = responseEthTxByHash.getResult();
        if (null == resultBean) {
            UChainLog.e(TAG, "resultBean is null!");
            mIGetEthTransactionByHashCallback.getEthTransactionByHash(mWalletAddress, null);
            return;
        }


        String nonce = WalletUtils.toDecString(resultBean.getNonce(), "0");
        mIGetEthTransactionByHashCallback.getEthTransactionByHash(mWalletAddress, nonce);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.i(TAG, "onFailed,msg:" + msg);
        mIGetEthTransactionByHashCallback.getEthTransactionByHash(mWalletAddress, null);
    }
}
