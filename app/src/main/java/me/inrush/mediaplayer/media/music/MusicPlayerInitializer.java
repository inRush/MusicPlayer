package me.inrush.mediaplayer.media.music;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

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
    private static Intent sIntent;

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mPlayer = ((MusicService.MusicBinder) service).getPlayer();
            if (mListener != null) {
                mListener.onBindComplete(mPlayer);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mPlayer = null;
        }
    };

    public void bindToService() {
//        Intent intent = new Intent(App.getInstance(), MusicService.class);
        mContext.bindService(sIntent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    public void onDestroy() {
        mContext.unbindService(mServiceConnection);
    }

    public MusicPlayerInitializer(Context context, OnServiceBindCompleteListener listener) {
        mContext = context;
        mListener = listener;
    }

    public static void init(Context context) {
        sIntent = new Intent(context, MusicService.class);
        context.startService(sIntent);
    }
}
