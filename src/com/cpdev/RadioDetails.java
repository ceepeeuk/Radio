package com.cpdev;

public class RadioDetails {

    private String _stationName;
    private String _streamUrl;
    private String _playlistUrl;

    public RadioDetails(String stationName, String streamUrl, String playlistUrl) {
        setStationName(stationName);
        setStreamUrl(streamUrl);
        setPlaylistUrl(playlistUrl);
    }

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
}
