package com.statichiss.recordio;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.*;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.statichiss.R;
import com.statichiss.recordio.recording.RecorderService;
import com.statichiss.recordio.utils.StringUtils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RadioActivity extends RecordioBaseActivity {

    private String TAG = "com.statichiss.recordio.RadioActivity";
    private AudioManager mAudioManager;
    private ComponentName mRemoteControlReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mRemoteControlReceiver = new ComponentName(getPackageName(), RemoteControlReceiver.class.getName());
    }

    @Override
    public void onStart() {

        super.onStart();
        final RadioApplication radioApplication = (RadioApplication) getApplication();

        SharedPreferences preferences = getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE);
        if (preferences.getBoolean(getString(R.string.first_run_flag), true)) {
            ShowFirstRunPopUp();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(getString(R.string.first_run_flag), false);
            editor.commit();
        }

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
                MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();
                // Bit of a hack looking at status!
                if (mediaPlayer != null && mediaPlayer.isPlaying() || radioApplication.getPlayingStatus().contains("Paused")) {
                    radioApplication.setPlayingStatus("");
                    updateUI();
                    PlayerService.sendWakefulWork(getApplicationContext(), createPlayingIntent(null, RadioApplication.StopPlaying));
                    findViewById(R.id.main_stop_playing_btn).setEnabled(false);
                }
            }
        });

        findViewById(R.id.main_stop_recording_btn).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (RecorderService.alreadyRecording()) {
                    radioApplication.setRecordingStatus("");
                    updateUI();
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

        if (alreadyPlaying()) {
            findViewById(R.id.main_stop_playing_btn).setEnabled(true);
        }

        if (RecorderService.alreadyRecording()) {
            findViewById(R.id.main_stop_recording_btn).setEnabled(true);
        }
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
    public void onPause() {
        super.onPause();
        this.unregisterReceiver(this.updateStatusBroadcastReceiver);
        this.unregisterReceiver(this.sendErrorBroadcastReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter statusIntentFilter = new IntentFilter();
        statusIntentFilter.addAction(getString(R.string.player_service_update_playing_key));
        registerReceiver(this.updateStatusBroadcastReceiver, statusIntentFilter);

        IntentFilter errorIntentFilter = new IntentFilter();
        errorIntentFilter.addAction(getString(R.string.player_service_update_playing_error_key));
        registerReceiver(this.sendErrorBroadcastReceiver, errorIntentFilter);

        mAudioManager.registerMediaButtonEventReceiver(mRemoteControlReceiver);

        if (alreadyPlaying()) {


            final RadioApplication radioApplication = (RadioApplication) getApplication();

            if (radioApplication.getPlayingType() == RadioApplication.PlayingFile) {
                Log.d(TAG, "Playing file...");
                findViewById(R.id.progress_layout).setVisibility(View.VISIBLE);

                final SeekBar seekProgress = (SeekBar) findViewById(R.id.seek_progress);
                final TextView timeElapsed = (TextView) findViewById(R.id.time_elapsed);
                final TextView timeRemaining = (TextView) findViewById(R.id.time_remaining);
                final MediaPlayer mp = radioApplication.getMediaPlayer();
                final int duration = radioApplication.getPlayingFileDetails().getDuration();

                seekProgress.setVisibility(View.VISIBLE);
                seekProgress.setMax(duration);
                seekProgress.setProgress(mp.getCurrentPosition());

                seekProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        if (fromUser) {
                            mp.seekTo(progress);
                            timeElapsed.setText(millisecondsToMinutes(progress));
                            timeRemaining.setText(millisecondsToMinutes(duration - progress));
                        }
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {
                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                    }
                });

                new UpdateProgressTask().execute();
            }
        }

        updateUI();
    }

    private class UpdateProgressTask extends AsyncTask<Void, Integer, Void> {

        RadioApplication radioApplication = (RadioApplication) getApplication();
        MediaPlayer mp = radioApplication.getMediaPlayer();
        int duration = radioApplication.getPlayingFileDetails().getDuration();
        SeekBar seekProgress = (SeekBar) findViewById(R.id.seek_progress);
        TextView timeElapsed = (TextView) findViewById(R.id.time_elapsed);
        TextView timeRemaining = (TextView) findViewById(R.id.time_remaining);

        @Override
        protected Void doInBackground(Void... voids) {
            while (mp != null && mp.isPlaying() && mp.getCurrentPosition() < duration) {
                publishProgress(mp.getCurrentPosition());

            }
            // Send final msg to reset UI to non playing state?
            publishProgress(-1);
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... currentPosition) {
            if (currentPosition[0] > 0) {
                seekProgress.setProgress(currentPosition[0]);
                timeElapsed.setText(millisecondsToMinutes(currentPosition[0]));
                timeRemaining.setText(millisecondsToMinutes(duration - currentPosition[0]));
            }

            if (currentPosition[0] == -1) {
                //Reset UI
                findViewById(R.id.progress_layout).setVisibility(View.GONE);
                updateUI();
            }
        }
    }

    private String millisecondsToMinutes(int milliseconds) {
        return String.format("%d:%d", TimeUnit.MILLISECONDS.toMinutes(milliseconds), TimeUnit.MILLISECONDS.toSeconds(milliseconds));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAudioManager.unregisterMediaButtonEventReceiver(mRemoteControlReceiver);
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
        if (alreadyPlaying()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Stop playing current station?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                            PlayerService.sendWakefulWork(getApplicationContext(), createPlayingIntent(null, RadioApplication.StopPlaying));
                            PlayerService.sendWakefulWork(getApplicationContext(), createPlayingIntent(radioDetails, RadioApplication.StartPlayingRadio));
                            findViewById(R.id.main_stop_playing_btn).setEnabled(true);
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
            builder.create().show();

        } else {
            PlayerService.sendWakefulWork(getApplicationContext(), createPlayingIntent(radioDetails, RadioApplication.StartPlayingRadio));
            findViewById(R.id.main_stop_playing_btn).setEnabled(true);
        }
    }

    private void record(final RadioDetails radioDetails) {
        final RadioApplication radioApplication = (RadioApplication) getApplication();
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

                            radioApplication.setRecordingStatus(getString(R.string.recording_string) + " " + (radioDetails.getStationName() != null ? radioDetails.getStationName() : ""));
                            updateUI();
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

            radioApplication.setRecordingStatus(getString(R.string.recording_string) + " " + (radioDetails.getStationName() != null ? radioDetails.getStationName() : ""));
            updateUI();
            findViewById(R.id.main_stop_recording_btn).setEnabled(true);
        }
    }

    protected void updateUI() {

        RadioApplication radioApplication = (RadioApplication) getApplication();
        StringBuilder sb = new StringBuilder();

        if (!StringUtils.IsNullOrEmpty(radioApplication.getPlayingStatus()) && !StringUtils.IsNullOrEmpty(radioApplication.getRecordingStatus())) {
            sb.append(radioApplication.getPlayingStatus().length() > 23 ? radioApplication.getPlayingStatus().substring(0, 20) + "..." : radioApplication.getPlayingStatus())
                    .append(" | ")
                    .append(radioApplication.getRecordingStatus().length() > 23 ? radioApplication.getRecordingStatus().substring(0, 20) + "..." : radioApplication.getRecordingStatus());
        }

        if (!StringUtils.IsNullOrEmpty(radioApplication.getPlayingStatus()) && StringUtils.IsNullOrEmpty(radioApplication.getRecordingStatus())) {
            sb.append(radioApplication.getPlayingStatus().length() > 46 ? radioApplication.getPlayingStatus().substring(0, 43) + "..." : radioApplication.getPlayingStatus());
        }

        if (StringUtils.IsNullOrEmpty(radioApplication.getPlayingStatus()) && !StringUtils.IsNullOrEmpty(radioApplication.getRecordingStatus())) {
            sb.append(radioApplication.getRecordingStatus().length() > 46 ? radioApplication.getRecordingStatus().substring(0, 43) + "..." : radioApplication.getRecordingStatus());
        }

        TextView txtStatus = (TextView) findViewById(R.id.txt_status);
        txtStatus.setText(sb.toString());

        findViewById(R.id.main_stop_playing_btn).setEnabled(!StringUtils.IsNullOrEmpty(radioApplication.getPlayingStatus()));
        findViewById(R.id.main_stop_recording_btn).setEnabled(!StringUtils.IsNullOrEmpty(radioApplication.getRecordingStatus()));
    }


    private void reportError(final String radioDetails, final String exception) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Sorry, cannot connect to this stream, would you like to send an error report so support can be added please?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StringBuilder stringBuilder = new StringBuilder()
                                .append("Exception caught trying to play stream: ")
                                .append(exception)
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

    private BroadcastReceiver updateStatusBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            RadioActivity.this.updateUI();
        }
    };

    private BroadcastReceiver sendErrorBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String radioDetails = bundle.getString(getString(R.string.player_service_update_playing_error_radio_details));
            String exception = bundle.getString(getString(R.string.player_service_update_playing_error_exception));
            RadioActivity.this.reportError(radioDetails, exception);
        }
    };
}