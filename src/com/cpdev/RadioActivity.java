package com.cpdev;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.cpdev.FileHandling.PlsHandler;

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

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        try {
            dbHelper.createDataBase();
            dbHelper.openDataBase();
        } catch (IOException e) {
            Log.e(TAG, "IOException thrown when trying to access DB", e);
        }

        final Cursor favouritesCursor = dbHelper.getFavourites();

        ListAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.favourite_list_item,
                favouritesCursor,
                new String[]{dbHelper.NAME},
                new int[]{R.id.name_entry});

        ListView lstFavourites = (ListView) findViewById(R.id.lst_favourites);

        lstFavourites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                favouritesCursor.moveToPosition((int) id - 1);
                String url = favouritesCursor.getString(2);
                decideStreamOption(url);
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
                    playerService.stopPlaying();
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
                if (playerServiceBound && playerService.alreadyPlaying()) {
                    RadioDetails radioDetails = ((RadioApplication) getApplicationContext()).getPlayingStation();
                    DatabaseHelper dbHelper = new DatabaseHelper(this);
                    if (confirmDetails(radioDetails)) {
                        try {
                            dbHelper.createDataBase();
                            dbHelper.openDataBase();
                            dbHelper.addFavourite(radioDetails);
                        } catch (IOException e) {
                            Log.e(TAG, "IOException thrown when trying to access DB", e);
                        } finally {
                            dbHelper.close();
                        }
                    }
                }

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean confirmDetails(final RadioDetails radioDetails) {
        final boolean[] result = new boolean[1];
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.edit_fav_pop_up, null, false);
        final PopupWindow pw = new PopupWindow(layout, 200, 200, true);
        final EditText txtName = (EditText) layout.findViewById(R.id.edit_fav_pop_up_txt_name);
        final AutoCompleteTextView txtUrl = (AutoCompleteTextView) layout.findViewById(R.id.edit_fav_pop_up_txt_url);

        txtName.setText(radioDetails.getStationName());
        txtUrl.setText(radioDetails.getStreamUrl());

        Button cancelButton = (Button) layout.findViewById(R.id.edit_fav_pop_up_btn_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View vv) {
                pw.dismiss();
                result[0] = false;
            }
        });

        Button saveButton = (Button) layout.findViewById(R.id.edit_fav_pop_up_btn_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View vv) {
                result[0] = true;
                radioDetails.setStationName(txtName.getText().toString());
                radioDetails.setStreamUrl(txtUrl.getText().toString());
                pw.dismiss();
            }
        });

        pw.showAtLocation(this.findViewById(R.id.layout_main), Gravity.CENTER, 0, 0);
        return result[0];
    }

    public void goClick(View view) {
        final String source = ((EditText) findViewById(R.id.txt_url)).getText().toString();

        Log.d(TAG, "url is: " + source);

        if (source.isEmpty()) {
            showToast("Please supply a URL");
        } else {
            decideStreamOption(source);
        }
    }

    private void decideStreamOption(final String source) {
        Log.d(TAG, "Source: " + source);
        RadioDetails radioDetails;
        if (source.endsWith(".pls") || source.endsWith(".m3u")) {
            if (source.endsWith(".pls")) {
                radioDetails = PlsHandler.parse(source);
            } else {
                // Add m3u handler here
                radioDetails = new RadioDetails(null, null, source);
            }
        } else {
            radioDetails = new RadioDetails(null, source, null);
        }

        Log.d(TAG, "RadioDetails:" + radioDetails);

        CharSequence[] goOptions = {"Play", "Record", "Both"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final RadioDetails finalRadioDetails = radioDetails;

        builder.setTitle("What shall we do?")
                .setItems(goOptions, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int item) {
                        switch (item) {
                            case 0:
                                play(finalRadioDetails);
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
                                playerService.stopPlaying();
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

    private void record(final String uri) {
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
    }

    private ServiceConnection playerConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            PlayerService.RadioServiceBinder binder = (PlayerService.RadioServiceBinder) iBinder;
            playerService = binder.getService();
            playerServiceBound = true;

            if (playerService.alreadyPlaying()) {
                StringBuilder sb = new StringBuilder("Playing ");
                RadioDetails radioDetails = ((RadioApplication) getApplicationContext()).getPlayingStation();
                if (radioDetails.getStationName() != null) {
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