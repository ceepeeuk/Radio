package com.cpdev;


import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class RecordingTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = "com.cpdev.RecordingTask";
    private boolean recordingState = false;
    private boolean cancelRecording = false;

    private FileOutputStream fileOutputStream;
    private InputStream inputStream;

    @Override
    protected Void doInBackground(String... urls) {
        try {
            URL url = new URL(urls[0]);
            Log.d(TAG, "RecordingTask attempting to stream from: " + url);
            inputStream = url.openStream();

            String recFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Radio";

            if (!new File(recFolder).exists()) {
                new File(recFolder).mkdir();
                Log.d(TAG, "Radio directory was not found, so created it");
            }

            String outputSource = recFolder + File.separator + getTimestamp() + ".mp3";
            Log.d(TAG, "Writing stream to : " + outputSource);
            fileOutputStream = new FileOutputStream(outputSource);
            recordingState = true;

            int c;
            int bytesRead = 0;

            while (!cancelRecording && (c = inputStream.read()) != -1) {
                fileOutputStream.write(c);
                bytesRead++;
            }

            Log.d(TAG, "Finished writing stream, " + bytesRead / 1024 / 1024 + " megabytes written");

        } catch (MalformedURLException e) {
            Log.e(TAG, "Uri malformed: " + e.getMessage(), e);
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage(), e);
            // Expected when stream closes
        } finally {
            try {
                inputStream.close();
                fileOutputStream.flush();
                fileOutputStream.close();
                recordingState = false;
                cancelRecording = false;
            } catch (IOException e) {
                Log.e(TAG, "Error flushing and close output stream", e);
            }
        }
        return null;
    }

    @Override
    public void onCancelled() {
        if (inputStream != null) {
            //inputStream.close();
            cancelRecording = true;
            Log.d(TAG, "cancelRecording=true");
        }
    }

    private String getTimestamp() {
        Calendar calendar = new GregorianCalendar(TimeZone.getDefault());

        StringBuilder timestamp = new StringBuilder();
        timestamp.append(calendar.get(Calendar.YEAR));
        timestamp.append(pad(calendar.get(Calendar.MONTH)));
        timestamp.append(pad(calendar.get(Calendar.DAY_OF_MONTH)));
        timestamp.append(pad(calendar.get(Calendar.HOUR_OF_DAY)));
        timestamp.append(pad(calendar.get(Calendar.MINUTE)));
        timestamp.append(pad(calendar.get(Calendar.SECOND)));

        return timestamp.toString();
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
}