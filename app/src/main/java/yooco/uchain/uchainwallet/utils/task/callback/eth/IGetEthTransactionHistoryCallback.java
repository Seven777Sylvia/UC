package yooco.uchain.uchainwallet.utils.task.callback.eth;

import java.util.List;

import yooco.uchain.uchainwallet.data.bean.TransactionRecord;

/**
 * Created by SteelCabbage on 2018/9/19 0019 15:26.
 * E-Mail：liuyi_61@163.com
 */
public interface IGetEthTransactionHistoryCallback {
    void getEthTransactionHistory(List<TransactionRecord> transactionRecords);
}
