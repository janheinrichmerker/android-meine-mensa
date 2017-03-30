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

import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.heinrichreimer.meinemensa.R;
import com.heinrichreimer.meinemensa.annotations.PriceCategory;
import com.heinrichreimer.meinemensa.databinding.ActivityDetailNoImageBinding;
import com.heinrichreimer.meinemensa.model.Meal;
import com.heinrichreimer.meinemensa.util.ColorUtils;
import com.heinrichreimer.meinemensa.util.IntroUtils;
import com.heinrichreimer.meinemensa.util.PreferencesUtils;

import io.realm.Realm;
import io.realm.RealmChangeListener;

public class DetailActivityNoImage extends LocationActivity {

    public static final String EXTRA_ID = "com.heinrichreimer.meinemensa.ID";

    private ActivityDetailNoImageBinding binding;
    private Realm realm;
    private
    @PriceCategory
    int priceCategory;
    private MenuItem menuItemDirections;
    private MenuItem menuItemVegetarian;
    private
    @ColorInt
    int menuItemTint = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_App);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail_no_image);
        setSupportActionBar(binding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        realm = Realm.getDefaultInstance();

        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(EXTRA_ID)) {
            finish();
            return;
        }

        long id = extras.getLong(EXTRA_ID);

        priceCategory = PreferencesUtils.getPriceCategory(this);

        realm.where(Meal.class)
                .equalTo(Meal.ID, id)
                .findFirstAsync()
                .addChangeListener(new RealmChangeListener<Meal>() {
                    @Override
                    public void onChange(Meal meal) {
                        updateMeal(meal);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        menuItemDirections = menu.findItem(R.id.menu_item_directions);
        menuItemVegetarian = menu.findItem(R.id.menu_item_vegetarian);
        if (menuItemTint != 0) {
            ColorUtils.tintMenuItemIcon(menuItemDirections, menuItemTint);
            ColorUtils.tintMenuItemIcon(menuItemVegetarian, menuItemTint);
        }
        if (binding.getMeal() != null) {
            menuItemVegetarian.setVisible(binding.getMeal().isVegetarian());
        }
        IntroUtils.showIntro(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_item_directions) {
            if (checkLocationPermission()) {
                navigateToNearestLocation();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onLocationPermissionGranted() {
        navigateToNearestLocation();
    }

    @Override
    protected void onDestroy() {
        if (realm != null) {
            realm.close();
        }
        super.onDestroy();
    }

    private void updateMeal(Meal meal) {
        if (!meal.isValid()) {
            finish();
            return;
        }

        binding.setMeal(meal);
        binding.setPriceCategory(priceCategory);
        binding.executePendingBindings();

        Palette.Swatch swatch = new Palette.Swatch(ColorUtils.getStatusBarColor(meal.getColor()), 0);
        int systemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
        @ColorInt final int textPrimary = swatch.getBodyTextColor();
        @ColorInt final int textSecondary = swatch.getTitleTextColor();
        if (!ColorUtils.isLight(textPrimary)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
        }

        Drawable navIcon = binding.toolbar.getNavigationIcon();
        if (navIcon != null) {
            DrawableCompat.setTint(navIcon, textPrimary);
        }
        if (menuItemDirections != null && menuItemVegetarian != null) {
            ColorUtils.tintMenuItemIcon(menuItemDirections, textPrimary);
            ColorUtils.tintMenuItemIcon(menuItemVegetarian, textPrimary);
            menuItemVegetarian.setVisible(meal.isVegetarian());
        } else {
            menuItemTint = textPrimary;
        }
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ColorUtils.getStatusBarColor(meal.getColor()));
        }

        binding.name.setTextColor(textPrimary);
        binding.description.setTextColor(textSecondary);
        binding.vegetarian.setTextColor(textSecondary);
        binding.price.setTextColor(textPrimary);
    }
}
