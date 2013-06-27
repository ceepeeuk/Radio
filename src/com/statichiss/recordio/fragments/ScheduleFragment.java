package com.statichiss.recordio.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.statichiss.R;
import com.statichiss.recordio.AddNewScheduledRecordingActivity;
import com.statichiss.recordio.AlarmHelper;
import com.statichiss.recordio.DBContentProvider;
import com.statichiss.recordio.DatabaseHelper;
import com.statichiss.recordio.ScheduledRecordingsCursorAdaptor;

/**
 * Created by chris on 20/06/2013.
 */
public class ScheduleFragment extends Fragment implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "com.statichiss.recordio.fragments.ScheduleFragment";
    private static final int SCHEDULE_LIST_ID = 1;

    ScheduledRecordingsCursorAdaptor adapter;
    private final Uri scheduleContentUri;

    public ScheduleFragment() {
        this.scheduleContentUri = Uri.withAppendedPath(DBContentProvider.CONTENT_URI, "recording_schedule");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.schedule_view, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        getLoaderManager().initLoader(SCHEDULE_LIST_ID, null, this);

        adapter = new ScheduledRecordingsCursorAdaptor(getActivity(),
                R.layout.list_recording_schedule_list,
                null,
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
                                        break;
                                    case 1:
                                        getActivity().getContentResolver().delete(scheduleContentUri, "_id = ?", new String[]{String.valueOf(id)});
                                        restartLoader();
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

    private void restartLoader() {
        getLoaderManager().restartLoader(SCHEDULE_LIST_ID, null, this);
    }

    public void onClick(View view) {
        Intent addNewScheduledRecordingActivityIntent = new Intent(getActivity(), AddNewScheduledRecordingActivity.class);
        startActivity(addNewScheduledRecordingActivityIntent);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(), scheduleContentUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        adapter.swapCursor(null);
    }
}
