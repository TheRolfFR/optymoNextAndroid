package com.therolf.optymoNext.controller.activities.Main;

import android.text.TextUtils;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.global.Utility;
import com.therolf.optymoNext.vue.adapters.TrafficAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

@SuppressWarnings("unused")
public class TrafficController implements ExpandableListView.OnGroupExpandListener, ExpandableListView.OnGroupCollapseListener {

    private AppCompatActivity context;

    private ArrayList<TrafficAdapter.TrafficInfo> data = new ArrayList<>();
    private TrafficAdapter trafficAdapter;
    private ExpandableListView listView;

    private static final String TRAFFIC_REQUEST_TAG = "request_traffic";
    private StringRequest trafficRequest;
    private RequestQueue trafficQueue;

    private static final Comparator<String> LINE_COMPARATOR = (o1, o2) -> {
        try {
            int i1 = Integer.parseInt(o1);
            int i2 = Integer.parseInt(o2);

            return Integer.compare(i1, i2);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    };

    private static final Comparator<TrafficAdapter.TrafficInfo> ALERT_COMPARATOR = (o1, o2) -> -o1.getPublicationDate().compareTo(o2.getPublicationDate());

    private static final String TRAFFIC_URL = "https://app.mecatran.com/utw/ws/alerts/active/belfort?preferredLang=fr&includeRoutes=true&includeStops=true&apiKey=76350b70682e051e6709782b551f5437173f1957";
    private static final String TRAFFIC_PAGE_ROOT = "https://www.optymo.fr/infos_trafic/#";

    // routes color and text color https://app.mecatran.com/utw/ws/gtfs/routes/belfort?includeAgencies=true&apiKey=76350b70682e051e6709782b551f5437173f1957

    private ProgressBar progressBar;

    TrafficController(AppCompatActivity context) {
        this.context = context;

        trafficAdapter = new TrafficAdapter(context, R.string.main_traffic_info, data);
        listView = context.findViewById(R.id.main_traffic_info_listview);
        listView.setAdapter(trafficAdapter);
        listView.setOnGroupExpandListener(this);
        listView.setOnGroupCollapseListener(this);

        progressBar = context.findViewById(R.id.main_traffic_info_progressbar);
        progressBar.setVisibility(View.GONE);

        this.trafficQueue = Volley.newRequestQueue(context);
        this.trafficRequest = null;
    }

    @Override
    public void onGroupExpand(int groupPosition) {
        trafficQueue.add(this.request());
    }

    private void hideProgressbar() {
        this.context.runOnUiThread(() -> this.progressBar.setVisibility(View.GONE));
    }

    private void requestCancel() {
        if(this.trafficRequest != null && !this.trafficRequest.isCanceled()) {
            this.trafficRequest.cancel();
        }
    }

    private StringRequest request() {
        this.requestCancel();

        this.data.clear();
        this.context.runOnUiThread(() -> {
            this.progressBar.setVisibility(View.VISIBLE); // visible visibility
            this.trafficAdapter.notifyDataSetChanged();
            Utility.setListViewHeightBasedOnChildren(this.listView);
        });

        this.trafficRequest = new StringRequest(Request.Method.GET, TRAFFIC_URL, (response) -> {
            response = new String(response.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);

            this.hideProgressbar();

            try {
                JSONArray result = new JSONArray(response);

                String title, formattedActiveRange, lines, url, publicationDate, from, to;
                JSONArray routes;
                ArrayList<String> linesList = new ArrayList<>();
                for(int alertIndex = 0; alertIndex < result.length(); ++alertIndex) {
                    JSONObject alert = result.getJSONObject(alertIndex);

                    title = alert.getString("title");
                    title = title.substring(0,1).toUpperCase()+title.substring(1);
                    formattedActiveRange = alert.getString("formattedActiveRange");
                    url = TRAFFIC_PAGE_ROOT + alert.getString("id");
                    publicationDate = alert.getJSONObject("apiPublication").getString("dateTime");

                    routes = alert.getJSONArray("routes");
                    linesList.clear();
                    for (int routeIndex = 0; routeIndex < routes.length(); ++routeIndex) {
                        linesList.add(routes.getJSONObject(routeIndex).getString("shortName"));
                    }
                    Collections.sort(linesList, LINE_COMPARATOR);
                    lines = TextUtils.join(", ", linesList);

                    // eventually add this to the data array list
                    TrafficAdapter.TrafficInfo infos = new TrafficAdapter.TrafficInfo(formattedActiveRange, lines, title, url, publicationDate);
                    infos.setFrom(alert.optString("activeFrom"));
                    infos.setTo(alert.optString("activeTo"));
                    this.data.add(infos);
                }
            } catch (JSONException e) {
                Toast.makeText(this.context, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            Collections.sort(this.data, ALERT_COMPARATOR);

            // notify update
            this.context.runOnUiThread(() -> {
                this.trafficAdapter.notifyDataSetChanged();
                Utility.setListViewHeightBasedOnChildren(this.listView);
            });
        }, error -> {
            this.hideProgressbar();
            error.printStackTrace();
        });

        return this.trafficRequest;
    }

    @Override
    public void onGroupCollapse(int groupPosition) {
        this.requestCancel();

        // notify update
        context.runOnUiThread(() -> {
            data.clear();
            trafficAdapter.notifyDataSetChanged();
            Utility.setListViewHeightBasedOnChildren(listView);
        });
    }
}
