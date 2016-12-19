package com.example.android.silence;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by strai on 12/19/2016.
 *
 * Made custom adapter in case of future
 * ui updates
 */

public class SilenceAdapter extends ArrayAdapter<SilentLocale>{

    public SilenceAdapter(Context context, List objects){
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;

        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
        }

        final SilentLocale currentLocale = getItem(position);

        TextView localeName = (TextView) listItemView.findViewById(R.id.silent_name);
        localeName.setText(currentLocale.getSilentName());

        TextView localeAddress = (TextView) listItemView.findViewById(R.id.silent_address);
        localeAddress.setText(currentLocale.getAddress());

        return listItemView;
    }
}
