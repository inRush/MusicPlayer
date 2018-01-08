package me.inrush.mediaplayer.media.music.base;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

import me.inrush.mediaplayer.media.common.MediaStatus;
import me.inrush.mediaplayer.media.common.PlayThread;
import me.inrush.mediaplayer.media.music.services.MusicService;

/**
 * 音乐进度变化处理器
 *
 * @author inrush
 * @date 2017/12/31.
 */

public class MusicProgressChangeProcessor {
    public static final int PROGRESS_CHANGE_ID = 0x10;
    private WeakReference<MusicService> mMusicPlayer;
    private MusicHandle mHandle;
    private PlayThread mThread;

    /**
     * 音乐进度变化监听器
     */
    private Runnable monitor = new Runnable() {
        @Override
        public void run() {
            try {
                if (mMusicPlayer.get().getStatus() == MediaStatus.START) {
                    Message msg = new Message();
                    msg.what = PROGRESS_CHANGE_ID;
                    msg.arg1 = mMusicPlayer.get().getCurrentProgress();
                    mHandle.sendMessage(msg);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    };

    public MusicProgressChangeProcessor(MusicService player, OnMusicProgressChangeListener listener) {
        mMusicPlayer = new WeakReference<>(player);
        mHandle = new MusicHandle(listener);
        mThread = new PlayThread(monitor);
        mThread.start();
    }

    public void onDestroy() {
        mThread.stopThread();
    }

    public interface OnMusicProgressChangeListener {
        /**
         * 进度变化
         *
         * @param progress 当前进度
         */
        void onProgressChange(int progress);
    }

    /**
     * 处理进度监听器发送的Message
     * 将进度传送出去
     */
    static class MusicHandle extends Handler {
        private OnMusicProgressChangeListener mListener;

        MusicHandle(OnMusicProgressChangeListener listener) {
            mListener = listener;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == PROGRESS_CHANGE_ID) {
                if (mListener != null) {
                    mListener.onProgressChange(msg.arg1);
                }
            }
        }
    }
}
