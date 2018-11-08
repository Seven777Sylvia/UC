package yooco.uchain.uchainwallet.view.page.assets;

/**
 * Created by SteelCabbage on 2018/8/24 0024 15:19.
 * E-Mail：liuyi_61@163.com
 */
public interface ICreateTxView {
    void checkTxFee(boolean isEnough, String msg);

    void createTxMsg(String toastMsg, boolean isFinish);
}
