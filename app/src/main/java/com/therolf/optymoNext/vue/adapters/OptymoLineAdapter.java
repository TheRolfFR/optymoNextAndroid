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
public class OptymoLineAdapter extends BaseAdapter {

    private static final String TAG = "OptymoStopAdapter";

    private Context context;
    private OptymoLine[] optymoLines;

    public void setData(OptymoLine[] arr) {
        this.optymoLines = arr;
    }

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
        convertView = inflater.inflate(R.layout.item_next_stop, parent, false);

        ((TextView) convertView.findViewById(R.id.next_el_line_number)).setText("" + optymoLines[position].getNumber());
        ((TextView) convertView.findViewById(R.id.next_el_title)).setText("" + optymoLines[position].getName());
        ((TextView) convertView.findViewById(R.id.next_el_time)).setText("");

        int id = context.getResources().getIdentifier("colorLine" + optymoLines[position].getNumber(), "color", context.getPackageName());
        convertView.findViewById(R.id.next_el_line).setBackgroundColor(ContextCompat.getColor(context, id));

        return convertView;
    }
}
