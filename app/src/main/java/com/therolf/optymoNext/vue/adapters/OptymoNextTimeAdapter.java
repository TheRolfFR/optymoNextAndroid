package com.therolf.optymoNext.vue.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

        // change color
        int colorId = R.color.colorLine3;
        switch (nextTimes[position].getLineNumber()) {
            case 1:
                colorId = R.color.colorLine1;
                break;
            case 2:
                colorId = R.color.colorLine2;
                break;
            case 3:
                colorId = R.color.colorLine3;
                break;
            case 4:
                colorId = R.color.colorLine4;
                break;
            case 5:
                colorId = R.color.colorLine5;
                break;
            case 8:
                colorId = R.color.colorLine8;
                break;
            default:
                break;
        }
        ((LinearLayout) convertView.findViewById(R.id.next_el_line)).setBackgroundColor(colorId);

        // change line number
        ((TextView) convertView.findViewById(R.id.next_el_line_number)).setText("" + nextTimes[position].getLineNumber());

        // change title
        ((TextView) convertView.findViewById(R.id.next_el_title)).setText(nextTimes[position].getStopName() + " - Dir. " + nextTimes[position].getDirection());

        // change time
        ((TextView) convertView.findViewById(R.id.next_el_time)).setText("" + nextTimes[position].getNextTime());

        return convertView;
    }
}
