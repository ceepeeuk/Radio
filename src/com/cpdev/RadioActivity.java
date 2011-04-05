package com.cpdev;

import android.app.Activity;
import android.media.AsyncPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static android.media.AudioManager.STREAM_MUSIC;

public class RadioActivity extends Activity {

    Button btnPlay;
    Button btnRecord;
    AsyncPlayer mediaPlayer;

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
        mediaPlayer = new AsyncPlayer("");
        mediaPlayer.play(this, Uri.parse("http://podcast.dgen.net:8000/rinseradio"), false, STREAM_MUSIC);
        setStatus("Playing...");
    }

    public void stopClick(View playButton) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }

        setStatus("Stopped...");
    }

    public void recordClick(View recordButton) {
        setStatus("Recording...");
    }


}