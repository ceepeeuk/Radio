package com.cpdev;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.io.IOException;


public class ListScheduledRecordingsActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ListScheduledRecordingsActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_recording_schedule);

        DatabaseHelper dbHelper = prepareDatabaseHelper();
        final Cursor scheduledRecordingsCursor = dbHelper.getScheduledRecordings();

        final SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.list_recording_schedule_list,
                scheduledRecordingsCursor,
                new String[]{DatabaseHelper.SCHEDULED_RECORDINGS_STATION,
                        DatabaseHelper.SCHEDULED_RECORDINGS_START_TIME,
                        DatabaseHelper.SCHEDULED_RECORDINGS_END_TIME,
                        DatabaseHelper.SCHEDULED_RECORDINGS_TYPE},
                new int[]{R.id.station_entry,
                        R.id.start_time_entry,
                        R.id.end_time_entry,
                        R.id.type_entry
                });

        ListView scheduledRecordings = (ListView) findViewById(R.id.list_recording_schedule_list_view);
        scheduledRecordings.setAdapter(adapter);
        dbHelper.close();

        Button addNewButton = (Button) findViewById(R.id.list_recording_schedule_new_button);
        addNewButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        Intent scheduledRecordingsIntent = new Intent(ListScheduledRecordingsActivity.this, AddNewScheduledRecordingActivity.class);
        startActivity(scheduledRecordingsIntent);
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