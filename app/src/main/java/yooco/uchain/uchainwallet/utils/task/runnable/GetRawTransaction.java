package yooco.uchain.uchainwallet.utils.task.runnable;

import android.text.TextUtils;

import java.util.ArrayList;

import yooco.uchain.uchainwallet.data.bean.request.RequestGetRawTransaction;
import yooco.uchain.uchainwallet.data.bean.response.ResponseGetRawTransaction;
import yooco.uchain.uchainwallet.utils.task.callback.IGetRawTransactionCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.GsonUtils;

/**
 * Created by SteelCabbage on 2018/6/28 0028 16:58.
 * E-Mail：liuyi_61@163.com
 */

public class GetRawTransaction implements Runnable, INetCallback {

    private static final String TAG = GetRawTransaction.class.getSimpleName();

    private String mTxId;
    private String mWalletAddress;
    private IGetRawTransactionCallback mIGetRawTransactionCallback;

    public GetRawTransaction(String txId, String walletAddress, IGetRawTransactionCallback iGetRawTransactionCallback) {
        mTxId = txId;
        mWalletAddress = walletAddress;
        mIGetRawTransactionCallback = iGetRawTransactionCallback;
    }

    @Override
    public void run() {
        if (null == mIGetRawTransactionCallback
                || TextUtils.isEmpty(mTxId)
                || TextUtils.isEmpty(mWalletAddress)) {
            UChainLog.e(TAG, "mIGetRawTransactionCallback or mTxId or mWalletAddress is null!");
            return;
        }

        RequestGetRawTransaction requestGetRawTransaction = new RequestGetRawTransaction();
        requestGetRawTransaction.setJsonrpc("2.0");
        requestGetRawTransaction.setMethod("getrawtransaction");
        ArrayList<String> params = new ArrayList<>();
        params.add(mTxId);
        params.add("1");
        requestGetRawTransaction.setParams(params);
        requestGetRawTransaction.setId(1);

        OkHttpClientManager.getInstance().postJson(Constant.URL_CLI_NEO, GsonUtils.toJsonStr(requestGetRawTransaction), this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        ResponseGetRawTransaction responseGetRawTransaction = GsonUtils.json2Bean(result, ResponseGetRawTransaction.class);
        if (null == responseGetRawTransaction) {
            UChainLog.e(TAG, "responseGetRawTransaction is null!");
            mIGetRawTransactionCallback.getRawTransaction(mTxId, mWalletAddress, Constant.TX_CONFIRM_EXCEPTION);
            return;
        }

        ResponseGetRawTransaction.ResultBean resultBean = responseGetRawTransaction.getResult();
        if (null == resultBean) {
            UChainLog.e(TAG, "resultBean is null!");
            mIGetRawTransactionCallback.getRawTransaction(mTxId, mWalletAddress, Constant.TX_CONFIRM_EXCEPTION);
            return;
        }

        long confirmations = resultBean.getConfirmations();
        UChainLog.w(TAG, "confirmations:" + confirmations);
        mIGetRawTransactionCallback.getRawTransaction(mTxId, mWalletAddress, confirmations);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIGetRawTransactionCallback.getRawTransaction(mTxId, mWalletAddress, Constant.TX_CONFIRM_EXCEPTION);
    }
}
