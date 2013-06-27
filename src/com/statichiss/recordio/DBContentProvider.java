package com.statichiss.recordio;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by chris on 25/06/2013.
 */
public class DBContentProvider extends ContentProvider {

//    private SQLiteDatabase db = null;

    private DatabaseHelper dbHelper;

    // public constants for client development
    public static final String AUTHORITY = "com.statichiss.recordio.provider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    @Override
    public boolean onCreate() {
        dbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;

        if (getTableName(uri).equals("recording_schedule")) {
            StringBuilder query = new StringBuilder();
            query.append("SELECT RECORDING_SCHEDULE._ID, STATIONS.NAME, RECORDING_TYPE.TYPE, RECORDING_SCHEDULE.START_TIME, RECORDING_SCHEDULE.END_TIME ");
            query.append("FROM RECORDING_SCHEDULE, RECORDING_TYPE, STATIONS ");
            query.append("WHERE RECORDING_SCHEDULE.STATION = STATIONS._ID AND RECORDING_SCHEDULE.TYPE = RECORDING_TYPE._ID ");
            query.append("ORDER BY RECORDING_SCHEDULE.START_TIME");
            cursor = dbHelper.getWritableDatabase().rawQuery(query.toString(), new String[]{});
        } else {
            String table = getTableName(uri);
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            cursor = database.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        String table = getTableName(uri);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        long value = database.insert(table, null, contentValues);
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
    }

    @Override
    public int delete(Uri uri, String where, String[] args) {
        String table = getTableName(uri);
        SQLiteDatabase dataBase = dbHelper.getWritableDatabase();
        return dataBase.delete(table, where, args);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String whereClause, String[] whereArgs) {
        String table = getTableName(uri);
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        return database.update(table, contentValues, whereClause, whereArgs);
    }

    private String getTableName(Uri uri) {
        String value = uri.getPath();
        value = value.replace("/", "");//we need to remove '/'
        return value;
    }
}
