package com.example.android.silence;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.silence.data.SilentContract.SilentEntry;

/**
 * Created by strai on 1/27/2017.
 */

public class SilentAdapter extends CursorAdapter {

    public SilentAdapter(Context context, Cursor c){
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView silentName = (TextView) view.findViewById(R.id.silent_name);
        TextView silentAddress = (TextView) view.findViewById(R.id.silent_address);

        int nameIndex = cursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_NAME);
        int addressIndex = cursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_ADDRESS);

        String name = cursor.getString(nameIndex);
        String address = cursor.getString(addressIndex);

        silentAddress.setText(address);
        silentName.setText(name);
    }
}
