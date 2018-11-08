package yooco.uchain.uchainwallet.data.model.balance;

import yooco.uchain.uchainwallet.data.bean.WalletBean;

/**
 * Created by SteelCabbage on 2018/8/17 0017 11:09.
 * E-Mail：liuyi_61@163.com
 */

public interface IGetBalanceModel {
    void init();

    void getGlobalAssetBalance(WalletBean walletBean);

    void getColorAssetBalance(WalletBean walletBean);
}
