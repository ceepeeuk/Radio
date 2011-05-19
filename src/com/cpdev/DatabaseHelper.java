package com.cpdev;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.cpdev.utils.StringUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DatabaseHelper extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.cpdev/databases/";

    private static final String DB_NAME = "db";

    private SQLiteDatabase myDataBase;

    private final Context myContext;


    public static final String FAVOURITES_TABLE = "stations";
    public static final String FAVOURITES_ID = "_id";
    public static final String FAVOURITES_NAME = "name";
    public static final String FAVOURITES_URL = "url";

    public static final String RECORDING_TYPES_TABLE = "recording_type";
    public static final String RECORDING_TYPES_ID = "_id";
    public static final String RECORDING_TYPES_TYPE = "type";

    private static final String TAG = "com.cpdev.DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if (dbExist) {
            //do nothing - database already exist
        } else {

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {
                copyDataBase();
            } catch (IOException e) {
                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {

            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
        }

        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {
        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);

    }

    @Override
    public synchronized void close() {
        if (myDataBase != null)
            myDataBase.close();
        super.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public Cursor getFavourites() {
        Cursor cursor = myDataBase.query(FAVOURITES_TABLE, new String[]{FAVOURITES_ID, FAVOURITES_NAME, FAVOURITES_URL}, null, null, null, null, null);
        return cursor;
    }

    public void addFavourite(RadioDetails radioDetails) {
        Log.d(TAG, "Attempting to add: " + radioDetails);
        ContentValues contentValues = new ContentValues();
        contentValues.put(FAVOURITES_NAME, radioDetails.getStationName());
        if (StringUtils.IsNullOrEmpty(radioDetails.getPlaylistUrl())) {
            contentValues.put(FAVOURITES_URL, radioDetails.getStreamUrl());
        } else {
            contentValues.put(FAVOURITES_URL, radioDetails.getPlaylistUrl());
        }
        myDataBase.insert(FAVOURITES_TABLE, FAVOURITES_NAME, contentValues);
    }

    public void deleteFavourite(long id) {
        myDataBase.delete(FAVOURITES_TABLE, "_id=?", new String[]{new Long(id).toString()});
    }

    public Cursor getRecordingTypes() {
        Cursor cursor = myDataBase.query(RECORDING_TYPES_TABLE, new String[]{RECORDING_TYPES_ID, RECORDING_TYPES_TYPE}, null, null, null, null, null);
        return cursor;
    }
}
