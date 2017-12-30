package me.inrush.mediaplayer.media.music;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Message;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.inrush.mediaplayer.App;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.common.MediaListenerUnBinder;
import me.inrush.mediaplayer.media.common.MediaStatus;
import me.inrush.mediaplayer.media.common.PlayHandle;
import me.inrush.mediaplayer.media.common.PlayThread;

/**
 * 音乐播放器
 *
 * @author inrush
 * @date 2017/12/18.
 */

public class MusicPlayer {
    /**
     * 播放进度容差
     */
    private static final int DEVIATION = 200;
    private MediaPlayer mMusicPlayer;
    private boolean isInit = false;
    /**
     * 播放列表
     */
    private ArrayList<Media> mMusicPlayList = new ArrayList<>();
    /**
     * 原始的音乐列表
     */
    private ArrayList<Media> mOriginMusicList = new ArrayList<>();
    private int mCurrentIndex = 0;
    private static MusicPlayer sMusicPlay;
    private PlayHandle mHandler;
    private List<OnMusicChangeListener> mListeners = new ArrayList<>();
    private PlayThread mPlayThread;
    /**
     * 播放模式:{@link MusicPlayMode} 默认列表循环
     * 随机模式 RANDOM
     * 列表循环 LIST_LOOP
     * 单曲循环
     */
    private MusicPlayMode mPlayMode = MusicPlayMode.LIST_LOOP;

    public static MusicPlayer getPlayer() {
        if (sMusicPlay == null) {
            synchronized (MusicPlayer.class) {
                if (sMusicPlay == null) {
                    sMusicPlay = new MusicPlayer();
                }
            }
        }
        return sMusicPlay;
    }

    public void init(Context context, int id) {
        int index = getMusicInPlayListIndex(id);
        Media media = mMusicPlayList.get(index);
        mMusicPlayer = MediaPlayer.create(context, media.getPath());
        mCurrentIndex = index;
    }

    private MusicPlayer() {
        mHandler = new PlayHandle(new WeakReference<>(this));
        mPlayThread = new PlayThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mMusicPlayer == null || getDuration() <= 0) {
                        return;
                    }
                    if (!isPlaying()) {
                        if (getCurrentProgress() >= getDuration() - DEVIATION) {
                            noticeChangeProgress(getDuration());
                        }
                    } else {
                        noticeChangeProgress(getCurrentProgress());
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        mPlayThread.start();
    }

    /**
     * 通知进度变化
     * 保证在主进程
     *
     * @param progress 进度
     */
    private void noticeChangeProgress(int progress) {
        Message message = new Message();
        message.arg1 = progress;
        message.what = PlayHandle.PROGRESS_CHANGE_ID;
        mHandler.sendMessage(message);
    }

    /**
     * 派发监听
     * 歌曲切换的时候触发
     */
    private void dispatchMusicChangeListener() {
        for (OnMusicChangeListener listener : mListeners) {
            if (listener != null) {
                // 通知歌曲切换
                listener.onMusicChange(getCurrentMusic());
            }
        }
    }

    /**
     * 歌曲状态变化的时候触发
     *
     * @param status 歌曲的状态 {@link MediaStatus}
     */
    private void dispatchMusicStatusChangeListener(MediaStatus status) {
        for (OnMusicChangeListener listener : mListeners) {
            if (listener != null) {
                // 通知歌曲切换
                listener.onPlayerStatusChange(status);
            }
        }
    }

    /**
     * 派发播放模式变化事件
     *
     * @param mode {@link MusicPlayMode}
     */
    private void dispatchMusicPlayModeChangeListener(MusicPlayMode mode) {
        for (OnMusicChangeListener listener : mListeners) {
            if (listener != null) {
                // 通知歌曲切换
                listener.onMusicPlayModeChange(mode);
            }
        }
    }

    private void dispatchMusicListCountChangeListener(int newCount, int oldCount) {
        for (OnMusicChangeListener listener : mListeners) {
            if (listener != null) {
                // 通知歌曲切换
                listener.onMusicPlayListCountChange(newCount, oldCount);
            }
        }
    }

    private void initMusicPlayer() {
        isInit = true;
        mMusicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
                dispatchMusicChangeListener();
            }
        });
    }

    /**
     * 播放音乐
     *
     * @param index 音乐在列表中的索引
     */
    private void playMusic(int index) {

        Uri mediaUri = mMusicPlayList.get(index).getPath();
        try {
            if (mMusicPlayer == null) {
                mMusicPlayer = MediaPlayer.create(App.getInstance(), mediaUri);
                initMusicPlayer();
            } else if (mCurrentIndex != index) {
                if (isPlaying()) {
                    mMusicPlayer.stop();
                }
                mMusicPlayer.reset();
                mMusicPlayer.setDataSource(App.getInstance(), mediaUri);
                try {
                    mMusicPlayer.prepareAsync();
                } catch (IllegalStateException e) {
                    mMusicPlayer = MediaPlayer.create(App.getInstance(), mediaUri);
                    initMusicPlayer();
                }
                if (!isInit) {
                    initMusicPlayer();
                }
            } else {
                if (!isPlaying()) {
                    mMusicPlayer.start();
                }
                dispatchMusicChangeListener();
            }
            mCurrentIndex = index;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置播放模式
     *
     * @param mode {@link MusicPlayMode}
     */
    private void setPlayMode(MusicPlayMode mode) {
        if (mode == MusicPlayMode.ONE_LOOP) {
            return;
        }
        Media music = null;
        if (mCurrentIndex != -1 && mMusicPlayList.size() > 0) {
            music = mMusicPlayList.get(mCurrentIndex);
        }
        mMusicPlayList.clear();
        mMusicPlayList.addAll(mOriginMusicList);
        if (mode == MusicPlayMode.RANDOM) {
            Collections.shuffle(mMusicPlayList);
        }
        if (music != null) {
            mCurrentIndex = getMusicInPlayListIndex(music.getId());
        }
    }

    /**
     * 给播放器绑定监听器
     *
     * @param listener {@link OnMusicChangeListener}
     * @return 解绑器 {@link MediaListenerUnBinder}
     */
    public MediaListenerUnBinder bindMusicProgressChangeListener(OnMusicChangeListener listener) {
        this.mListeners.add(listener);
        this.mHandler.addListener(listener);
        MediaListenerUnBinder unBinder = new MediaListenerUnBinder(this.mListeners.size() - 1,
                new MediaListenerUnBinder.OnUnBindListener() {
                    @Override
                    public void onUnBind(int index) {
                        mHandler.removeListener(index);
                        mListeners.remove(index);
                    }
                });
        return unBinder;
    }

    /**
     * 下一首歌
     */
    public void nextMusic() {
        if (mPlayMode == MusicPlayMode.ONE_LOOP) {
            play(mCurrentIndex);
        } else {
            play(mCurrentIndex + 1);
        }
    }

    /**
     * 上一首歌
     */
    public void preMusic() {
        if (mPlayMode == MusicPlayMode.ONE_LOOP) {
            play(mCurrentIndex);
        } else {
            play(mCurrentIndex - 1);
        }
    }

    /**
     * 改变播放模式
     */
    public void changePlayMode() {
        MusicPlayMode mode = MusicPlayMode.LIST_LOOP;
        if (mPlayMode == MusicPlayMode.ONE_LOOP) {
            mode = MusicPlayMode.RANDOM;
        } else if (mPlayMode == MusicPlayMode.LIST_LOOP) {
            mode = MusicPlayMode.ONE_LOOP;
        }
        mPlayMode = mode;
        setPlayMode(mode);
        dispatchMusicPlayModeChangeListener(mode);
    }


    /**
     * 添加音乐到播放列表
     *
     * @param media 音乐
     */
    public void addMusic(Media media) {
        mOriginMusicList.add(media);
        setPlayMode(mPlayMode);
        dispatchMusicListCountChangeListener(mOriginMusicList.size(), mOriginMusicList.size() - 1);
    }

    /**
     * 批量添加音乐到播放列表里
     *
     * @param medias 播放列表
     */
    public void addMusics(List<Media> medias) {
        mOriginMusicList.addAll(medias);
        setPlayMode(mPlayMode);
        dispatchMusicListCountChangeListener(mOriginMusicList.size(), mOriginMusicList.size() - medias.size());
    }

    /**
     * 清空播放列表
     */
    public void cleanPlayList() {
        dispatchMusicListCountChangeListener(0, mOriginMusicList.size());
        mMusicPlayList.clear();
        mOriginMusicList.clear();
        mMusicPlayer.stop();
        mCurrentIndex = 0;
    }

    /**
     * 移除音乐列表
     *
     * @param music 需要移除的音乐
     */
    public void removeMusic(Media music) {
        if (getCurrentMusic() == music) {
            play(mCurrentIndex + 1);
        }
        Media currentMusic = getCurrentMusic();
        mOriginMusicList.remove(music);
        mMusicPlayList.remove(music);
        // 更新当前索引
        mCurrentIndex = getMusicInPlayListIndex(currentMusic.getId());
        if (mOriginMusicList.size() == 0) {
            mMusicPlayer.stop();
            mCurrentIndex = 0;
        }
        // 主要是用来通知更新列表的歌曲数量
        dispatchMusicListCountChangeListener(mOriginMusicList.size(), mOriginMusicList.size() + 1);
    }


    /**
     * 获取音乐在播放列表中的位置
     *
     * @param id 音乐ID
     * @return music index
     */
    public int getMusicInPlayListIndex(int id) {
        for (int i = 0; i < mMusicPlayList.size(); i++) {
            if (id == mMusicPlayList.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    public int getMusicInOriginListIndex(int id) {
        for (int i = 0; i < mOriginMusicList.size(); i++) {
            if (id == mOriginMusicList.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    public void play(Media music) {
        int index = getMusicInPlayListIndex(music.getId());
        if (index == -1) {
            mMusicPlayList.add(music);
        }
        play(index);
    }

    public void play(int index) {
        if (mMusicPlayList.size() == 0) {
            return;
        }
        if (index >= mMusicPlayList.size()) {
            index = 0;
        }
        if (index < 0) {
            index = mMusicPlayList.size() - 1;
        }
        playMusic(index);
        dispatchMusicStatusChangeListener(MediaStatus.START);
    }

    public void play() {
        if (mMusicPlayer == null) {
            init(App.getInstance(), getCurrentMusic().getId());
        }
        mMusicPlayer.start();
        dispatchMusicStatusChangeListener(MediaStatus.START);
    }

    public void pause() {
        if (mMusicPlayer == null) {
            throw new NullPointerException("the music player is null");
        }
        mMusicPlayer.pause();
        dispatchMusicStatusChangeListener(MediaStatus.PAUSE);
    }

    public void switchState() {
        if (isPlaying()) {
            pause();
        } else {
            play();
        }
    }

    public int getDuration() {
        if (mMusicPlayer == null) {
            init(App.getInstance(), getCurrentMusic().getId());
        }
        return mMusicPlayer.getDuration();
    }

    public boolean isPlaying() {
        if (mMusicPlayer == null) {
            init(App.getInstance(), getCurrentMusic().getId());
        }
        return mMusicPlayer.isPlaying();
    }

    public int getCurrentProgress() {
        return mMusicPlayer.getCurrentPosition();
    }

    public int getCurrentMusicInPlayListIndex() {
        return mCurrentIndex;
    }

    public int getCurrentMusicInMusicListIndex() {
        if (getCurrentMusic() == null) {
            return -1;
        }
        return getMusicInOriginListIndex(getCurrentMusic().getId());
    }

    public Media getCurrentMusic() {
        if (mMusicPlayList.size() == 0) {
            return null;
        }
        return mMusicPlayList.get(mCurrentIndex);
    }

    public void setCurrentProgress(int mCurrentProgress) {
        if (mMusicPlayer == null) {
            throw new NullPointerException("the music player is null");
        }
        mMusicPlayer.seekTo(mCurrentProgress);
    }

    public MusicPlayMode getPlayMode() {
        return mPlayMode;
    }

    public ArrayList<Media> getMusicList() {
        return mOriginMusicList;
    }
}
