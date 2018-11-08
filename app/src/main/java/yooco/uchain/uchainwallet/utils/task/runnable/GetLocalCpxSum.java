package yooco.uchain.uchainwallet.utils.task.runnable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import yooco.uchain.uchainwallet.data.bean.BalanceBean;
import yooco.uchain.uchainwallet.data.bean.WalletBean;
import yooco.uchain.uchainwallet.data.local.UChainWalletDbDao;
import yooco.uchain.uchainwallet.utils.UChainLog;
import yooco.uchain.uchainwallet.utils.task.TaskController;
import yooco.uchain.uchainwallet.utils.task.callback.IGetLocalCpxSumCallback;
import yooco.uchain.uchainwallet.utils.task.callback.IGetNep5BalanceCallback;
import yooco.uchain.uchainwallet.global.UChainWalletApplication;
import yooco.uchain.uchainwallet.global.Constant;

public class GetLocalCpxSum implements Runnable, IGetNep5BalanceCallback {

    private static final String TAG = GetLocalCpxSum.class.getSimpleName();

    private IGetLocalCpxSumCallback iGetLocalCpxSumCallback;
    private int cpxWalletNum;
    private int cpxWalletCounter;
    private BigDecimal cpxSum;

    public GetLocalCpxSum(IGetLocalCpxSumCallback iGetLocalCpxSumCallback) {
        this.iGetLocalCpxSumCallback = iGetLocalCpxSumCallback;
    }

    @Override
    public void run() {
        if (null == iGetLocalCpxSumCallback) {
            UChainLog.e(TAG, "iGetLocalCpxSumCallback is null!");
            return;
        }

        UChainWalletDbDao uChainWalletDbDao = UChainWalletDbDao.getInstance(UChainWalletApplication
                .getInstance());
        if (null == uChainWalletDbDao) {
            UChainLog.e(TAG, "uChainWalletDbDao is null!");
            return;
        }

        cpxSum = new BigDecimal(0);
        List<WalletBean> walletBeans = uChainWalletDbDao.queryWallets(Constant.TABLE_NEO_WALLET);
        if (null == walletBeans || walletBeans.isEmpty()) {
            UChainLog.w(TAG, "walletBeans is null or empty!");
            iGetLocalCpxSumCallback.getLocalCpxSum(cpxSum.toPlainString());
            return;
        }

        cpxWalletNum = walletBeans.size();
        for (WalletBean walletBean : walletBeans) {
            if (null == walletBean) {
                UChainLog.e(TAG, "walletBean is null!");
                continue;
            }

            TaskController.getInstance().submit(new GetNep5Balance(Constant.ASSETS_CPX,
                    walletBean.getAddress(), this));
        }
    }

    @Override
    public void getNep5Balance(Map<String, BalanceBean> balanceBeans) {
        cpxWalletCounter++;

        if (null == balanceBeans || balanceBeans.isEmpty()) {
            UChainLog.e(TAG, "balanceBeans is null or empty!");
            return;
        }

        BalanceBean balanceBean = balanceBeans.get(Constant.ASSETS_CPX);
        if (null == balanceBean) {
            UChainLog.w(TAG, "balanceBean is null!");
        } else {
            cpxSum = cpxSum.add(new BigDecimal(balanceBean.getAssetsValue()));
        }

        if (cpxWalletCounter == cpxWalletNum) {
            iGetLocalCpxSumCallback.getLocalCpxSum(cpxSum.toPlainString());
        }

    }
}
