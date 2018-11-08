package yooco.uchain.uchainwallet.utils.task.callback;

import java.util.Map;

import yooco.uchain.uchainwallet.data.bean.BalanceBean;

/**
 * Created by SteelCabbage on 2018/6/7 23:44
 * E-Mail：liuyi_61@163.com
 */
public interface IGetAccountStateCallback {
    void getNeoGlobalAssetBalance(Map<String, BalanceBean> balanceBeans);
}
