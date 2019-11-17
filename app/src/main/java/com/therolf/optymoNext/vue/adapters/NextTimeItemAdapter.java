package com.therolf.optymoNext.vue.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.global.Utility;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.util.ArrayList;
import java.util.Collections;

@SuppressWarnings("unused")
public class NextTimeItemAdapter extends BaseAdapter {

    private static final String TAG = "NextTimeItemAdapter";

    private Context context;
    private ArrayList<OptymoNextTime> nextTimes;
    private ListView listView;

    public NextTimeItemAdapter(@NonNull Context context, ArrayList<OptymoNextTime> nextTimes) {
        this.context = context;
        this.nextTimes = nextTimes;
    }

    void addNextTime(OptymoNextTime nextTime) {
        nextTimes.add(nextTime);
        Collections.sort(nextTimes);
        notifyDataSetChanged();
        udpateHeight();
    }

    int getLineNumber() {
        int v = 0;
        if(nextTimes != null && nextTimes.size() > 0) {
            v = nextTimes.get(0).getLineNumber();
        }

        return v;
    }

    void removeNextTime(OptymoNextTime nextTime) {
        nextTimes.remove(nextTime);
        udpateHeight();
    }

    private void udpateHeight() {
        if(listView != null) {
            Utility.setListViewHeightBasedOnChildren(listView);
        }
    }

    String getDirection() {
        String v = "";
        if(nextTimes != null && nextTimes.size() > 0) {
            v = nextTimes.get(0).getDirection();
        }

        return v;
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

        // change stop name
        ((TextView) convertView.findViewById(R.id.item_next_stop_stop_name)).setText(nextTimes.get(position).getStopName());

        // change time
        ((TextView) convertView.findViewById(R.id.item_next_stop_time)).setText(nextTimes.get(position).getNextTime());

        return convertView;
    }

    void setListView(ListView itemListView) {
        this.listView = itemListView;
    }

    public interface ItemLongClickListener {
        void onItemLongClick(Object nextTime);
    }
}
