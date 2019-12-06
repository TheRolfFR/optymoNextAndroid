package com.therolf.optymoNext.vue.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.therolf.optymoNext.R;

@SuppressWarnings("unused")
public class LinePdfAdapter extends BaseAdapter {

    public static class LinePdf {
        private String number;
        private String pdfUrl;

        public LinePdf(String number, String pdfUrl) {
            this.number = number;
            this.pdfUrl = pdfUrl;
        }

        public String getNumber() {
            return number;
        }

        public String getPdfUrl() {
            return pdfUrl;
        }
    }

    public LinePdfAdapter(LinePdf[] linePdfs, @NonNull Context context) {
        this.linePdfs = linePdfs;
        this.context = context;
    }

    private LinePdf[] linePdfs;
    private Context context;

    @Override
    public int getCount() {
        return linePdfs.length;
    }

    @Override
    public Object getItem(int position) {
        return linePdfs[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        convertView = inflater.inflate(R.layout.item_line_number, parent, false);

        ((TextView) convertView.findViewById(R.id.item_next_stop_number)).setText(linePdfs[position].getNumber());

        int id = context.getResources().getIdentifier("colorLine" + linePdfs[position].getNumber(), "color", context.getPackageName());
        if(id != 0)
            convertView.findViewById(R.id.item_next_stop_line_bg).setBackgroundColor(ContextCompat.getColor(context, id));

        return convertView;
    }
}
