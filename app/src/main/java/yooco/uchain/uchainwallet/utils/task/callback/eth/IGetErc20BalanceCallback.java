package yooco.uchain.uchainwallet.utils.task.callback.eth;

import java.util.Map;

import yooco.uchain.uchainwallet.data.bean.BalanceBean;

/**
 * Created by SteelCabbage on 2018/7/2 0002 11:12.
 * E-Mail：liuyi_61@163.com
 */

public interface IGetErc20BalanceCallback {
    void getErc20Balance(Map<String, BalanceBean> balanceBeans);
}

