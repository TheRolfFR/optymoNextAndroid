package com.therolf.optymoNext.vue.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.global.Utility;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Scanner;

@SuppressWarnings("unused")
public class LinePdfAdapter extends BaseAdapter {

    public static class LinePdf {
        private String number = null;
        private String pdfUrl = null;

        public LinePdf() {}

        public LinePdf(String number, String pdfUrl) {
            this.number = number;
            this.pdfUrl = pdfUrl;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public void setPdfUrl(String pdfUrl) {
            this.pdfUrl = pdfUrl;
        }

        public String getNumber() {
            return number;
        }

        public String getPdfUrl() {
            return pdfUrl;
        }

        @Override
        public String toString() {
            return "LinePdf{" +
                    "number='" + number + '\'' +
                    ", pdfUrl='" + pdfUrl + '\'' +
                    '}';
        }
    }

    public static class LinePdfAdapterFactory {
        private RequestQueue requestQueue;
        private Context context;

        private LinePdf[] staticResult;

        private GridView view;
        private TextView descView;

        private final static String HOMEPAGE = "https://www.optymo.fr/";

        private LinePdfAdapter empty;

        public LinePdfAdapterFactory(Context ctx, LinePdf[] staticResult, TextView descView, GridView gridView) {
            this.requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
            this.context = ctx;

            this.staticResult = staticResult;

            this.view = gridView;
            this.descView = descView;

            // define empty result
            this.empty = new LinePdfAdapter(new LinePdf[0], ctx);

            // start first request
            this.request();
        }

        public void request() {
            this.descView.setText(this.context.getResources().getText(R.string.main_lines_pdf_loading));
            this.view.setAdapter(empty);
            StringRequest feedRequest = new StringRequest(Request.Method.GET, HOMEPAGE,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            // parse HTML
                            Document htmlDocument = Jsoup.parse(response);

                            // get only the regular lines
                            Elements urbanLines = htmlDocument.select("div.group-fiches:nth-child(1) > div:nth-child(2) a");
                            if(urbanLines != null) {
                                ArrayList<LinePdf> resultList = new ArrayList<>();
                                for(int i = 0; i < urbanLines.size(); ++i) {
                                    //extract number from href
                                    String href = urbanLines.get(i).attr("href");
                                    String[] path = href.split("/");

                                    // the number is the first integer from the filename
                                    String number = "" + new Scanner(path[path.length - 1]).useDelimiter("\\D+").nextInt();

                                    // create ne line PDF
                                    resultList.add(new LinePdf(number, href));
                                }

                                // convert result to array
                                LinePdf[] result = resultList.toArray(new LinePdf[0]);

                                // change text to dynamic result
                                descView.setText(context.getResources().getString(R.string.main_lines_pdf_dynamic));

                                // change results with adapter
                                view.setAdapter(new LinePdfAdapter(result, context));
                            } else {
                                errorHandler();
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // if request error (internet, timeout...)
                    errorHandler(); // but change the result to static result
                    error.printStackTrace(); // print stack
                }
            });

            requestQueue.add(feedRequest);
        }

        private void errorHandler() {
            // set static text with build date
            descView.setText(this.context.getResources().getString(R.string.main_lines_pdf_static, Utility.getBuildDate(context)));

            // set static result
            view.setAdapter(new LinePdfAdapter(this.staticResult, context));
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
