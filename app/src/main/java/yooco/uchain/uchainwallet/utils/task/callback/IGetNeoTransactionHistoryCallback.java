package yooco.uchain.uchainwallet.utils.task.callback;

import java.util.List;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;

/**
 * Created by SteelCabbage on 2018/6/22 0022 11:45.
 * E-Mail：liuyi_61@163.com
 */

public interface IGetNeoTransactionHistoryCallback {
    void getNeoTransactionHistory(List<TransactionRecord> transactionRecords);
}
