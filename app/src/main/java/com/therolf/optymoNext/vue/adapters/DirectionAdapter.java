package com.therolf.optymoNext.vue.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.therolf.optymoNextModel.OptymoDirection;

@SuppressWarnings("unused")
public class DirectionAdapter extends BaseAdapter {

    private static final String TAG = "StopAdapter";

    private Context context;
    private OptymoDirection[] directions;

    public DirectionAdapter(@NonNull Context context, OptymoDirection[] directions) {
        this.context = context;
        this.directions = directions;
    }
    @Override
    public int getCount() {
        return directions.length;
    }

    @Override
    public Object getItem(int i) {
        return directions[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"SetTextI18n", "ViewHolder"})
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);

        ((TextView) convertView.findViewById(android.R.id.text1)).setText(directions[position].toString());

        return convertView;
    }
}
