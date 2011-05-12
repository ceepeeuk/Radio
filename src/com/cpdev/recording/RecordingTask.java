package com.cpdev.recording;


import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import com.cpdev.R;
import com.cpdev.RadioActivity;
import com.cpdev.RadioDetails;
import com.cpdev.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class RecordingTask extends AsyncTask<RadioDetails, Void, Boolean> {

    private static final String TAG = "com.cpdev.recording.RecordingTask";
    private boolean recordingState = false;
    private boolean cancelRecording = false;

    private FileOutputStream fileOutputStream;
    private InputStream inputStream;
    private RadioActivity activity;
    private Exception exception;

    @Override
    protected Boolean doInBackground(RadioDetails... radioDetails) {

        long startTime = System.currentTimeMillis();

        try {
            URL url = new URL(radioDetails[0].getStreamUrl());
            Log.d(TAG, "RecordingTask attempting to stream from: " + url);
            inputStream = url.openStream();

            String recFolder = GetRecordingsFolder();

            if (!new File(recFolder).exists()) {
                new File(recFolder).mkdir();
                Log.d(TAG, "Recordio directory was not found, so created it");
            }

            StringBuilder outputSource = new StringBuilder()
                    .append(recFolder)
                    .append(File.separator);
            if (!StringUtils.IsNullOrEmpty(radioDetails[0].getStationName())) {
                outputSource.append(radioDetails[0].getStationName())
                        .append("-");
            }
            outputSource.append(getTimestamp())
                    .append(".mp3");


            Log.d(TAG, "Writing stream to : " + outputSource);
            fileOutputStream = new FileOutputStream(outputSource.toString());
            recordingState = true;

            int c;
            int bytesRead = 0;

            if (radioDetails[0].getDuration() > 0) {
                // Timed recording
                long endTime = startTime + radioDetails[0].getDuration();
                while (System.currentTimeMillis() < endTime && (c = inputStream.read()) != -1) {
                    fileOutputStream.write(c);
                    bytesRead++;
                }
            } else {
                // Manual recording
                while (!cancelRecording && (c = inputStream.read()) != -1) {
                    fileOutputStream.write(c);
                    bytesRead++;
                }
            }


            Log.d(TAG, "Finished writing stream, " + bytesRead + " bytes written");

        } catch (MalformedURLException e) {
            Log.e(TAG, "Uri malformed: " + e.getMessage(), e);
            this.exception = e;
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
                recordingState = false;
                cancelRecording = false;
            } catch (IOException e) {
                Log.e(TAG, "Error flushing and close output stream", e);
            }
        }
        return true;
    }

    @Override
    public void onCancelled() {
        if (inputStream != null) {
            cancelRecording = true;
            Log.d(TAG, "cancelRecording=true");
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (exception != null) {
            activity.setStatus("Error occurred trying to record stream.");
        }
    }

    private String getTimestamp() {

        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());

        StringBuilder timestamp = new StringBuilder();
        timestamp.append(calendar.get(Calendar.YEAR));
        timestamp.append(("."));
        timestamp.append(pad(calendar.get(Calendar.MONTH)));
        timestamp.append(("."));
        timestamp.append(pad(calendar.get(Calendar.DAY_OF_MONTH)));
        timestamp.append(("-"));
        timestamp.append(pad(calendar.get(Calendar.HOUR_OF_DAY)));
        timestamp.append(("."));
        timestamp.append(pad(calendar.get(Calendar.MINUTE)));
        timestamp.append(("."));
        timestamp.append(pad(calendar.get(Calendar.SECOND)));

        return timestamp.toString();
    }

    private String GetRecordingsFolder() {
        StringBuilder sb = new StringBuilder();
        sb.append(Environment.getExternalStorageDirectory().getAbsolutePath());
        sb.append(File.separator);
        sb.append(activity.getString(R.string.app_name));
        return sb.toString();
    }

    private String pad(int originalNum) {
        String original = String.valueOf(originalNum);
        if (original.length() == 1) {
            return "0" + original;
        } else {
            return original;
        }
    }

    public boolean alreadyRecording() {
        return recordingState;
    }

    public void attach(RadioActivity radioActivity) {
        this.activity = radioActivity;
    }
}