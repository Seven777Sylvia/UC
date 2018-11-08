package yooco.uchain.uchainwallet.utils.task.callback.eth;

import java.util.List;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;

/**
 * Created by SteelCabbage on 2018/7/13 0013 13:11.
 * E-Mail：liuyi_61@163.com
 */

public interface ICheckIsUpdateEthTxStateCallback {
    void checkIsUpdateEthTxState(List<TransactionRecord> transactionRecords);
}
