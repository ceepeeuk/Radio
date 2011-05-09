package com.cpdev;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

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

//    private void confirmDetails(final RadioDetails radioDetails) {
//
//        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        final View layout = layoutInflater.inflate(R.layout.edit_fav_pop_up, null, false);
//        final PopupWindow pw = new PopupWindow(layout, 250, 300, true);
//
//        final EditText txtName = (EditText) layout.findViewById(R.id.edit_fav_pop_up_txt_name);
//        final EditText txtUrl = (EditText) layout.findViewById(R.id.edit_fav_pop_up_txt_url);
//
//        txtName.setText(radioDetails.getStationName());
//        if (radioDetails.getPlaylistUrl() == null || radioDetails.getPlaylistUrl() == "") {
//            txtUrl.setText(radioDetails.getStreamUrl());
//        } else {
//            txtUrl.setText(radioDetails.getPlaylistUrl());
//        }
//
//        Button cancelButton = (Button) layout.findViewById(R.id.edit_fav_pop_up_btn_cancel);
//        cancelButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View vv) {
//                pw.dismiss();
//            }
//        });
//
//        Button saveButton = (Button) layout.findViewById(R.id.edit_fav_pop_up_btn_save);
//        saveButton.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View vv) {
//                radioDetails.setStationName(txtName.getText().toString());
//                radioDetails.setStreamUrl(txtUrl.getText().toString());
//                pw.dismiss();
//
//                try {
//                    dbHelper.createDataBase();
//                    dbHelper.openDataBase();
//                    dbHelper.addFavourite(radioDetails);
//                } catch (IOException e) {
//                    Log.e(TAG, "IOException thrown when trying to access DB", e);
//                } finally {
//                    dbHelper.close();
//                }
//            }
//        });
//
//        pw.showAtLocation(this.findViewById(R.id.layout_main), Gravity.CENTER, 0, 0);
//    }

    public void onClick(View view) {
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
                break;
        }

        Intent intent = new Intent(ConfirmDetailsActivity.this, RadioActivity.class);
        startActivity(intent);
    }
}
