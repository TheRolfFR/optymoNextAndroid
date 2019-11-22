package com.therolf.optymoNext.vue.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.therolf.optymoNext.R;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class TrafficAdapter extends BaseExpandableListAdapter {

    private Context context;
    private int titleId;
    private ArrayList<TrafficInfo> data;

    private Drawable upArrow;
    private Drawable downArrow;

    public TrafficAdapter(Context context, int titleId, ArrayList<TrafficInfo> data) {
        this.context = context;
        this.titleId = titleId;
        this.data = data;
        this.upArrow = context.getResources().getDrawable(R.drawable.ic_arrow_up_red);
        this.downArrow = context.getResources().getDrawable(R.drawable.ic_arrow_down_red);
    }

    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return data.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return data;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return data.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //noinspection ConstantConditions
            convertView = inflater.inflate(R.layout.expandable_title, null);
        }

        ((TextView) convertView.findViewById(R.id.expandable_title)).setText(titleId);

        ImageView arrow = convertView.findViewById(R.id.expandable_arrow);
        if(isExpanded) {
            arrow.setImageDrawable(upArrow);
        } else {
            arrow.setImageDrawable(downArrow);
        }

        return convertView;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //noinspection ConstantConditions
            convertView = inflater.inflate(R.layout.expandable_item, null);
        }

        // update date
        ((TextView) convertView.findViewById(R.id.expandable_date)).setText(data.get(childPosition).date);

        // update line
        if (data.get(childPosition).lines.equals("")) {
            convertView.findViewById(R.id.expandable_lines).setVisibility(View.GONE);
        } else {
            convertView.findViewById(R.id.expandable_lines).setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.expandable_lines)).setText(data.get(childPosition).lines);
        }

        // update content
        ((TextView) convertView.findViewById(R.id.expandable_content)).setText(data.get(childPosition).content);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public static class TrafficInfo {
        String date;
        String lines;
        String content;

        public TrafficInfo(String date, String lines, String content) {
            this.date = date;
            this.lines = lines;
            this.content = content;
        }
    }
}
