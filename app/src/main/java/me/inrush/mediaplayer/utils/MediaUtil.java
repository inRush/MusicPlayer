package me.inrush.mediaplayer.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.inrush.mediaplayer.App;
import me.inrush.mediaplayer.R;
import me.inrush.mediaplayer.media.bean.Media;

/**
 * @author inrush
 * @date 2017/12/18.
 */

public class MediaUtil {
    /**
     * 获取专辑封面的Uri
     */
    private static final Uri albumArtUri = Uri.parse("content://media/external/audio/albumart");
    private final static String[] AUDIO_PROJECTION = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.IS_MUSIC
    };

    public static List<Media> getAudioMedias(Context context) {
        List<Media> list = new ArrayList<Media>();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_PROJECTION, null, new String[]{}, MediaStore.Audio.Media.DATE_ADDED);
        if (cursor != null) {
            while (cursor.moveToNext()) {
//                int isMusic = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
//                if (isMusic == 0) {
//                    continue;
//                }

                Media media = new Media();
                // 去除不是mp3的
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                if (!path.contains(".mp3")) {
                    continue;
                }
                int size = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE));
                if (size <= 1000000) {
                    continue;
                }
                media.setSize(size);

                media.setPath(Uri.parse(path));
                media.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                media.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));

                media.setDate(new Date(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)) * 1000));
                media.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                media.setThumb(getAudioAlbum(App.getInstance(), media.getId(), albumId));

                list.add(media);
            }
            cursor.close();
        }
        return list;
    }

    private static Bitmap getThumbBitmap(Context context, String path) {
        if (path == null) {
            return null;
        } else {
            return BitmapFactory.decodeFile(path);
        }
    }

    private static Bitmap getAudioAlbum(Context context, int songId, int albumId) {
        Bitmap bm = null;
        if (albumId < 0 && songId < 0) {
            throw new IllegalArgumentException("Must specify an album or a song id");
        }
        try {
            FileDescriptor fd = null;
            if (albumId < 0) {
                Uri uri = Uri.parse("content://media/external/audio/media/"
                        + songId + "/albumart");
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            } else {
                Uri uri = ContentUris.withAppendedId(albumArtUri, albumId);
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                if (pfd != null) {
                    fd = pfd.getFileDescriptor();
                }
            }
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inSampleSize = 1;
//            // 只进行大小判断
//            options.inJustDecodeBounds = true;
//            // 调用此方法得到options得到图片大小
//            BitmapFactory.decodeFileDescriptor(fd, null, options);
//            // 我们的目标是在800pixel的画面上显示
//            // 所以需要调用computeSampleSize得到图片缩放的比例
//            options.inSampleSize = 100;
//            // 我们得到了缩放的比例，现在开始正式读入Bitmap数据
//            options.inJustDecodeBounds = false;
//            options.inDither = false;
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            //根据options参数，减少所需要的内存
            bm = BitmapFactory.decodeFileDescriptor(fd, null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bm;
    }

    private static Bitmap sDefaultMusicThumb = BitmapFactory.decodeResource(App.getInstance().getResources(),
            R.drawable.placeholder_disk_play_program);

    public static Bitmap getDefaultMusicThumb() {
        return sDefaultMusicThumb;
    }

    private static String getAudioAlbum(Context context, int albumId) {
        String mUriAlbums = "content://media/external/audio/albums";
        String[] projection = new String[]{MediaStore.Audio.Albums.ALBUM_ART};
        Cursor cursor = context.getContentResolver().query(
                Uri.parse(mUriAlbums + "/" + Integer.toString(albumId)), projection,
                null, null, null);
        String albumArt = null;
        if (cursor != null) {
            if (cursor.getCount() > 0 && cursor.getColumnCount() > 0) {
                cursor.moveToNext();
                albumArt = cursor.getString(0);
            }
            cursor.close();
        }
        return albumArt;
    }


    /**
     * 缩略图需要的信息
     */
    private final static String[] THUMB_COLUMNS = new String[]{
            MediaStore.Video.Thumbnails.DATA,
            MediaStore.Video.Thumbnails.VIDEO_ID
    };
    /**
     * 视频信息
     */
    private final static String[] VIDEO_COLUMNS = new String[]{
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED

    };

    /**
     * 获得所有视频文件
     *
     * @param context 上下文
     */
    public static ArrayList<Media> getVideoMedias(Context context) {
        //首先检索sdcard上所有的video
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI, VIDEO_COLUMNS,
                null, null, MediaStore.Video.Media.DATE_ADDED);
        ArrayList<Media> videoList = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Media info = new Media();
                info.setSize(cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media.SIZE)));
                info.setDate(new Date(cursor.getLong(cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)) * 1000));
                info.setPath(Uri.parse(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))));
                info.setName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE)));
                int id = cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID));
                String selection = MediaStore.Video.Thumbnails.VIDEO_ID + "=?";
                String[] selectionArgs = new String[]{id + ""};
                Cursor thumbCursor = context.getContentResolver().query(
                        MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                        THUMB_COLUMNS, selection, selectionArgs, null);
                if (thumbCursor != null && thumbCursor.moveToFirst()) {
                    String path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                    info.setThumb(getThumbBitmap(context, path));
                    thumbCursor.close();
                }
                //然后将其加入到videoList
                videoList.add(info);
            }
            cursor.close();
        }
        return videoList;
    }


}
