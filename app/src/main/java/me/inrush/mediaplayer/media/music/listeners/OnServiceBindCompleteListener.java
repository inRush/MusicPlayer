package me.inrush.mediaplayer.media.music.listeners;

import me.inrush.mediaplayer.media.music.services.MusicService;

/**
 * @author inrush
 * @date 2017/12/31.
 */

public interface OnServiceBindCompleteListener {
    /**
     * 音乐服务绑定结束执行
     *
     * @param player 音乐播放器{@link MusicService}
     */
    void onBindComplete(MusicService player);
}
