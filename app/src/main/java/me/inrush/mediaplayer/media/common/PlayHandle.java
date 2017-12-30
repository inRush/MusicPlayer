package me.inrush.mediaplayer.media.common;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import me.inrush.mediaplayer.media.music.MusicPlayer;
import me.inrush.mediaplayer.media.music.OnMusicChangeListener;

/**
 * @author inrush
 * @date 2017/12/27.
 */

public class PlayHandle extends Handler {
    public static final int PROGRESS_CHANGE_ID = 0x10;
    private List<OnMusicChangeListener> mListeners = new ArrayList<>();
    /**
     * 保持对音乐播放器的弱引用
     */
    private WeakReference<MusicPlayer> mMediaPlay;

    public void addListener(OnMusicChangeListener listener) {
        this.mListeners.add(listener);
    }

    public void removeListener(int index) {
        this.mListeners.remove(index);
    }

    public PlayHandle(WeakReference<MusicPlayer> player) {
        this.mMediaPlay = player;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == PROGRESS_CHANGE_ID) {
            for (OnMusicChangeListener listener : mListeners) {
                if (listener != null) {
                    // 通知进度变化
                    listener.onProgressChange(msg.arg1);
                }
            }
            if (msg.arg1 == mMediaPlay.get().getDuration()) {
                // 自动下一首
                mMediaPlay.get().nextMusic();
            }
        }
    }
}
