package com.therolf.optymoNext.vue.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.Utility;
import com.therolf.optymoNextModel.OptymoNextTime;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class LineNextTimeAdapter extends BaseAdapter {

    private static final String TAG = "LineNextTimeAdapter";

    private Context context;
    private ArrayMap<String, NextTimeItemAdapter> nextTimeAdapters = new ArrayMap<>();
    private ListView listView;
    private NextTimeItemAdapter.ItemLongClickListener itemListener;

    public void addNextTime(Context context, OptymoNextTime nextTime) {
        // add adapter and key
        if(!nextTimeAdapters.containsKey(nextTime.getLineToString())) {
            nextTimeAdapters.put(nextTime.getLineToString(), new NextTimeItemAdapter(context, new ArrayList<>()));
        }

        NextTimeItemAdapter adapter = nextTimeAdapters.get(nextTime.getLineToString());
        if(adapter != null) {
            adapter.addNextTime(nextTime);
            adapter.notifyDataSetChanged();
            if(listView != null)
                Utility.setListViewHeightBasedOnChildren(listView);
        }
    }

    public void clear() {
        nextTimeAdapters.clear();

        notifyDataSetChanged();
        if(listView != null) {
            Utility.setListViewHeightBasedOnChildren(listView);
        }
    }

    public void removeNextTime(OptymoNextTime nextTime) {
        if(nextTimeAdapters.containsKey(nextTime.directionToString())) {

            NextTimeItemAdapter adapter = nextTimeAdapters.get(nextTime.directionToString());
            if(adapter != null) {
                adapter.removeNextTime(nextTime);
                if(adapter.getCount() > 0) {
                    adapter.notifyDataSetChanged();
                    if(listView != null)
                        Utility.setListViewHeightBasedOnChildren(listView);
                } else {
                    // no more for the line delete item
                    nextTimeAdapters.remove(nextTime.directionToString());
                }

                notifyDataSetChanged();
                if(listView != null) {
                    Utility.setListViewHeightBasedOnChildren(listView);
                }
            }
        }
    }

    public LineNextTimeAdapter(Context context, ListView listView) {
        this.context = context;
        this.listView = listView;
    }

    @Override
    public int getCount() {
        return nextTimeAdapters.size();
    }

    @Override
    public Object getItem(int i) {
        return nextTimeAdapters.valueAt(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint({"ViewHolder", "SetTextI18n"})
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.item_line_next_stop, parent, false);

        int id = context.getResources().getIdentifier("colorLine" + nextTimeAdapters.valueAt(position).getLineNumber(), "color", context.getPackageName());
        convertView.findViewById(R.id.item_line_next_stop_line_bg).setBackgroundColor(ContextCompat.getColor(context, id));

        // change line number
        ((TextView) convertView.findViewById(R.id.item_line_next_stop_number)).setText("" + nextTimeAdapters.valueAt(position).getLineNumber());

        // change line name
        ((TextView) convertView.findViewById(R.id.item_line_next_stop_line_name)).setText(nextTimeAdapters.valueAt(position).getDirection());

        // set adapter
        ListView itemListView =  convertView.findViewById(R.id.item_line_next_stop_stop_listview);
        NextTimeItemAdapter adapter = nextTimeAdapters.valueAt(position);
        adapter.setListView(itemListView);
        itemListView.setAdapter(adapter);
        itemListView.setOnItemLongClickListener((parent1, view, position1, id1) -> {
            LineNextTimeAdapter.this.itemListener.onItemLongClick(adapter.getItem(position1));
            return true;
        });
        Utility.setListViewHeightBasedOnChildren(itemListView);

        return convertView;
    }

    public void setOnItemLongClickListener(NextTimeItemAdapter.ItemLongClickListener listener) {
        this.itemListener = listener;
    }
}
