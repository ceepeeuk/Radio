package com.cpdev;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class RadioActivity extends Activity {

    private PlayerService playerService;
    private RecorderService recorderService;
    private boolean playerServiceBound = false;
    private boolean recorderServiceBound = false;
    private Intent playerIntent = new Intent("com.cpdev.PlayerService");
    private Intent recorderIntent = new Intent("com.cpdev.RecorderService");

    private String TAG = "com.cpdev.RadioActivity";
    private final CharSequence[] items = {"Play", "Record", "Both"};


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public void onStart() {
        super.onStart();
        bindService(playerIntent, playerConnection, Context.BIND_AUTO_CREATE);
        bindService(recorderIntent, recorderConnection, Context.BIND_AUTO_CREATE);

        ListView lstFavourites = (ListView) findViewById(R.id.lst_favourites);
        ArrayAdapter<String> favArray = new ArrayAdapter<String>(getApplicationContext(), R.layout.simple_list_item_1);
        for (int i = 0; i < 10; i++) {
            favArray.add("Favourite #" + i);
        }
        lstFavourites.setAdapter(favArray);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (playerServiceBound) {
            unbindService(playerConnection);
            playerServiceBound = false;
        }

        if (recorderServiceBound) {
            unbindService(recorderConnection);
            recorderServiceBound = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(playerIntent, playerConnection, Context.BIND_AUTO_CREATE);
        bindService(recorderIntent, recorderConnection, Context.BIND_AUTO_CREATE);
    }

    public void goClick(View view) {
        final String source = ((EditText) findViewById(R.id.txt_url)).getText().toString();
        Log.d(TAG, "url is: " + source);

        if (source.isEmpty()) {
            showToast("Please supply a URL");
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("What shall we do?")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int item) {
                            switch (item) {
                                case 0:
                                    play(source);
                                    break;
                                case 1:
                                    showToast("Record clicked");
                                    break;
                                case 2:
                                    showToast("Both clicked");
                                    break;
                                default:
                                    Log.e(TAG, "Unexpected option returned from dialog, option #" + item);
                                    break;
                            }
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void play(String uri) {
        if (playerServiceBound) {
            if (playerService.alreadyPlaying()) {
                Log.d(TAG, "Stopping play");
                playerService.stopPlaying();
                updateUIForPlaying(false, "Stopped");
            } else {
                Log.d(TAG, "Starting play");
                playerService.startPlaying(this, uri);
                updateUIForPlaying(true, "Buffering");
            }
        }
    }

//    public void recordClick(View recordButton) {
//        if (recorderServiceBound) {
//            if (recorderService.alreadyRecording()) {
//                Log.d(TAG, "Stopping recording");
//                recorderService.stopRecording(this);
//                updateUIForRecording(false);
//            } else {
//                Log.d(TAG, "Starting recording");
//                recorderService.startRecording(this, rinseUri);
//                updateUIForRecording(true);
//            }
//        }
//    }

    public void updateUIForPlaying(boolean playingNow, String status) {
        if (playingNow) {
            setStatus(status);
            setPlayButtonText("Stop");
        } else {
            setStatus(status);
            setPlayButtonText("Play");
        }
    }

    public void updateUIForRecording(boolean recordingNow) {
        if (recordingNow) {
            setStatus("Recording");
            setRecordButtonText("Stop");
        } else {
            setRecordButtonText("Record");
        }
    }

    public void setStatus(String message) {
//        TextView txtStatus = (TextView) findViewById(R.id.txt_status);
//        txtStatus.setText(message);
//        Log.d(TAG, "Status set to: " + message);
    }

    public void setPlayButtonText(String newText) {
//        Button playButton = (Button) findViewById(R.id.play);
//        playButton.setText(newText);
    }

    public void setRecordButtonText(String newText) {
//        Button playButton = (Button) findViewById(R.id.record);
//        playButton.setText(newText);
    }

    private ServiceConnection playerConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            PlayerService.RadioServiceBinder binder = (PlayerService.RadioServiceBinder) iBinder;
            playerService = binder.getService();
            playerServiceBound = true;
            if (playerService.alreadyPlaying()) {
                updateUIForPlaying(true, "Playing");
            } else {
                updateUIForPlaying(false, "");
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            playerServiceBound = false;
        }
    };

    private ServiceConnection recorderConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            RecorderService.RecorderServiceBinder binder = (RecorderService.RecorderServiceBinder) iBinder;
            recorderService = binder.getService();
            recorderServiceBound = true;
            updateUIForRecording(recorderService.alreadyRecording());
        }

        public void onServiceDisconnected(ComponentName componentName) {
            recorderServiceBound = false;
        }
    };
}