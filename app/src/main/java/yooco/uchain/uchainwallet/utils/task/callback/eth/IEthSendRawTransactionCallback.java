package yooco.uchain.uchainwallet.utils.task.callback.eth;

/**
 * Created by SteelCabbage on 2018/9/7 0007 16:27.
 * E-Mail：liuyi_61@163.com
 */
public interface IEthSendRawTransactionCallback {
    void ethSendRawTransaction(Boolean isSendSuccess, String txId);
}
