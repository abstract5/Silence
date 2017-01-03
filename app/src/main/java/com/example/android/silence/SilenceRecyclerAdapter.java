package com.example.android.silence;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by strai on 12/19/2016.
 */

public class SilenceRecyclerAdapter extends RecyclerView.Adapter<SilenceRecyclerAdapter.ViewHolder> {
    private ArrayList<SilentLocale> mList;

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public LinearLayout silentLayout;

        public ViewHolder(LinearLayout l){
            super(l);
            silentLayout = l;
        }

    }

    public SilenceRecyclerAdapter(ArrayList<SilentLocale> list){
        mList = list;
    }

    @Override
    public SilenceRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);

        ViewHolder vh = new ViewHolder((LinearLayout) v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position){
        TextView silentName = (TextView) holder.silentLayout.findViewById(R.id.silent_name);
        silentName.setText(mList.get(position).getSilentName());

        TextView silentAddress = (TextView) holder.silentLayout.findViewById(R.id.silent_address);
        silentAddress.setText(mList.get(position).getAddress());

        holder.silentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mList.get(position).SilentIntent();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }
}