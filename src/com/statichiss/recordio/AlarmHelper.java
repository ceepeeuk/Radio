package com.statichiss.recordio;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.statichiss.R;
import com.statichiss.recordio.recording.RecordingBroadcastReceiver;

import java.io.IOException;

public class AlarmHelper {
    private static final String TAG = "com.statichiss.recordio.AlarmHelper";


    // Table not zero indexed, so starts at 1
    public static final int ONE_OFF_SCHEDULED_RECORDING = 1;
    public static final int DAILY_SCHEDULED_RECORDING = 2;
    public static final int WEEKLY_SCHEDULED_RECORDING = 3;


    public void setAlarm(Context context, long databaseId, long stationId, long typeId, long startDateTime, long endDateTime) {

        DatabaseHelper databaseHelper = prepareDatabaseHelper(context);
        RadioDetails radioDetails = databaseHelper.getRadioDetail(stationId);
        databaseHelper.close();

        radioDetails.setRecordingType(typeId);
        radioDetails.setDuration(endDateTime - startDateTime);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RecordingBroadcastReceiver.class);
        intent.setType(String.valueOf(databaseId));

        intent.putExtra(context.getString(R.string.timed_recorder_service_database_id_key), databaseId);
        intent.putExtra(context.getString(R.string.radio_details_name_key), radioDetails.getStationName());
        intent.putExtra(context.getString(R.string.radio_details_stream_url_key), radioDetails.getStreamUrl());
        intent.putExtra(context.getString(R.string.radio_details_playlist_url_key), radioDetails.getPlaylistUrl());
        intent.putExtra(context.getString(R.string.timed_recorder_service_recording_duration), (endDateTime - startDateTime));
        intent.putExtra(context.getString(R.string.timed_recorder_service_operation_key), typeId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        switch ((int) typeId) {

            case ONE_OFF_SCHEDULED_RECORDING:
                alarmManager.set(AlarmManager.RTC_WAKEUP, startDateTime, pendingIntent);
                break;

            case DAILY_SCHEDULED_RECORDING:
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startDateTime, AlarmManager.INTERVAL_DAY, pendingIntent);
                break;

            case WEEKLY_SCHEDULED_RECORDING:
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, startDateTime, AlarmManager.INTERVAL_DAY * 7, pendingIntent);
                break;

        }
    }

    public void cancelAlarm(Context context, long databaseId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(context, RecordingBroadcastReceiver.class);
        intent.setType(String.valueOf(databaseId));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pendingIntent);

        Log.d(TAG, "Cancelled alarm id " + databaseId);
    }

    private DatabaseHelper prepareDatabaseHelper(Context context) {

        DatabaseHelper dbHelper = new DatabaseHelper(context);

        try {
            dbHelper.createDataBase();
            dbHelper.openDataBase();
        } catch (IOException e) {
            Log.e(TAG, "IOException thrown when trying to access DB", e);
        }

        return dbHelper;
    }
}
