package me.inrush.mediaplayer.media.bean;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * @author inrush
 * @date 2017/12/18.
 */

public class Media implements Parcelable {
    private int id;
    private String name;
    private int size;
    private Date date;
    private Uri path;
    private Bitmap thumb;
    private String artist;


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

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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
        dest.writeInt(this.size);
        dest.writeLong(this.date != null ? this.date.getTime() : -1);
        dest.writeParcelable(this.path, flags);
        dest.writeParcelable(this.thumb, flags);
        dest.writeString(this.artist);
    }

    public Media() {
    }

    protected Media(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.size = in.readInt();
        long tmpDate = in.readLong();
        this.date = tmpDate == -1 ? null : new Date(tmpDate);
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
