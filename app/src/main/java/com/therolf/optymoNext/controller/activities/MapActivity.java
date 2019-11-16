package com.therolf.optymoNext.controller.activities;

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
import androidx.core.graphics.drawable.DrawableCompat;

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
import com.therolf.optymoNext.controller.GlobalApplication;
import com.therolf.optymoNext.controller.MyLocationController;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener, LocationListener {

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
    private static final int BUS_INDEX = 7;
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
        busIcon = bitmapDescriptorFromVector(this, R.drawable.ic_bus);
        Set<String> keys = zIndexes.keySet();
        for(String key : keys) {
            int id = getResources().getIdentifier("colorLine" + key, "color", getPackageName());
            int color = ContextCompat.getColor(this, id);
            busIcons.put(key, bitmapDescriptorFromVector(this, R.drawable.ic_bus, color));
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
        googleMap.setMinZoomPreference(10);

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

//                    Log.d("optymonext", "" + lat + ", " + lon);
                    marker.position(new LatLng(lat, lon));

                    marker.zIndex(STOP_Z_INDEX);
                    marker.icon(icon);
                    marker.title(title);
                    marker.anchor(0.5f, 0.5f);

                    googleMap.addMarker(marker);
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            e.printStackTrace();
        }

        googleMap.setOnMarkerClickListener(this);
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

    @SuppressWarnings("SameParameterValue")
    private static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorId) {
        return bitmapDescriptorFromVector(context, vectorId, 0);
    }

    private static BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorId, int color) {
        BitmapDescriptor result = null;

        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorId);
        if(color != 0 && vectorDrawable != null) {
            vectorDrawable = vectorDrawable.mutate();
            DrawableCompat.setTint(vectorDrawable, color);
        }
        if (vectorDrawable != null) {
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);

            result = BitmapDescriptorFactory.fromBitmap(bitmap);
        }

        return result;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(((GlobalApplication) getApplication()).getNetworkController().isGenerated() && ((GlobalApplication) getApplication()).getNetworkController().getStopBySlug(OptymoStop.nameToSlug(marker.getTitle())) != null)
            StopActivity.launchStopActivity(this, OptymoStop.nameToSlug(marker.getTitle()));
        return false;
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

    @SuppressWarnings("SameParameterValue")
    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuilder buffer = new StringBuilder();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
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
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 14));
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
                result = readUrl("https://www.optymo.fr/wp-admin/admin-ajax.php?action=getItrBus&src=itrsub/get_markers_urb.php");
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
