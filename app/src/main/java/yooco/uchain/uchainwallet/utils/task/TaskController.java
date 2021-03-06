package yooco.uchain.uchainwallet.utils.task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import yooco.uchain.uchainwallet.utils.UChainLog;

/**
 * Created by SteelCabbage on 2018/3/22.
 */

public class TaskController {

    private static final String TAG = TaskController.class.getSimpleName();

    private static TaskController sTaskController;

    private ScheduledExecutorService mScheduledExecutorService;

    private TaskController() {

    }

    public static TaskController getInstance() {
        if (null == sTaskController) {
            synchronized (TaskController.class) {
                if (null == sTaskController) {
                    sTaskController = new TaskController();
                }
            }
        }

        return sTaskController;
    }


    public void doInit() {
        mScheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }


    public void onDestroy() {
        if (null == mScheduledExecutorService) {
            UChainLog.e(TAG, "onDestroy() -> mScheduledExecutorService is null!");
            return;
        }

        mScheduledExecutorService.shutdown();
        if (!mScheduledExecutorService.isShutdown()) {
            UChainLog.e(TAG, "onDestroy() -> mScheduledExecutorService did not close!");
            return;
        }

        UChainLog.i(TAG, "onDestroy() -> mScheduledExecutorService has been closed");
        mScheduledExecutorService = null;
    }

    public void submit(Runnable runnable) {
        if (null == mScheduledExecutorService || null == runnable) {
            UChainLog.e(TAG, "submit() -> mScheduledExecutorService or runnable is null!");
            return;
        }

        mScheduledExecutorService.submit(runnable);
    }

    public ScheduledFuture schedule(Runnable runnable, long initialDelay, long period) {
        if (null == mScheduledExecutorService || null == runnable) {
            UChainLog.e(TAG, "schedule() -> mScheduledExecutorService or runnable is null!");
            return null;
        }

        return mScheduledExecutorService.scheduleAtFixedRate(runnable, initialDelay, period, TimeUnit.MILLISECONDS);
    }

}
