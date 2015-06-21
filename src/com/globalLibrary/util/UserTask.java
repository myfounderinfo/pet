package com.globalLibrary.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 用户任务，用于异步执行任务
 */
public abstract class UserTask<Params, Progress, Result> {
    private static final AtomicInteger mCount = new AtomicInteger(1);

    private static final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            UserTaskResult result = (UserTaskResult) msg.obj;
            switch (msg.what) {
                case MESSAGE_POST_RESULT:
                    result.mTask.onPostExecute(result.mData[0]);
                    break;
                case MESSAGE_POST_PROGRESS:
                    result.mTask.onProgressUpdate(result.mData);
                    break;
                case MESSAGE_POST_CANCEL:
                    result.mTask.onCancelled();
                    break;
            }
        }
    };
    private static final int MESSAGE_POST_CANCEL = 3;
    private static final int MESSAGE_POST_PROGRESS = 2;
    private static final int MESSAGE_POST_RESULT = 1;
    private static ThreadPoolExecutor mDefaultExecutor = null;
    private final FutureTask<Result> mFuture;

    private final WorkerRunnable<Params, Result> mWorker;

    protected UserTask() {
        mWorker = new WorkerRunnable<Params, Result>() {
            @Override
            public Result call() throws Exception {
                return doInBackground(mParams);
            }
        };

        mFuture = new FutureTask<Result>(mWorker) {
            @Override
            protected void done() {
                Result result = null;
                try {
                    result = mFuture.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    return;
                } catch (CancellationException e) {
                    mHandler.obtainMessage(MESSAGE_POST_CANCEL, new UserTaskResult<Result>(UserTask.this, (Result[]) null)).sendToTarget();
                    return;
                }
                mHandler.obtainMessage(MESSAGE_POST_RESULT, new UserTaskResult<Result>(UserTask.this, result)).sendToTarget();
            }
        };
    }

    /**
     * 在线程中执行
     *
     * @param mParams 任务参数
     * @return 任务执行结束后的返回值
     */
    protected abstract Result doInBackground(Params... mParams);

    /**
     * 更新任务进度提示
     *
     * @param values 进度提示所需参数
     */
    protected final void publishProgress(Progress... values) {
        mHandler.obtainMessage(MESSAGE_POST_PROGRESS, new UserTaskResult<Progress>(this, values)).sendToTarget();
    }

    /**
     * 当任务被终止时，在 UI 线程中调用
     */
    protected void onCancelled() {
    }

    /**
     * 异步任务执行完成后调用
     *
     * @param result 异步任务的返回值
     */
    protected void onPostExecute(Result result) {
    }

    /**
     * 在 UI 线程中更新进度提示
     *
     * @param values 进度提示信息
     */
    protected void onProgressUpdate(Progress... values) {
    }

    /**
     * 试图终止任务的执行
     *
     * @param mayInterruptIfRunning 如果应该中断执行此任务的线程，则为 true；否则允许正在运行的任务运行完成
     * @return 如果无法取消任务，则返回 false，这通常是由于它已经正常完成；否则返回 true
     */
    public final boolean cancel(boolean mayInterruptIfRunning) {
        return mFuture.cancel(mayInterruptIfRunning);
    }

    /**
     * 如果在任务正常完成前将其取消，则返回 true
     *
     * @return 如果在任务正常完成前将其取消，则返回 true
     */
    public final boolean isCancelled() {
        return mFuture.isCancelled();
    }

    /**
     * 执行异步任务
     *
     * @param params 任务需要的参数
     */
    public final void execute(Params... params) {
        execute(getDefaultThreadPoolExecutor(), params);
    }

    private ThreadPoolExecutor getDefaultThreadPoolExecutor() {
        if (mDefaultExecutor == null || mDefaultExecutor.isShutdown()) {
            mDefaultExecutor = new ThreadPoolExecutor(1, 1, 15, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable runnable) {
                    Thread thread = new Thread(runnable, "UserTask #" + mCount.getAndIncrement());
                    thread.setPriority(Thread.NORM_PRIORITY - 1);
                    return thread;
                }
            });
            if (PhoneUtil.getPhoneSDK() >= 9) {
                mDefaultExecutor.allowCoreThreadTimeOut(true);
            }
        }
        return mDefaultExecutor;
    }

    /**
     * 执行异步任务
     *
     * @param threadPoolExecutor 任务执行的线程池
     * @param params             任务需要的参数
     */
    public final void execute(ThreadPoolExecutor threadPoolExecutor, Params... params) {
        onPreExecute();
        mWorker.mParams = params;
        if (threadPoolExecutor == null) {
            getDefaultThreadPoolExecutor().execute(mFuture);
        } else {
            threadPoolExecutor.execute(mFuture);
        }
    }

    /**
     * 在 UI 线程，在调用 onInBackground 方法前执行
     */
    protected void onPreExecute() {

    }

    private abstract static class WorkerRunnable<Params, Result> implements Callable<Result> {
        Params[] mParams;
    }

    private static class UserTaskResult<Data> {
        final Data[] mData;
        final UserTask mTask;

        private UserTaskResult(UserTask mTask, Data... mData) {
            this.mData = mData;
            this.mTask = mTask;
        }
    }
}
