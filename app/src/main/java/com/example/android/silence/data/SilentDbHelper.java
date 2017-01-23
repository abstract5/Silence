package com.example.android.silence.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.silence.data.SilentContract.SilentEntry;

/**
 * Created by strai on 1/22/2017.
 */

public class SilentDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "silence.db";
    public static final int DATABASE_VERSION = 1;

    public SilentDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_LOCALES_TABLE = "CREATE TABLE " + SilentEntry.TABLE_NAME + "("
                + SilentEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + SilentEntry.COLUMN_LOCALE_NAME + " TEXT NOT NULL, "
                + SilentEntry.COLUMN_LOCALE_ADDRESS + " TEXT, "
                + SilentEntry.COLUMN_LOCALE_LONGITUDE + " REAL NOT NULL, "
                + SilentEntry.COLUMN_LOCALE_LATITUDE + " REAL NOT NULL, "
                + SilentEntry.COLUMN_LOCALE_RADIUS + " INTEGER NOT NULL DEFAULT 50);";

        db.execSQL(SQL_CREATE_LOCALES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
