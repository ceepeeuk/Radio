package com.cpdev.recording;

import android.content.Intent;
import android.os.Bundle;
import com.commonsware.cwac.wakeful.WakefulIntentService;
import com.cpdev.R;
import com.cpdev.RadioApplication;
import com.cpdev.RadioDetails;

public class TimedRecorderService extends WakefulIntentService {

    public TimedRecorderService() {
        super("TimedRecorderService");
    }

    @Override
    protected void doWakefulWork(Intent intent) {
        Bundle bundle = intent.getExtras();
        String name = bundle.getString(getString(R.string.timed_recorder_service_name_key));
        String url = bundle.getString(getString(R.string.timed_recorder_service_url_key));
        long duration = bundle.getLong(getString(R.string.timed_recorder_service_recording_duration));

        RadioApplication radioApplication = (RadioApplication) this.getApplicationContext();
        RadioDetails radioDetails = new RadioDetails(name, null, url, duration);
        //radioApplication.getRecordingTask().execute(radioDetails);
    }
}
