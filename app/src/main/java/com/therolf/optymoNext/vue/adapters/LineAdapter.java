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
import androidx.core.content.ContextCompat;

import com.therolf.optymoNext.R;
import com.therolf.optymoNextModel.OptymoLine;

@SuppressWarnings("unused")
public class LineAdapter extends BaseAdapter {

    private static final String TAG = "StopAdapter";

    private Context context;
    private OptymoLine[] optymoLines;

    public void setData(OptymoLine[] arr) {
        this.optymoLines = arr;
    }

    public LineAdapter(@NonNull Context context, OptymoLine[] optymoLines) {
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
        convertView = inflater.inflate(R.layout.item_line, parent, false);

        ((TextView) convertView.findViewById(R.id.item_next_stop_number)).setText("" + optymoLines[position].getNumber());
        ((TextView) convertView.findViewById(R.id.item_next_stop_main)).setText("" + optymoLines[position].getName());
        ((TextView) convertView.findViewById(R.id.item_next_stop_time)).setText("");

        int id = context.getResources().getIdentifier("colorLine" + optymoLines[position].getNumber(), "color", context.getPackageName());
        convertView.findViewById(R.id.item_next_stop_line_bg).setBackgroundColor(ContextCompat.getColor(context, id));

        return convertView;
    }
}
