package com.example.kaush;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MusicInfo implements Parcelable {
    String title;
    String date;
    String url;
    String emotion = "a";

    public MusicInfo(String title, String date, String url)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date time = new Date();
        String TIME = format.format(time);
        this.title = title;
        this.date = TIME;
        this.url = url;
    }

    public MusicInfo(String title, String date, String url, String emotion){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date time = new Date();
        String TIME = format.format(time);
        this.title = title;
        this.date = TIME;
        this.url = url;
        this.emotion = emotion;
    }

    public MusicInfo(Parcel in)
    {
        title = in.readString();
        date = in.readString();
        url = in.readString();
        emotion = in.readString();
    }

    public static final Creator<MusicInfo> CREATOR = new Creator<MusicInfo>() {
        @Override
        public MusicInfo createFromParcel(Parcel in) {
            return new MusicInfo(in);
        }

        @Override
        public MusicInfo[] newArray(int size) {
            return new MusicInfo[size];
        }
    };

    public String getTitle() { return title; }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getDate() { return date; }
    public void setDate(String date)
    {
        this.date = date;
    }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getEmotion() { return emotion; }
    public void setEmotion(String emotion) { this.emotion = emotion; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(date);
        parcel.writeString(url);
        parcel.writeString(emotion);
    }

}
