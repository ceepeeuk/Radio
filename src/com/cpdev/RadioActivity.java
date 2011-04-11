package com.cpdev;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RadioActivity extends Activity {

    RadioService localService;
    boolean mBound = false;
    Button btnRecord;
    Intent intent = new Intent("com.cpdev.RadioService");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void setStatus(String message) {
        TextView txtStatus = (TextView) findViewById(R.id.txt_status);
        txtStatus.setText(message);
    }

    public void playClick(View playButton) {
        if (mBound) {
            if (localService.alreadyPlaying()) {
                localService.stop();
                setupUIForPlaying(false);
            } else {
                startService(intent);
                localService.start(this);
                setupUIForPlaying(true);
            }
        }
    }

    public void recordClick(View recordButton) {
        setStatus("Recording...");
    }

    public void setupUIForPlaying(boolean playingNow) {
        if (playingNow) {
            //setStatus("Playing");
            setPlayButtonText("Stop");
        } else {
            setStatus("");
            setPlayButtonText("Play");
        }
    }

    public void setPlayButtonText(String newText) {
        Button playButton = (Button) findViewById(R.id.play);
        playButton.setText(newText);
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RadioService.RadioServiceBinder binder = (RadioService.RadioServiceBinder) iBinder;
            localService = binder.getService();
            mBound = true;
            setupUIForPlaying(localService.alreadyPlaying());
        }

        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
        }
    };
}