package yooco.uchain.uchainwallet.utils.task.runnable;

import java.util.ArrayList;

import yooco.uchain.uchainwallet.data.bean.request.RequestSendRawTransaction;
import yooco.uchain.uchainwallet.data.bean.response.ResponseSendRawTransaction;
import yooco.uchain.uchainwallet.utils.task.callback.ISendRawTransactionCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.GsonUtils;

/**
 * Created by SteelCabbage on 2018/5/30 0030.
 */

public class SendRawTransaction implements Runnable, INetCallback {

    private static final String TAG = SendRawTransaction.class.getSimpleName();

    private String mTxData;
    private ISendRawTransactionCallback mISendRawTransactionCallback;

    public SendRawTransaction(String txData, ISendRawTransactionCallback iSendRawTransactionCallback) {
        mTxData = txData;
        mISendRawTransactionCallback = iSendRawTransactionCallback;
    }

    @Override
    public void run() {
        if (null == mTxData) {
            UChainLog.e(TAG, "mTxData is null!");
            return;
        }

        if (null == mISendRawTransactionCallback) {
            UChainLog.e(TAG, "mISendRawTransactionCallback is null!");
            return;
        }

        RequestSendRawTransaction requestSendRawTransaction = new RequestSendRawTransaction();
        requestSendRawTransaction.setJsonrpc("2.0");
        requestSendRawTransaction.setMethod("sendrawtransaction");
        ArrayList<String> sendDatas = new ArrayList<>();
        sendDatas.add(mTxData);
        requestSendRawTransaction.setParams(sendDatas);
        requestSendRawTransaction.setId(1);

        OkHttpClientManager.getInstance().postJson(Constant.URL_CLI_NEO, GsonUtils.toJsonStr
                (requestSendRawTransaction), this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        ResponseSendRawTransaction responseSendRawTransaction = GsonUtils
                .json2Bean(result, ResponseSendRawTransaction.class);
        if (null == responseSendRawTransaction) {
            mISendRawTransactionCallback.sendTxData(false);
            return;
        }

        UChainLog.i(TAG, "onSuccess() -> broadcast:" + responseSendRawTransaction.isResult());
        mISendRawTransactionCallback.sendTxData(responseSendRawTransaction.isResult());
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mISendRawTransactionCallback.sendTxData(false);
    }
}
