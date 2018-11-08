package yooco.uchain.uchainwallet.utils.task.callback;

import java.util.List;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;

/**
 * Created by SteelCabbage on 2018/6/28 0028 10:22.
 * E-Mail：liuyi_61@163.com
 */

public interface ILoadTransactionRecordCallback {
    void loadTransactionRecord(List<TransactionRecord> transactionRecords);
}
