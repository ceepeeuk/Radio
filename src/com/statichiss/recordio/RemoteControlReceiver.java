package com.statichiss.recordio;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.KeyEvent;
import com.statichiss.R;


public class RemoteControlReceiver extends BroadcastReceiver {

    private static final String TAG = "com.statichiss.recordio.RemoteControlReceiver";

    public RemoteControlReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, "RemoteControlReceiver.onReceive");

        RadioApplication radioApplication = (RadioApplication) context.getApplicationContext();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();

        String intentAction = intent.getAction();
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            return;
        }

        KeyEvent event = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            return;
        }

        int action = event.getAction();
        int keyCode = event.getKeyCode();

        if (action == KeyEvent.ACTION_DOWN) {
            Intent playerIntent;
            switch (keyCode) {
                case KeyEvent.KEYCODE_MEDIA_STOP:
                    if (mediaPlayer.isPlaying()) {
                        Log.d(TAG, "Bluetooth stop received");
                        playerIntent = createPlayingIntent(context, null, RadioApplication.StopPlaying);
                        PlayerService.sendWakefulWork(context, playerIntent);
                    }
                    break;
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    if (mediaPlayer.isPlaying()) {
                        Log.d(TAG, "Bluetooth pause received");
                        playerIntent = createPlayingIntent(context, null, RadioApplication.PausePlaying);
                        PlayerService.sendWakefulWork(context, playerIntent);
                    } else {
                        Log.d(TAG, "Bluetooth resume received");
                        playerIntent = createPlayingIntent(context, null, RadioApplication.ResumePlaying);
                        PlayerService.sendWakefulWork(context, playerIntent);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    protected Intent createPlayingIntent(Context context, RadioDetails radioDetails, int operation) {
        Intent intent = new Intent("com.statichiss.recordio.PlayerService");

        if (radioDetails != null) {
            intent.putExtra(context.getString(R.string.radio_details_key), radioDetails);
        }

        intent.putExtra(context.getString(R.string.player_service_operation_key), operation);
        return intent;
    }
}

