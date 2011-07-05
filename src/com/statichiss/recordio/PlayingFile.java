package com.statichiss.recordio;

public class PlayingFile {
    private int _duration;
    private String _name;

    public PlayingFile(int duration, String name) {
        _duration = duration;
        _name = name;
    }

    public int getDuration() {
        return _duration;
    }

    public String getName() {
        return _name;
    }
}
