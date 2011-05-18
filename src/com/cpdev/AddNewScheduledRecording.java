package com.cpdev;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class AddNewScheduledRecording extends Activity implements View.OnClickListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_scheduled_recording);

        Button addNewButton = (Button) findViewById(R.id.new_recording_cancel_button);
        addNewButton.setOnClickListener(this);
    }

    public void onClick(View view) {

    }
}