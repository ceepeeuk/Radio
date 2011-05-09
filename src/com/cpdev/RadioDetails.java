package com.cpdev;

import android.os.Parcel;
import android.os.Parcelable;

public class RadioDetails implements Parcelable {

    private String _stationName;
    private String _streamUrl;
    private String _playlistUrl;

    public RadioDetails(String stationName, String streamUrl, String playlistUrl) {
        setStationName(stationName);
        setStreamUrl(streamUrl);
        setPlaylistUrl(playlistUrl);
    }

    public RadioDetails(Parcel parcel) {
        setStationName(parcel.readString());
        setStreamUrl(parcel.readString());
        setPlaylistUrl(parcel.readString());
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
        this._stationName = stationName;
    }

    public String getStreamUrl() {
        return _streamUrl;
    }

    public void setStreamUrl(String streamUrl) {
        this._streamUrl = streamUrl;
    }

    public String getPlaylistUrl() {
        return _playlistUrl;
    }

    public void setPlaylistUrl(String _playlistUrl) {
        this._playlistUrl = _playlistUrl;
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
        return sb.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(getStationName());
        parcel.writeString(getStreamUrl());
        parcel.writeString(getPlaylistUrl());
    }
}
