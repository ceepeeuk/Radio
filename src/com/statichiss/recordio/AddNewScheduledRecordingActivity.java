package com.statichiss.recordio;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.statichiss.R;
import com.statichiss.recordio.utils.DateUtils;

import java.io.IOException;
import java.util.Date;

public class AddNewScheduledRecordingActivity extends Activity implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    private long startDateTime = 0;
    private long endDateTime = 0;
    private static final String TAG = "com.statichiss.recordio.AddNewScheduledRecordingActivity";
    private boolean editMode = false;
    private long scheduledRecordingId;
    private int callerId;
    private int year;
    private int month;
    private int day;


    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_scheduled_recording);

        Button setStartTimeButton = (Button) findViewById(R.id.add_new_scheduled_recording_set_start_time_button);
        Button setEndTimeButton = (Button) findViewById(R.id.add_new_scheduled_recording_set_end_time_button);
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

        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey(getString(R.string.edit_scheduled_recording_id))) {
            editMode = true;
            scheduledRecordingId = bundle.getLong(getString(R.string.edit_scheduled_recording_id));
            DatabaseHelper.ScheduledRecording scheduledRecording = dbHelper.GetScheduledRecording(scheduledRecordingId);
            favouriteStationSpinner.setSelection(scheduledRecording.station - 1);
            recordingTypeSpinner.setSelection(scheduledRecording.type - 1);
            startDateTime = scheduledRecording.startDateTime;
            ((TextView) findViewById(R.id.add_new_scheduled_recording_start_time_text)).setText(DateUtils.getDateTimeString(startDateTime));
            endDateTime = scheduledRecording.endDateTime;
            ((TextView) findViewById(R.id.add_new_scheduled_recording_end_time_text)).setText(DateUtils.getDateTimeString(endDateTime));
        }

        dbHelper.close();
    }

    public void onClick(View view) {
        Intent listScheduledRecordingsIntent = new Intent(AddNewScheduledRecordingActivity.this, ListScheduledRecordingsActivity.class);

        switch (view.getId()) {
            case R.id.add_new_scheduled_recording_set_start_time_button:
//                showDateTimeDialog(view.getId());
                showDatePickerDialog(view);
                break;
            case R.id.add_new_scheduled_recording_set_end_time_button:
//                showDateTimeDialog(view.getId());
                showDatePickerDialog(view);
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

                if (editMode) {
                    updateScheduledRecording();
                } else {
                    addNewScheduledRecording();
                }

                onBackPressed();
                break;

            case R.id.add_new_scheduled_recording_cancel_button:

                onBackPressed();
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

        cursor.close();
        dbhHelper.close();
        return result;
    }

    private void addNewScheduledRecording() {
        // Add to database
        DatabaseHelper dbHelper = prepareDatabaseHelper();
        Spinner station = (Spinner) findViewById(R.id.add_new_scheduled_recording_favourite_station_spinner);
        Spinner type = (Spinner) findViewById(R.id.add_new_scheduled_recording_recording_type_spinner);
        long dbId = dbHelper.insertScheduledRecording(startDateTime, endDateTime, station.getSelectedItemPosition() + 1, type.getSelectedItemPosition() + 1);

        dbHelper.close();

        //Set alarm
        AlarmHelper.setAlarm(this, dbId, station.getSelectedItemId(), type.getSelectedItemId(), startDateTime, endDateTime);
    }

    private void updateScheduledRecording() {
        // Add to database
        DatabaseHelper dbHelper = prepareDatabaseHelper();
        Spinner station = (Spinner) findViewById(R.id.add_new_scheduled_recording_favourite_station_spinner);
        Spinner type = (Spinner) findViewById(R.id.add_new_scheduled_recording_recording_type_spinner);
        dbHelper.updateScheduledRecording(scheduledRecordingId, startDateTime, endDateTime, station.getSelectedItemPosition() + 1, type.getSelectedItemPosition() + 1);
        dbHelper.close();

        // Delete old alarm
        AlarmHelper.cancelAlarm(this, scheduledRecordingId);

        // Set new alarm
        AlarmHelper.setAlarm(this, scheduledRecordingId, station.getSelectedItemId(), type.getSelectedItemId(), startDateTime, endDateTime);
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

    @Override
    public void onBackPressed() {
        Intent RadioActivityIntent = new Intent(AddNewScheduledRecordingActivity.this, ListScheduledRecordingsActivity.class);
        startActivity(RadioActivityIntent);
        finish();
    }

    public void showDatePickerDialog(View v) {
        callerId = v.getId();

        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        this.year = year;
        this.month = month;
        this.day = day;

        Date date = new Date(year, month, day);

        switch (callerId) {
            case R.id.add_new_scheduled_recording_set_start_time_button:
                ((TextView) findViewById(R.id.add_new_scheduled_recording_start_time_text)).setText(DateUtils.getDateTimeString(date.getTime()));
                break;
            case R.id.add_new_scheduled_recording_set_end_time_button:
                ((TextView) findViewById(R.id.add_new_scheduled_recording_end_time_text)).setText(DateUtils.getDateTimeString(date.getTime()));
                break;
        }
    }

//    private void showDateTimeDialog(int viewId) {
//
//        final int originalViewId = viewId;
//
//        // Create the dialog
//        final Dialog mDateTimeDialog = new Dialog(this);
//        // Inflate the root layout
//        final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.date_time_dialog, null);
//        // Grab widget instance
//        final com.ptashek.widgets.datetimepicker.DateTimePicker mDateTimePicker = (com.ptashek.widgets.datetimepicker.DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);
//        // Check is system is set to use 24h time (this doesn't seem to work as expected though)
//        final String timeS = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
//        final boolean is24h = !(timeS == null || timeS.equals("12"));
//
//        if (editMode) {
//            switch (originalViewId) {
//                case R.id.add_new_scheduled_recording_set_start_time_button:
//                    mDateTimePicker.updateDate(DateUtils.getYear(startDateTime), DateUtils.getMonth(startDateTime), DateUtils.getDay(startDateTime));
//                    mDateTimePicker.updateTime(DateUtils.getHour(startDateTime), DateUtils.getMinute(startDateTime));
//                    break;
//                case R.id.add_new_scheduled_recording_end_time_button:
//                    mDateTimePicker.updateDate(DateUtils.getYear(endDateTime), DateUtils.getMonth(endDateTime), DateUtils.getDay(endDateTime));
//                    mDateTimePicker.updateTime(DateUtils.getHour(endDateTime), DateUtils.getMinute(endDateTime));
//                    break;
//            }
//        }
//
//        // Update demo TextViews when the "OK" button is clicked
//        mDateTimeDialogView.findViewById(R.id.SetDateTime).setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//
//                switch (originalViewId) {
//                    case R.id.add_new_scheduled_recording_set_start_time_button:
//                        startDateTime = roundDownToMinute(mDateTimePicker.getDateTimeMillis());
//                        ((TextView) findViewById(R.id.add_new_scheduled_recording_start_time_text)).setText(DateUtils.getDateTimeString(startDateTime));
//                        break;
//                    case R.id.add_new_scheduled_recording_end_time_button:
//                        endDateTime = roundDownToMinute(mDateTimePicker.getDateTimeMillis());
//                        ((TextView) findViewById(R.id.add_new_scheduled_recording_end_time_text)).setText(DateUtils.getDateTimeString(endDateTime));
//                        break;
//                }
//
//                mDateTimeDialog.dismiss();
//            }
//
//            private long roundDownToMinute(long dateTimeMillis) {
//                long MILLISECONDS_PER_MINUTE = 60 * 1000L;
//                long msRem = dateTimeMillis % MILLISECONDS_PER_MINUTE;
//                return dateTimeMillis - msRem;
//            }
//        });
//
//        // Cancel the dialog when the "Cancel" button is clicked
//        mDateTimeDialogView.findViewById(R.id.CancelDialog).setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//                mDateTimeDialog.cancel();
//            }
//        });
//
//        // Reset Date and Time pickers when the "Reset" button is clicked
//        mDateTimeDialogView.findViewById(R.id.ResetDateTime).setOnClickListener(new View.OnClickListener() {
//
//            public void onClick(View v) {
//                mDateTimePicker.reset();
//            }
//        });
//
//        // Setup TimePicker
//        mDateTimePicker.setIs24HourView(is24h);
//        // No title on the dialog window
//        mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        // Set the dialog content view
//        mDateTimeDialog.setContentView(mDateTimeDialogView);
//        // Display the dialog
//        mDateTimeDialog.show();
//
//
//    }
}