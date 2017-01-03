package com.example.android.silence;

import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
GoogleApiClient.OnConnectionFailedListener, LocationListener{

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private AudioManager mAudioManager;
    private Toast toast;
    private TextView mTxtLatitude;
    private TextView mTxtLongitude;
    private RecyclerView mRecycleList;
    private SilenceRecyclerAdapter mRecyclerAdapter;
    private LinearLayout mContainer;
    private Location mCurrentLocation;
    private ArrayList<SilentLocale> silentList;
    private int listSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        silentList = new ArrayList<>();
        listSize = silentList.size();

        mRecyclerAdapter = new SilenceRecyclerAdapter(silentList);
        mRecycleList = (RecyclerView) findViewById(R.id.silent_list);
        mRecycleList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mTxtLatitude = (TextView) findViewById(R.id.latitude);
        mTxtLongitude = (TextView) findViewById(R.id.longitude);
        mContainer = (LinearLayout) findViewById(R.id.button_container);
        mRecycleList.setAdapter(mRecyclerAdapter);

        mContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silentList.add(new SilentLocale(mCurrentLocation, MainActivity.this));
                mRecyclerAdapter.notifyDataSetChanged();
                listSize = silentList.size();
            }
        });

        buildGoogleApiClient();
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
        mTxtLongitude.setText(Double.toString(location.getLongitude()));
        mTxtLatitude.setText(Double.toString(location.getLatitude()));
        mCurrentLocation = location;
        int j = listSize;

        for(int i = 0; i < listSize; i++) {
            if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                if (silentList.get(i).isSilent(location)) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    toast = Toast.makeText(this, "You're silenced!", Toast.LENGTH_SHORT);
                    toast.show();
                     j = i;
                }
            }
            if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT && j < listSize) {
                if (!silentList.get(j).isSilent(location)) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    toast = Toast.makeText(this, "You're free ringing!", Toast.LENGTH_SHORT);
                    toast.show();
                    j = listSize;
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
}
