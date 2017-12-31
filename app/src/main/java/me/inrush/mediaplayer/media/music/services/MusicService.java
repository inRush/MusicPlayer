package me.inrush.mediaplayer.media.music.services;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.inrush.mediaplayer.App;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.common.MediaStatus;
import me.inrush.mediaplayer.media.music.base.MusicPlayMode;

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
     * 当前播放器的状态
     */
    private MediaStatus mStatus = MediaStatus.STOP;
    /**
     * 播放模式(默认列表循环)
     */
    private MusicPlayMode mPlayMode = MusicPlayMode.LIST_LOOP;
    /**
     * 通讯器
     */
    private MusicBinder mBinder = new MusicBinder();
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
    /**
     * 自动切换下一首歌
     */
    private Runnable autoNextThread = new Runnable() {
        @Override
        public void run() {
            if (!mIsPlayerInit) {
                return;
            }
            if (mPlayer.getCurrentPosition() == mPlayer.getDuration() - 200) {
                nextMusic();
            }
        }
    };

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
    private int getPlayListIndex(int id) {
        int count = mPlayList.size();
        for (int i = 0; i < count; i++) {
            if (id == mPlayList.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    private int getPlayListInfoIndex(int id) {
        int count = mPlayListInfo.size();
        for (int i = 0; i < count; i++) {
            if (id == mPlayListInfo.get(i).getId()) {
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
            mCurrentIndex = getPlayListIndex(music.getId());
        }
        if (!mIsPlayerInit) {
            if (mPlayer == null) {
                mPlayer = new MediaPlayer();
            }
            initPlayer(mPlayList.get(0).getPath());
        }
    }

    /**
     * 初始化播放器的监听器
     */
    private void initPlayer(Uri musicUri) {
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (shouldStart) {
                    mp.start();
                    shouldStart = false;
                }
                mIsPlayerInit = true;
            }
        });
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mIsPlayerInit = false;
                MusicService.this.mPlayer.release();
                MusicService.this.mPlayer = null;
                if (mPlayMode == MusicPlayMode.ONE_LOOP) {
                    play(mCurrentIndex);
                } else {
                    nextMusic();
                }
            }
        });
        mPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mIsPlayerInit = false;
                MusicService.this.mPlayer.release();
                MusicService.this.mPlayer = null;
                play(mCurrentIndex);
                return true;
            }
        });
        try {
            mPlayer.setDataSource(App.getInstance(), musicUri);
            mPlayer.prepareAsync();
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
            mIsPlayerInit = false;
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
        int index = getPlayListIndex(music.getId());
        if (index == -1) {
            return;
        }
        play(index);
        mStatus = MediaStatus.START;
        noticeChange(MusicAction.MUSIC_PLAY_STATUS_CHANGE);
    }


    public void play() {
        if (!mIsPlayerInit) {
            return;
        }
        if (!mPlayer.isPlaying()) {
            mPlayer.start();
            mStatus = MediaStatus.START;
        } else {
            mPlayer.pause();
            mStatus = MediaStatus.PAUSE;
        }
        noticeChange(MusicAction.MUSIC_PLAY_STATUS_CHANGE);
    }


    public void pause() {
        if (!mIsPlayerInit) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mStatus = MediaStatus.PAUSE;
        noticeChange(MusicAction.MUSIC_PLAY_STATUS_CHANGE);
    }

    public void nextMusic() {
        play(mCurrentIndex + 1);
    }

    public void preMusic() {
        if (mPlayMode == MusicPlayMode.ONE_LOOP) {
            play(mCurrentIndex);
        } else {
            play(mCurrentIndex - 1);
        }
    }

    private void playListEmpty() {
        mPlayer.stop();
        mPlayer.release();
        mStatus = MediaStatus.STOP;
        mPlayer = null;
        mIsPlayerInit = false;
        mCurrentIndex = 0;
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
        mCurrentIndex = getPlayListIndex(currentMusic.getId());
        // 全部清空了
        if (mPlayListInfo.size() == 0) {
            playListEmpty();
        }
        noticeChange(MusicAction.MUSIC_LIST_COUNT_CHANGE);
    }

    /**
     * 清空播放列表
     */
    public void cleanPlayList() {
        mPlayListInfo.clear();
        mPlayList.clear();
        playListEmpty();
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
        if (mPlayer == null || !mIsPlayerInit) {
            return -1;
        }
        return mPlayer.getDuration();
    }

    public int getCurrentProgress() {
        if (mPlayer == null || !mIsPlayerInit) {
            return -1;
        }
        return mPlayer.getCurrentPosition();
    }

    public MediaStatus getStatus() {
        return mStatus;
    }

    public void setCurrentProgress(int progress) {
        if (progress > mPlayer.getDuration()) {
            return;
        }
        mPlayer.seekTo(progress);
    }

    public int getMusicCount() {
        return mPlayListInfo.size();
    }

    public void changePlayMode() {
        MusicPlayMode mode = MusicPlayMode.LIST_LOOP;
        if (mPlayMode == MusicPlayMode.ONE_LOOP) {
            mode = MusicPlayMode.RANDOM;
        } else if (mPlayMode == MusicPlayMode.LIST_LOOP) {
            mode = MusicPlayMode.ONE_LOOP;
        }
        mPlayMode = mode;
        resetPlayList();
        noticeChange(MusicAction.MUSIC_PLAY_MODE_CHANGE);
    }

    public MusicPlayMode getPlayMode() {
        return mPlayMode;
    }

    public ArrayList<Media> getMusicList() {
        return mPlayListInfo;
    }

    public boolean hasMusic(Media music) {
        return getPlayListInfoIndex(music.getId()) != -1;
    }

    /**
     * 获取音乐的索引
     *
     * @param id              音乐ID
     * @param isPlayListIndex True|False
     * @return isPlayListIndex-true:取播放列表的Index,
     * isPlayListIndex-false:取原始列表的Index
     */
    public int getMusicIndex(int id, boolean isPlayListIndex) {
        if (isPlayListIndex) {
            return getPlayListIndex(id);
        } else {
            return getPlayListInfoIndex(id);
        }
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
        public MusicService getPlayer() {
            return MusicService.this;
        }
    }

}
