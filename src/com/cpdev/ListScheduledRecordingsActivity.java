package com.cpdev;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import java.io.IOException;

public class ListScheduledRecordingsActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ListScheduledRecordingsActivity";
    DatabaseHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = prepareDatabaseHelper();

        setContentView(R.layout.list_recording_schedule);

        final Cursor scheduledRecordingsCursor = dbHelper.getScheduledRecordingsList();

        final ScheduledRecordingsCursorAdaptor adapter = new ScheduledRecordingsCursorAdaptor(this,
                R.layout.list_recording_schedule_list,
                scheduledRecordingsCursor,
                new String[]{DatabaseHelper.STATIONS_NAME,
                        DatabaseHelper.RECORDING_TYPES_TYPE,
                        DatabaseHelper.SCHEDULED_RECORDINGS_START_TIME,
                        DatabaseHelper.SCHEDULED_RECORDINGS_END_TIME},
                new int[]{R.id.station_entry,
                        R.id.type_entry,
                        R.id.start_time_entry,
                        R.id.end_time_entry
                });

        ListView scheduledRecordings = (ListView) findViewById(R.id.list_recording_schedule_list_view);
        scheduledRecordings.setAdapter(adapter);

        scheduledRecordings.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, final long id) {
                new AlertDialog.Builder(view.getContext())
                        .setMessage("Delete scheduled recording?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int i) {
                                DatabaseHelper databaseHelper = prepareDatabaseHelper();
                                databaseHelper.deleteScheduledRecording(id);
                                databaseHelper.close();
                                scheduledRecordingsCursor.requery();
                                new AlarmHelper().cancelAlarm(getApplicationContext(), id);
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

        Button addNewButton = (Button) findViewById(R.id.list_recording_schedule_new_button);
        addNewButton.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    @Override
    public void onBackPressed() {
        Intent RadioActivityIntent = new Intent(ListScheduledRecordingsActivity.this, RadioActivity.class);
        startActivity(RadioActivityIntent);
    }

    public void onClick(View view) {
        Intent addNewScheduledRecordingActivityIntent = new Intent(ListScheduledRecordingsActivity.this, AddNewScheduledRecordingActivity.class);
        startActivity(addNewScheduledRecordingActivityIntent);
    }

    private DatabaseHelper prepareDatabaseHelper() {
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        try {
            dbHelper.createDataBase();
            dbHelper.openDataBase();
        } catch (IOException e) {
            Log.e(TAG, "IOException thrown when trying to access DB", e);
        }

        return dbHelper;
    }


}