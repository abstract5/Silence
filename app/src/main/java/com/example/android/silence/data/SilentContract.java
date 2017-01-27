package com.example.android.silence.data;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.BaseColumns;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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

        public static String getAddress(Context context, Cursor cursor){
            List<Address> address;
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            String silentAddress = "";
            int lonIndex = cursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_LONGITUDE);
            int latIndex = cursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_LATITUDE);
            double longitude = cursor.getDouble(lonIndex);
            double latitude = cursor.getDouble(latIndex);
            try {
                address = geocoder.getFromLocation(latitude, longitude, 1);
                String streetAdd = address.get(0).getAddressLine(0);
                String city = address.get(0).getLocality();
                String postal = address.get(0).getPostalCode();

                silentAddress = silentAddress + streetAdd + ", " +
                        city + " " + postal;
            }catch(IOException e){
                silentAddress = null;
            }

            return silentAddress;
        }
    }
}
