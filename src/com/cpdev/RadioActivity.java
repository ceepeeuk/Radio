package com.cpdev;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.cpdev.filehandling.M3uHandler;
import com.cpdev.filehandling.PlsHandler;
import com.cpdev.utils.StringUtils;

import java.io.IOException;

public class RadioActivity extends Activity {

    private PlayerService playerService;
    private RecorderService recorderService;
    private boolean playerServiceBound = false;
    private boolean recorderServiceBound = false;
    private Intent playerIntent = new Intent("com.cpdev.PlayerService");
    private Intent recorderIntent = new Intent("com.cpdev.RecorderService");

    private String TAG = "com.cpdev.RadioActivity";

    private static final int STOP_PLAYING = 0;
    private static final int STOP_RECORDING = 1;
    private static final int ADD_FAVOURITE = 2;

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

        final DatabaseHelper dbHelper = new DatabaseHelper(this);

        try {
            dbHelper.createDataBase();
            dbHelper.openDataBase();
        } catch (IOException e) {
            Log.e(TAG, "IOException thrown when trying to access DB", e);
        }

        final Cursor favouritesCursor = dbHelper.getFavourites();

        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.favourite_list_item,
                favouritesCursor,
                new String[]{dbHelper.NAME},
                new int[]{R.id.name_entry});

        ListView lstFavourites = (ListView) findViewById(R.id.lst_favourites);

        lstFavourites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                favouritesCursor.moveToPosition(pos);
                decideStreamOption(new RadioDetails(favouritesCursor.getString(1), null, favouritesCursor.getString(2)));
            }
        });

        lstFavourites.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, final long id) {
                favouritesCursor.moveToPosition(pos);

                Dialog dialog = new AlertDialog.Builder(view.getContext())
                        .setMessage("Delete " + favouritesCursor.getString(1))
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "Deleting " + favouritesCursor.getString(1));
                                dbHelper.deleteFavourite(id);
                                favouritesCursor.requery();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();

                return false;
            }
        });

        lstFavourites.setAdapter(adapter);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, STOP_PLAYING, Menu.NONE, "Stop Playing");
        menu.add(Menu.NONE, STOP_RECORDING, Menu.NONE, "Stop Recording");
        menu.add(Menu.NONE, ADD_FAVOURITE, Menu.NONE, "Add Favourite");
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case STOP_PLAYING:
                if (playerServiceBound && playerService.alreadyPlaying()) {
                    if (recorderServiceBound && !recorderService.alreadyRecording()) {
                        setStatus("Stopped");
                    }
                    playerService.stopPlaying(this);
                }
                return true;

            case STOP_RECORDING:
                if (recorderServiceBound && recorderService.alreadyRecording()) {
                    if (playerServiceBound && !playerService.alreadyPlaying()) {
                        setStatus("Stopped");
                    }
                    recorderService.stopRecording(this);
                }
                return true;

            case ADD_FAVOURITE:

                RadioDetails radioDetails = new RadioDetails();

                if (playerServiceBound && playerService.alreadyPlaying()) {
                    radioDetails = ((RadioApplication) getApplicationContext()).getPlayingStation();
                }

                Intent confirmDetailsIntent = new Intent(RadioActivity.this, ConfirmDetailsActivity.class);
                confirmDetailsIntent.putExtra("RadioDetails", radioDetails);
                startActivity(confirmDetailsIntent);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void goClick(View view) {
        final String source = ((EditText) findViewById(R.id.txt_url)).getText().toString();

        Log.d(TAG, "url is: " + source);

        if (source.isEmpty()) {
            showToast("Please supply a URL");
        } else {
            decideStreamOption(new RadioDetails(null, null, source));
        }
    }

    private void decideStreamOption(RadioDetails radioDetails) {

        if (radioDetails.getPlaylistUrl().endsWith(".pls") || radioDetails.getPlaylistUrl().endsWith(".m3u")) {
            if (radioDetails.getPlaylistUrl().endsWith(".pls")) {
                radioDetails = PlsHandler.parse(radioDetails);
            } else {
                radioDetails = M3uHandler.parse(radioDetails);
            }
        } else {
            radioDetails.setStreamUrl(radioDetails.getPlaylistUrl());
        }

        Log.d(TAG, "RadioDetails:" + radioDetails);

        CharSequence[] goOptions = {"Play", "Record", "Both"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final RadioDetails finalRadioDetails = radioDetails;

        builder.setTitle("Play or Record?")
                .setItems(goOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int item) {
                        switch (item) {
                            case 0:
                                play(finalRadioDetails);
                                break;
                            case 1:
                                record(finalRadioDetails);
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

    private void showToast(String message) {
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void play(final RadioDetails radioDetails) {
        if (playerServiceBound) {
            if (playerService.alreadyPlaying()) {

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Stop playing current station?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "Stopping play");
                                playerService.stopPlaying(null);
                                setStatus("Buffering");
                                playerService.startPlaying(RadioActivity.this, radioDetails);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                builder.create().show();

            } else {
                Log.d(TAG, "Starting play");
                playerService.startPlaying(this, radioDetails);
                updateUIForPlaying(true, "Buffering");
            }
        } else {
            Log.e(TAG, "Playerservice unbound so cannot start playing");
        }
    }

    private void record(final RadioDetails radioDetails) {
        if (recorderServiceBound) {
            if (recorderService.alreadyRecording()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage("Stop recording current station?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(TAG, "Stopping recording");
                                recorderService.stopRecording(RadioActivity.this);
                                recorderService.startRecording(RadioActivity.this, radioDetails);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                builder.create().show();

            } else {
                Log.d(TAG, "Starting recording");
                recorderService.startRecording(this, radioDetails);
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
    }

    private ServiceConnection playerConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            PlayerService.RadioServiceBinder binder = (PlayerService.RadioServiceBinder) iBinder;
            playerService = binder.getService();
            playerServiceBound = true;

            if (playerService.alreadyPlaying()) {
                StringBuilder sb = new StringBuilder("Playing ");
                RadioDetails radioDetails = ((RadioApplication) getApplicationContext()).getPlayingStation();
                if (!StringUtils.IsNullOrEmpty(radioDetails.getStationName())) {
                    sb.append(radioDetails.getStationName());
                }
                updateUIForPlaying(true, sb.toString());
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