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

import com.therolf.optymoNext.R;
import com.therolf.optymoNextModel.OptymoStop;

@SuppressWarnings("unused")
public class StopAdapter extends BaseAdapter {

    private static final String TAG = "StopAdapter";

    private Context context;
    private OptymoStop[] nextStops;

    public void setData(OptymoStop[] arr) {
        this.nextStops = arr;
    }

    public StopAdapter(@NonNull Context context, OptymoStop[] nextStops) {
        this.context = context;
        this.nextStops = nextStops;
    }
    @Override
    public int getCount() {
        return nextStops.length;
    }

    @Override
    public Object getItem(int i) {
        return nextStops[i];
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
        convertView = inflater.inflate(R.layout.item_stop, parent, false);

        ((TextView) convertView.findViewById(R.id.text1)).setText(nextStops[position].toString());

        return convertView;
    }
}
