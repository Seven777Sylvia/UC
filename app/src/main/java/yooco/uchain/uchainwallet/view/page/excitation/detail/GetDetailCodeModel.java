package yooco.uchain.uchainwallet.view.page.excitation.detail;

import yooco.uchain.uchainwallet.data.bean.AddressResultCode;
import yooco.uchain.uchainwallet.data.bean.request.RequestSubmitExcitation;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.global.Constant;

public class GetDetailCodeModel implements IDetailCodeModel, IGetDetailCodeCallback {

    private static final String TAG = GetDetailCodeModel.class.getSimpleName();

    private IGetResultCodeModelCallback mIGetResultCodeModelCallback;

    public GetDetailCodeModel(IGetResultCodeModelCallback iGetResultCodeModelCallback) {
        mIGetResultCodeModelCallback = iGetResultCodeModelCallback;
    }

    @Override
    public void getDetailCode(RequestSubmitExcitation requestSubmitExcitation) {
        TaskController.getInstance().submit(new GetDetailCode(Constant.EXCITATION_DETAIL_UPLOAD_ADDRESS,
                requestSubmitExcitation, this));
    }


    @Override
    public void getDetailCode(AddressResultCode addressResultCode) {
        mIGetResultCodeModelCallback.getResultCode(addressResultCode);
    }
}
