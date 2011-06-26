package com.statichiss.recordio;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.statichiss.R;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;

public class RecordingsActivity extends Activity {
    private String TAG = "com.statichiss.recordio.RecordingsActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recordings);

        final File recFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getString(R.string.app_name));
        final ArrayList<String> fileNames = getFileList();

        final ListView fileList = (ListView) findViewById(R.id.recordings_lst_files);
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.recordings_list_item, fileNames);
        fileList.setAdapter(adapter);

        fileList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, final long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("File")
                        .setItems(new String[]{"Rename", "Delete"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int item) {
                                switch (item) {
                                    case 0:
                                        showRenamePopUp(recFolder.getAbsolutePath(), fileNames.get((int) id), fileNames, id);
                                        break;
                                    case 1:
                                        new File(recFolder, fileNames.get((int) id)).delete();
                                        fileNames.remove((int) id);
                                        if (fileNames.size() == 0) {
                                            fileNames.add("No recordings available");
                                        }
                                        break;
                                    default:
                                        Log.e(TAG, "Unexpected option returned from File dialog, option #" + item);
                                        break;
                                }
                                adapter.notifyDataSetChanged();
                            }
                        });
                builder.create().show();
                return false;
            }
        });

        //TODO: implement single press that sends file to playerservice and updates UI!
    }

    private void showRenamePopUp(final String recFolder, final String originalFileName, final ArrayList<String> fileNames, final long id) {

        final EditText newName = new EditText(this);
        newName.setSingleLine();
        newName.setText(originalFileName);

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Rename file")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new File(recFolder, originalFileName).renameTo(new File(recFolder, newName.getText().toString()));
                        fileNames.set((int) id, newName.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        builder.setView(newName);

        AlertDialog dialog = builder.create();
        dialog.show();
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
            fileNames.add("No recordings available");
        }
        return fileNames;
    }
}