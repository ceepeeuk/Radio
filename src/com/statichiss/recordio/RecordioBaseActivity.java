package com.statichiss.recordio;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import com.statichiss.R;


public class RecordioBaseActivity extends Activity {

    private static final int ADD_FAVOURITE = 1;
    private static final int SCHEDULED_RECORDINGS = 2;
    private static final int RECORDINGS = 3;
    private static final int EXIT = 4;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, ADD_FAVOURITE, Menu.NONE, "Add Favourite");
        menu.add(Menu.NONE, EXIT, Menu.NONE, "Exit");
        menu.add(Menu.NONE, SCHEDULED_RECORDINGS, Menu.NONE, "Scheduled Recordings");
        menu.add(Menu.NONE, RECORDINGS, Menu.NONE, "Recordings");
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        RadioApplication radioApplication = (RadioApplication) getApplication();
        switch (item.getItemId()) {

            case ADD_FAVOURITE:

                RadioDetails radioDetails = new RadioDetails();

                if (alreadyPlaying()) {
                    radioDetails = radioApplication.getPlayingStation();
                }

                Intent confirmDetailsIntent = new Intent(RecordioBaseActivity.this, ConfirmDetailsActivity.class);
                confirmDetailsIntent.putExtra(getString(R.string.radio_details_key), radioDetails);
                startActivity(confirmDetailsIntent);
                return true;

            case SCHEDULED_RECORDINGS:

                Intent scheduledRecordingsIntent = new Intent(RecordioBaseActivity.this, ListScheduledRecordingsActivity.class);
                startActivity(scheduledRecordingsIntent);
                return true;

            case RECORDINGS:

                Intent recordingsIntent = new Intent(RecordioBaseActivity.this, RecordingsActivity.class);
                startActivity(recordingsIntent);
                return true;

            case EXIT:
                // TODO - send an intent to RadioActivity, which lets it know that finish needs to be called.
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public boolean alreadyPlaying() {
        RadioApplication radioApplication = (RadioApplication) getApplication();
        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();
        return mediaPlayer != null && mediaPlayer.isPlaying();
    }
}