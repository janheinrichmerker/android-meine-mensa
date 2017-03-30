package com.heinrichreimer.meinemensa.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.heinrichreimer.meinemensa.R;
import com.heinrichreimer.meinemensa.annotations.Location;
import com.heinrichreimer.meinemensa.databinding.ActivityMapBinding;
import com.heinrichreimer.meinemensa.network.SyncService;
import com.heinrichreimer.meinemensa.util.IntroUtils;
import com.heinrichreimer.meinemensa.util.LocationUtils;
import com.heinrichreimer.meinemensa.util.PreferencesUtils;

import java.util.HashMap;
import java.util.Map;

public class MapActivity extends LocationActivity implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener {

    private static final LatLng CENTER = new LatLng(51.588486, 11.978551);
    private static final float CENTER_ZOOM = 8;

    private ActivityMapBinding binding;
    private GoogleMap map;
    private Map<Marker, Integer> markers;
    private Rect mapInsets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_App_Translucent);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_map);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        markers = new HashMap<>();

        SupportMapFragment mapFragment = SupportMapFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .add(R.id.map, mapFragment)
                .commit();
        mapFragment.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.getRoot().setOnApplyWindowInsetsListener(new WindowInsetsListener());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        IntroUtils.showIntro(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnMarkerClickListener(this);
        map.setBuildingsEnabled(true);
        map.setIndoorEnabled(false);
        map.setMinZoomPreference(CENTER_ZOOM);
        map.setTrafficEnabled(false);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            map.setMyLocationEnabled(true);
        }


        if (mapInsets != null) {
            map.setPadding(
                    mapInsets.left,
                    mapInsets.top,
                    mapInsets.right,
                    mapInsets.bottom);
        }

        LatLngBounds.Builder bounds = LatLngBounds.builder();
        for (@Location int location : Location.ALL) {
            for (LatLng latLng : Location.Converter.toLatLng(location)) {
                Marker marker = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(false)
                        .flat(true)
                        .title(getString(Location.Converter.toString(location))));
                markers.put(marker, location);
                bounds.include(latLng);
            }
        }

        map.setLatLngBoundsForCameraTarget(bounds.build());
        map.moveCamera(
                CameraUpdateFactory.newLatLngZoom(CENTER, CENTER_ZOOM));
        //noinspection WrongConstant
        map.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                        Location.Converter.toLatLng(
                                PreferencesUtils.getLocations(this)[0]
                        )[0],
                        18),
                3000, null);
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (markers.containsKey(marker)) {
            @Location final int location = markers.get(marker);
            if (!PreferencesUtils.containsLocation(this, location)) {
                Snackbar.make(binding.coordinator,
                        getString(R.string.label_select_location, marker.getTitle()),
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_select_location, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PreferencesUtils.addLocation(MapActivity.this, location);
                                scheduleSyncImmediately();
                                NavUtils.navigateUpFromSameTask(MapActivity.this);
                            }
                        })
                        .show();
            } else {
                //TODO let users deselect locations instead
                Snackbar.make(binding.coordinator,
                        getString(R.string.label_select_location_already_default, marker.getTitle()),
                        Snackbar.LENGTH_LONG)
                        .setAction(R.string.action_select_location_already_default, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                LatLng latLng = marker.getPosition();
                                Intent mapIntent = LocationUtils.getDirectionsIntent(latLng);
                                try {
                                    startActivity(mapIntent);
                                } catch (ActivityNotFoundException e) {
                                    Snackbar.make(binding.coordinator,
                                            R.string.label_select_location_already_default_maps_not_found,
                                            Snackbar.LENGTH_LONG)
                                            .show();
                                }
                            }
                        })
                        .show();
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_nearest_location) {
            if (map == null || markers == null) {
                return true;
            }
            if (checkLocationPermission()) {
                calculateNearestDistance();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onLocationPermissionGranted() {
        calculateNearestDistance();
    }


    private void calculateNearestDistance() {
        Marker nearest = LocationUtils.findNearestMarker(this, getApiClient(), markers.keySet());
        if (nearest == null) {
            return;
        }

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(nearest.getPosition(), 18), 2000, null);
        onMarkerClick(nearest);
    }

    private void scheduleSyncImmediately() {
        Intent intent = new Intent(this, SyncService.class);
        intent.setAction(SyncService.ACTION_IMMEDIATELY);
        startService(intent);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class WindowInsetsListener implements View.OnApplyWindowInsetsListener {
        @Override
        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
            ViewGroup.MarginLayoutParams statusBarBackgroundDrawerLayoutParams =
                    (ViewGroup.MarginLayoutParams) binding.statusBarBackground.getLayoutParams();
            statusBarBackgroundDrawerLayoutParams.height = insets.getSystemWindowInsetTop();
            binding.statusBarBackground.setLayoutParams(statusBarBackgroundDrawerLayoutParams);
            binding.statusBarBackground.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams navigationBarBackgroundLayoutParams =
                    (ViewGroup.MarginLayoutParams) binding.navigationBarBackground.getLayoutParams();
            navigationBarBackgroundLayoutParams.height = insets.getSystemWindowInsetBottom();
            binding.navigationBarBackground.setLayoutParams(navigationBarBackgroundLayoutParams);
            binding.navigationBarBackground.setVisibility(View.VISIBLE);

            binding.toolbar.setPadding(
                    binding.toolbar.getPaddingLeft() + insets.getSystemWindowInsetLeft(),
                    binding.toolbar.getPaddingTop(),
                    binding.toolbar.getPaddingRight() + insets.getSystemWindowInsetRight(),
                    binding.toolbar.getPaddingBottom());

            if (map != null) {
                map.setPadding(
                        insets.getSystemWindowInsetLeft(),
                        0,
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom());
            } else {
                mapInsets = new Rect(
                        insets.getSystemWindowInsetLeft(),
                        0,
                        insets.getSystemWindowInsetRight(),
                        insets.getSystemWindowInsetBottom());
            }

            binding.getRoot().setOnApplyWindowInsetsListener(null);

            return insets.consumeSystemWindowInsets();
        }
    }
}
