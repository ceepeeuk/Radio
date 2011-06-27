package com.statichiss.recordio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.statichiss.R;

public class CallReceiver extends BroadcastReceiver {

    private static final String TAG = "com.statichiss.recordio.CallReceiver";
    private Context appContext;

    public void onReceive(Context context, Intent intent) {
        RecordioPhoneStateListener recordioPhoneStateListener = new RecordioPhoneStateListener();
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(recordioPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        appContext = context;
    }

    private class RecordioPhoneStateListener extends PhoneStateListener {
        Intent intent;

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            RadioApplication radioApplication = (RadioApplication) appContext.getApplicationContext();
            MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();

            if (mediaPlayer != null) {
                switch (state) {
                    case TelephonyManager.CALL_STATE_RINGING:
                        // pause
                        if (mediaPlayer.isPlaying()) {
                            Log.d(TAG, "Phone ringing, need to pause playback");
                            intent = createPlayingIntent(RadioApplication.PausePlayingRadio);
                            WakefulPlayerService.sendWakefulWork(appContext, intent);
                        }
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        // pause
                        if (mediaPlayer.isPlaying()) {
                            Log.d(TAG, "Phone offhook, need to pause playback");
                            intent = createPlayingIntent(RadioApplication.PausePlayingRadio);
                            WakefulPlayerService.sendWakefulWork(appContext, intent);
                        }
                        break;
                    case TelephonyManager.CALL_STATE_IDLE:
                        // resume
                        Log.d(TAG, "Phone now idle, resuming playing");
                        intent = createPlayingIntent(RadioApplication.ResumePlayingRadio);
                        WakefulPlayerService.sendWakefulWork(appContext, intent);
                        break;
                }
            }
        }
    }

    private Intent createPlayingIntent(int operation) {
        Intent intent = new Intent("com.statichiss.recordio.WakefulPlayerService");
        intent.putExtra(appContext.getString(R.string.player_service_operation_key), operation);
        return intent;
    }
}
