package yooco.uchain.uchainwallet.view.page.excitation;

import java.util.List;

import yooco.uchain.uchainwallet.data.bean.ExcitationBean;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.global.Constant;

public class GetExcitationModel implements IGetExcitationModel, IGetExcitationCallback {

    private static final String TAG = GetExcitationModel.class.getSimpleName();

    private IGetExcitationModelCallback mIGetExcitationModelCallback;

    public GetExcitationModel(IGetExcitationModelCallback mIGetExcitationModelCallback) {
        this.mIGetExcitationModelCallback = mIGetExcitationModelCallback;
    }

    @Override
    public void getExcitation() {
        TaskController.getInstance().submit(new GetExcitation(Constant.EXCITATION_SHOW_LIST, this));
    }

    @Override
    public void getExcitation(List<ExcitationBean> excitationBeans) {
        mIGetExcitationModelCallback.getExcitation(excitationBeans);
    }
}
