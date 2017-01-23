package com.example.android.silence;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
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
    private RecyclerView mRecycleList;
    private SilentRecyclerAdapter mRecyclerAdapter;
    private LinearLayout mContainer;
    private Location mCurrentLocation;
    private Cursor silentCursor;
    private int mCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);


        mCount = silentCursor.getCount();

        mRecyclerAdapter = new SilentRecyclerAdapter(silentCursor, this);
        mRecycleList = (RecyclerView) findViewById(R.id.silent_list);
        mRecycleList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mContainer = (LinearLayout) findViewById(R.id.button_container);
        mRecycleList.setAdapter(mRecyclerAdapter);

        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                startActivity(intent);
            }
        });

        buildGoogleApiClient();
        getLoaderManager().initLoader(0, null, this);
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
        int j = mCount;

        for(int i = 0; i < mCount; i++) {
            if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                if (silentCursor.get(i).isSilent(location)) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    Toast.makeText(this, "You're silenced!", Toast.LENGTH_SHORT).show();
                     j = i;
                }
            }
            if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT && j < mCount) {
                if (!silentCursor.get(j).isSilent(location)) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    Toast.makeText(this, "You're free ringing!", Toast.LENGTH_SHORT).show();
                    j = mCount;
                }
            }
        }
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
        if(mRecyclerAdapter == null){
            mRecyclerAdapter = new SilentRecyclerAdapter(silentCursor, MainActivity.this);
            mRecycleList.setAdapter(mRecyclerAdapter);
        }
        mRecyclerAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mRecyclerAdapter.swapCursor(null);
    }
}
