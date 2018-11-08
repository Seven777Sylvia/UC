package yooco.uchain.uchainwallet.presenter.balance;

import yooco.uchain.uchainwallet.data.bean.WalletBean;

/**
 * Created by SteelCabbage on 2018/8/17 0017 10:54.
 * E-Mail：liuyi_61@163.com
 */

public interface IGetBalancePresenter {
    void init(int walletType);

    void getGlobalAssetBalance(WalletBean walletBean);

    void getColorAssetBalance(WalletBean walletBean);
}
