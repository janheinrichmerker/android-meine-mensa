package com.heinrichreimer.meinemensa.util;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.heinrichreimer.meinemensa.annotations.Location;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LocationUtils {
    private LocationUtils() {
    }

    public static Intent getDirectionsIntent(LatLng latLng) {
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" +
                latLng.latitude + "," + latLng.longitude + "&mode=w");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        return mapIntent;
    }

    @Nullable
    private static LatLng findNearestPosition(Context context, GoogleApiClient apiClient, Set<LatLng> positions) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return null;
        }
        if (!apiClient.isConnected()) {
            return null;
        }

        android.location.Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(apiClient);
        if (lastLocation == null) {
            return null;
        }

        LatLng nearest = null;
        float nearestDistance = Float.MAX_VALUE;
        for (LatLng position : positions) {
            android.location.Location targetLocation = new android.location.Location("");
            targetLocation.setLatitude(position.latitude);
            targetLocation.setLongitude(position.longitude);

            float distance = lastLocation.distanceTo(targetLocation);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = position;
            }
        }
        return nearest;
    }

    @Nullable
    public static Marker findNearestMarker(Context context, GoogleApiClient apiClient, Set<Marker> markers) {
        Set<LatLng> positions = new HashSet<>(markers.size());
        for (Marker marker : markers) {
            positions.add(marker.getPosition());
        }
        LatLng nearestPosition = findNearestPosition(context, apiClient, positions);
        if (nearestPosition == null) {
            return null;
        }
        for (Marker marker : markers) {
            if (marker.getPosition().equals(nearestPosition)) {
                return marker;
            }
        }
        return null;
    }

    @Nullable
    public static LatLng findNearestLocation(Context context, GoogleApiClient apiClient, @Location int[] locations) {
        Set<LatLng> positions = new HashSet<>(locations.length);
        for (int location : locations) {
            LatLng[] locationPositions = Location.Converter.toLatLng(location);
            Collections.addAll(positions, locationPositions);
        }
        return findNearestPosition(context, apiClient, positions);
    }
}
