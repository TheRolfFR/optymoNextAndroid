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
import com.therolf.optymoNextModel.OptymoNextTime;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class OptymoNextTimeAdapter extends BaseAdapter {

    private static final String TAG = "OptymoNextTimeAdapter";

    private Context context;
    private ArrayList<OptymoNextTime> nextTimes;

    public OptymoNextTimeAdapter(@NonNull Context context, ArrayList<OptymoNextTime> nextTimes) {
        this.context = context;
        this.nextTimes = nextTimes;
    }

    @Override
    public int getCount() {
        return nextTimes.size();
    }

    @Override
    public Object getItem(int i) {
        return nextTimes.get(i);
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

        int id = context.getResources().getIdentifier("colorLine" + nextTimes.get(position).getLineNumber(), "color", context.getPackageName());
        convertView.findViewById(R.id.next_el_line).setBackgroundColor(ContextCompat.getColor(context, id));

        // change line number
        ((TextView) convertView.findViewById(R.id.next_el_line_number)).setText("" + nextTimes.get(position).getLineNumber());

        // change title
        ((TextView) convertView.findViewById(R.id.next_el_title)).setText(nextTimes.get(position).getStopName() + " - Dir. " + nextTimes.get(position).getDirection());

        // change time
        ((TextView) convertView.findViewById(R.id.next_el_time)).setText(nextTimes.get(position).getNextTime());

        return convertView;
    }
}
