package com.cpdev;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RadioActivity extends Activity {

    Button btnRecord;
    boolean playingNow = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    public void setStatus(String message) {
        TextView txtStatus = (TextView) findViewById(R.id.txt_status);
        txtStatus.setText(message);
    }

    public void playClick(View playButton) {
        if (playingNow) {
            setStatus("Stopping...");
            //mediaPlayer.stop();
            stopService(new Intent("com.cpdev.RadioService"));
            //btnPlay.setText(R.string.btn_play);
            playingNow = false;
        } else {
            setStatus("Starting..");
            startService(new Intent("com.cpdev.RadioService"));
            setStatus("Playing...");
            playingNow = true;
        }
    }

    public void recordClick(View recordButton) {
        setStatus("Recording...");
    }


}