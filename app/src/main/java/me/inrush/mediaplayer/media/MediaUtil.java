package me.inrush.mediaplayer.media;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.inrush.mediaplayer.media.bean.Media;

/**
 * @author inrush
 * @date 2017/12/18.
 */

public class MediaUtil {
    private final static String[] AUDIO_PROJECTION = new String[]{
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.ARTIST

    };

    public static List<Media> getAudioMedias(Context context) {
        List<Media> list = new ArrayList<Media>();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                AUDIO_PROJECTION, null, new String[]{}, MediaStore.Audio.Media.DATE_ADDED);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Media media = new Media();
                media.setId(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)));
                media.setName(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)));
                media.setSize(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)));
                if (media.getSize() <= 10000) {
                    continue;
                }
                media.setDate(new Date(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)) * 1000));
                media.setPath(Uri.parse(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))));
                media.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
                int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                media.setThumb(getThumbBitmap(context, getAudioAlbum(context, albumId)));
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
