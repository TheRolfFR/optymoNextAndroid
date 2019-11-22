package com.therolf.optymoNext.controller.activities.Main;

import android.os.AsyncTask;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;

import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.global.Utility;
import com.therolf.optymoNext.vue.adapters.TrafficAdapter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class TrafficController implements ExpandableListView.OnGroupExpandListener, ExpandableListView.OnGroupCollapseListener {

    private AppCompatActivity context;

    private ArrayList<TrafficAdapter.TrafficInfo> data = new ArrayList<>();
    private TrafficAdapter trafficAdapter;
    private ExpandableListView listView;

    private TrafficInfoRequest request;

    TrafficController(AppCompatActivity context) {
        this.context = context;

        trafficAdapter = new TrafficAdapter(context, R.string.main_traffic_info, data);
        listView = context.findViewById(R.id.main_traffic_info_listview);
        listView.setAdapter(trafficAdapter);
        listView.setOnGroupExpandListener(this);
        listView.setOnGroupCollapseListener(this);
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        if(request != null && !request.isCancelled()) {
            request.cancel(true);
        }

        request = new TrafficInfoRequest(this);
        request.execute();
    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        // cancel request
        if(request != null && !request.isCancelled()) {
            request.cancel(true);
        }

        // notify update
        context.runOnUiThread(() -> {
            data.clear();
            trafficAdapter.notifyDataSetChanged();
            Utility.setListViewHeightBasedOnChildren(listView);
        });
    }

    public static class TrafficInfoRequest extends AsyncTask<Void, Void, String> {

        private TrafficController tc;

        TrafficInfoRequest(TrafficController tc) {
            this.tc = tc;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;

            // clear data
            tc.data.clear();

            // notify update
            tc.context.runOnUiThread(() -> {
                tc.trafficAdapter.notifyDataSetChanged();
                Utility.setListViewHeightBasedOnChildren(tc.listView);
            });

            // read traffic RSS feed URL
            try {
                result = Utility.readUrl("https://www.optymo.fr/infos_trafic/feed");

            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String page) {
            super.onPostExecute(page);

            if(page == null)
                return;

            Document document = Jsoup.parse(page), dateDocument;
            Elements items = document.getElementsByTag("item"), dates;
            String body, date, lines, content;
            for(Element item : items) {
                dateDocument = Jsoup.parse(item.getElementsByTag("description").get(0).text());
                body = dateDocument.body().text();

                int lineIndex = body.indexOf("Lignes");

                if(lineIndex == -1) {
                    date = body;
                    lines = "";
                } else {
                    date = body.substring(0, lineIndex);
                    lines = body.substring(lineIndex);
                }
                content = item.getElementsByTag("title").get(0).text();

                // add string
                tc.data.add(new TrafficAdapter.TrafficInfo(date, lines, content));
            }

            // notify update
            tc.context.runOnUiThread(() -> {
                tc.trafficAdapter.notifyDataSetChanged();
                Utility.setListViewHeightBasedOnChildren(tc.listView);
            });
        }
    }
}
