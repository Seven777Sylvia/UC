package yooco.uchain.uchainwallet.data.remote;

/**
 * Created by SteelCabbage on 2018/3/26.
 */

public interface INetCallback<T> {

    void onSuccess(int statusCode, String msg, String result);

    void onFailed(int failedCode, String msg);

}
