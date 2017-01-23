package com.example.android.silence.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by strai on 1/22/2017.
 */

public final class SilentContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.silence";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_LOCALES = "locales";

    private SilentContract(){}

    public static final class SilentEntry implements BaseColumns{
        public static final String TABLE_NAME = "Locales";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_LOCALE_NAME = "name";
        public static final String COLUMN_LOCALE_ADDRESS = "address";
        public static final String COLUMN_LOCALE_RADIUS = "radius";
        public static final String COLUMN_LOCALE_LONGITUDE = "longitude";
        public static final String COLUMN_LOCALE_LATITUDE = "latitude";

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_LOCALES;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_LOCALES;

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_LOCALES);
    }
}
