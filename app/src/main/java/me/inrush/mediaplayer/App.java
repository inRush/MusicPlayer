package me.inrush.mediaplayer;

import android.annotation.SuppressLint;
import android.app.Application;
import android.widget.Toast;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import me.inrush.mediaplayer.media.music.MusicPlayerInitializer;

/**
 * @author inrush
 * @date 2017/12/18.
 */

public class App extends Application {
    @SuppressLint("StaticFieldLeak")
    private static Application mInstance;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        MusicPlayerInitializer.init(this);
    }

    public static Application getInstance() {
        return mInstance;
    }

    /**
     * 显示一个Toast
     *
     * @param msg 字符串
     */
    public static void showToast(final String msg) {
        // Toast 只能在主线程中显示，所有需要进行线程转换，
        // 保证一定是在主线程进行的show操作
        Run.onUiAsync(new Action() {
            @Override
            public void call() {
                // 这里进行回调的时候一定就是主线程状态了
                Toast.makeText(mInstance, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }
}
