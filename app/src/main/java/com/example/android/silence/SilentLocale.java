package com.example.android.silence;

import android.location.Location;

/**
 * Created by strai on 12/14/2016.
 */

public class SilentLocale {
    private double mLat;
    private double mLon;
    private double mRadius;
    private float[] distance = new float[1];
    private String mName;
    public static final String SILENCE_TAG = SilentLocale.class.getSimpleName();

    public SilentLocale(double latitude, double longitude){
        mLat = latitude;
        mLon = longitude;
        mRadius = 50.0;
        mName = "LaunchCode";
    }

    /**
     *Returns the latitude of SilentLocale object
     */
    public double getSilentLatitude(){
        return mLat;
    }

    /**
     *Returns the longitude of SilentLocale object
     */
    public double getSilentLongitude(){
        return mLon;
    }

    /**
     *Returns the radius of SilentLocale object effect
     */
    public double getSilentRadius(){
        return mRadius;
    }

    /**
     *Returns the name of the SilentLocale
     */
    public String getSilentName(){
        return mName;
    }

    /**
     *Allows the user to set a new value for the radius
     */
    public void setSilentRadius(double radius){
        mRadius = radius;
    }

    /**
     *Takes the user's current location and calculates the
     * distance to the SilentLocale object and returns
     * true if it is within the radius and false otherwise
     * @param currentLocation the user's current location
     */
    public boolean isSilent(Location currentLocation){

        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                mLat, mLon, distance);

        if((double)distance[0] < mRadius){
            return true;
        }

        return false;
    }
}
