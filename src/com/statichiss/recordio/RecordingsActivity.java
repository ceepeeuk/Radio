package com.statichiss.recordio;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

                                            Toast.makeText(RecordingsActivity.this, "Not implemented", Toast.LENGTH_SHORT).show();
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

        //TODO: implement single press that sends file to playerservice and updates UI!
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
}