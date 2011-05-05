package com.cpdev;

public class RadioDetails {

    private String _stationName;
    private String _streamUrl;

    public RadioDetails(String stationName, String stationUrl, String playlist) {
        setStationName(stationName);
        setStreamUrl(stationUrl);
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

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nName = ");
        sb.append(getStationName());
        sb.append("\nUrl = ");
        sb.append(getStreamUrl());
        return sb.toString();
    }
}
