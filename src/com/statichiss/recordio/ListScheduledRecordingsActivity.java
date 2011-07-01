package com.statichiss.recordio;

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
import com.statichiss.R;

import java.io.IOException;

public class ListScheduledRecordingsActivity extends RecordioBaseActivity implements View.OnClickListener {
    private static final String TAG = "com.statichiss.recordio.ListScheduledRecordingsActivity";
    DatabaseHelper dbHelper;
    ScheduledRecordingsCursorAdaptor adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        dbHelper = prepareDatabaseHelper();

        setContentView(R.layout.list_recording_schedule);

        final Cursor scheduledRecordingsCursor = dbHelper.getScheduledRecordingsList();

        adapter = new ScheduledRecordingsCursorAdaptor(this,
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

        scheduledRecordings.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View view, int pos, final long id) {

                CharSequence[] favOptions = {"Edit", "Delete"};
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                builder.setTitle("Scheduled Recording")
                        .setItems(favOptions, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int item) {
                                switch (item) {
                                    case 0:
                                        Intent addNewScheduledRecordingActivityIntent = new Intent(ListScheduledRecordingsActivity.this, AddNewScheduledRecordingActivity.class);
                                        addNewScheduledRecordingActivityIntent.putExtra(getString(R.string.edit_scheduled_recording_id), id);
                                        startActivity(addNewScheduledRecordingActivityIntent);
                                        break;
                                    case 1:
                                        DatabaseHelper databaseHelper = prepareDatabaseHelper();
                                        databaseHelper.deleteScheduledRecording(id);
                                        databaseHelper.close();
                                        scheduledRecordingsCursor.requery();
                                        AlarmHelper.cancelAlarm(getApplicationContext(), id);
                                        break;
                                }
                            }
                        }).show();
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
            dbHelper.openDataBase();
        } catch (IOException e) {
            Log.e(TAG, "IOException thrown when trying to access DB", e);
        }

        return dbHelper;
    }


}