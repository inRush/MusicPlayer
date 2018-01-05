package me.inrush.mediaplayer.media.bean;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;

import java.text.DecimalFormat;
import java.util.Date;

/**
 * @author inrush
 * @date 2017/12/18.
 */

public class Media implements Parcelable {
    private int id;
    private String name;
    private String size;
    private String date;
    private Uri path;
    private Bitmap thumb;
    private String artist;
    private static DecimalFormat sSizeDf = new DecimalFormat("0.00");

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = sSizeDf.format(size * 1.0 / 1024 / 1024).concat("M");
    }

    public String getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = DateFormat.format("yyyy.MM.dd kk:mm", date).toString();
    }

    public Uri getPath() {
        return path;
    }

    public void setPath(Uri path) {
        this.path = path;
    }

    public Bitmap getThumb() {
        return thumb;
    }

    public void setThumb(Bitmap thumb) {
        this.thumb = thumb;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.name);
        dest.writeString(this.size);
        dest.writeString(this.date);
        dest.writeParcelable(this.path, flags);
        dest.writeParcelable(this.thumb, flags);
        dest.writeString(this.artist);
    }

    public Media() {
    }

    protected Media(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.size = in.readString();
        this.date = in.readString();
        this.path = in.readParcelable(Uri.class.getClassLoader());
        this.thumb = in.readParcelable(Bitmap.class.getClassLoader());
        this.artist = in.readString();
    }

    public static final Parcelable.Creator<Media> CREATOR = new Parcelable.Creator<Media>() {
        @Override
        public Media createFromParcel(Parcel source) {
            return new Media(source);
        }

        @Override
        public Media[] newArray(int size) {
            return new Media[size];
        }
    };
}
