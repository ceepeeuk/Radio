package com.cpdev.recording;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.cpdev.NotificationHelper;
import com.cpdev.R;
import com.cpdev.RadioDetails;
import com.cpdev.filehandling.M3uHandler;
import com.cpdev.filehandling.PlsHandler;
import com.cpdev.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class RecorderService extends WakefulIntentService {

    private static final String TAG = "com.cpdev.recording.RecorderService";

    private FileOutputStream fileOutputStream;
    private InputStream inputStream;

    private static boolean recordingState = false;
    private static boolean cancelRecordingFlag = false;

    public RecorderService() {
        super("RecorderService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {

        Bundle bundle = intent.getExtras();
        RadioDetails radioDetails = bundle.getParcelable(getString(R.string.radio_details_key));

        if (!android.os.Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Cannot write to SDCARD, so stuffed!
            String error = buildErrorMessage(radioDetails, "cannot write to SD Card");
            Notification errorNotification = NotificationHelper.getNotification(this, NotificationHelper.NOTIFICATION_RECORDING_ID, radioDetails, error, error, Notification.FLAG_ONLY_ALERT_ONCE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NotificationHelper.NOTIFICATION_RECORDING_ID, errorNotification);
            Log.e(TAG, error);
            return;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager.getActiveNetworkInfo() == null || !connectivityManager.getActiveNetworkInfo().isConnected()) {
            // No network connection
            String error = buildErrorMessage(radioDetails, "cannot get network connection");
            Notification errorNotification = NotificationHelper.getNotification(this, NotificationHelper.NOTIFICATION_RECORDING_ID, radioDetails, error, error, Notification.FLAG_ONLY_ALERT_ONCE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NotificationHelper.NOTIFICATION_RECORDING_ID, errorNotification);
            Log.e(TAG, error);
            return;
        }

        // SDCARD must be writable and network available, so here we go...

        if (radioDetails.getPlaylistUrl().endsWith(".pls") || radioDetails.getPlaylistUrl().endsWith(".m3u")) {
            if (radioDetails.getPlaylistUrl().endsWith(".pls")) {
                radioDetails = PlsHandler.parse(radioDetails);
            } else {
                radioDetails = M3uHandler.parse(radioDetails);
            }
        } else {
            radioDetails.setStreamUrl(radioDetails.getPlaylistUrl());
        }

        CharSequence ticketText = new StringBuilder()
                .append("Recording ")
                .append(radioDetails.getStationName())
                .toString();

        Notification notification = NotificationHelper.getNotification(this, NotificationHelper.NOTIFICATION_RECORDING_ID, radioDetails, ticketText, ticketText, Notification.FLAG_ONGOING_EVENT);
        startForeground(NotificationHelper.NOTIFICATION_RECORDING_ID, notification);

        String recFolder = GetRecordingsFolder();

        if (!new File(recFolder).exists()) {
            if (!(new File(recFolder).mkdir())) {
                //Failed to create dir, so have to quit
                String error = buildErrorMessage(radioDetails, "cannot create directory to store recordings");
                Notification errorNotification = NotificationHelper.getNotification(this, NotificationHelper.NOTIFICATION_RECORDING_ID, radioDetails, error, error, Notification.FLAG_ONLY_ALERT_ONCE);
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(NotificationHelper.NOTIFICATION_RECORDING_ID, errorNotification);
                return;
            }
            Log.d(TAG, "Recordio directory was not found, so created it");
        }

        StringBuilder outputSource = new StringBuilder()
                .append(recFolder)
                .append(File.separator);

        if (!StringUtils.IsNullOrEmpty(radioDetails.getStationName())) {
            outputSource.append(radioDetails.getStationName())
                    .append("-");
        }

        outputSource.append(getTimestamp())
                .append(".mp3");

        Log.d(TAG, "Writing stream to : " + outputSource);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiManager.WifiLock wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "MyWifiLock");

        if (!wifiLock.isHeld()) {
            wifiLock.acquire();
        }

        try {


            byte[] buffer = new byte[4096];
            int len;

            if (radioDetails.getDuration() > 0) {
                // Timed recording
                URLConnection url = new URL(radioDetails.getStreamUrl()).openConnection();
                inputStream = url.getInputStream();
                fileOutputStream = new FileOutputStream(outputSource.toString());
                recordingState = true;
                long endTime = System.currentTimeMillis() + radioDetails.getDuration();

                while (System.currentTimeMillis() < endTime) {
                    //fileOutputStream.write(buffer, 0, len);
                    record(radioDetails, outputSource.toString(), buffer, endTime);
                }

            } else {

                // Manual recording
                recordingState = true;
                Log.d(TAG, "Starting manual recording...");
                while (!cancelRecordingFlag) {
                    record(radioDetails, outputSource.toString(), buffer, 0);
                }
            }

            Log.d(TAG, "Finished writing stream");

        } catch (MalformedURLException e) {
            Log.e(TAG, "Uri malformed: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.d(TAG, "IOException: " + e.getMessage(), e);
            // Expected when stream closes
        } finally {

            try {
                if (inputStream != null) {
                    inputStream.close();
                    inputStream = null;
                }
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                    fileOutputStream = null;
                }
                if (wifiLock != null) {
                    wifiLock.release();
                }

                recordingState = false;
                cancelRecordingFlag = false;

            } catch (IOException e) {
                Log.e(TAG, "Error flushing and close output stream", e);
            }

        }
    }

    private void record(RadioDetails radioDetails, String outputSource, byte[] buffer, long endTime) {
        try {
            int len;
            URLConnection url = new URL(radioDetails.getStreamUrl()).openConnection();
            inputStream = url.getInputStream();
            fileOutputStream = new FileOutputStream(outputSource, true);

            if (endTime > 0) {
                while ((System.currentTimeMillis() < endTime) && (len = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, len);
                }
            } else {
                while (!cancelRecordingFlag && (len = inputStream.read(buffer)) > 0) {
                    fileOutputStream.write(buffer, 0, len);
                }
            }

        } catch (IOException ignored) {
            try {
                // 1 second delay
                Thread.sleep(1000);
            } catch (InterruptedException ignored2) {
                Log.d(TAG, "Sleep interrupted");
            }
        }
    }

    private String buildErrorMessage(RadioDetails radioDetails, String error) {
        return new StringBuilder()
                .append("Failed to record ")
                .append(radioDetails.getStationName())
                .append(" ")
                .append(error)
                .toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onDestroy();
        Log.d(TAG, "onLowMemory called");
    }

    private String getTimestamp() {

        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());

        StringBuilder timestamp = new StringBuilder();
        timestamp.append(calendar.get(Calendar.YEAR));
        timestamp.append(("."));
        timestamp.append(StringUtils.pad(calendar.get(Calendar.MONTH)));
        timestamp.append(("."));
        timestamp.append(StringUtils.pad(calendar.get(Calendar.DAY_OF_MONTH)));
        timestamp.append(("-"));
        timestamp.append(StringUtils.pad(calendar.get(Calendar.HOUR_OF_DAY)));
        timestamp.append(("."));
        timestamp.append(StringUtils.pad(calendar.get(Calendar.MINUTE)));
        timestamp.append(("."));
        timestamp.append(StringUtils.pad(calendar.get(Calendar.SECOND)));

        return timestamp.toString();
    }

    private String GetRecordingsFolder() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append(getString(R.string.app_name));
        return sb.toString();
    }

    public static boolean alreadyRecording() {
        return recordingState;
    }

    public static void cancelRecording() {
        Log.d(TAG, "Cancel recording stream");
        cancelRecordingFlag = true;
    }
}