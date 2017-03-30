package com.heinrichreimer.meinemensa.app;

import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.graphics.Palette;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.heinrichreimer.meinemensa.R;
import com.heinrichreimer.meinemensa.annotations.PriceCategory;
import com.heinrichreimer.meinemensa.databinding.ActivityDetailBinding;
import com.heinrichreimer.meinemensa.model.Meal;
import com.heinrichreimer.meinemensa.network.InternalUrlLoader;
import com.heinrichreimer.meinemensa.util.ColorUtils;
import com.heinrichreimer.meinemensa.util.GlideUtils;
import com.heinrichreimer.meinemensa.util.IntroUtils;
import com.heinrichreimer.meinemensa.util.PreferencesUtils;

import io.realm.Realm;
import io.realm.RealmChangeListener;

public class DetailActivity extends LocationActivity {

    public static final String EXTRA_ID = "com.heinrichreimer.meinemensa.ID";

    private ActivityDetailBinding binding;
    private Realm realm;
    private
    @PriceCategory
    int priceCategory;
    private MenuItem menuItemVegetarian;
    private MenuItem menuItemDirections;
    private
    @ColorInt
    int menuItemTint = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_App);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail);
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

    private void updateMeal(final Meal meal) {
        if (!meal.isValid()) {
            finish();
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed()) {
            return;
        }

        binding.setMeal(meal);
        binding.setPriceCategory(priceCategory);

        binding.progress.setVisibility(View.VISIBLE);

        RequestListener<String, GlideDrawable> listener = new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                binding.progress.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                binding.progress.setVisibility(View.GONE);
                final Bitmap bitmap = GlideUtils.getBitmap(resource);
                if (bitmap == null || bitmap.isRecycled()) {
                    return false;
                }

                Palette.from(bitmap)
                        .maximumColorCount(5)
                        .setRegion(bitmap.getWidth() / 4,
                                bitmap.getHeight() / 4,
                                bitmap.getWidth() / 4 * 3,
                                bitmap.getHeight() / 4 * 3)
                        .generate(new Palette.PaletteAsyncListener() {
                            @Override
                            public void onGenerated(Palette palette) {
                                Palette.Swatch swatch = ColorUtils.parsePalette(palette, binding.getMeal().getColor());
                                binding.scrim.setBackgroundColor(swatch.getRgb());
                                binding.name.setTextColor(swatch.getBodyTextColor());
                                binding.description.setTextColor(swatch.getTitleTextColor());
                                binding.vegetarian.setTextColor(swatch.getTitleTextColor());
                                binding.price.setTextColor(swatch.getBodyTextColor());
                            }
                        });


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                            24, getResources().getDisplayMetrics());
                    Palette.from(bitmap)
                            .maximumColorCount(3)
                            .clearFilters()
                            .setRegion(0, 0, bitmap.getWidth() - 1, twentyFourDip)
                            .generate(new Palette.PaletteAsyncListener() {
                                @Override
                                public void onGenerated(Palette palette) {
                                    Palette.Swatch swatch = ColorUtils.parsePalette(palette, Color.BLACK);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                        getWindow().setStatusBarColor(swatch.getRgb());
                                    }

                                    int systemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                                    @ColorInt final int foregroundPrimary;
                                    if (ColorUtils.isLight(swatch.getRgb())) {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            systemUiVisibility |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                                        }
                                        foregroundPrimary = ContextCompat.getColor(DetailActivity.this, R.color.text_color_primary);
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            systemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                                        }
                                        foregroundPrimary = ContextCompat.getColor(DetailActivity.this, R.color.text_color_primary_inverse);
                                    }
                                    Drawable navIcon = binding.toolbar.getNavigationIcon();
                                    if (navIcon != null) {
                                        DrawableCompat.setTint(navIcon, foregroundPrimary);
                                    }
                                    if (menuItemDirections != null && menuItemVegetarian != null) {
                                        ColorUtils.tintMenuItemIcon(menuItemDirections, foregroundPrimary);
                                        ColorUtils.tintMenuItemIcon(menuItemVegetarian, foregroundPrimary);
                                        menuItemVegetarian.setVisible(meal.isVegetarian());
                                    } else {
                                        menuItemTint = foregroundPrimary;
                                    }
                                    getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
                                }
                            });
                }
                return false;
            }
        };

        Glide.with(this)
                .using(new InternalUrlLoader(this))
                .load(meal.getImageUrl())
                .listener(listener)
                .crossFade()
                .centerCrop()
                .into(binding.image);
    }
}
