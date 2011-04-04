package com.cpdev;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RadioActivity extends Activity {

    Button btnPlay;
    Button btnRecord;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button btnPlay = (Button) findViewById(R.id.play);
        Button btnRecord = (Button) findViewById(R.id.play);

        setContentView(R.layout.main);
    }

    public void setStatus(String message) {
        TextView txtStatus = (TextView) findViewById(R.id.txt_status);
        txtStatus.setText(message);
    }

    public void playClick(View playButton) {
        setStatus("Play...clicked, so now do something");
    }

    public void recordClick(View recordButton) {
        setStatus("Record...clicked, oh dear stopped now");
    }
}