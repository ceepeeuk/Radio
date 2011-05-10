package com.cpdev;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;


public class ConfirmDetailsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "com.cpdev.ConfirmDetailsActivity";

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.edit_fav_pop_up);

        Bundle bundle = this.getIntent().getExtras();
        RadioDetails radioDetails = bundle.getParcelable("RadioDetails");

        EditText txtName = (EditText) findViewById(R.id.edit_fav_pop_up_txt_name);
        EditText txtUrl = (EditText) findViewById(R.id.edit_fav_pop_up_txt_url);

        txtName.setText(radioDetails.getStationName());
        if (radioDetails.getPlaylistUrl() == null || radioDetails.getPlaylistUrl() == "") {
            txtUrl.setText(radioDetails.getStreamUrl());
        } else {
            txtUrl.setText(radioDetails.getPlaylistUrl());
        }

        Button cancelButton = (Button) findViewById(R.id.edit_fav_pop_up_btn_cancel);
        Button saveButton = (Button) findViewById(R.id.edit_fav_pop_up_btn_save);

        radioDetails.setStationName(txtName.getText().toString());
        radioDetails.setStreamUrl(txtUrl.getText().toString());

        cancelButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
    }

    public void onClick(View view) {

        Intent intent = new Intent(ConfirmDetailsActivity.this, RadioActivity.class);

        String txtName = ((EditText) findViewById(R.id.edit_fav_pop_up_txt_name)).getText().toString();
        String txtUrl = ((EditText) findViewById(R.id.edit_fav_pop_up_txt_url)).getText().toString();

        RadioDetails radioDetails = new RadioDetails(txtName, null, null);

        if (txtUrl.endsWith(".pls") || txtUrl.endsWith(".m3u")) {
            radioDetails.setPlaylistUrl(txtUrl);
        } else {
            radioDetails.setStreamUrl(txtUrl);
        }

        switch (view.getId()) {

            case R.id.edit_fav_pop_up_btn_save:

                if (txtName.equals("")) {
                    Toast.makeText(this, "Please enter a name before saving", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (txtUrl.equals("")) {
                    Toast.makeText(this, "Please enter a URL before saving", Toast.LENGTH_SHORT).show();
                    return;
                }

                DatabaseHelper dbHelper = new DatabaseHelper(this);

                try {
                    dbHelper.createDataBase();
                    dbHelper.openDataBase();
                    dbHelper.addFavourite(radioDetails);
                } catch (IOException e) {
                    Log.e(TAG, "IOException thrown when trying to access DB", e);
                } finally {
                    dbHelper.close();
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(findViewById(R.id.edit_fav_pop_up_txt_name).getWindowToken(), 0);

                startActivity(intent);
                break;

            case R.id.edit_fav_pop_up_btn_cancel:
                startActivity(intent);
        }


    }
}
