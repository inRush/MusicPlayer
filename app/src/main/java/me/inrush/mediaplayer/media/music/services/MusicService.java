package me.inrush.mediaplayer.media.music.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.inrush.mediaplayer.App;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.music.MusicPlayMode;

/**
 * @author inrush
 * @date 2017/12/30.
 */

public class MusicService extends Service {

    /**
     * 音乐播放器
     */
    private MediaPlayer mPlayer;
    /**
     * 当前的播放器是否初始化
     */
    private boolean mIsPlayerInit = false;
    /**
     * 在播放器准备好音乐以后是否应该开始播放,默认不播放
     */
    private boolean shouldStart = false;
    /**
     * 播放模式(默认列表循环)
     */
    private MusicPlayMode mPlayMode = MusicPlayMode.LIST_LOOP;
    /**
     * 通讯器
     */
    private MusicBinder mBinder = new MusicBinder(this);
    /**
     * 播放列表(用于切换不同的播放模式的列表)
     */
    private ArrayList<Media> mPlayList = new ArrayList<>();
    /**
     * 原始播放列表(用于保存最原始的列表)
     */
    private ArrayList<Media> mPlayListInfo = new ArrayList<>();
    /**
     * 当前播放音乐的索引
     */
    private int mCurrentIndex = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * 广播通知变化
     *
     * @param actionCode {@link MusicAction}
     */
    private void noticeChange(String actionCode) {
        Intent intent = new Intent();
        intent.setAction(actionCode);
        sendBroadcast(intent);
    }

    /**
     * 返回对应ID的Music在播放列表中的索引
     *
     * @param id Music ID
     * @return if exist will return music index in play list else return -1
     */
    private int getMusicIndex(int id) {
        int count = mPlayList.size();
        for (int i = 0; i < count; i++) {
            if (id == mPlayList.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 重置播放列表
     */
    private void resetPlayList() {
        Media music = getCurrentMusic();
        mPlayList.clear();
        mPlayList.addAll(mPlayListInfo);
        if (mPlayMode == MusicPlayMode.RANDOM) {
            Collections.shuffle(mPlayList);
        }
        if (music == null) {
            mCurrentIndex = 0;
        } else {
            mCurrentIndex = getMusicIndex(music.getId());
        }
        if (!mIsPlayerInit) {
            initPlayer(mPlayList.get(0).getPath());
        }
    }

    /**
     * 初始化播放器的监听器
     */
    private void initPlayer(Uri musicUri) {
        mIsPlayerInit = true;
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (shouldStart) {
                    mp.start();
                    shouldStart = false;
                }
            }
        });
        try {
            mPlayer.setDataSource(App.getInstance(), musicUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 替换当前播放的音乐
     *
     * @param musicUri 需要替换的音乐URI
     */
    private void replaceMusic(Uri musicUri) {
        try {
            if (isPlaying()) {
                mPlayer.stop();
            }
            mPlayer.reset();
            mPlayer.setDataSource(App.getInstance(), musicUri);
            mPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            // 发生异常,重新创建MusicPlayer
            mPlayer = new MediaPlayer();
            initPlayer(musicUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 播放音乐
     *
     * @param index 音乐索引
     */
    public void play(int index) {
        if (mPlayList.size() == 0) {
            return;
        }
        if (index >= mPlayList.size()) {
            index = 0;
        }
        if (index < 0) {
            index = mPlayList.size() - 1;
        }

        Uri mediaUri = mPlayList.get(index).getPath();
        shouldStart = true;
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
            initPlayer(mediaUri);
        } else if (mCurrentIndex != index) {
            replaceMusic(mediaUri);
        } else {
            if (!isPlaying()) {
                mPlayer.start();
            }
        }
        mCurrentIndex = index;
        // 通知音乐发生变化
        noticeChange(MusicAction.MUSIC_CHANGE);
    }

    public void play(Media music) {
        int index = getMusicIndex(music.getId());
        if (index == -1) {
            return;
        }
        play(index);
    }

    public void play() {
        if (!mIsPlayerInit) {
            return;
        }
        if (!mPlayer.isPlaying()) {
            mPlayer.start();
        }
    }

    public void pause() {
        if (!mIsPlayerInit) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
    }

    /**
     * 移除音乐列表
     *
     * @param music 需要移除的音乐
     */
    public void removeMusic(Media music) {
        if (!mIsPlayerInit) {
            return;
        }
        // 移除的音乐是当前播放的,那么切换下一首
        if (getCurrentMusic() == music) {
            play(mCurrentIndex + 1);
        }
        Media currentMusic = getCurrentMusic();
        mPlayListInfo.remove(music);
        mPlayList.remove(music);
        // 更新当前索引
        mCurrentIndex = getMusicIndex(currentMusic.getId());
        // 全部清空了
        if (mPlayListInfo.size() == 0) {
            mPlayer.stop();
            mCurrentIndex = 0;
        }
        noticeChange(MusicAction.MUSIC_LIST_COUNT_CHANGE);
    }

    public void addMusic(Media music) {
        mPlayListInfo.add(music);
        resetPlayList();
        noticeChange(MusicAction.MUSIC_LIST_COUNT_CHANGE);
    }

    public void addMusics(List<Media> musics) {
        mPlayListInfo.addAll(musics);
        resetPlayList();
        noticeChange(MusicAction.MUSIC_LIST_COUNT_CHANGE);
    }

    public Media getCurrentMusic() {
        if (mPlayList.size() == 0) {
            return null;
        }
        return mPlayList.get(mCurrentIndex);
    }

    public int getDuration() {
        if (mPlayer == null) {
            return -1;
        }
        return mPlayer.getDuration();
    }

    public int getCurrentProgress() {
        if (mPlayer == null) {
            return -1;
        }
        return mPlayer.getCurrentPosition();
    }


    /**
     * 获取播放器是否在播放
     *
     * @return True | False
     */
    public boolean isPlaying() {
        return mIsPlayerInit && mPlayer.isPlaying();
    }


    public class MusicBinder extends Binder {

        private WeakReference<MusicService> mService;

        public MusicBinder(MusicService service) {
            mService = new WeakReference<>(service);
        }

        public void play(Media music) {
            mService.get().play(music);
        }

        public void play() {
            mService.get().play();
        }

        public void pause() {
            mService.get().pause();
        }

        public void addMusic(Media music) {
            mService.get().addMusic(music);
        }

        public void addMusic(List<Media> musics) {
            mService.get().addMusics(musics);
        }

        public void removeMusic(Media music) {
            mService.get().removeMusic(music);
        }

        public boolean hasMusic(Media music) {
            return mService.get().getMusicIndex(music.getId()) != -1;
        }

        public int getDuration() {
            return mService.get().getDuration();
        }

        public int getCurrentProgress() {
            return mService.get().getCurrentProgress();
        }

        public MusicPlayMode getPlayMode() {
            return mService.get().mPlayMode;
        }

        public ArrayList<Media> getMusicList() {
            return mService.get().mPlayListInfo;
        }

        public int getMusicCount() {
            return mService.get().mPlayListInfo.size();
        }
    }

}
