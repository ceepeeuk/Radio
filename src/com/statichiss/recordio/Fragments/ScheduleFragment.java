package com.statichiss.recordio.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.statichiss.R;
import com.statichiss.recordio.AddNewScheduledRecordingActivity;
import com.statichiss.recordio.AlarmHelper;
import com.statichiss.recordio.DatabaseHelper;
import com.statichiss.recordio.ScheduledRecordingsCursorAdaptor;

import java.io.IOException;

/**
 * Created by chris on 20/06/2013.
 */
public class ScheduleFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "com.statichiss.recordio.fragments.ScheduleFragment";
    DatabaseHelper dbHelper;
    ScheduledRecordingsCursorAdaptor adapter;
    private final Uri stationContentUri;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_view, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        dbHelper = prepareDatabaseHelper();

//        setContentView(R.layout.list_recording_schedule);

        final Cursor scheduledRecordingsCursor = dbHelper.getScheduledRecordingsList();

        adapter = new ScheduledRecordingsCursorAdaptor(getActivity(),
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

        ListView scheduledRecordings = (ListView) getActivity().findViewById(R.id.list_recording_schedule_list_view);
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
                                        Intent addNewScheduledRecordingActivityIntent = new Intent(getActivity(), AddNewScheduledRecordingActivity.class);
                                        addNewScheduledRecordingActivityIntent.putExtra(getString(R.string.edit_scheduled_recording_id), id);
                                        startActivity(addNewScheduledRecordingActivityIntent);
//                                        getActivity().finish();
                                        break;
                                    case 1:
                                        DatabaseHelper databaseHelper = prepareDatabaseHelper();
                                        databaseHelper.deleteScheduledRecording(id);
                                        databaseHelper.close();
                                        scheduledRecordingsCursor.requery();
                                        AlarmHelper.cancelAlarm(getActivity().getApplicationContext(), id);
                                        break;
                                }
                            }
                        }).show();
            }
        });

        Button addNewButton = (Button) getActivity().findViewById(R.id.list_recording_schedule_new_button);
        addNewButton.setOnClickListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

//    @Override
//    public void onBackPressed() {
//        Intent RadioActivityIntent = new Intent(ListScheduledRecordingsActivity.this, RadioActivity.class);
//        startActivity(RadioActivityIntent);
//        finish();
//    }

    public void onClick(View view) {
        Intent addNewScheduledRecordingActivityIntent = new Intent(getActivity(), AddNewScheduledRecordingActivity.class);
        startActivity(addNewScheduledRecordingActivityIntent);
//        getActivity().finish();
    }

    private DatabaseHelper prepareDatabaseHelper() {
        DatabaseHelper dbHelper = new DatabaseHelper(getActivity());

        try {
            dbHelper.openDataBase();
        } catch (IOException e) {
            Log.e(TAG, "IOException thrown when trying to access DB", e);
        }

        return dbHelper;
    }
}
