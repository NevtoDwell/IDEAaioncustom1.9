package mw.utils.threading;

import com.mw.threading.CancellationToken;
import com.mw.threading.ManualResetEvent;
import com.ne.gs.utils.ThreadPoolManager;

import java.util.concurrent.ScheduledFuture;

public class Task implements Runnable {

    private static final CancellationToken EmptyToken = new CancellationToken();

    private final ManualResetEvent _mutex = new ManualResetEvent(false);

    private final CancellationToken _semaphore;

    private final Runnable _runnable;

    private ScheduledFuture<?> _task;

    public Task(CancellationToken cancellationToken, Runnable runnable) {
        _semaphore = cancellationToken;
        _runnable = runnable;
    }

    public void setTask(ScheduledFuture<?> task) {
        if (_task != null)
            throw new Error("Task was already setted");

        _task = task;
        _mutex.set();
    }

    @Override
    public void run() {

        try {
            _mutex.wait();
        } catch (Exception e) {
            throw new Error(e);
        }

        if (!_semaphore.isCancelled() & !_task.isCancelled()) {

            //try to run parent runnable
            try {
                _runnable.run();
            } catch (Exception e) {

                cancel(); //cancel task if any exception was occured
                throw e;
            }
        } else
            cancel();

    }

    public void cancel() {
        if (!_task.isCancelled()) {
            _task.cancel(false);
        }
    }

    public static Task start(Runnable runnable, long delay){

        Task task = new Task(EmptyToken, runnable);
        task.setTask(ThreadPoolManager.getInstance().schedule(runnable, delay));
        return task;
    }

    public static Task start(Runnable runnable, CancellationToken cancellationToken, long delay){

        Task task = new Task(cancellationToken, runnable);

        try {
            cancellationToken.addAction(task::cancel);
        } catch (InterruptedException e) {
            throw new Error(e);
        }

        task.setTask(ThreadPoolManager.getInstance().schedule(runnable, delay));
        return task;
    }

    public static Task start(Runnable runnable, CancellationToken cancellationToken, long delay, long period){

        Task task = new Task(cancellationToken, runnable);

        try {
            cancellationToken.addAction(task::cancel);
        } catch (InterruptedException e) {
            throw new Error(e);
        }

        task.setTask(ThreadPoolManager.getInstance().scheduleAtFixedRate(runnable, delay, period));
        return task;
    }
}