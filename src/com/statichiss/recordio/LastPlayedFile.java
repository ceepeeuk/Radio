package com.statichiss.recordio;

public class LastPlayedFile {
    private String _name;
    private int _currentPosition;

    public LastPlayedFile(String name, int currentPosition) {
        _currentPosition = currentPosition;
        _name = name;
    }

    public String getName() {
        return _name;
    }

    public int getCurrentPosition() {
        return _currentPosition;
    }
}
