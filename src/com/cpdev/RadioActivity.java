package com.cpdev;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.cpdev.recording.RecorderService;
import com.cpdev.utils.StringUtils;

import java.io.IOException;

public class RadioActivity extends Activity {

    private PlayerService playerService;
    private boolean playerServiceBound = false;

    private Intent playerIntent = new Intent("com.cpdev.PlayerService");

    private String TAG = "com.cpdev.RadioActivity";

    private static final int STOP_PLAYING = 0;
    private static final int STOP_RECORDING = 1;
    private static final int ADD_FAVOURITE = 2;
    private static final int SCHEDULED_RECORDINGS = 3;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public void onStart() {

        super.onStart();
        bindService(playerIntent, playerConnection, Context.BIND_AUTO_CREATE);
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
                new String[]{DatabaseHelper.FAVOURITES_NAME},
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

                new AlertDialog.Builder(view.getContext())
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
        dbHelper.close();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (playerServiceBound) {
            unbindService(playerConnection);
            playerServiceBound = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bindService(playerIntent, playerConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, STOP_PLAYING, Menu.NONE, "Stop Playing");
        menu.add(Menu.NONE, STOP_RECORDING, Menu.NONE, "Stop Recording");
        menu.add(Menu.NONE, ADD_FAVOURITE, Menu.NONE, "Add Favourite");
        menu.add(Menu.NONE, SCHEDULED_RECORDINGS, Menu.NONE, "Scheduled Recordings");
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case STOP_PLAYING:
                if (playerServiceBound && playerService.alreadyPlaying()) {
                    //if (!RecorderService.alreadyRecording()) {
                    updateUIForPlaying(false, "");
                    //}
                    playerService.stopPlaying(this);
                }
                return true;

            case STOP_RECORDING:
                if (RecorderService.alreadyRecording()) {
                    //if (playerServiceBound && !playerService.alreadyPlaying()) {
                    updateUIForRecording(false, "");
                    //}
                    RecorderService.cancelRecording();
                }
                return true;

            case ADD_FAVOURITE:

                RadioDetails radioDetails = new RadioDetails();

                if (playerServiceBound && playerService.alreadyPlaying()) {
                    radioDetails = ((RadioApplication) getApplicationContext()).getPlayingStation();
                }

                Intent confirmDetailsIntent = new Intent(RadioActivity.this, ConfirmDetailsActivity.class);
                confirmDetailsIntent.putExtra(getString(R.string.radio_details_key), radioDetails);
                startActivity(confirmDetailsIntent);

                return true;

            case SCHEDULED_RECORDINGS:

                Intent scheduledRecordingsIntent = new Intent(RadioActivity.this, ListScheduledRecordingsActivity.class);
                startActivity(scheduledRecordingsIntent);

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
                                //TODO implement do both
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
                                updateUIForPlaying(true, "Buffering");
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

        if (RecorderService.alreadyRecording()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setMessage("Stop recording current station?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d(TAG, "Stopping recording");
                            // Set stop var
                            RecorderService.cancelRecording();
                            // Fire start intent
                            RecorderService.sendWakefulWork(getApplicationContext(), createRecordingIntent(radioDetails));
                            updateUIForRecording(true, "Recording");
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
            builder.create().show();

        } else {
            Log.d(TAG, "Starting recording");
            Intent intent = createRecordingIntent(radioDetails);
            RecorderService.sendWakefulWork(this, intent);
            updateUIForRecording(true, "Recording");
        }
    }

    private Intent createRecordingIntent(RadioDetails radioDetails) {
        Intent intent = new Intent("com.cpdev.recording.RecorderService");
        if (radioDetails != null) {
            intent.putExtra(getString(R.string.radio_details_key), radioDetails);
        }
        return intent;
    }

    public void updateUIForPlaying(boolean playingNow, String status) {
        StringBuilder sb = new StringBuilder();
        sb.append(status);
        if (RecorderService.alreadyRecording()) {
            TextView txtStatus = (TextView) findViewById(R.id.txt_status);
            sb.append(" | ");
            sb.append(txtStatus.getText());
        }
        setStatus(sb.toString());
    }

    public void updateUIForRecording(boolean recordingNow, String status) {
        StringBuilder sb = new StringBuilder();
        if (playerService != null && playerService.alreadyPlaying()) {
            TextView txtStatus = (TextView) findViewById(R.id.txt_status);
            sb.append(txtStatus.getText());
            sb.append(" | ");
        }
        sb.append(status);
        setStatus(sb.toString());
    }

    private void setStatus(String message) {
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
            }


            if (RecorderService.alreadyRecording()) {
                updateUIForRecording(true, "Recording");
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            playerServiceBound = false;
        }
    };
}