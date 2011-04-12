package com.cpdev;

import android.media.MediaPlayer;

public class SingletonMediaPlayer {

    private static MediaPlayer ourInstance = new MediaPlayer();

    public static MediaPlayer getInstance() {
        return ourInstance;
    }

    private SingletonMediaPlayer() {
    }
}

