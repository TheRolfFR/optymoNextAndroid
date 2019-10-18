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

import com.therolf.optymoNextModel.OptymoLine;

@SuppressWarnings("unused")
public class OptymoLineAdapter extends BaseAdapter {

    private static final String TAG = "OptymoStopAdapter";

    private Context context;
    private OptymoLine[] optymoLines;

    public OptymoLineAdapter(@NonNull Context context, OptymoLine[] optymoLines) {
        this.context = context;
        this.optymoLines = optymoLines;
    }
    @Override
    public int getCount() {
        return optymoLines.length;
    }

    @Override
    public Object getItem(int i) {
        return optymoLines[i];
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

        ((TextView) convertView.findViewById(android.R.id.text1)).setText(optymoLines[position].toString());

        return convertView;
    }
}
