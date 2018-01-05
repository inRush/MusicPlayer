package me.inrush.mediaplayer.media.music.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.lang.ref.WeakReference;

import me.inrush.mediaplayer.media.music.listeners.OnMusicChangeListener;
import me.inrush.mediaplayer.media.music.services.MusicAction;
import me.inrush.mediaplayer.media.music.services.MusicService;

/**
 * 公用的音乐变化事件接受者
 *
 * @author inrush
 * @date 2017/12/31.
 */

public class MusicBroadcastReceiver extends BroadcastReceiver {
    private OnMusicChangeListener mMusicChangeListener;
    private WeakReference<MusicService> mMusicPlayer;

    public MusicBroadcastReceiver(MusicService musicPlayer) {
        mMusicPlayer = new WeakReference<>(musicPlayer);
    }

    public void setMusicChangeListener(OnMusicChangeListener listener) {
        this.mMusicChangeListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mMusicChangeListener == null) {
            return;
        }
        String action = intent.getAction();
        if (MusicAction.MUSIC_CHANGE.equals(action)) {
            // 切歌事件
            mMusicChangeListener.onMusicChange(mMusicPlayer.get().getCurrentMusic());
        } else if (MusicAction.MUSIC_LIST_COUNT_CHANGE.equals(action)) {
            // 音乐列表数量变化事件
            mMusicChangeListener.onMusicPlayListCountChange(mMusicPlayer.get().getMusicCount());
        } else if (MusicAction.MUSIC_PLAY_MODE_CHANGE.equals(action)) {
            // 音乐播放器播放模式变化
            mMusicChangeListener.onMusicPlayModeChange(mMusicPlayer.get().getPlayMode());
        } else if (MusicAction.MUSIC_PLAY_STATUS_CHANGE.equals(action)) {
            // 音乐播放器播放状态变化
            mMusicChangeListener.onPlayerStatusChange(mMusicPlayer.get().getStatus());
        }
    }
}
