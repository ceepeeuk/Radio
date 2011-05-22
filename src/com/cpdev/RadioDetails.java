package com.cpdev;

import android.os.Parcel;
import android.os.Parcelable;

public class RadioDetails implements Parcelable {

    private String _stationName;
    private String _streamUrl;
    private String _playlistUrl;
    private long _duration = 0;
    public long _recordingType = 0;

    // Table not zero indexed, so starts at 1
    public static final int ONE_OFF_SCHEDULED_RECORDING = 1;
    public static final int DAILY_SCHEDULED_RECORDING = 2;
    public static final int WEEKLY_SCHEDULED_RECORDING = 3;


    public RadioDetails(String stationName, String streamUrl, String playlistUrl) {
        setStationName(stationName);
        setStreamUrl(streamUrl);
        setPlaylistUrl(playlistUrl);
    }

    public RadioDetails(String name, String streamUrl, String playlistUrl, long duration, long recordingType) {
        this(name, streamUrl, playlistUrl);
        setDuration(duration);
        setRecordingType(recordingType);
    }

    public RadioDetails(Parcel parcel) {
        setStationName(parcel.readString());
        setStreamUrl(parcel.readString());
        setPlaylistUrl(parcel.readString());
        setDuration(parcel.readLong());
        setRecordingType(parcel.readLong());
    }

    public RadioDetails() {
        setStationName("");
        setStreamUrl("");
        setPlaylistUrl("");
    }

    public static final Creator CREATOR = new Creator() {
        public RadioDetails createFromParcel(Parcel parcel) {
            return new RadioDetails(parcel);  //To change body of implemented methods use File | Settings | File Templates.
        }

        public Object[] newArray(int i) {
            return new Object[0];  //To change body of implemented methods use File | Settings | File Templates.
        }
    };


    public String getStationName() {
        return _stationName;
    }

    public void setStationName(String stationName) {
        _stationName = stationName;
    }

    public String getStreamUrl() {
        return _streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        _streamUrl = streamUrl;
    }

    public String getPlaylistUrl() {
        return _playlistUrl;
    }

    public void setPlaylistUrl(String playlistUrl) {
        _playlistUrl = playlistUrl;
    }

    public long getDuration() {
        return _duration;
    }

    public void setDuration(long duration) {
        _duration = duration;
    }

    public void setRecordingType(long type) {
        _recordingType = type;
    }

    public long getRecordingType() {
        return _recordingType;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n\nName = ");
        sb.append(getStationName());
        sb.append("\nStreamUrl = ");
        sb.append(getStreamUrl());
        sb.append("\nPlaylistUrl = ");
        sb.append(getPlaylistUrl());
        sb.append("\n");
        sb.append("\nDuration = ");
        sb.append(getDuration());
        sb.append("\n");
        sb.append("\nRecordingType = ");
        sb.append(getRecordingType());
        sb.append("\n");
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getStationName());
        parcel.writeString(getStreamUrl());
        parcel.writeString(getPlaylistUrl());
        parcel.writeLong(getDuration());
        parcel.writeLong(getRecordingType());
    }
}
