package com.example.android.silence;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.silence.data.SilentContract.SilentEntry;

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
        mIdIndex = mCursor.getColumnIndex(SilentEntry._ID);
    }

    @Override
    public SilentRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        ViewHolder vh = new ViewHolder((LinearLayout) v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        int nameIndex = mCursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_NAME);
        int addressIndex = mCursor.getColumnIndex(SilentEntry.COLUMN_LOCALE_ADDRESS);

        TextView silentName = (TextView) holder.silentLayout.findViewById(R.id.silent_name);
        silentName.setText(mCursor.getString(nameIndex));

        TextView silentAddress = (TextView) holder.silentLayout.findViewById(R.id.silent_address);
        silentAddress.setText(mCursor.getString(addressIndex));

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
}