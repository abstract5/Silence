package com.example.android.silence;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.silence.data.SilentContract.SilentEntry;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener, LocationListener, LoaderManager.LoaderCallbacks<Cursor>{

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private AudioManager mAudioManager;
    private ListView mSilentList;
    private SilentAdapter mSilentAdapter;
    private LinearLayout mContainer;
    private Location mCurrentLocation;
    private TextView longTextView;
    private TextView latTextView;
    private double mLongitude;
    private int mLongitudeIndex;
    private double mLatitude;
    private int mLatitudeIndex;
    private int mRadius;
    private int mRadiusIndex;
    private Cursor mSilentCursor;
    private int mCount;
    private int mCountReset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        mSilentAdapter = new SilentAdapter(this, null);
        mSilentList = (ListView) findViewById(R.id.silent_list);
        mSilentList.setAdapter(mSilentAdapter);

        longTextView = (TextView) findViewById(R.id.longitude);
        latTextView = (TextView) findViewById(R.id.latitude);

        mContainer = (LinearLayout) findViewById(R.id.button_container);

        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, EditActivity.class);
//                startActivity(intent);
                insertDummyData();
            }
        });

        mSilentList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);

                Uri currentLocaleUri = ContentUris.withAppendedId(SilentEntry.CONTENT_URI, id);
                intent.setData(currentLocaleUri);

                startActivity(intent);
            }
        });

        getLoaderManager().initLoader(0, null, this);
        buildGoogleApiClient();
        mCountReset = mCount;
    }

    @Override
    protected void onStop() {
        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_delete_all:
                deleteAllLocations();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5000);
        try{
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }catch(SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(LOG_TAG, "Connection failed: " + connectionResult.getErrorCode());
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        latTextView.setText(Double.toString(location.getLatitude()));
        longTextView.setText(Double.toString(location.getLongitude()));

        for(int i = 0; i < mCount; i++){
            mSilentCursor.moveToPosition(i);
            getLocationData();

            if(mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT){
                if(isSilent(location)){
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    Toast.makeText(this, "You're silenced!", Toast.LENGTH_SHORT).show();
                    mCountReset = i;
                }
            }
            if(mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT && mCountReset < mCount){
                mSilentCursor.moveToPosition(mCountReset);
                getLocationData();
                if(!isSilent(location)){
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    Toast.makeText(this, "You're free ringing!", Toast.LENGTH_SHORT).show();
                    mCountReset = mCount;
                }
            }
        }
    }

    private void getLocationData(){
        mLatitude = mSilentCursor.getDouble(mLatitudeIndex);
        mLongitude = mSilentCursor.getDouble(mLongitudeIndex);
        mRadius = mSilentCursor.getInt(mRadiusIndex);
    }

    public synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public void deleteAllLocations(){
        int rowsDeleted = getContentResolver().delete(SilentEntry.CONTENT_URI, null, null);
        Log.v(LOG_TAG, rowsDeleted + " rows deleted from silence database.");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {SilentEntry._ID,
                SilentEntry.COLUMN_LOCALE_NAME,
                SilentEntry.COLUMN_LOCALE_ADDRESS,
                SilentEntry.COLUMN_LOCALE_LONGITUDE,
                SilentEntry.COLUMN_LOCALE_LATITUDE,
                SilentEntry.COLUMN_LOCALE_RADIUS};

        return new CursorLoader(MainActivity.this,
                SilentEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(mSilentAdapter == null){
            mSilentAdapter = new SilentAdapter(this, null);
            mSilentList.setAdapter(mSilentAdapter);
        }
        mSilentCursor = data;
        mSilentAdapter.swapCursor(mSilentCursor);

        mCount = mSilentCursor.getCount();
        mLongitudeIndex = mSilentCursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_LONGITUDE);
        mLatitudeIndex = mSilentCursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_LATITUDE);
        mRadiusIndex = mSilentCursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_RADIUS);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSilentAdapter.swapCursor(null);
    }

    public boolean isSilent(Location currentLocation){

        float[] distance = new float[1];

        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                mLatitude, mLongitude, distance);

        Log.i(LOG_TAG, "Distance from location is " + distance[0] +" and radius is " + mRadius);

        if((double)distance[0] < mRadius){
            return true;
        }

        return false;
    }


    public void insertDummyData(){
        ContentValues values = new ContentValues();
        values.put(SilentEntry.COLUMN_LOCALE_NAME, "Silent Locale");
        values.put(SilentEntry.COLUMN_LOCALE_ADDRESS, "No address available");
        values.put(SilentEntry.COLUMN_LOCALE_LONGITUDE, mCurrentLocation.getLongitude());
        values.put(SilentEntry.COLUMN_LOCALE_LATITUDE, mCurrentLocation.getLatitude());
        values.put(SilentEntry.COLUMN_LOCALE_RADIUS, 5);

        getContentResolver().insert(SilentEntry.CONTENT_URI, values);
        mSilentAdapter.notifyDataSetChanged();
    }
}
