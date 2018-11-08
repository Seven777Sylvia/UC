package yooco.uchain.uchainwallet.view.page.excitation.detail;

import yooco.uchain.uchainwallet.data.bean.AddressResultCode;
import yooco.uchain.uchainwallet.data.bean.request.RequestSubmitExcitation;

public class GetDetailCodePresenter implements IGetDetailCodePresenter, IGetResultCodeModelCallback {

    private IDetailView mIDetailView;
    private IDetailCodeModel mIDetailCodeModel;

    public GetDetailCodePresenter(IDetailView iDetailView) {
        mIDetailView = iDetailView;
    }

    @Override
    public void getDetailCode(RequestSubmitExcitation requestSubmitExcitation) {
        mIDetailCodeModel = new GetDetailCodeModel(this);
        mIDetailCodeModel.getDetailCode(requestSubmitExcitation);
    }

    @Override
    public void getResultCode(AddressResultCode resultCode) {
        mIDetailView.getDetailCode(resultCode);
    }
}
