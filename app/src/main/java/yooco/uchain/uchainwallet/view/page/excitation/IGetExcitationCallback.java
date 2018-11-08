package yooco.uchain.uchainwallet.view.page.excitation;

import java.util.List;

import yooco.uchain.uchainwallet.data.bean.ExcitationBean;

public interface IGetExcitationCallback {
    void getExcitation(List<ExcitationBean> excitationBeans);
}
