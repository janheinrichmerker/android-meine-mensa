/*
 * MIT License
 *
 * Copyright (c) 2017 Jan Heinrich Reimer
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.heinrichreimer.meinemensa.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.DatePicker;

import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.OneoffTask;
import com.google.android.gms.gcm.PeriodicTask;
import com.heinrichreimer.meinemensa.R;
import com.heinrichreimer.meinemensa.adapter.MenuAdapter;
import com.heinrichreimer.meinemensa.annotations.Location;
import com.heinrichreimer.meinemensa.annotations.PriceCategory;
import com.heinrichreimer.meinemensa.databinding.ActivityMainBinding;
import com.heinrichreimer.meinemensa.network.SyncService;
import com.heinrichreimer.meinemensa.network.SyncStatusChangedEvent;
import com.heinrichreimer.meinemensa.util.DateUtils;
import com.heinrichreimer.meinemensa.util.FirstStartUtils;
import com.heinrichreimer.meinemensa.util.IntroUtils;
import com.heinrichreimer.meinemensa.util.PreferencesUtils;
import com.heinrichreimer.meinemensa.view.SpacingItemDecoration;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import io.realm.Realm;

public class MainActivity extends LocationActivity {

    private static final String DEBUG_TAG = "MainActivity";

    private ActivityMainBinding binding;
    private MenuAdapter adapter;
    private AdapterObserver observer = new AdapterObserver();
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_App_Translucent);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        setSupportActionBar(binding.toolbar);

        updateSyncStatus();

        realm = Realm.getDefaultInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.getRoot().setOnApplyWindowInsetsListener(new WindowInsetsListener());
        }
        loadGrid();

        binding.swipeRefresh.setColorSchemeResources(R.color.swipe_refresh_1,
                R.color.swipe_refresh_2, R.color.swipe_refresh_3, R.color.swipe_refresh_4);
        binding.swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                scheduleSyncImmediately();
            }
        });

        binding.drawer.setScrimColor(ContextCompat.getColor(this, R.color.scrim));

        int[] locations = PreferencesUtils.getLocations(this);
        for (@Location int location : locations) {
            binding.navigation.getMenu()
                    .findItem(Location.Converter.toId(location))
                    .setChecked(true);
        }
        binding.navigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_item_location_map) {
                    Intent intent = new Intent(MainActivity.this, MapActivity.class);
                    startActivity(intent);
                    binding.drawer.closeDrawer(binding.navigationLayout);
                } else if (Location.Converter.isId(id)) {
                    @Location int location = Location.Converter.toLocation(id);
                    if (PreferencesUtils.containsLocation(MainActivity.this, location)) {
                        PreferencesUtils.removeLocation(MainActivity.this, location);
                        item.setChecked(false);
                    } else {
                        PreferencesUtils.addLocation(MainActivity.this, location);
                        item.setChecked(true);
                    }
                    scheduleSyncImmediately();
                    updateGrid();
                }
                binding.drawer.closeDrawer(binding.navigationLayout);
                return false;
            }
        });

        scheduleSync();

        if (FirstStartUtils.isFirstStart(this)) {
            scheduleSyncImmediately();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(PriceCategory.Converter.toId(
                PreferencesUtils.getPriceCategory(this))).setChecked(true);
        menu.findItem(R.id.menu_item_vegetarian_only).setChecked(
                PreferencesUtils.isVegetarianOnly(this));
        IntroUtils.showIntro(MainActivity.this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_location) {
            binding.drawer.openDrawer(binding.navigationLayout);
            return true;
        } else if (id == R.id.menu_item_directions) {
            if (checkLocationPermission()) {
                navigateToNearestLocation();
            }
            return true;
        } else if (id == R.id.menu_item_location_map) {
            Intent intent = new Intent(this, MapActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_item_date) {
            DateTime oldDate = PreferencesUtils.getDate(this);
            new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    DateTime newDate = new DateTime(year, month + 1, dayOfMonth, 0, 0);
                    PreferencesUtils.setDate(MainActivity.this, newDate);
                    scheduleSyncImmediately();
                    updateGrid();
                }
            }, oldDate.getYear(), oldDate.getMonthOfYear() - 1, oldDate.getDayOfMonth()).show();
            return true;
        } else if (PriceCategory.Converter.isId(id)) {
            PreferencesUtils.setPriceCategory(this, PriceCategory.Converter.toPriceCategory(id));
            updateGrid();
            item.setChecked(true);
            return true;
        } else if (id == R.id.menu_item_vegetarian_only) {
            PreferencesUtils.setVegetarianOnly(this, !item.isChecked());
            updateGrid();
            item.setChecked(!item.isChecked());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncStatusChanged(SyncStatusChangedEvent event) {
        updateSyncStatus();
    }

    @Override
    protected void onLocationPermissionGranted() {
        navigateToNearestLocation();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (adapter != null) {
            adapter.unregisterAdapterDataObserver(observer);
        }
        if (realm != null) {
            realm.close();

        }
        super.onDestroy();
    }

    private void updateTitle() {
        final int[] locations = PreferencesUtils.getLocations(this);
        final String title;
        if (locations.length > 1) {
            title = getString(R.string.title_activity_main_multiple_locations, locations.length);
        } else {
            title = getString(Location.Converter.toString(locations[0]));
        }
        setTitle(title);
        DateTimeFormatter formatter = DateTimeFormat.forPattern("dd.MM.yyyy");
        String subtitle = formatter.print(PreferencesUtils.getNextWeekdayDate(this));
        binding.toolbar.setSubtitle(subtitle);
    }

    private void updateSyncStatus() {
        if (SyncStatusChangedEvent.isSyncing()) {
            Log.d(DEBUG_TAG, "Syncing global...");
            binding.swipeRefresh.setRefreshing(true);
        } else {
            Log.d(DEBUG_TAG, "Sync finished global...");
            binding.swipeRefresh.setRefreshing(false);
            updateGrid();
        }
    }

    private void loadGrid() {
        int gridSpans = getResources().getInteger(R.integer.grid_spans);
        GridLayoutManager layout = new GridLayoutManager(this, gridSpans);
        adapter = MenuAdapter.Builder.with(this)
                .realm(realm)
                .build();
        adapter.registerAdapterDataObserver(observer);
        SpacingItemDecoration decoration = new SpacingItemDecoration(
                getResources().getDimensionPixelSize(R.dimen.guideline_quarter), false);

        binding.grid.setLayoutManager(layout);
        binding.grid.addItemDecoration(decoration);
        binding.grid.setAdapter(adapter);
        updateEmptyView();
        updateTitle();
    }

    private void updateGrid() {
        if (adapter != null) {
            adapter.update(this).build();
            updateEmptyView();
        }
        updateTitle();
    }

    private void updateEmptyView() {
        if (adapter.getItemCount() > 0) {
            binding.emptyView.animate()
                    .alpha(0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            binding.emptyView.setVisibility(View.GONE);
                        }
                    })
                    .start();
        } else {
            binding.emptyView.animate()
                    .alpha(1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            binding.emptyView.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
        }
    }

    private void scheduleSync() {
        PeriodicTask syncTask = new PeriodicTask.Builder()
                .setService(SyncService.class)
                .setTag(SyncService.TAG_PERIODICALLY)
                .setPeriod(DateUtils.SECONDS_PER_MINUTE * DateUtils.MINUTES_PER_HOUR * 3)
                .setFlex(DateUtils.SECONDS_PER_MINUTE * DateUtils.MINUTES_PER_HOUR)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .setRequiredNetwork(OneoffTask.NETWORK_STATE_CONNECTED)
                .build();

        GcmNetworkManager.getInstance(this)
                .schedule(syncTask);
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
            ViewGroup.MarginLayoutParams statusBarBackgroundLayoutParams =
                    (ViewGroup.MarginLayoutParams) binding.statusBarBackground.getLayoutParams();
            statusBarBackgroundLayoutParams.height = insets.getSystemWindowInsetTop();
            binding.statusBarBackground.setLayoutParams(statusBarBackgroundLayoutParams);
            binding.statusBarBackground.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams statusBarBackgroundDrawerLayoutParams =
                    (ViewGroup.MarginLayoutParams) binding.statusBarBackgroundDrawer.getLayoutParams();
            statusBarBackgroundDrawerLayoutParams.height = insets.getSystemWindowInsetTop();
            binding.statusBarBackgroundDrawer.setLayoutParams(statusBarBackgroundDrawerLayoutParams);
            binding.statusBarBackgroundDrawer.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams navigationBarBackgroundLayoutParams =
                    (ViewGroup.MarginLayoutParams) binding.navigationBarBackground.getLayoutParams();
            navigationBarBackgroundLayoutParams.height = insets.getSystemWindowInsetBottom();
            binding.navigationBarBackground.setLayoutParams(navigationBarBackgroundLayoutParams);
            binding.navigationBarBackground.setVisibility(View.VISIBLE);

            ViewGroup.MarginLayoutParams navigationBarBackgroundDrawerLayoutParams =
                    (ViewGroup.MarginLayoutParams) binding.navigationBarBackgroundDrawer.getLayoutParams();
            navigationBarBackgroundDrawerLayoutParams.height = insets.getSystemWindowInsetBottom();
            binding.navigationBarBackgroundDrawer.setLayoutParams(navigationBarBackgroundDrawerLayoutParams);
            binding.navigationBarBackgroundDrawer.setVisibility(View.VISIBLE);

            binding.toolbar.setPadding(
                    binding.toolbar.getPaddingLeft() + insets.getSystemWindowInsetLeft(),
                    binding.toolbar.getPaddingTop(),
                    binding.toolbar.getPaddingRight() + insets.getSystemWindowInsetRight(),
                    binding.toolbar.getPaddingBottom());

            binding.grid.setPadding(
                    binding.grid.getPaddingLeft() + insets.getSystemWindowInsetLeft(),
                    binding.grid.getPaddingTop(),
                    binding.grid.getPaddingRight() + insets.getSystemWindowInsetRight(),
                    binding.grid.getPaddingBottom() + insets.getSystemWindowInsetBottom());

            binding.navigation.setPadding(
                    binding.navigation.getPaddingLeft(),
                    binding.navigation.getPaddingTop(),
                    binding.navigation.getPaddingRight() + insets.getSystemWindowInsetRight(),
                    binding.navigation.getPaddingBottom() + insets.getSystemWindowInsetBottom());

            binding.getRoot().setOnApplyWindowInsetsListener(null);

            return insets.consumeSystemWindowInsets();
        }
    }

    private class AdapterObserver extends RecyclerView.AdapterDataObserver {
        @Override
        public void onChanged() {
            updateEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            onChanged();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            onChanged();
        }
    }
}
