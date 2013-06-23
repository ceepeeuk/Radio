//package com.statichiss.recordio;
//
//import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.media.MediaPlayer;
//import android.view.Menu;
//import android.view.MenuItem;
//
//import com.statichiss.R;
//import com.statichiss.recordio.recording.RecorderService;
//
//
//public class RecordioBaseActivity extends Activity {
//
//    private static final int ADD_FAVOURITE = 1;
//    private static final int EXIT = 2;
//    private static final int SCHEDULED_RECORDINGS = 3;
//    private static final int RECORDINGS = 4;
//    private static final String TAG = "com.statichiss.recordio.RecordioBaseActivity";
//
//    @Override
//    public void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    public void finish() {
//        super.finish();
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        menu.add(Menu.NONE, ADD_FAVOURITE, Menu.NONE, "Add Favourite");
//        menu.add(Menu.NONE, EXIT, Menu.NONE, "Exit");
//        menu.add(Menu.NONE, SCHEDULED_RECORDINGS, Menu.NONE, "Scheduled Recordings");
//        menu.add(Menu.NONE, RECORDINGS, Menu.NONE, "Recordings");
//        return (super.onCreateOptionsMenu(menu));
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        RadioApplication radioApplication = (RadioApplication) getApplication();
//        switch (item.getItemId()) {
//
//            case ADD_FAVOURITE:
//
//                RadioDetails radioDetails = new RadioDetails();
//
//                if (alreadyPlaying()) {
//                    radioDetails = radioApplication.getPlayingStation();
//                }
//
//                Intent confirmDetailsIntent = new Intent(RecordioBaseActivity.this, ConfirmDetailsActivity.class);
//                confirmDetailsIntent.putExtra(getString(R.string.radio_details_key), radioDetails);
//                startActivity(confirmDetailsIntent);
//                finish();
//                return true;
//
//            case SCHEDULED_RECORDINGS:
//
//                Intent scheduledRecordingsIntent = new Intent(RecordioBaseActivity.this, ListScheduledRecordingsActivity.class);
//                startActivity(scheduledRecordingsIntent);
//                finish();
//                return true;
//
//            case RECORDINGS:
//
//                Intent recordingsIntent = new Intent(RecordioBaseActivity.this, RecordingsActivity.class);
//                startActivity(recordingsIntent);
//                finish();
//                return true;
//
//            case EXIT:
//                AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                builder.setMessage("Exit Recordio?")
//                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                checkIfExitingFromRadioActivity();
//                            }
//                        })
//                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                            }
//                        });
//                builder.create().show();
//                return true;
//
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }
//
//    private void checkIfExitingFromRadioActivity() {
////        if (getClass().getSimpleName().equals("RadioActivity")) {
//        MediaPlayer mediaPlayer = ((RadioApplication) getApplication()).getMediaPlayer();
//        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
//            PlayerService.sendWakefulWork(getApplicationContext(), createPlayingIntent(null, RadioApplication.StopPlaying));
//        }
//        if (RecorderService.alreadyRecording()) {
//            RecorderService.cancelRecording();
//        }
////        } else {
////            ((RadioApplication) getApplication()).
//// (true);
////        }
//        finish();
//    }
//
//
//    public boolean alreadyPlaying() {
//        RadioApplication radioApplication = (RadioApplication) getApplication();
//        MediaPlayer mediaPlayer = radioApplication.getMediaPlayer();
//        return mediaPlayer != null && mediaPlayer.isPlaying();
//    }
//
//    protected Intent createRecordingIntent(RadioDetails radioDetails) {
//        Intent intent = new Intent("com.statichiss.recordio.recording.RecorderService");
//        if (radioDetails != null) {
//            intent.putExtra(getString(R.string.radio_details_key), radioDetails);
//        }
//        return intent;
//    }
//
//    protected Intent createPlayingIntent(RadioDetails radioDetails, int operation) {
//        Intent intent = new Intent("com.statichiss.recordio.PlayerService");
//
//        if (radioDetails != null) {
//            intent.putExtra(getString(R.string.radio_details_key), radioDetails);
//        }
//
//        intent.putExtra(getString(R.string.player_service_operation_key), operation);
//        return intent;
//    }
//}