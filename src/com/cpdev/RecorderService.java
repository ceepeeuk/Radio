package com.cpdev;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
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
        caller = view;
        caller.setStatus("Recording");
        recordingTask = new RecordingTask();
        recordingTask.execute(streamUri);
        recording = true;
    }

    public void stopRecording(RadioActivity view) {
        Log.d(TAG, "Stopping recording");
        caller = view;
        recording = false;
        if (recordingTask != null) {
            recordingTask.cancel(true);
        }
        view.setStatus("Stopped recording");
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
        InputStream inputStream;
        private boolean _recording;

        public boolean isRecording() {
            return _recording;
        }

        private void setRecording(boolean Recording) {
            this._recording = Recording;
        }

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

                int c;
                int bytesRead = 0;

                while ((c = inputStream.read()) != -1) {
                    fileOutputStream.write(c);
                    bytesRead++;
                }

                Log.i(TAG, "Finished writing stream, " + bytesRead * 1024 * 1024 + " megabytes written");

            } catch (MalformedURLException e) {
                Log.e(TAG, "Uri malformed: " + e.getMessage(), e);
            } catch (IOException e) {
                e.printStackTrace();
                // Expected when stream closes
            } finally {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error flushing and close output stream", e);
                }
            }
            return null;
        }

        @Override
        public void onCancelled() {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    Log.d(TAG, "Closed stream");
                } catch (IOException e) {
                    Log.e(TAG, "Failed to close input stream", e);
                }
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
