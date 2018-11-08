package yooco.uchain.uchainwallet.presenter.transfer;

import yooco.uchain.uchainwallet.data.bean.gasfee.ITxFee;
import yooco.uchain.uchainwallet.data.bean.tx.ITxBean;
import yooco.uchain.uchainwallet.global.Constant;
import yooco.uchain.uchainwallet.data.model.transfer.CreateEthTxModel;
import yooco.uchain.uchainwallet.data.model.transfer.CreateNeoTxModel;
import yooco.uchain.uchainwallet.data.model.transfer.ICreateTxModel;
import yooco.uchain.uchainwallet.data.model.transfer.ICreateTxModelCallback;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.view.page.assets.ICreateTxView;

/**
 * Created by SteelCabbage on 2018/8/24 0024 15:15.
 * E-Mail：liuyi_61@163.com
 */
public class CreateTxPresenter implements ICreateTxPresenter, ICreateTxModelCallback {

    private static final String TAG = CreateTxPresenter.class.getSimpleName();

    private ICreateTxView mICreateTxView;
    private ICreateTxModel mICreateTxModel = new CreateEthTxModel(this);

    public CreateTxPresenter(ICreateTxView ICreateTxView) {
        mICreateTxView = ICreateTxView;
    }

    @Override
    public void init(int walletType) {
        switch (walletType) {
            case Constant.WALLET_TYPE_NEO:
                mICreateTxModel = new CreateNeoTxModel(this);
                break;
            case Constant.WALLET_TYPE_ETH:
                mICreateTxModel = new CreateEthTxModel(this);
                break;
            default:
                break;
        }
    }

    @Override
    public void checkTxFee(ITxFee iTxFee) {
        mICreateTxModel.checkTxFee(iTxFee);
    }

    @Override
    public void createGlobalTx(ITxBean iTxBean) {
        mICreateTxModel.createGlobalTx(iTxBean);
    }

    @Override
    public void createColorTx(ITxBean iTxBean) {
        mICreateTxModel.createColorTx(iTxBean);
    }

    @Override
    public void checkTxFee(boolean isEnough, String msg) {
        mICreateTxView.checkTxFee(isEnough, msg);
    }

    @Override
    public void CreateTxModel(String toastMsg, boolean isFinish) {
        if (null == mICreateTxView) {
            UChainLog.e(TAG, "mICreateTxView is null!");
            return;
        }

        mICreateTxView.createTxMsg(toastMsg, isFinish);
    }

}
