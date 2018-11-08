package yooco.uchain.uchainwallet.view.page.excitation.detail;

import android.text.TextUtils;

import yooco.uchain.uchainwallet.data.bean.AddressResultCode;
import yooco.uchain.uchainwallet.data.bean.request.RequestSubmitExcitation;
import yooco.uchain.uchainwallet.data.bean.response.ResponseExcitationDetailCode;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;

public class GetDetailCode implements Runnable, INetCallback {

    private static final String TAG = GetDetailCode.class.getSimpleName();

    private String mAddress;
    private RequestSubmitExcitation mRequestSubmitExcitation;
    private IGetDetailCodeCallback mIGetDetailCodeCallback;

    public GetDetailCode(String address, RequestSubmitExcitation requestSubmitExcitation, IGetDetailCodeCallback
            iGetDetailCodeCallback) {
        mAddress = address;
        mRequestSubmitExcitation = requestSubmitExcitation;
        mIGetDetailCodeCallback = iGetDetailCodeCallback;
    }

    @Override
    public void run() {
        if (TextUtils.isEmpty(mAddress) || null == mIGetDetailCodeCallback || null == mRequestSubmitExcitation) {
            UChainLog.e(TAG, "run() -> mAddress or mIGetExcitationCallback or mRequestSubmitExcitation is null！");
            return;
        }

        OkHttpClientManager.getInstance().postJson(mAddress, GsonUtils.toJsonStr(mRequestSubmitExcitation), this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        if (TextUtils.isEmpty(result)) {
            UChainLog.i(TAG, "result == null ");
            mIGetDetailCodeCallback.getDetailCode(null);
            return;
        }

        ResponseExcitationDetailCode responseExcitationDetailCode = GsonUtils.json2Bean(result,
                ResponseExcitationDetailCode.class);
        if (null == responseExcitationDetailCode) {
            UChainLog.e(TAG, "responseExcitationDetailCode is null ");
            mIGetDetailCodeCallback.getDetailCode(null);
            return;
        }

        ResponseExcitationDetailCode.DataBean dataBeans = responseExcitationDetailCode.getData();
        if (null == dataBeans) {
            UChainLog.e(TAG, "DataBean is null ");
            mIGetDetailCodeCallback.getDetailCode(null);
            return;
        }

        AddressResultCode addressResultCode = new AddressResultCode();
        addressResultCode.setCpxCode(dataBeans.getCPX());
        addressResultCode.setEthCode(dataBeans.getETH());

        mIGetDetailCodeCallback.getDetailCode(addressResultCode);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIGetDetailCodeCallback.getDetailCode(null);
    }

}
