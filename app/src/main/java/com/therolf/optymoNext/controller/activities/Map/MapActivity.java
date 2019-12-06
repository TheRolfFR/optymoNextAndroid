package com.therolf.optymoNext.controller.activities.Map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.therolf.optymoNext.R;
import com.therolf.optymoNext.controller.activities.StopActivity;
import com.therolf.optymoNext.controller.global.GlobalApplication;
import com.therolf.optymoNext.controller.global.Utility;
import com.therolf.optymoNextModel.OptymoNetwork;
import com.therolf.optymoNextModel.OptymoStop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, View.OnClickListener, LocationListener, GoogleMap.OnInfoWindowClickListener {

    private static final int LOCATION_PERMISSION_CODE = 1;
    private MyLocationController myLocationController;

    private static final int LINE_DEFAULT_INDEX = 0;
    private Map<String, Integer> zIndexes = new HashMap<String, Integer>() {{
        put("5", 6);
        put("3", 5);
        put("2", 4);
        put("8", 3);
        put("4", 2);
        put("1", 1);
    }};
    private static final int BUS_INDEX = 10;
    private static final int STOP_Z_INDEX = 8;

    private Map<String, BitmapDescriptor> busIcons = new HashMap<>();

    private GoogleMap googleMap;
    private ArrayList<Marker> busMarkers = new ArrayList<>();
    private BitmapDescriptor busIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // my location controller
        myLocationController = new MyLocationController(this);
        myLocationController.setLocationListener(this);

        // close button
        findViewById(R.id.map_close_button).setOnClickListener(v -> finish());

        // load bus icon
        busIcon = bitmapDescriptorFromVector(this, R.drawable.ic_bus_default);
        Set<String> keys = zIndexes.keySet();
        for(String key : keys) {
            int id = getResources().getIdentifier("ic_bus_" + key, "drawable", getPackageName());
            busIcons.put(key, bitmapDescriptorFromVector(this, id));
        }

        // map view
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

        // location button
        findViewById(R.id.map_my_position_button).setOnClickListener(this);
    }

    public void startMap() {
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        googleMap.setMapStyle(new MapStyleOptions("[\n  {\n    \"featureType\": \"administrative.locality\",\n    \"elementType\": \"labels.text\",\n    \"stylers\": [\n      {\n        \"color\": \"#a2a2a2\"\n      },\n      {\n        \"visibility\": \"simplified\"\n      }\n    ]\n  },\n  {\n    \"featureType\": \"poi\",\n    \"stylers\": [\n      {\n        \"visibility\": \"off\"\n      }\n    ]\n  },\n  {\n    \"featureType\": \"road\",\n    \"elementType\": \"labels.icon\",\n    \"stylers\": [\n      {\n        \"visibility\": \"off\"\n      }\n    ]\n  }\n]"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(47.63557, 6.85780), 14));
        googleMap.setMinZoomPreference(11.7f);

        InputStream jsonInputStream = (getResources().openRawResource(getResources().getIdentifier("lines", "raw", getPackageName())));

        String jsonContent = null;
        try {
            byte[] content = new byte[jsonInputStream.available()];
            //noinspection ResultOfMethodCallIgnored
            jsonInputStream.read(content);
            jsonContent = new String(content);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(jsonContent != null) {
            try {
                JSONObject lines = new JSONObject(jsonContent);

                Iterator<String> keys = lines.keys();

                JSONArray allLinesPaths, linePathPoints;
                JSONObject linePath, point;
                PolylineOptions polyLine;
                String lineNumber;
                while(keys.hasNext()) {
                    lineNumber = keys.next();
                    allLinesPaths = lines.getJSONArray(lineNumber);
                    for(int b = 0; b < allLinesPaths.length(); ++b) {
                        linePath = allLinesPaths.getJSONObject(b);

                        polyLine = new PolylineOptions();
                        polyLine.color(Color.parseColor("#" + linePath.getString("color")));
                        polyLine.clickable(true);

                        if(zIndexes.containsKey(lineNumber)) {
                            Integer index = zIndexes.get(lineNumber);
                            if(index != null)
                                polyLine.zIndex(index);
                            else
                                polyLine.zIndex(LINE_DEFAULT_INDEX);
                        } else {
                            polyLine.zIndex(LINE_DEFAULT_INDEX);
                        }

                        linePathPoints = linePath.getJSONArray("path");
                        for(int c = 0; c < linePathPoints.length(); ++c) {
                            point = linePathPoints.getJSONObject(c);

                            polyLine.add(new LatLng(Float.parseFloat(point.getString("lat")), Float.parseFloat(point.getString("lng"))));
                        }

                        googleMap.addPolyline(polyLine);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // stops belfort
        InputStream xmlInputStream = (getResources().openRawResource(getResources().getIdentifier("belfort", "raw", getPackageName())));

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder documentBuilder;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(xmlInputStream);

            NodeList stops = document.getElementsByTagName("transitStop");
            Node node;
            Element element;
            MarkerOptions marker;
            BitmapDescriptor icon = bitmapDescriptorFromVector(this, R.drawable.ic_marker);
            String title;
            float lat, lon;
            for(int i = 0; i < stops.getLength(); ++i) {
                node = stops.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    element = (Element) node;

                    marker = new MarkerOptions();

                    lat = Float.parseFloat(element.getElementsByTagName("latitude").item(0).getTextContent());
                    lon = Float.parseFloat(element.getElementsByTagName("longitude").item(0).getTextContent());
                    title = element.getElementsByTagName("name").item(0).getTextContent();

                    //check if this is an urban stop so if it belongs to the network
                    if( ((GlobalApplication) getApplication()).getNetworkController().getStopBySlug(OptymoStop.nameToSlug(title)) != null) {
//                    Log.d("optymonext", "" + lat + ", " + lon);
                        marker.position(new LatLng(lat, lon));

                        marker.zIndex(STOP_Z_INDEX)
                        .icon(icon)
                        .title(title)
                        .snippet(this.getString(R.string.map_more_infos));
                        marker.anchor(0.5f, 0.5f);

                        googleMap.addMarker(marker);
                    }
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        googleMap.setOnInfoWindowClickListener(this);
        startRepeatingTask();
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        this.googleMap = gMap;

        //noinspection Convert2Lambda
        ((GlobalApplication) getApplication()).getNetworkController().addProgressListenerIfNotGenenerated(new OptymoNetwork.ProgressListener() {
            @Override
            public void OnGenerationEnd(boolean returnValue) {
                startMap();
            }
        });
    }

    private static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorId) {
        BitmapDescriptor result = null;

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorId);
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);

            result = BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        return result;
    }

    private final static int INTERVAL = 1000 *10; // 10s = 10000ms
    private Handler mHandler = new Handler();
    private GetBusMarkers request = null;

    private Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
//            Log.d("optymonext", "Querying new bus locations");
            getBusPosition();
            mHandler.postDelayed(mHandlerTask, INTERVAL);
        }
    };

    private void startRepeatingTask()
    {
        mHandlerTask.run();
    }

    private void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mHandlerTask);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopRepeatingTask();
    }

    private void getBusPosition() {

        // stop last request
        if(this.request != null) {
            this.request.cancel(true);
        }

        // launch new request
        this.request = new GetBusMarkers(this);
        this.request.execute();
    }

    @Override
    public void onClick(View v) {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            moveCameraToMyLocation();
        } else {
            requestLocationPermission();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this, R.style.AppTheme_CustomDialog)
                    .setTitle(R.string.permission_required)
                    .setMessage(R.string.permission_message)
                    .setPositiveButton(R.string.dialog_ok, (dialog, which) -> ActivityCompat.requestPermissions(MapActivity.this,
                            new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE))
                    .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> dialog.dismiss())
                    .create().show();

        } else {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
        }
    }

    private void moveCameraToMyLocation() {
        googleMap.setMyLocationEnabled(true);
        myLocationController.requestLocation();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_CODE)  {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                moveCameraToMyLocation();
            } else {
                Toast.makeText(this, R.string.permission_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // change zoom only if lower
        float zoom = googleMap.getCameraPosition().zoom;
        if(zoom < 14)
            zoom = 14;

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), zoom));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if(((GlobalApplication) getApplication()).getNetworkController().isGenerated() && ((GlobalApplication) getApplication()).getNetworkController().getStopBySlug(OptymoStop.nameToSlug(marker.getTitle())) != null)
            StopActivity.launchStopActivity(this, OptymoStop.nameToSlug(marker.getTitle()));
    }

    @SuppressWarnings("unused")
    private static class GetBusMarkers extends AsyncTask<Void, Void, String> {

        private WeakReference<MapActivity> reference;

        GetBusMarkers(MapActivity reference) {
            this.reference = new WeakReference<>(reference);
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result = null;

            try {
                result = Utility.readUrl("https://www.optymo.fr/wp-admin/admin-ajax.php?action=getItrBus&src=itrsub/get_markers_urb.php");
            } catch (Exception e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            // return if map activity
            MapActivity mapActivity = reference.get();
            if(mapActivity == null || mapActivity.isFinishing()) return;

            // remove all markers
            while(mapActivity.busMarkers.size() > 0) {
                mapActivity.busMarkers.get(0).remove();
                mapActivity.busMarkers.remove(0);
            }

            try {
                if(s != null) {
                    JSONObject buses = new JSONObject(s);

                    Iterator<String> keys = buses.keys();
                    String busNumber;
                    JSONObject bus;
                    MarkerOptions markerOptions;
                    BitmapDescriptor icon; // = null
                    while(keys.hasNext()) {
                        busNumber = keys.next();
                        bus = buses.getJSONObject(busNumber);

                        markerOptions = new MarkerOptions();
                        markerOptions.title(bus.getString("ligne") + " - " + bus.getString("course"));
                        markerOptions.position(new LatLng(Float.parseFloat(bus.getString("lat")), Float.parseFloat(bus.getString("lng"))));

                        // set bus icon
                        icon = mapActivity.busIcons.get(bus.getString("ligne"));
                        if(icon == null) {
                            icon = mapActivity.busIcon;
                        }
                        markerOptions.icon(icon);

                        markerOptions.snippet("#" + bus.getString("pupitre") + " - " + bus.getString("horodate"));
                        markerOptions.anchor(.5f, .5f);
                        markerOptions.zIndex(BUS_INDEX);

                        mapActivity.busMarkers.add(mapActivity.googleMap.addMarker(markerOptions));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
