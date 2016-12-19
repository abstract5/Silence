package com.example.android.silence;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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
    private Context mContext;

    public SilentLocale(double latitude, double longitude, Context context){
        mLat = latitude;
        mLon = longitude;
        mRadius = 50.0;
        mName = "LaunchCode";
        mContext = context;
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

    public String getAddress(){
        String silentAddress = "";
        List<Address> address;
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        try {
            address = geocoder.getFromLocation(mLat, mLon, 1);
            String streetAdd = address.get(0).getAddressLine(0);
            String city = address.get(0).getLocality();
            String postal = address.get(0).getPostalCode();
            String country = address.get(0).getCountryName();

            silentAddress = silentAddress + streetAdd + ", " +
                    city + " " + postal + " " + country;
        }catch(IOException e){
            e.printStackTrace();
        }

        return silentAddress;
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
