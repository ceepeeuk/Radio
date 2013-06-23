package com.statichiss.recordio;

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

import com.statichiss.R;
import com.statichiss.recordio.utils.StringUtils;

import java.io.IOException;


public class ConfirmDetailsActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "com.statichiss.recordio.ConfirmDetailsActivity";
    private int MODE;
    private long radioDetailsId;

    private final int ADD_NEW_MODE = 0;
    private final int EDIT_MODE = 1;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.edit_fav_pop_up);

        Bundle bundle = this.getIntent().getExtras();

        RadioDetails radioDetails = null;

        if (bundle.getLong(getString(R.string.edit_favourite_id)) > 0) {
            // Edit mode
            MODE = EDIT_MODE;
            DatabaseHelper dbHelper = new DatabaseHelper(this);

            try {
                dbHelper.openDataBase();
                radioDetails = dbHelper.getFavourite(bundle.getLong(getString(R.string.edit_favourite_id)));
                radioDetailsId = bundle.getLong(getString(R.string.edit_favourite_id));
            } catch (IOException e) {
                Log.e(TAG, "IOException thrown when trying to access DB", e);
            } finally {
                dbHelper.close();
            }
        } else {
            // New mode
            MODE = ADD_NEW_MODE;
            radioDetails = bundle.getParcelable(getString(R.string.radio_details_key));
        }

        EditText txtName = (EditText) findViewById(R.id.edit_fav_pop_up_txt_name);
        EditText txtUrl = (EditText) findViewById(R.id.edit_fav_pop_up_txt_url);

        if (radioDetails != null) {
            txtName.setText(radioDetails.getStationName());
            if (StringUtils.IsNullOrEmpty(radioDetails.getPlaylistUrl())) {
                txtUrl.setText(radioDetails.getStreamUrl());
            } else {
                txtUrl.setText(radioDetails.getPlaylistUrl());
            }
            radioDetails.setStationName(txtName.getText().toString());
            radioDetails.setStreamUrl(txtUrl.getText().toString());
        }


        Button cancelButton = (Button) findViewById(R.id.edit_fav_pop_up_btn_cancel);
        Button saveButton = (Button) findViewById(R.id.edit_fav_pop_up_btn_save);

        cancelButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
    }

    public void onClick(View view) {

        Intent intent = new Intent(ConfirmDetailsActivity.this, MainActivity.class);

        String txtName = ((EditText) findViewById(R.id.edit_fav_pop_up_txt_name)).getText().toString();
        String txtUrl = ((EditText) findViewById(R.id.edit_fav_pop_up_txt_url)).getText().toString();

        RadioDetails radioDetails = new RadioDetails(radioDetailsId, txtName, null, null);

        if (txtUrl.endsWith(".pls") || txtUrl.endsWith(".m3u")) {
            radioDetails.setPlaylistUrl(txtUrl);
        } else {
            radioDetails.setStreamUrl(txtUrl);
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

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
                    dbHelper.openDataBase();
                    switch (MODE) {
                        case EDIT_MODE:
                            dbHelper.updateFavourite(radioDetails);
                            break;
                        case ADD_NEW_MODE:
                            dbHelper.addFavourite(radioDetails);
                            break;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "IOException thrown when trying to access DB", e);
                } finally {
                    dbHelper.close();
                }

                imm.hideSoftInputFromWindow(findViewById(R.id.edit_fav_pop_up_txt_name).getWindowToken(), 0);

                finish();
                break;

            case R.id.edit_fav_pop_up_btn_cancel:
                onBackPressed();
                imm.hideSoftInputFromWindow(findViewById(R.id.edit_fav_pop_up_txt_name).getWindowToken(), 0);
                break;
        }


    }

    @Override
    public void onBackPressed() {
        Intent RadioActivityIntent = new Intent(ConfirmDetailsActivity.this, MainActivity.class);
        startActivity(RadioActivityIntent);
        finish();
    }
}
