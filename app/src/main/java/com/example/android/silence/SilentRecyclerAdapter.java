package com.example.android.silence;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.silence.data.SilentContract.SilentEntry;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by strai on 12/19/2016.
 */

public class SilentRecyclerAdapter extends RecyclerView.Adapter<SilentRecyclerAdapter.ViewHolder> {
    private Cursor mCursor;
    private Context mContext;
    private int mIdIndex;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout silentLayout;

        public ViewHolder(LinearLayout l){
            super(l);
            silentLayout = l;
        }

    }

    public SilentRecyclerAdapter(Cursor cursor, Context context){
        mCursor = cursor;
        mContext = context;
    }

    @Override
    public SilentRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        ViewHolder vh = new ViewHolder((LinearLayout) v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        mCursor.moveToPosition(position);
        int nameIndex = mCursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_NAME);
        int addressIndex = mCursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_ADDRESS);
        mIdIndex = mCursor.getColumnIndex(SilentEntry._ID);
        String address = getAddress();

        TextView silentName = (TextView) holder.silentLayout.findViewById(R.id.silent_name);
        silentName.setText(mCursor.getString(nameIndex));

        TextView silentAddress = (TextView) holder.silentLayout.findViewById(R.id.silent_address);
        if(address != null){
            silentAddress.setText(address);
        }else {
            silentAddress.setText(mCursor.getString(addressIndex));
        }

        holder.silentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentId = mCursor.getInt(mIdIndex);
                Intent intent = new Intent(mContext, EditActivity.class);
                Uri currentLocaleUri = ContentUris.withAppendedId(SilentEntry.CONTENT_URI, currentId);
                intent.setData(currentLocaleUri);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor){
        if(mCursor == newCursor){
            return;
        }
        if(newCursor != null){
            mCursor = newCursor;
            mIdIndex = mCursor.getColumnIndex(SilentEntry._ID);
            notifyDataSetChanged();
        }else{
            notifyItemRangeRemoved(0, getItemCount());
            mCursor = null;
        }
    }

    public String getAddress(){
        List<Address> address;
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        String silentAddress = "";
        int lonIndex = mCursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_LONGITUDE);
        int latIndex = mCursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_LATITUDE);
        double longitude = mCursor.getDouble(lonIndex);
        double latitude = mCursor.getDouble(latIndex);
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