package com.statichiss.recordio;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.statichiss.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

public class RecordingsActivity extends RecordioBaseActivity {
    private String TAG = "com.statichiss.recordio.RecordingsActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recordings);

        final File recFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.app_name));
        final ArrayList<String> fileNames = getFileList();

        final ListView fileList = (ListView) findViewById(R.id.recordings_lst_files);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.recordings_list_item, fileNames);
        fileList.setAdapter(adapter);

        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View view, int pos, final long id) {

                if (!fileNames.get(0).equals(getString(R.string.no_recordings_available))) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle(fileNames.get((int) id))
                            .setItems(new String[]{"Play", "Rename", "Delete"}, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialogInterface, int item) {

                                    switch (item) {
                                        // Play
                                        case 0:
                                            final RadioApplication radioApplication = (RadioApplication) getApplication();

                                            if (alreadyPlaying() || radioApplication.isBuffering()) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(RecordingsActivity.this);

                                                StringBuilder sb = new StringBuilder("Stop playing ");
                                                if (!radioApplication.isBuffering()) {
                                                    sb.append(radioApplication.getPlayingType() == RadioApplication.PlayingStream ? radioApplication.getPlayingStation().getStationName() : radioApplication.getPlayingFileDetails().getName());
                                                }
                                                sb.append("?");

                                                builder.setMessage(sb.toString())
                                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                // Need to wait until buffering is complete before continuing
                                                                updateActivity("Preparing to play" + fileNames.get((int) id));
                                                                while (radioApplication.isBuffering()) {
                                                                    try {
                                                                        Thread.sleep(500);
                                                                    } catch (InterruptedException ignored) {
                                                                    }
                                                                }
                                                                radioApplication.getMediaPlayer().reset();
                                                                Intent playerIntent = new Intent("com.statichiss.recordio.PlayerService");
                                                                playerIntent.putExtra(getString(R.string.player_service_operation_key), RadioApplication.StartPlayingFile);
                                                                playerIntent.putExtra(getString(R.string.player_service_file_name_key), fileNames.get((int) id));
                                                                PlayerService.sendWakefulWork(RecordingsActivity.this, playerIntent);

                                                                Intent radioActivityIntent = new Intent(RecordingsActivity.this, RadioActivity.class);
                                                                startActivity(radioActivityIntent);
                                                            }
                                                        })
                                                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                            }
                                                        });
                                                builder.create().show();

                                            } else {

                                                Intent playerIntent = new Intent("com.statichiss.recordio.PlayerService");
                                                playerIntent.putExtra(getString(R.string.player_service_operation_key), RadioApplication.StartPlayingFile);
                                                playerIntent.putExtra(getString(R.string.player_service_file_name_key), fileNames.get((int) id));
                                                PlayerService.sendWakefulWork(RecordingsActivity.this, playerIntent);

                                                Intent radioActivityIntent = new Intent(RecordingsActivity.this, RadioActivity.class);
                                                startActivity(radioActivityIntent);
                                            }

                                            break;

                                        // Rename
                                        case 1:
                                            final EditText newName = new EditText(RecordingsActivity.this);
                                            newName.setSingleLine();
                                            newName.setText(fileNames.get((int) id));

                                            AlertDialog.Builder builder = new AlertDialog.Builder(RecordingsActivity.this)
                                                    .setTitle("Rename file")
                                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            if (!new File(recFolder, fileNames.get((int) id)).renameTo(new File(recFolder, newName.getText().toString()))) {
                                                                Log.e(TAG, "Failed to rename " + fileNames.get((int) id) + " to " + newName.getText().toString());
                                                                Toast.makeText(getApplicationContext(), "Failed to rename file", Toast.LENGTH_SHORT).show();
                                                            }
                                                            fileNames.set((int) id, newName.getText().toString());
                                                            adapter.notifyDataSetChanged();
                                                        }
                                                    })
                                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                        }
                                                    });
                                            builder.setView(newName);

                                            AlertDialog dialog = builder.create();
                                            dialog.show();
                                            break;

                                        // Delete
                                        case 2:

                                            new AlertDialog.Builder(RecordingsActivity.this)
                                                    .setMessage("Delete " + fileNames.get((int) id) + "?")
                                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                            if (!new File(recFolder, fileNames.get((int) id)).delete()) {
                                                                Log.e(TAG, "Failed to delete " + fileNames.get((int) id));
                                                                Toast.makeText(getApplicationContext(), "Failed to delete " + fileNames.get((int) id), Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                fileNames.remove((int) id);
                                                                if (fileNames.size() == 0) {
                                                                    fileNames.add("No recordings available");
                                                                }
                                                                adapter.notifyDataSetChanged();
                                                            }
                                                        }
                                                    })
                                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialogInterface, int i) {
                                                        }
                                                    })
                                                    .show();

                                            break;

                                        default:
                                            Log.e(TAG, "Unexpected option returned from File dialog, option #" + item);
                                            break;
                                    }
                                }
                            });
                    builder.create().show();
                }
            }
        });
    }

    private ArrayList<String> getFileList() {
        final File recFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.app_name));
        final String[] files = recFolder.list(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith("mp3");
            }
        });

        final ArrayList<String> fileNames = new ArrayList<String>();

        if (files != null && files.length > 0) {
            Collections.addAll(fileNames, files);
        } else {
            fileNames.add(getString(R.string.no_recordings_available));
        }
        return fileNames;
    }

    private void updateActivity(String text) {
        Intent intent = new Intent(getString(R.string.player_service_update_playing_key));
        ((RadioApplication) getApplication()).setPlayingStatus(text);
        getApplicationContext().sendBroadcast(intent);
    }

    @Override
    public void onBackPressed() {
        Intent RadioActivityIntent = new Intent(RecordingsActivity.this, RadioActivity.class);
        startActivity(RadioActivityIntent);
        finish();
    }
}