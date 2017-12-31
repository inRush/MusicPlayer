package me.inrush.mediaplayer.media.music.base;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;

import me.inrush.mediaplayer.common.BaseActivity;
import me.inrush.mediaplayer.media.music.MusicPlayerInitializer;
import me.inrush.mediaplayer.media.music.listeners.OnMusicChangeListener;
import me.inrush.mediaplayer.media.music.listeners.OnServiceBindCompleteListener;
import me.inrush.mediaplayer.media.music.services.MusicAction;
import me.inrush.mediaplayer.media.music.services.MusicService;

/**
 * @author inrush
 * @date 2017/12/31.
 */

public abstract class BaseMusicActivity extends BaseActivity {
    protected MusicService mMusicPlayer;
    private MusicBroadcastReceiver mReceiver;
    protected boolean mIsBindComplete = false;
    private MusicPlayerInitializer mInitializer;
    private OnMusicChangeListener mListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mInitializer = new MusicPlayerInitializer(this, new OnServiceBindCompleteListener() {
            @Override
            public void onBindComplete(MusicService player) {
                mMusicPlayer = player;
                mIsBindComplete = true;
                initReceiver();
                onServiceBindComplete();
            }
        });
        super.onCreate(savedInstanceState);
        mListener = getMusicChangeListener();
    }

    /**
     * 在服务绑定完成后执行
     */
    protected abstract void onServiceBindComplete();

    protected abstract OnMusicChangeListener getMusicChangeListener();

    private void initReceiver() {
        mReceiver = new MusicBroadcastReceiver(mMusicPlayer);
        mReceiver.setMusicChangeListener(mListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicAction.MUSIC_CHANGE);
        intentFilter.addAction(MusicAction.MUSIC_LIST_COUNT_CHANGE);
        intentFilter.addAction(MusicAction.MUSIC_PLAY_MODE_CHANGE);
        intentFilter.addAction(MusicAction.MUSIC_PLAY_STATUS_CHANGE);
        registerReceiver(mReceiver, intentFilter);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mIsBindComplete) {
            mInitializer.bindToService();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
        mInitializer.onDestroy();
        mIsBindComplete = false;
    }
}
