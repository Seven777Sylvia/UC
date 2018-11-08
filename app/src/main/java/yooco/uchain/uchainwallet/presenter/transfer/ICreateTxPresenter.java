package yooco.uchain.uchainwallet.presenter.transfer;

import yooco.uchain.uchainwallet.data.bean.gasfee.ITxFee;
import yooco.uchain.uchainwallet.data.bean.tx.ITxBean;

/**
 * Created by SteelCabbage on 2018/8/24 0024 15:15.
 * E-Mail：liuyi_61@163.com
 */
public interface ICreateTxPresenter {
    void init(int walletType);

    void checkTxFee(ITxFee iTxFee);

    void createGlobalTx(ITxBean iTxBean);

    void createColorTx(ITxBean iTxBean);
}
