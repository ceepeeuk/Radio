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

    public static final String STATIONS_TABLE = "stations";
    public static final String STATIONS_ID = "_id";
    public static final String STATIONS_NAME = "name";
    public static final String STATIONS_URL = "url";

    private static final String SCHEDULED_RECORDINGS_TABLE = "recording_schedule";
    private static final String SCHEDULED_RECORDINGS_ID = "_id";
    public static final String SCHEDULED_RECORDINGS_START_TIME = "start_time";
    public static final String SCHEDULED_RECORDINGS_END_TIME = "end_time";
    public static final String SCHEDULED_RECORDINGS_STATION = "station";
    public static final String SCHEDULED_RECORDINGS_TYPE = "type";

    private static final String TAG = "com.cpdev.DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    // Creates a empty database on the system and rewrites it with your own database.
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

    // Check if the database already exist to avoid re-copying the file each time you open the application.
    private boolean checkDataBase() {

        SQLiteDatabase checkDB = null;

        try {

            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        } catch (SQLiteException e) {
            Log.e(TAG, "SQLException occurred in checkDataBase()", e);
        }

        if (checkDB != null) {
            checkDB.close();
        }

        return checkDB != null;
    }

    // Copies your database from your local assets-folder to the just created empty database in the
    // system folder, from where it can be accessed and handled.This is done by transfering bytestream.
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
        return myDataBase.query(FAVOURITES_TABLE, new String[]{FAVOURITES_ID, FAVOURITES_NAME, FAVOURITES_URL}, null, null, null, null, null);
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
        myDataBase.delete(FAVOURITES_TABLE, "_id=?", new String[]{Long.toString(id)});
    }

    public Cursor getRecordingTypes() {
        return myDataBase.query(RECORDING_TYPES_TABLE, new String[]{RECORDING_TYPES_ID, RECORDING_TYPES_TYPE}, null, null, null, null, null);
    }

    public void insertScheduledRecording(long startTime, long endTime, int station, int type) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(SCHEDULED_RECORDINGS_START_TIME, startTime);
        contentValues.put(SCHEDULED_RECORDINGS_END_TIME, endTime);
        contentValues.put(SCHEDULED_RECORDINGS_STATION, station + 1);
        contentValues.put(SCHEDULED_RECORDINGS_TYPE, type + 1);
        myDataBase.insert(SCHEDULED_RECORDINGS_TABLE, SCHEDULED_RECORDINGS_START_TIME, contentValues);
    }

    public Cursor getScheduledRecordingsList() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT RECORDING_SCHEDULE._ID, STATIONS.NAME, RECORDING_TYPE.TYPE, RECORDING_SCHEDULE.START_TIME, RECORDING_SCHEDULE.END_TIME ");
        query.append("FROM RECORDING_SCHEDULE, RECORDING_TYPE, STATIONS ");
        query.append("WHERE RECORDING_SCHEDULE.STATION = STATIONS._ID AND RECORDING_SCHEDULE.TYPE = RECORDING_TYPE._ID");
        return myDataBase.rawQuery(query.toString(), new String[]{});
    }

    public Cursor getAllScheduledRecordings() {
        String[] columns = {
                SCHEDULED_RECORDINGS_ID,
                SCHEDULED_RECORDINGS_START_TIME,
                SCHEDULED_RECORDINGS_END_TIME,
                SCHEDULED_RECORDINGS_STATION,
                SCHEDULED_RECORDINGS_TYPE};
        return myDataBase.query(SCHEDULED_RECORDINGS_TABLE, columns, null, null, null, null, null);
    }
}
