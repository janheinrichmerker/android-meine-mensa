package com.heinrichreimer.meinemensa.app;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.heinrichreimer.meinemensa.R;
import com.heinrichreimer.meinemensa.util.LocationUtils;
import com.heinrichreimer.meinemensa.util.PreferencesUtils;

public class LocationActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_LOCATION = 1;

    private GoogleApiClient apiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (apiClient == null) {
            apiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        apiClient.connect();
    }

    protected final boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(getWindow().getDecorView(),
                        R.string.label_nearest_location_permission_rationale,
                        Snackbar.LENGTH_LONG)
                        .show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onLocationPermissionGranted();
                }
            }
        }
    }

    protected void onLocationPermissionGranted() {
        //do nothing
    }

    protected final GoogleApiClient getApiClient() {
        return apiClient;
    }

    protected final void navigateToNearestLocation() {
        int[] locations = PreferencesUtils.getLocations(this);
        LatLng nearestLocation = LocationUtils.findNearestLocation(this, getApiClient(), locations);
        if (nearestLocation == null) {
            return;
        }
        try {
            startActivity(LocationUtils.getDirectionsIntent(nearestLocation));
        } catch (ActivityNotFoundException e) {
            Snackbar.make(getWindow().getDecorView(),
                    R.string.label_select_location_already_default_maps_not_found,
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onStop() {
        apiClient.disconnect();
        super.onStop();
    }
}
