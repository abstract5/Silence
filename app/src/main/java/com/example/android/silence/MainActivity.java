package com.example.android.silence;

import android.location.Location;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;
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
    private SilentLocale launchCode = new SilentLocale(38.651703, -90.2593865);
    private AudioManager mAudioManager;
    private Toast toast;
    private TextView mTxtLatitude;
    private TextView mTxtLongitude;
    private ListView mList;
    private SilenceAdapter mAdapter;
    private  ArrayList<SilentLocale> silentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        silentList = new ArrayList<>();
        silentList.add(launchCode);
        mAdapter = new SilenceAdapter(this, silentList);
        mList = (ListView) findViewById(R.id.silent_list);
        mTxtLatitude = (TextView) findViewById(R.id.latitude);
        mTxtLongitude = (TextView) findViewById(R.id.longitude);
        mList.setAdapter(mAdapter);
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

        for(int i = 0; i < silentList.size(); i++) {
            if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                if (silentList.get(i).isSilent(location)) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    toast = Toast.makeText(this, "You're silenced!", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                if (!silentList.get(i).isSilent(location)) {
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    toast = Toast.makeText(this, "You're free ringing!", Toast.LENGTH_SHORT);
                    toast.show();
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
