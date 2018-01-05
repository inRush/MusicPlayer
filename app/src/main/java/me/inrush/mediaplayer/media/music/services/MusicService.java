package me.inrush.mediaplayer.media.music.services;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.inrush.mediaplayer.App;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.common.MediaStatus;
import me.inrush.mediaplayer.media.music.MusicNotificationManager;
import me.inrush.mediaplayer.media.music.base.MusicPlayMode;

/**
 * 音乐服务
 *
 * @author inrush
 * @date 2017/12/30.
 */

public class MusicService extends Service
        implements AudioManager.OnAudioFocusChangeListener {

    private static final int MUSIC_SERVICE_ID = 0x11;
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
     * 音乐通知栏管理器
     */
    private MusicNotificationManager mNotification;
    /**
     * 指令接收器
     */
    private MusicPlayerBroadcastReceiver mReceiver;
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
     * 接受者是否已经注册
     */
    private boolean isReceiverRegister = false;
    /**
     * 音频管理服务
     */
    private AudioManager mAudioManager;
    /**
     * 当前的音乐播放器的焦点被夺取以后,是否应该获取焦点继续播放音乐
     * True:被短暂夺取,焦点恢复以后,可以继续播放音乐
     * False:被长时间夺取焦点,焦点恢复以后,不可以继续播放音乐
     */
    private boolean shouldContinuePlay = false;
    /**
     * Android 8.0 以上 获取音频焦点方法必须的
     */
    private AudioAttributes mPlayerAttribute = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build();
    /**
     * Android 8.0以上,获取音频焦点必须
     */
    private AudioFocusRequest mAudioFocusRequest;


    /**
     * 更新通知栏
     */
    private void updateNotification() {
        if (mNotification == null) {
            mNotification = new MusicNotificationManager(App.getInstance());
        }
        if (getCurrentMusic() != null) {
            Notification notification = mNotification.initNotification(this);
            /*
              使用播放器service来启动notification
              1. 可以有效的降低service被系统杀死的概率
              2. 顺便启动一个notification来控制音乐
             */
            this.startForeground(MUSIC_SERVICE_ID, notification);
        }
    }

    /**
     * 关闭通知栏
     */
    private void closeNotification() {
        this.stopForeground(true);
    }

    /**
     * 请求音频焦点
     * 防止多个声音同时播放
     */
    private void requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (mAudioFocusRequest == null) {
                mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                        .setAudioAttributes(mPlayerAttribute)
                        .setAcceptsDelayedFocusGain(true)
                        .setWillPauseWhenDucked(true)
                        .setOnAudioFocusChangeListener(this)
                        .build();
            }
            mAudioManager.requestAudioFocus(mAudioFocusRequest);
        } else {
            mAudioManager.requestAudioFocus(this,
                    AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mPlayer = new MediaPlayer();
        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // 注册事件接收者
        registerMusicReceiver();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        }
        if (!isReceiverRegister) {
            registerMusicReceiver();
        }
        return START_STICKY;
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
     * 在添加音乐的时候,或者播放模式发生改变的时候要对播放列表进行重置
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
            initPlayer();
        }
    }

    /**
     * 初始化播放器的监听器
     * 初始化内容:
     * 1.音乐播放器音乐准备完成监听器
     * 2.音乐播放器歌曲完成事件
     * 3.音乐播放器错误状态监听器
     * 4.设置音乐播放器的歌曲
     */
    private void initPlayer(Uri musicUri) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (shouldStart) {
                    // 请求获取焦点
                    requestAudioFocus();
                    mp.start();
                    shouldStart = false;
                }
                // 设置播放器初始化完成
                mIsPlayerInit = true;
                // 更新通知栏
                updateNotification();
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
            if (musicUri != null) {
                mPlayer.setDataSource(App.getInstance(), musicUri);
                mPlayer.prepareAsync();
            }
        } catch (Exception e) {
            initPlayer(musicUri);
        }
    }

    private void initPlayer() {
        initPlayer(mPlayList.get(0).getPath());
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
            mIsPlayerInit = false;
            mPlayer.setDataSource(App.getInstance(), musicUri);
            mPlayer.prepareAsync();
        } catch (Exception e) {
            // 发生异常,重新创建MusicPlayer
            initPlayer(musicUri);
        }
    }

    /**
     * 列表空的时候执行的方法
     * 1.停止播放器
     * 2.释放播放器,设置播放状态为STOP
     * 3.设置播放器初始化状态为FALSE
     */
    private void recyclePlayer() {
        mPlayer.stop();
        mPlayer.release();
        mStatus = MediaStatus.STOP;
        mPlayer = null;
        mIsPlayerInit = false;
        mCurrentIndex = 0;
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
            initPlayer(mediaUri);
        } else if (mCurrentIndex != index) {
            replaceMusic(mediaUri);
        } else {
            if (!isPlaying()) {
                requestAudioFocus();
                mPlayer.start();
            }
        }
        // 通知音乐发生变化
        if (mCurrentIndex != index) {
            noticeChange(MusicAction.MUSIC_CHANGE);
            mCurrentIndex = index;

        }
        if (mStatus != MediaStatus.START) {
            noticeChange(MusicAction.MUSIC_PLAY_STATUS_CHANGE);
            mStatus = MediaStatus.START;
        }
    }

    /**
     * 播放指定音乐
     *
     * @param music 需要播放的音乐
     */
    public void play(Media music) {
        int index = getPlayListIndex(music.getId());
        if (index == -1) {
            return;
        }
        play(index);
        mStatus = MediaStatus.START;
        noticeChange(MusicAction.MUSIC_PLAY_STATUS_CHANGE);
    }

    /**
     * 切换播放状态
     * 暂停变播放
     * 播放变暂停
     */
    public void play() {
        if (!mIsPlayerInit) {
            return;
        }
        if (!mPlayer.isPlaying()) {
            requestAudioFocus();
            mPlayer.start();
            mStatus = MediaStatus.START;
        } else {
            mPlayer.pause();
            mStatus = MediaStatus.PAUSE;
        }
        noticeChange(MusicAction.MUSIC_PLAY_STATUS_CHANGE);
        updateNotification();
    }

    /**
     * 暂停
     */
    public void pause() {
        if (!mIsPlayerInit) {
            return;
        }
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        }
        mStatus = MediaStatus.PAUSE;
        noticeChange(MusicAction.MUSIC_PLAY_STATUS_CHANGE);
        updateNotification();
    }

    /**
     * 下一首歌
     */
    public void nextMusic() {
        play(mCurrentIndex + 1);
    }

    /**
     * 下一首歌
     */
    public void preMusic() {
        if (mPlayMode == MusicPlayMode.ONE_LOOP) {
            play(mCurrentIndex);
        } else {
            play(mCurrentIndex - 1);
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
        mCurrentIndex = getPlayListIndex(currentMusic.getId());
        // 全部清空了
        if (mPlayListInfo.size() == 0) {
            recyclePlayer();
        }
        noticeChange(MusicAction.MUSIC_LIST_COUNT_CHANGE);
    }

    /**
     * 清空播放列表
     */
    public void cleanPlayList() {
        mPlayListInfo.clear();
        mPlayList.clear();
        recyclePlayer();
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

    /**
     * 改变播放器的播放模式
     * if mode is list-loop,the mode will be set to one-loop
     * if mode is one-loop,the mode will be set to random
     * if mode is random,the mode will be set to list-loop
     */
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
        try {
            return mIsPlayerInit && mPlayer.isPlaying();
        } catch (IllegalStateException e) {
            Media music = getCurrentMusic();
            if (music != null) {
                initPlayer(music.getPath());
                return mPlayer.isPlaying();
            } else {
                initPlayer(null);
                return false;
            }
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        // 获得音频焦点
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN && shouldContinuePlay) {
            if (!mIsPlayerInit) {
                initPlayer(getCurrentMusic().getPath());
            } else {
                play();
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            shouldContinuePlay = false;
            // 长时间失去焦点
            pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            shouldContinuePlay = true;
            // 短暂失去焦点
            pause();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            shouldContinuePlay = true;
            // 短暂失去焦点
            pause();
        }
    }


    /**
     * the music service binder
     * through this binder to get a music service
     */
    public class MusicBinder extends Binder {
        public MusicService getPlayer() {
            return MusicService.this;
        }
    }

    /**
     * 注册音乐通知栏指令的监听器
     * 还有耳机拔出事件的指令监听
     */
    private void registerMusicReceiver() {
        mReceiver = new MusicPlayerBroadcastReceiver(this);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addAction(MusicAction.MUSIC_PLAY_START);
        intentFilter.addAction(MusicAction.MUSIC_PLAY_PAUSE);
        intentFilter.addAction(MusicAction.MUSIC_PLAY_NEXT);
        intentFilter.addAction(MusicAction.MUSIC_PLAY_PRE);
        intentFilter.addAction(MusicAction.MUSIC_NOTIFICATION_CLOSE);
        registerReceiver(mReceiver, intentFilter);
        isReceiverRegister = true;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
            mAudioFocusRequest = null;
        } else {
            mAudioManager.abandonAudioFocus(this);
        }
        // 重启service
        Intent localIntent = new Intent(this, MusicService.class);
        this.startService(localIntent);
        super.onDestroy();
    }

    /**
     * 通知栏控制音乐时,音乐播放器接受指令的接收器
     */
    public class MusicPlayerBroadcastReceiver extends BroadcastReceiver {

        private WeakReference<MusicService> mPlayer;

        public MusicPlayerBroadcastReceiver(MusicService player) {
            mPlayer = new WeakReference<>(player);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(action)) {
                // 接收到耳机拔出事件,暂停播放
                mPlayer.get().pause();
            } else if (MusicAction.MUSIC_PLAY_START.equals(action)) {
                // 播放音乐
                mPlayer.get().play();
            } else if (MusicAction.MUSIC_PLAY_PAUSE.equals(action)) {
                // 暂停音乐
                mPlayer.get().pause();
            } else if (MusicAction.MUSIC_PLAY_NEXT.equals(action)) {
                // 下一首音乐
                mPlayer.get().nextMusic();
            } else if (MusicAction.MUSIC_PLAY_PRE.equals(action)) {
                // 上一首音乐
                mPlayer.get().preMusic();
            } else if (MusicAction.MUSIC_NOTIFICATION_CLOSE.equals(action)) {
                // 音乐通知栏被关闭
                mPlayer.get().pause();
                mPlayer.get().setCurrentProgress(0);
                mPlayer.get().closeNotification();
            }
        }
    }
}
