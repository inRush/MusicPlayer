package me.inrush.mediaplayer.media.music;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

import me.inrush.mediaplayer.R;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.music.pages.MusicActivity;
import me.inrush.mediaplayer.media.music.services.MusicAction;
import me.inrush.mediaplayer.media.music.services.MusicService;

/**
 * 控制音乐通知栏
 *
 * @author inrush
 * @date 2018/1/1.
 */

public class MusicNotificationManager {

    /**
     * android 8.0 notification channel id
     */
    private static final String CHANNEL_ID = "notification_channel_id";
    /**
     * android 8.0 notification channel name
     */
    private static final String CHANNEL_NAME = "HMusicPlayer";

    /**
     * 上下文
     */
    private Context mContext;
    /**
     * notification 管理器
     */
    private NotificationManager mNotificationManager;

    /**
     * notification 构建器
     */
    private NotificationCompat.Builder mNotificationBuilder;
    private long mNotificationPostTime = 0;

    /**
     * 兼容Android8.0
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
        mNotificationManager.createNotificationChannel(channel);
    }

    /**
     * 兼容Android 8.0
     *
     * @param remoteViews notification custom view
     * @param click       点击notification以后执行的Intent
     * @return {@link NotificationCompat.Builder}
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationCompat.Builder getChannelNotification(RemoteViews remoteViews, PendingIntent click) {
        return new NotificationCompat.Builder(mContext, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(click)
                .setWhen(mNotificationPostTime)
                .setContent(remoteViews)
                .setCategory(Notification.CATEGORY_SERVICE);
    }

    /**
     * 获取Android API 26 以下手机的notification builder
     *
     * @param remoteViews notification custom view
     * @param click       点击notification以后执行的Intent
     * @return {@link NotificationCompat.Builder}
     */
    private NotificationCompat.Builder getNotification25(RemoteViews remoteViews, PendingIntent click) {
        return new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(click)
                .setWhen(mNotificationPostTime)
                .setContent(remoteViews);
    }


    public MusicNotificationManager(Context context) {
        this.mContext = context;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }


    /**
     * 初始化一个Notification
     *
     * @return {@link Notification}
     */
    public Notification initNotification(MusicService service) {
        Media media = service.getCurrentMusic();
        RemoteViews remoteViews;
        remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_music_player);
        remoteViews.setTextViewText(R.id.tv_name, media.getName());
        remoteViews.setTextViewText(R.id.tv_singer, media.getArtist());
        remoteViews.setImageViewResource(R.id.iv_play, service.isPlaying() ? R.drawable.playbar_btn_pause : R.drawable.playbar_btn_play);
        if (media.getThumb() != null) {
            remoteViews.setViewVisibility(R.id.iv_thumb, View.VISIBLE);
            remoteViews.setImageViewBitmap(R.id.iv_thumb, media.getThumb());
            remoteViews.setViewVisibility(R.id.iv_placeholder, View.GONE);
        } else {
            remoteViews.setViewVisibility(R.id.iv_placeholder, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.iv_thumb, View.GONE);
        }

        Intent startIntent = new Intent(MusicAction.MUSIC_PLAY_START);
        PendingIntent startPIntent = PendingIntent.getBroadcast(mContext, 0, startIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.iv_play, startPIntent);

        Intent nextIntent = new Intent(MusicAction.MUSIC_PLAY_NEXT);
        PendingIntent nextPIntent = PendingIntent.getBroadcast(mContext, 0, nextIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.iv_next, nextPIntent);

        Intent closeIntent = new Intent(MusicAction.MUSIC_NOTIFICATION_CLOSE);
        PendingIntent closePIntent = PendingIntent.getBroadcast(mContext, 0, closeIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.iv_close, closePIntent);


        // 设置点击通知栏以后打开的Activity
        Intent intent = new Intent(mContext, MusicActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        // 使用 Launcher 模式启动
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        PendingIntent click = PendingIntent.getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (mNotificationPostTime == 0) {
            mNotificationPostTime = System.currentTimeMillis();
        }
        Notification notification;
        if (mNotificationBuilder == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // android 8.0 需要一个channel来启动notification
                createNotificationChannel();
                mNotificationBuilder = getChannelNotification(remoteViews, click);
            } else {
                mNotificationBuilder = getNotification25(remoteViews, click);
            }
            mNotificationBuilder.setShowWhen(false);
        } else {
            mNotificationBuilder.setContent(remoteViews);
        }
        notification = mNotificationBuilder.build();
        // 常驻状态栏,防止被滑动删除掉(有些手机不行)
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        return notification;
    }
}
