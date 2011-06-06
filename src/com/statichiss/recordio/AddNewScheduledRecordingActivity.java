package com.statichiss.recordio;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.*;
import com.statichiss.R;
import com.statichiss.recordio.utils.StringUtils;

import java.io.IOException;
import java.util.Calendar;

public class AddNewScheduledRecordingActivity extends Activity implements View.OnClickListener {

    private long startDateTime = 0;
    private long endDateTime = 0;
    private static final String TAG = "com.statichiss.recordio.AddNewScheduledRecordingActivity";

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_scheduled_recording);

        Button setStartTimeButton = (Button) findViewById(R.id.add_new_scheduled_recording_set_start_time_button);
        Button setEndTimeButton = (Button) findViewById(R.id.add_new_scheduled_recording_end_time_button);
        Button okButton = (Button) findViewById(R.id.add_new_scheduled_recording_ok_button);
        Button cancelButton = (Button) findViewById(R.id.add_new_scheduled_recording_cancel_button);

        setStartTimeButton.setOnClickListener(this);
        setEndTimeButton.setOnClickListener(this);
        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        final DatabaseHelper dbHelper = prepareDatabaseHelper();

        Spinner favouriteStationSpinner = (Spinner) findViewById(R.id.add_new_scheduled_recording_favourite_station_spinner);
        Cursor favouriteStationCursor = dbHelper.getFavourites();
        SimpleCursorAdapter favouriteStationAdapter = new SimpleCursorAdapter(this,
                R.layout.add_new_scheduled_recording_favourite_stations,
                favouriteStationCursor,
                new String[]{DatabaseHelper.FAVOURITES_NAME},
                new int[]{R.id.name_entry});

        favouriteStationAdapter.setDropDownViewResource(R.layout.add_new_scheduled_recording_favourite_stations);
        favouriteStationSpinner.setAdapter(favouriteStationAdapter);

        Spinner recordingTypeSpinner = (Spinner) findViewById(R.id.add_new_scheduled_recording_recording_type_spinner);
        Cursor recordingTypeCursor = dbHelper.getRecordingTypes();
        SimpleCursorAdapter recordingTypesAdapter = new SimpleCursorAdapter(this,
                R.layout.add_new_scheduled_recording_types,
                recordingTypeCursor,
                new String[]{DatabaseHelper.RECORDING_TYPES_TYPE},
                new int[]{R.id.add_new_scheduled_recording_type_entry});

        recordingTypesAdapter.setDropDownViewResource(R.layout.add_new_scheduled_recording_types);
        recordingTypeSpinner.setAdapter(recordingTypesAdapter);

        dbHelper.close();
    }

    public void onClick(View view) {
        Intent listScheduledRecordingsIntent = new Intent(AddNewScheduledRecordingActivity.this, ListScheduledRecordingsActivity.class);

        switch (view.getId()) {
            case R.id.add_new_scheduled_recording_set_start_time_button:
                showDateTimeDialog(view.getId());
                break;
            case R.id.add_new_scheduled_recording_end_time_button:
                showDateTimeDialog(view.getId());
                break;
            case R.id.add_new_scheduled_recording_ok_button:
                if (this.startDateTime < 1 && this.endDateTime < 1) {
                    Toast.makeText(this, R.string.add_new_scheduled_recording_start_and_end_time_not_set, Toast.LENGTH_SHORT).show();
                    break;
                }
                if (this.startDateTime < 1) {
                    Toast.makeText(this, R.string.add_new_scheduled_recording_start_time_not_set, Toast.LENGTH_SHORT).show();
                    break;
                }
                if (this.endDateTime < 1) {
                    Toast.makeText(this, R.string.add_new_scheduled_recording_end_time_not_set, Toast.LENGTH_SHORT).show();
                    break;
                }
                if (validateStartTimeIsNotAlreadyInUse(this.startDateTime, this.endDateTime)) {
                    Toast.makeText(this, R.string.add_new_scheduled_recording_recording_already_scheduled, Toast.LENGTH_SHORT).show();
                    break;
                }
                addNewScheduledRecording();
                startActivity(listScheduledRecordingsIntent);
                break;
            case R.id.add_new_scheduled_recording_cancel_button:
                startActivity(listScheduledRecordingsIntent);
                break;
        }
    }

    private boolean validateStartTimeIsNotAlreadyInUse(long startDateTime, long endDateTime) {

        boolean result = false;
        DatabaseHelper dbhHelper = prepareDatabaseHelper();
        Cursor cursor = dbhHelper.getAllScheduledRecordings();
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            long cursorStartDateTime = cursor.getLong(1);
            long cursorEndDateTime = cursor.getLong(2);

            if ((startDateTime > cursorStartDateTime && startDateTime < cursorEndDateTime) ||
                    (startDateTime > cursorStartDateTime && startDateTime < cursorEndDateTime)) {

                dbhHelper.close();
                return true;
            }

            cursor.moveToNext();
        }

        dbhHelper.close();
        return result;
    }

    private void addNewScheduledRecording() {
        // Add to database
        DatabaseHelper dbHelper = prepareDatabaseHelper();
        Spinner station = (Spinner) findViewById(R.id.add_new_scheduled_recording_favourite_station_spinner);
        Spinner type = (Spinner) findViewById(R.id.add_new_scheduled_recording_recording_type_spinner);
        long dbId = dbHelper.insertScheduledRecording(startDateTime, endDateTime, station.getSelectedItemPosition(), type.getSelectedItemPosition());

        dbHelper.close();

        //Set alarm
        new AlarmHelper().setAlarm(this, dbId, station.getSelectedItemId(), type.getSelectedItemId(), startDateTime, endDateTime);
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

    private void showDateTimeDialog(int viewId) {

        final int originalViewId = viewId;

        // Create the dialog
        final Dialog mDateTimeDialog = new Dialog(this);
        // Inflate the root layout
        final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.date_time_dialog, null);
        // Grab widget instance
        final com.ptashek.widgets.datetimepicker.DateTimePicker mDateTimePicker = (com.ptashek.widgets.datetimepicker.DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);
        // Check is system is set to use 24h time (this doesn't seem to work as expected though)
        final String timeS = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
        final boolean is24h = !(timeS == null || timeS.equals("12"));

        // Update demo TextViews when the "OK" button is clicked
        mDateTimeDialogView.findViewById(R.id.SetDateTime).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

                StringBuilder date = new StringBuilder();

                if (mDateTimePicker.is24HourView()) {
                    date.append(StringUtils.pad(mDateTimePicker.get(Calendar.HOUR_OF_DAY)))
                            .append(":")
                            .append(StringUtils.pad(mDateTimePicker.get(Calendar.MINUTE)))
                            .append(" ");
                } else {
                    date.append(StringUtils.pad(mDateTimePicker.get(Calendar.HOUR)))
                            .append(":")
                            .append(StringUtils.pad(mDateTimePicker.get(Calendar.MINUTE)))
                            .append(" ")
                            .append((mDateTimePicker.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM"))
                            .append(" ");
                }

                date.append(mDateTimePicker.get(Calendar.DAY_OF_MONTH))
                        .append("/")
                        .append(mDateTimePicker.get(Calendar.MONTH) + 1)
                        .append("/")
                        .append(mDateTimePicker.get(Calendar.YEAR));

                switch (originalViewId) {
                    case R.id.add_new_scheduled_recording_set_start_time_button:
                        startDateTime = roundDownToMinute(mDateTimePicker.getDateTimeMillis());
                        ((TextView) findViewById(R.id.add_new_scheduled_recording_start_time_text)).setText(date);
                        break;
                    case R.id.add_new_scheduled_recording_end_time_button:
                        endDateTime = roundDownToMinute(mDateTimePicker.getDateTimeMillis());
                        ((TextView) findViewById(R.id.add_new_scheduled_recording_end_time_text)).setText(date);
                        break;
                }

                mDateTimeDialog.dismiss();
            }

            private long roundDownToMinute(long dateTimeMillis) {
                long MILLISECONDS_PER_MINUTE = 60 * 1000L;
                long msRem = dateTimeMillis % MILLISECONDS_PER_MINUTE;
                return dateTimeMillis - msRem;
            }
        });

        // Cancel the dialog when the "Cancel" button is clicked
        mDateTimeDialogView.findViewById(R.id.CancelDialog).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDateTimeDialog.cancel();
            }
        });

        // Reset Date and Time pickers when the "Reset" button is clicked
        mDateTimeDialogView.findViewById(R.id.ResetDateTime).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDateTimePicker.reset();
            }
        });

        // Setup TimePicker
        mDateTimePicker.setIs24HourView(is24h);
        // No title on the dialog window
        mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Set the dialog content view
        mDateTimeDialog.setContentView(mDateTimeDialogView);
        // Display the dialog
        mDateTimeDialog.show();
    }

}