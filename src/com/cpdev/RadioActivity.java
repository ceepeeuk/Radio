package com.cpdev;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class RadioActivity extends Activity {

    Button btnPlay;
    Button btnRecord;
    MediaPlayer mediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button btnPlay = (Button) findViewById(R.id.play);
        Button btnRecord = (Button) findViewById(R.id.play);

        setContentView(R.layout.main);
    }

    public void setStatus(String message) {
        TextView txtStatus = (TextView) findViewById(R.id.txt_status);
        txtStatus.setText(message);
    }

    public void playClick(View playButton) {
        setStatus("Starting..");
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource("http://podcast.dgen.net:8000/rinseradio");
            setStatus("Buffering...");
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            setStatus("Oops error occurred...");
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        setStatus("Playing...");
    }

    public void stopClick(View playButton) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }

        setStatus("Stopped...");
    }

    public void recordClick(View recordButton) {
        setStatus("Recording...");
    }
}