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
        Intent intent = new Intent("com.cpdev.RadioService");

        if (playingNow) {
            setStatus("Stopping...");
            stopService(intent);
            setPlayButtonText("Play");
            playingNow = false;
        } else {
            setStatus("Starting..");
            startService(intent);
            setStatus("Playing...");
            setPlayButtonText("Stop");
            playingNow = true;
        }
    }

    public void recordClick(View recordButton) {
        setStatus("Recording...");
    }

    public void setPlayButtonText(String newText) {
        Button playButton = (Button) findViewById(R.id.play);
        playButton.setText(newText);
    }


}