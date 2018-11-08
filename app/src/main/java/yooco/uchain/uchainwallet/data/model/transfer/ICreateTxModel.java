package yooco.uchain.uchainwallet.data.model.transfer;

import yooco.uchain.uchainwallet.data.bean.gasfee.ITxFee;
import yooco.uchain.uchainwallet.data.bean.tx.ITxBean;

/**
 * Created by SteelCabbage on 2018/8/24 0024 15:48.
 * E-Mail：liuyi_61@163.com
 */
public interface ICreateTxModel {
    void checkTxFee(ITxFee iTxFee);

    void createGlobalTx(ITxBean iTxBean);

    void createColorTx(ITxBean iTxBean);

}
