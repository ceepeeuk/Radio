package com.statichiss.recordio;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.statichiss.R;
import com.statichiss.recordio.utils.StringUtils;

public class ScheduledRecordingsCursorAdaptor extends SimpleCursorAdapter {

    private int layout;

    public ScheduledRecordingsCursorAdaptor(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context, layout, c, from, to, 0);
        this.layout = layout;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Cursor c = getCursor();

        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(layout, parent, false);

        int stationColumn = c.getColumnIndex(DatabaseHelper.STATIONS_NAME);
        int typeColumn = c.getColumnIndex(DatabaseHelper.RECORDING_TYPES_TYPE);
        int startTimeColumn = c.getColumnIndex(DatabaseHelper.SCHEDULED_RECORDINGS_START_TIME);
        int endTimeColumn = c.getColumnIndex(DatabaseHelper.SCHEDULED_RECORDINGS_END_TIME);

        String station = c.getString(stationColumn);
        String type = c.getString(typeColumn);
        long start = c.getLong(startTimeColumn);
        long end = c.getLong(endTimeColumn);

        TextView station_text = (TextView) v.findViewById(R.id.station_entry);
        if (station_text != null) {
            station_text.setText(station);
        }

        TextView type_text = (TextView) v.findViewById(R.id.type_entry);
        if (type_text != null) {
            type_text.setText(type);
        }

        TextView start_time_text = (TextView) v.findViewById(R.id.start_time_entry);
        if (start_time_text != null) {
            start_time_text.setText(StringUtils.convertDateTimeToString(start));
        }

        TextView end_time_text = (TextView) v.findViewById(R.id.end_time_entry);
        if (end_time_text != null) {
            end_time_text.setText(StringUtils.convertDateTimeToString(end));
        }

        return v;

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        int stationColumn = cursor.getColumnIndex(DatabaseHelper.STATIONS_NAME);
        int typeColumn = cursor.getColumnIndex(DatabaseHelper.RECORDING_TYPES_TYPE);
        int startTimeColumn = cursor.getColumnIndex(DatabaseHelper.SCHEDULED_RECORDINGS_START_TIME);
        int endTimeColumn = cursor.getColumnIndex(DatabaseHelper.SCHEDULED_RECORDINGS_END_TIME);

        String station = cursor.getString(stationColumn);
        String type = cursor.getString(typeColumn);
        long start = cursor.getLong(startTimeColumn);
        long end = cursor.getLong(endTimeColumn);

        TextView station_text = (TextView) view.findViewById(R.id.station_entry);
        if (station_text != null) {
            station_text.setText(station);
        }

        TextView type_text = (TextView) view.findViewById(R.id.type_entry);
        if (type_text != null) {
            type_text.setText(type);
        }

        TextView start_time_text = (TextView) view.findViewById(R.id.start_time_entry);
        if (start_time_text != null) {
            start_time_text.setText(StringUtils.convertDateTimeToString(start));
        }

        TextView end_time_text = (TextView) view.findViewById(R.id.end_time_entry);
        if (end_time_text != null) {
            end_time_text.setText(StringUtils.convertDateTimeToString(end));
        }
    }
}
