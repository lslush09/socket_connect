package cg.smart.remote_cloud.thread;

/**
 * author: shun
 * created on: 2017/9/14 16:43
 * description:
 */
public abstract class CgAbstractRunnable implements CgThreadRunnable{
    private boolean running = true;
    private Object lock = this;
    protected long signal = 0;

    public CgAbstractRunnable() {
    }

    public CgAbstractRunnable(long signal) {
        this.signal = signal;
    }

    public CgAbstractRunnable(Object lock,long signal) {
        this.lock = lock;
        this.signal = signal;
    }

    public Object getLock() {
        return lock;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void close() {
        setRunning(false);
    }
}
