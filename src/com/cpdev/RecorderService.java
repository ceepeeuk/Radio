package com.cpdev;

import android.app.Service;
import android.content.Intent;
import android.os.*;
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


public class RecorderService extends Service {

    private static final String TAG = "RecorderService";
    private RadioActivity caller;
    private RecordingTask recordingTask;
    private boolean recording;

    private final IBinder mBinder = new RecorderServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void startRecording(RadioActivity view, String streamUri) {
        Log.i(TAG, "RecorderService thread:  " + Looper.myLooper().getThread().getName());
        caller = view;
        caller.setStatus("Recording");
        recordingTask = new RecordingTask();
        Log.i(TAG, "Starting recordingTask");
        recordingTask.doInBackground(streamUri);
        Log.i(TAG, "Completed starting recordingTask off");
        recording = true;
    }

    public void stopRecording(RadioActivity view) {
        caller = view;
        if (recordingTask != null) {
            recordingTask.cancel(true);
            recordingTask = null;
        }
        view.setStatus("Cancelled recording");
        recording = false;
    }

    public boolean alreadyRecording() {
        return recording;
    }

    public class RecorderServiceBinder extends Binder {
        RecorderService getService() {
            return RecorderService.this;
        }
    }

    class RecordingTask extends AsyncTask<String, Void, Void> {

        FileOutputStream fileOutputStream;
        private boolean _recording;

        public boolean isRecording() {
            return _recording;
        }

        private void setRecording(boolean Recording) {
            this._recording = Recording;
        }

        @Override
        protected Void doInBackground(String... urls) {
            Log.i(TAG, "RecordingTask thread: " + Looper.myLooper().getThread().getName());
            try {
                URL url = new URL(urls[0]);
                Log.d(TAG, "RecordingTask attempting to stream from: " + url);
                InputStream inputStream = url.openStream();

                String recFolder = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Radio";

                if (!new File(recFolder).exists()) {
                    new File(recFolder).mkdir();
                    Log.d(TAG, "Radio directory was not found, so created it");
                }

                String outputSource = recFolder + File.separator + getTimestamp() + ".mp3";
                Log.d(TAG, "Writing stream to : " + outputSource);
                fileOutputStream = new FileOutputStream(outputSource);

                int c;
                int bytesRead = 0;

                while ((c = inputStream.read()) != -1) {
                    fileOutputStream.write(c);
                    bytesRead++;
                }

                Log.d(TAG, "Finished writing stream, " + bytesRead + " bytes written");

                fileOutputStream.flush();
                fileOutputStream.close();

            } catch (MalformedURLException e) {
                Log.e(TAG, "Uri malformed: " + e.getMessage(), e);
            } catch (IOException e) {
                Log.e(TAG, "IOException: " + e.getMessage(), e);
            }
            return null;
        }

        @Override
        public void onCancelled() {
            try {
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        private String getTimestamp() {
            Calendar calendar = new GregorianCalendar(TimeZone.getDefault());
            return String.valueOf(calendar.get(Calendar.YEAR)) +
                    pad(String.valueOf(calendar.get(Calendar.MONTH))) +
                    pad(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))) +
                    "-" +
                    pad(String.valueOf(calendar.get(Calendar.HOUR_OF_DAY))) +
                    pad(String.valueOf(calendar.get(Calendar.MINUTE))) +
                    pad(String.valueOf(calendar.get(Calendar.SECOND)));
        }

        private String pad(String original) {
            if (original.length() == 1) {
                return "0" + original;
            } else {
                return original;
            }
        }
    }


}
