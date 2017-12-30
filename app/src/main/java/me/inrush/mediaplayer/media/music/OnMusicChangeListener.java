package me.inrush.mediaplayer.media.music;

import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.common.MediaStatus;

/**
 * @author inrush
 * @date 2017/12/27.
 */

public interface OnMusicChangeListener {
    /**
     * 进度变化
     *
     * @param progress 进度
     */
    void onProgressChange(int progress);

    /**
     * 在Music发生变化时,下一个
     *
     * @param media 下一个media
     */
    void onMusicChange(Media media);

    /**
     * 在播放器状态发生改变时执行
     *
     * @param status 播放器的状态
     */
    void onPlayerStatusChange(MediaStatus status);

    /**
     * 音乐播放模式变化触发
     *
     * @param mode 播放模式
     */
    void onMusicPlayModeChange(MusicPlayMode mode);

    /**
     * 歌曲数量变化时触发
     *
     * @param newCount 新的数量
     * @param oldCount 旧的数量
     */
    void onMusicPlayListCountChange(int newCount, int oldCount);
}
