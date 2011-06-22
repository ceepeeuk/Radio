package com.statichiss.recordio;

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
import com.statichiss.R;
import com.statichiss.recordio.recording.RecorderService;
import com.statichiss.recordio.utils.StringUtils;

import java.io.IOException;

public class RadioActivity extends Activity {

    private PlayerService playerService;
    private boolean playerServiceBound = false;

    private Intent playerIntent = new Intent("com.statichiss.recordio.PlayerService");

    private String TAG = "com.statichiss.recordio.RadioActivity";

    //private static final int STOP_PLAYING = 0;
    //private static final int STOP_RECORDING = 1;
    private static final int ADD_FAVOURITE = 2;
    private static final int SCHEDULED_RECORDINGS = 3;
    private static final int RECORDINGS = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    public void onStart() {

        super.onStart();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        if (preferences.getBoolean(getString(R.string.first_run_flag), true)) {
            ShowFirstRunPopUp();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getString(R.string.first_run_flag), false);
            editor.commit();
        }

        bindService(playerIntent, playerConnection, Context.BIND_AUTO_CREATE);
        final DatabaseHelper dbHelper = new DatabaseHelper(this);

        try {
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

        final EditText url = (EditText) findViewById(R.id.txt_url);
        url.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                url.setText("");
            }
        });

        findViewById(R.id.main_stop_playing_btn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (playerServiceBound && playerService.alreadyPlaying()) {
                    updateUIForPlaying(false, "");
                    playerService.stopPlaying(RadioActivity.this);
                    findViewById(R.id.main_stop_playing_btn).setEnabled(false);
                }
            }
        });

        findViewById(R.id.main_stop_recording_btn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (RecorderService.alreadyRecording()) {
                    updateUIForRecording(false, "");
                    RecorderService.cancelRecording();
                    findViewById(R.id.main_stop_recording_btn).setEnabled(false);
                }
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
                                try {
                                    dbHelper.openDataBase();
                                } catch (IOException e) {
                                    Log.e(TAG, "Unable to open db to delete favourite", e);
                                }
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
        //dbHelper.close();
    }

    private void ShowFirstRunPopUp() {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.first_run_popup);
        dialog.setTitle(getString(R.string.app_name));
        dialog.setCancelable(true);

        TextView popUpText = (TextView) dialog.findViewById(R.id.popUpText);
        popUpText.setText(R.string.first_run_text);

        Button cancelButton = (Button) dialog.findViewById(R.id.popUpCancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
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
//        menu.add(Menu.NONE, STOP_PLAYING, Menu.NONE, "Stop Playing");
//        menu.add(Menu.NONE, STOP_RECORDING, Menu.NONE, "Stop Recording");
        menu.add(Menu.NONE, ADD_FAVOURITE, Menu.NONE, "Add Favourite");
        menu.add(Menu.NONE, SCHEDULED_RECORDINGS, Menu.NONE, "Scheduled Recordings");
        menu.add(Menu.NONE, RECORDINGS, Menu.NONE, "Recordings");
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

//            case STOP_PLAYING:
//                if (playerServiceBound && playerService.alreadyPlaying()) {
//                    updateUIForPlaying(false, "");
//                    playerService.stopPlaying(this);
//                }
//                return true;
//
//            case STOP_RECORDING:
//                if (RecorderService.alreadyRecording()) {
//                    updateUIForRecording(false, "");
//                    RecorderService.cancelRecording();
//                }
//                return true;

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
                return true;

            case RECORDINGS:

                Intent recordingsIntent = new Intent(RadioActivity.this, RecordingsActivity.class);
                startActivity(recordingsIntent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void goClick(View view) {
        final String source = ((EditText) findViewById(R.id.txt_url)).getText().toString();
        Log.d(TAG, "url is: " + source);

        if (StringUtils.IsNullOrEmpty(source)) {
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
                                play(finalRadioDetails);
                                record(finalRadioDetails);
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
                                playerService.stopPlaying(RadioActivity.this);
                                playerService.startPlaying(RadioActivity.this, radioDetails);
                                findViewById(R.id.main_stop_playing_btn).setEnabled(true);
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
                findViewById(R.id.main_stop_playing_btn).setEnabled(true);
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

                            String recordingStatus = getString(R.string.recording_string) + " " + (radioDetails.getStationName() != null ? radioDetails.getStationName() : "");
                            updateUIForRecording(true, recordingStatus);
                            findViewById(R.id.main_stop_recording_btn).setEnabled(true);
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

            String recordingStatus = getString(R.string.recording_string) + " " + (radioDetails.getStationName() != null ? radioDetails.getStationName() : "");
            updateUIForRecording(false, recordingStatus);
            findViewById(R.id.main_stop_recording_btn).setEnabled(true);
        }
    }

    private Intent createRecordingIntent(RadioDetails radioDetails) {
        Intent intent = new Intent("com.statichiss.recordio.recording.RecorderService");
        if (radioDetails != null) {
            intent.putExtra(getString(R.string.radio_details_key), radioDetails);
        }
        return intent;
    }

    public void updateUIForPlaying(boolean playingNow, String status) {
        StringBuilder sb = new StringBuilder();
        String currentStatus = ((TextView) findViewById(R.id.txt_status)).getText().toString();

        if (playingNow) {
            sb.append(status);
            if (currentStatus.contains(getString(R.string.recording_string))) {
                sb.append(" | ");
                sb.append(currentStatus.substring(currentStatus.indexOf(getString(R.string.recording_string))));
            }
        } else {
            if (currentStatus.contains(getString(R.string.recording_string))) {
                sb.append(currentStatus.substring(currentStatus.indexOf(getString(R.string.recording_string))));
            }
        }
        setStatus(sb.toString());
    }

    public void updateUIForRecording(boolean recordingNow, String status) {
        StringBuilder sb = new StringBuilder();
        String currentStatus = ((TextView) findViewById(R.id.txt_status)).getText().toString();

        if (recordingNow) {
            if (currentStatus.startsWith(getString(R.string.playing_string)) || currentStatus.startsWith(getString(R.string.buffering_string))) {
                sb.append(currentStatus);
                sb.append(currentStatus.endsWith(" | ") ? "" : " | ");
            }
            sb.append(status);
        } else {
            if (currentStatus.startsWith(getString(R.string.playing_string)) || currentStatus.startsWith(getString(R.string.buffering_string))) {
                if (currentStatus.contains("|")) {
                    sb.append(currentStatus.substring(0, currentStatus.indexOf("|")));
                } else {
                    sb.append(currentStatus);
                }
            }
            sb.append(status);
        }

        setStatus(sb.toString());
    }

    private void setStatus(String message) {
        TextView txtStatus = (TextView) findViewById(R.id.txt_status);
        txtStatus.setText(message);
    }

    public void reportError(final RadioDetails radioDetails, final Exception e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Sorry, cannot connect to this stream, would you like to send an error report so support can be added please?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder()
                                .append("Exception caught trying to play stream: ")
                                .append(e.getMessage())
                                .append("\n\n")
                                .append(radioDetails);

                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("plain/text");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"statichiss@gmail.com"});
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Stream Error Report");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, stringBuilder.toString());
                        startActivity(Intent.createChooser(emailIntent, "Send Error Report"));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .create().show();
    }

    private ServiceConnection playerConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            PlayerService.RadioServiceBinder binder = (PlayerService.RadioServiceBinder) iBinder;
            playerService = binder.getService();
            playerServiceBound = true;

            if (playerService.alreadyPlaying()) {
                StringBuilder sb = new StringBuilder(getString(R.string.playing_string));
                RadioDetails radioDetails = ((RadioApplication) getApplicationContext()).getPlayingStation();
                if (!StringUtils.IsNullOrEmpty(radioDetails.getStationName())) {
                    sb.append(" ");
                    sb.append(radioDetails.getStationName());
                }
                updateUIForPlaying(true, sb.toString());
            }


            if (RecorderService.alreadyRecording()) {
                RadioDetails radioDetails = ((RadioApplication) getApplicationContext()).getRecordingStation();
                String recordingStatus = new StringBuilder()
                        .append(getString(R.string.recording_string))
                        .append(" ")
                        .append(radioDetails != null && !StringUtils.IsNullOrEmpty(radioDetails.getStationName()) ? radioDetails.getStationName() : "")
                        .toString();
                updateUIForRecording(true, recordingStatus);
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            playerServiceBound = false;
        }
    };
}