package com.cpdev;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cpdev.utils.StringUtils;

import java.util.Calendar;

public class AddNewScheduledRecording extends Activity implements View.OnClickListener {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_scheduled_recording);

        Button setStartTimeButton = (Button) findViewById(R.id.setStartTimeButton);
        Button setEndTimeButton = (Button) findViewById(R.id.setEndTimeButton);
        setStartTimeButton.setOnClickListener(this);
        setEndTimeButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.setStartTimeButton:
                showDateTimeDialog(view.getId());
            case R.id.setEndTimeButton:
                showDateTimeDialog(view.getId());
                break;

        }
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
                // TODO Auto-generated method stub
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
                    case R.id.setStartTimeButton:
                        ((TextView) findViewById(R.id.newRecordingStartTimeText)).setText(date);
                        break;
                    case R.id.setEndTimeButton:
                        ((TextView) findViewById(R.id.newRecordingEndTimeText)).setText(date);
                        break;

                }

                mDateTimeDialog.dismiss();
            }
        });

        // Cancel the dialog when the "Cancel" button is clicked
        ((Button) mDateTimeDialogView.findViewById(R.id.CancelDialog)).setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub
                mDateTimeDialog.cancel();
            }
        });

        // Reset Date and Time pickers when the "Reset" button is clicked
        ((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new View.OnClickListener() {

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