package yooco.uchain.uchainwallet.utils.task.runnable;

import yooco.uchain.uchainwallet.data.bean.response.ResponseGetNeoUtxos;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.callback.IGetUtxosCallback;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.remote.INetCallback;
import yooco.uchain.uchainwallet.data.remote.OkHttpClientManager;
import yooco.uchain.uchainwallet.utils.GsonUtils;

/**
 * Created by SteelCabbage on 2018/5/30 0030.
 */

public class GetUtxos implements Runnable, INetCallback {

    private static final String TAG = GetUtxos.class.getSimpleName();

    private String mAddress;
    private IGetUtxosCallback mIGetUtxosCallback;

    public GetUtxos(String address, IGetUtxosCallback iGetUtxosCallback) {
        mAddress = address;
        mIGetUtxosCallback = iGetUtxosCallback;
    }

    @Override
    public void run() {
        String url = Constant.URL_UTXOS_NEO + mAddress;
        if (null == mIGetUtxosCallback) {
            UChainLog.e(TAG, "mIGetUtxosCallback is null！");
            return;
        }

        OkHttpClientManager.getInstance().get(url, this);
    }

    @Override
    public void onSuccess(int statusCode, String msg, String result) {
        ResponseGetNeoUtxos responseGetNeoUtxos = GsonUtils.json2Bean(result, ResponseGetNeoUtxos.class);
        if (null == responseGetNeoUtxos) {
            UChainLog.e(TAG, "responseGetNeoUtxos is null!");
            mIGetUtxosCallback.getUtxos(null);
            return;
        }

        String utxos = GsonUtils.toJsonStr(responseGetNeoUtxos.getData());
        mIGetUtxosCallback.getUtxos(utxos);
    }

    @Override
    public void onFailed(int failedCode, String msg) {
        UChainLog.e(TAG, "onFailed() -> msg:" + msg);
        mIGetUtxosCallback.getUtxos(null);
    }
}
