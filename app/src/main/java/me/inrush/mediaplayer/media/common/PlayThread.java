package me.inrush.mediaplayer.media.common;

/**
 * @author inrush
 * @date 2017/12/27.
 */

public class PlayThread extends Thread {
    private boolean run = true;
    private Runnable mRunnable;

    @Override
    public synchronized void start() {
        super.start();
        run = true;
    }

    public void stopThread() {
        this.run = false;
    }

    public PlayThread(Runnable runnable) {
        this.mRunnable = runnable;
    }

    @Override
    public void run() {
        super.run();
        while (run) {
            this.mRunnable.run();
        }
    }
}
