package com.example.android.silence.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.android.silence.R;
import com.example.android.silence.data.SilentContract.SilentEntry;

/**
 * Created by strai on 1/22/2017.
 */

public class SilentProvider extends ContentProvider {

    public static final String LOG_TAG = SilentProvider.class.getSimpleName();
    private SilentDbHelper mDbHelper;

    private static final int LOCALES = 100;
    private static final int LOCALES_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(SilentContract.CONTENT_AUTHORITY, SilentContract.PATH_LOCALES, LOCALES);
        sUriMatcher.addURI(SilentContract.CONTENT_AUTHORITY, SilentContract.PATH_LOCALES + "/#", LOCALES_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new SilentDbHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;
        final int match = sUriMatcher.match(uri);

        switch(match){
            case LOCALES:
                cursor = db.query(SilentEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case LOCALES_ID:
                selection = SilentEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(SilentEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch(match){
            case LOCALES:
                return SilentEntry.CONTENT_LIST_TYPE;
            case LOCALES_ID:
                return SilentEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unkown uri " + uri +
                        " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final int match = sUriMatcher.match(uri);

        switch(match){
            case LOCALES:
               return insertLocale(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertLocale(Uri uri, ContentValues values){
        String name = values.getAsString(SilentEntry.COLUMN_LOCALE_NAME);
        Double lat = values.getAsDouble(SilentEntry.COLUMN_LOCALE_LATITUDE);
        Double lon = values.getAsDouble(SilentEntry.COLUMN_LOCALE_LONGITUDE);
        Integer radius = values.getAsInteger(SilentEntry.COLUMN_LOCALE_RADIUS);

        if(name == null){
            throw new IllegalArgumentException("Location requires a name.");
        }
        if(lat == null || lon == null){
            throw new IllegalArgumentException("Location requires a latitude and longitude.");
        }
        if(radius <= 0){
            throw new IllegalArgumentException("Location requires a valid radius.");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(SilentEntry.TABLE_NAME, null, values);

        if(id == -1){
            Toast.makeText(getContext(), R.string.location_add_error, Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }else{
            Toast.makeText(getContext(), R.string.add_new_location, Toast.LENGTH_SHORT).show();
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        switch(match){
            case LOCALES:
                rowsDeleted = db.delete(SilentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case LOCALES_ID:
                selection = SilentEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(SilentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if(rowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);

        switch(match){
            case LOCALES:
                return updateLocale(uri, values, selection, selectionArgs);
            case LOCALES_ID:
                selection = SilentEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                return updateLocale(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    public int updateLocale(Uri uri, ContentValues values, String selection, String[] selectionArgs){
        if(values.containsKey(SilentEntry.COLUMN_LOCALE_NAME)){
            String name = values.getAsString(SilentEntry.COLUMN_LOCALE_NAME);
            if(name == null){
                throw new IllegalArgumentException("Location requires a name.");
            }
        }
        if(values.containsKey(SilentEntry.COLUMN_LOCALE_LONGITUDE)){
            Double lon = values.getAsDouble(SilentEntry.COLUMN_LOCALE_LONGITUDE);
            if(lon == null){
                throw new IllegalArgumentException("Location must contain a valid latitude and longitude.");
            }
        }
        if(values.containsKey(SilentEntry.COLUMN_LOCALE_LATITUDE)){
            Double lat = values.getAsDouble(SilentEntry.COLUMN_LOCALE_LATITUDE);
            if(lat == null){
                throw new IllegalArgumentException("Location must contain a valid latitude and longitude.");
            }
        }
        if(values.containsKey(SilentEntry.COLUMN_LOCALE_RADIUS)){
            Integer radius = values.getAsInteger(SilentEntry.COLUMN_LOCALE_RADIUS);
            if(radius == null || radius <= 0){
                throw new IllegalArgumentException("Location must have a valid radius.");
            }
        }
        if(values.size() == 0){
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int updatedRows = db.update(SilentEntry.TABLE_NAME, values, selection, selectionArgs);

        if(updatedRows != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return updatedRows;
    }
}
