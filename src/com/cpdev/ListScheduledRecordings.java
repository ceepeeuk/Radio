package com.cpdev;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class ListScheduledRecordings extends Activity implements View.OnClickListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_recording_schedule);

        Button addNewButton = (Button) findViewById(R.id.list_recording_schedule_new_button);
        addNewButton.setOnClickListener(this);
    }

    public void onClick(View view) {
        Intent scheduledRecordingsIntent = new Intent(ListScheduledRecordings.this, AddNewScheduledRecording.class);
        startActivity(scheduledRecordingsIntent);
    }
}