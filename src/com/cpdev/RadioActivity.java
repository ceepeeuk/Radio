package com.cpdev;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.*;

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
                                    record(source);
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
            builder.create().show();
        }
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void play(final String uri) {
        if (playerServiceBound) {
            if (playerService.alreadyPlaying()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Stop playing current station?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "Stopping play");
                                playerService.stopPlaying();
                                setStatus("Buffering");
                                playerService.startPlaying(RadioActivity.this, uri);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                builder.create().show();

            } else {
                Log.d(TAG, "Starting play");
                playerService.startPlaying(this, uri);
                updateUIForPlaying(true, "Buffering");
            }
        } else {
            Log.e(TAG, "Playerservice unbound so cannot start playing");
        }
    }

    public void record(final String uri) {
        if (recorderServiceBound) {
            if (recorderService.alreadyRecording()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Stop recording current station?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "Stopping recording");
                                recorderService.stopRecording(RadioActivity.this);
                                recorderService.startRecording(RadioActivity.this, uri);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                builder.create().show();

            } else {
                Log.d(TAG, "Starting recording");
                recorderService.startRecording(this, uri);
                updateUIForRecording(true);
            }
        }
    }

    public void updateUIForPlaying(boolean playingNow, String status) {
        if (playingNow) {
            setStatus(status);
        } else {
            setStatus(status);
        }
    }

    public void updateUIForRecording(boolean recordingNow) {
        if (recordingNow) {
            setStatus("Recording");
        }
    }

    public void setStatus(String message) {
        TextView txtStatus = (TextView) findViewById(R.id.txt_status);
        txtStatus.setText(message);
        Log.d(TAG, "Status set to: " + message);
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