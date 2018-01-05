package me.inrush.mediaplayer.media.music.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.qiujuer.genius.kit.handler.Run;

import me.inrush.mediaplayer.App;
import me.inrush.mediaplayer.media.music.MusicPlayerInitializer;
import me.inrush.mediaplayer.media.music.listeners.OnServiceBindCompleteListener;
import me.inrush.mediaplayer.media.music.services.MusicService;

/**
 * 耳机线控指令接收器
 *
 * @author inrush
 * @date 2018/1/5.
 */

public class MediaButtonReceiver extends BroadcastReceiver {
    private MusicService mPlayer;
    private MusicPlayerInitializer mInitializer;
    /**
     * 最后一次执行耳机操作的时间
     */
    private static long mLastOperaTime = 0;
    /**
     * 耳机在当前操作执行的点击次数
     */
    private static int mClickCount = 0;
    private static boolean isRun = false;
    /**
     * 因为不知什么原因下面的onReceive会执行两次,所以这里的次数都要乘2
     */
    private static final int HEADSET_ONCE_CLICK = 2;
    private static final int HEADSET_TWICE_CLICK = 4;
    private static final int HEADSET_THRICE_CLICK = 6;
    /**
     * 两次耳机操作的时间间隔为两秒
     */
    private static final int HEADSET_OPERA_LIMIT = 1000;

    /**
     * 初始化绑定音乐服务
     */
    private synchronized void init() {
        if (mInitializer == null) {
            MusicPlayerInitializer.init(App.getInstance());
            mInitializer = new MusicPlayerInitializer(App.getInstance(), new OnServiceBindCompleteListener() {
                @Override
                public void onBindComplete(MusicService player) {
                    mPlayer = player;
                    process();
                }
            });
            mInitializer.bindToService();
        }
    }

    private void process() {
        // 耳机线按钮点击,这次耳机操作的时间距离上次要超过预设的临界值
        if (!isRun && System.currentTimeMillis() - mLastOperaTime > HEADSET_OPERA_LIMIT) {
            isRun = true;
            // 延迟一秒执行检测点击耳机按键的事件
            Run.getBackgroundHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // 1秒以内点击1次,切换播放状态
                    if (mClickCount == HEADSET_ONCE_CLICK) {
                        mPlayer.play();
                    } else if (mClickCount == HEADSET_TWICE_CLICK) {
                        // 1秒以内点击2次,下一首音乐
                        mPlayer.nextMusic();
                    } else if (mClickCount == HEADSET_THRICE_CLICK) {
                        // 1秒以内点击3次,上一首音乐
                        mPlayer.preMusic();
                    }
                    mClickCount = 0;
                    mLastOperaTime = System.currentTimeMillis();
                    isRun = false;
                }
            }, 1000);
        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mInitializer == null) {
            init();
        } else {
            process();
        }
        // 防止在等待期间设置了mClickCount的值
        if (System.currentTimeMillis() - mLastOperaTime > HEADSET_OPERA_LIMIT) {
            mClickCount++;
        }
    }
}
