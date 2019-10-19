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

@SuppressWarnings("unused")
public class OptymoNextTimeAdapter extends BaseAdapter {

    private static final String TAG = "OptymoNextTimeAdapter";

    private Context context;
    private OptymoNextTime[] nextTimes;

    public OptymoNextTimeAdapter(@NonNull Context context, OptymoNextTime[] nextTimes) {
        this.context = context;
        this.nextTimes = nextTimes;
    }
    @Override
    public int getCount() {
        return nextTimes.length;
    }

    @Override
    public Object getItem(int i) {
        return nextTimes[i];
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
        convertView = inflater.inflate(R.layout.next_element_list, parent, false);

        int id = context.getResources().getIdentifier("colorLine" + nextTimes[position].getLineNumber(), "color", context.getPackageName());
        convertView.findViewById(R.id.next_el_line).setBackgroundColor(ContextCompat.getColor(context, id));

        // change line number
        ((TextView) convertView.findViewById(R.id.next_el_line_number)).setText("" + nextTimes[position].getLineNumber());

        // change title
        ((TextView) convertView.findViewById(R.id.next_el_title)).setText(nextTimes[position].getStopName() + " - Dir. " + nextTimes[position].getDirection());

        // change time
        ((TextView) convertView.findViewById(R.id.next_el_time)).setText("" + nextTimes[position].getNextTime());

        return convertView;
    }
}
