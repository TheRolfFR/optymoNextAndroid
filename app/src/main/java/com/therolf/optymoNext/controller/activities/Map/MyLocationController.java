package com.therolf.optymoNext.controller.activities.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

@SuppressWarnings("unused")
class MyLocationController {

    private LocationManager locationManager;
    private com.google.android.gms.location.LocationListener locationListener;

    void setLocationListener(com.google.android.gms.location.LocationListener locationListener) {
        this.locationListener = locationListener;
    }

    MyLocationController(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @SuppressLint("MissingPermission")
    void requestLocation() {
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                locationListener.onLocationChanged(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        });
    }
}
