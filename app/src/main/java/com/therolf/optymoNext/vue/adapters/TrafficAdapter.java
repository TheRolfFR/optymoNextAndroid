package com.therolf.optymoNext.vue.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.therolf.optymoNext.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

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

        TrafficAdapter.TrafficInfo info = data.get(childPosition);

        // update date
        ((TextView) convertView.findViewById(R.id.expandable_date)).setText(info.getTime(context));

        // update line
        if (data.get(childPosition).lines.equals("")) {
            convertView.findViewById(R.id.expandable_lines).setVisibility(View.GONE);
        } else {
            convertView.findViewById(R.id.expandable_lines).setVisibility(View.VISIBLE);
            ((TextView) convertView.findViewById(R.id.expandable_lines)).setText(this.context.getResources().getString(R.string.main_traffic_info_lines_prefix, info.lines));
        }

        // update more infos url
        TextView moreInfos = convertView.findViewById(R.id.expandable_more_infos);
        TextView titleView = convertView.findViewById(R.id.expandable_content);
        if(info.url != null && info.url.startsWith("http")) {
            // link to more infos
            CharSequence text = moreInfos.getText();
            String linkText = "<a href='" + info.url + "'>" + text + "</a>";
            moreInfos.setMovementMethod(LinkMovementMethod.getInstance());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                moreInfos.setText(Html.fromHtml(linkText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                moreInfos.setText(Html.fromHtml(linkText));
            }

            // link to title
            titleView.setMovementMethod(LinkMovementMethod.getInstance());
            linkText = "<a href='" + info.url + "'>" + info.content + "</a>";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                titleView.setText(Html.fromHtml(linkText, Html.FROM_HTML_MODE_LEGACY));
            } else {
                titleView.setText(Html.fromHtml(linkText));
            }

            Spannable s = (Spannable) titleView.getText();
            URLSpan[] spans = s.getSpans(0, s.length(), URLSpan.class);
            for (URLSpan span: spans) {
                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                s.removeSpan(span);
                span = new URLSpanline_none(span.getURL());
                s.setSpan(span, start, end, 0);
            }
            titleView.setText(s);
        } else {
            // update content
            titleView.setText(info.content);
            moreInfos.setVisibility(View.INVISIBLE);
        }

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
        String url;
        String publicationDate;

        Date from;
        Date to;

        public TrafficInfo(String date, String lines, String content, String url, String publicationDate) {
            this.date = date;
            this.lines = lines;
            this.content = content;
            this.url = url;
            this.publicationDate = publicationDate;
        }

        public String getPublicationDate() {
            return publicationDate;
        }

        private Date parse(String utc) throws ParseException {
            @SuppressLint("SimpleDateFormat")
            DateFormat m_ISO8601Local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            m_ISO8601Local.setTimeZone(TimeZone.getTimeZone("UTC"));

            return m_ISO8601Local.parse(utc);
        }

        public void setFrom(String from) {
            try {
                this.from = this.parse(from);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public void setTo(String to) {
            try {
                this.to = this.parse(to);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public String getTime(Context ctx) {
            if(from == null && to == null) return this.date;

            String result;

            Locale locale = ctx.getResources().getConfiguration().locale;
            DateFormat dayFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM, locale);
            DateFormat timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT, locale);

            String fromString = "", toString = "";

            Calendar cal = Calendar.getInstance();
            int h, m;
            int[] dates = new int[2];

            if(from != null) {
                fromString = dayFormat.format(from);
                cal.setTime(from);
                h = cal.get(Calendar.HOUR_OF_DAY);
                m = cal.get(Calendar.MINUTE);
                if((h != 0 || m != 0) && (h != 23 || m != 59)) {
                    fromString += " " + timeFormat.format(from);
                }
                dates[0] = cal.get(Calendar.DAY_OF_YEAR);
                dates[1] = cal.get(Calendar.YEAR);
            }

            if(to != null) {
                toString = dayFormat.format(to);
                cal.setTime(to);
                h = cal.get(Calendar.HOUR_OF_DAY);
                m = cal.get(Calendar.MINUTE);
                if((h != 0 || m != 0) && (h != 23 || m != 59)) {
                    toString += " " + timeFormat.format(to);
                }
            }

            if(to != null) {
                // From X to X
                // or
                // The X
                if(cal.get(Calendar.DAY_OF_YEAR) == dates[0] && cal.get(Calendar.YEAR) == dates[1]) {
                    result = ctx.getResources().getString(R.string.main_traffic_info_date_same_day, fromString);
                } else {
                    result = ctx.getResources().getString(R.string.main_traffic_info_date_from_to, fromString, toString);
                }
            } else {
                // From X
                result = ctx.getResources().getString(R.string.main_traffic_info_date_from, fromString);
            }

            return result;
        }
    }

    private static class URLSpanline_none extends URLSpan {
        public URLSpanline_none(String url) {
            super(url);
        }
        @Override public void updateDrawState(@SuppressWarnings("NullableProblems") TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);
        }
    }
}
