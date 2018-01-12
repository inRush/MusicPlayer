package me.inrush.mediaplayer.media.music.base;

import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;

import me.inrush.mediaplayer.common.BaseFragment;
import me.inrush.mediaplayer.media.music.MusicPlayerInitializer;
import me.inrush.mediaplayer.media.music.listeners.OnMusicChangeListener;
import me.inrush.mediaplayer.media.music.listeners.OnServiceBindCompleteListener;
import me.inrush.mediaplayer.media.music.services.MusicAction;
import me.inrush.mediaplayer.media.music.services.MusicService;

/**
 * @author inrush
 * @date 2017/12/31.
 */

public abstract class BaseMusicFragment extends BaseFragment {
    protected MusicService mMusicPlayer;
    private MusicBroadcastReceiver mReceiver;
    private MusicPlayerInitializer mInitializer;
    protected boolean mIsBindComplete = false;
    private OnMusicChangeListener mListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mInitializer = new MusicPlayerInitializer(getContext(), new OnServiceBindCompleteListener() {
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

    /**
     * 获取音乐变化监听器
     *
     * @return {@link OnMusicChangeListener}
     */
    protected abstract OnMusicChangeListener getMusicChangeListener();

    private void initReceiver() {
        mReceiver = new MusicBroadcastReceiver(mMusicPlayer);
        mReceiver.setMusicChangeListener(mListener);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicAction.MUSIC_CHANGE);
        intentFilter.addAction(MusicAction.MUSIC_LIST_COUNT_CHANGE);
        intentFilter.addAction(MusicAction.MUSIC_PLAY_MODE_CHANGE);
        intentFilter.addAction(MusicAction.MUSIC_PLAY_STATUS_CHANGE);
        getContext().registerReceiver(mReceiver, intentFilter);
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
        if (mReceiver != null) {
            getContext().unregisterReceiver(mReceiver);
        }
        mReceiver = null;
        mInitializer.onDestroy();
        mIsBindComplete = false;
    }
}
