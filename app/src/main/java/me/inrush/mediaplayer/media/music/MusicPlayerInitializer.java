package me.inrush.mediaplayer.media.music;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import me.inrush.mediaplayer.App;
import me.inrush.mediaplayer.media.music.listeners.OnServiceBindCompleteListener;
import me.inrush.mediaplayer.media.music.services.MusicService;

/**
 * @author inrush
 * @date 2017/12/31.
 */

public class MusicPlayerInitializer {
    private Context mContext;
    private MusicService mPlayer;
    private OnServiceBindCompleteListener mListener;
    private boolean isBinded = false;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // 绑定成功
            mPlayer = ((MusicService.MusicBinder) service).getPlayer();
            if (mListener != null) {
                // 回调监听器
                mListener.onBindComplete(mPlayer);
            }
            isBinded = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayer = null;
        }
    };

    /**
     * 绑定到音乐播放器
     */
    public void bindToService() {
        Intent intent = new Intent(App.getInstance(), MusicService.class);
        mContext.bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    /**
     * 销毁绑定
     */
    public void onDestroy() {
        if (isBinded) {
            mContext.unbindService(mServiceConnection);
        }
        isBinded = false;
    }

    public MusicPlayerInitializer(Context context, OnServiceBindCompleteListener listener) {
        mContext = context;
        mListener = listener;
    }

    /**
     * 初始化全局播放器
     * 在{@link App} 中调用
     *
     * @param context 上下文
     */
    public static void init(Context context) {
        Intent intent = new Intent(context, MusicService.class);
        context.startService(intent);
    }
}
