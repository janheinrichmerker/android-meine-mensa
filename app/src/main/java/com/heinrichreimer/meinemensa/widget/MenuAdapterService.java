package com.heinrichreimer.meinemensa.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.bumptech.glide.Glide;
import com.heinrichreimer.meinemensa.R;
import com.heinrichreimer.meinemensa.annotations.Location;
import com.heinrichreimer.meinemensa.annotations.PriceCategory;
import com.heinrichreimer.meinemensa.model.Meal;
import com.heinrichreimer.meinemensa.model.UnlinkedMeal;
import com.heinrichreimer.meinemensa.network.InternalUrlLoader;
import com.heinrichreimer.meinemensa.util.ColorUtils;
import com.heinrichreimer.meinemensa.util.PreferencesUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class MenuAdapterService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new Factory(this, intent);
    }

    private static class Factory implements RemoteViewsFactory {
        private Context context;
        private int widgetId;
        private List<UnlinkedMeal> menu;
        private
        @PriceCategory
        int priceCategory;

        private Factory(Context context, Intent intent) {
            this.context = context;
            widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDataSetChanged() {
            Realm realm = Realm.getDefaultInstance();
            @Location int[] locations = PreferencesUtils.getLocations(context);
            priceCategory = PreferencesUtils.getPriceCategory(context);
            DateTime date = PreferencesUtils.getNextWeekdayDate(context);
            boolean vegetarianOnly = PreferencesUtils.isVegetarianOnly(context);

            DateTime day = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0);


            RealmQuery<Meal> query = realm.where(Meal.class);

            //Locations
            query.beginGroup();
            boolean first = true;
            for (int location : locations) {
                if (Location.Converter.isLocation(location)) {
                    if (first) {
                        first = false;
                    } else {
                        query.or();
                    }
                    query.equalTo(Meal.LOCATION, location);
                }
            }
            query.endGroup();

            //Date
            query.equalTo(Meal.DATE, day.getMillis());

            //Vegetarian
            if (vegetarianOnly) {
                query.equalTo(Meal.VEGETARIAN, true);
            }

            final RealmResults<Meal> results;
            results = query.findAllSorted(Meal.NAME, Sort.ASCENDING);
            /*TODO enable this once https://github.com/realm/realm-java/issues/672 is implemented and remove line above
            switch (priceCategory) {
                case PriceCategory.STUDENTS:
                    results = query.findAllSorted(Meal.PRICE_STUDENTS, Sort.DESCENDING,
                            Meal.NAME, Sort.ASCENDING);
                    break;
                case PriceCategory.EMPLOYEES:
                    results = query.findAllSorted(Meal.PRICE_EMPLOYEES, Sort.DESCENDING,
                            Meal.NAME, Sort.ASCENDING);
                    break;
                case PriceCategory.GUESTS:
                default:
                    results = query.findAllSorted(Meal.PRICE_GUESTS, Sort.DESCENDING,
                            Meal.NAME, Sort.ASCENDING);
                    break;
            }
            */

            menu = new ArrayList<>(results.size());
            for (Meal meal : results) {
                menu.add(new UnlinkedMeal(meal));
            }
            realm.close();
        }

        @Override
        public void onDestroy() {
        }

        @Override
        public int getCount() {
            return menu.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            final UnlinkedMeal meal = menu.get(position);

            final RemoteViews remoteViews = new RemoteViews(context.getApplicationContext().getPackageName(),
                    R.layout.widget_grid_item);

            //Name
            remoteViews.setTextViewText(R.id.name, meal.getName());

            //Price
            String price = context.getString(R.string.label_price,
                    meal.getPrice().forCategory(priceCategory));
            remoteViews.setViewVisibility(R.id.price, View.VISIBLE);
            remoteViews.setTextViewText(R.id.price, price);

            //Click intent
            final Intent clickIntent = new Intent(context, DetailActivityLauncher.class);
            clickIntent.putExtra(DetailActivityLauncher.EXTRA_ID, meal.getId());
            clickIntent.putExtra(DetailActivityLauncher.EXTRA_HAS_IMAGE, meal.hasImage());
            remoteViews.setOnClickFillInIntent(R.id.root, clickIntent);

            if (meal.hasImage()) {
                //Name
                remoteViews.setInt(R.id.name, "setMaxLines", 2);

                //Description
                remoteViews.setViewVisibility(R.id.description, View.GONE);

                //Image
                remoteViews.setViewVisibility(R.id.image, View.VISIBLE);
                remoteViews.setContentDescription(R.id.image, meal.getName());

                final int width = context.getResources().getDimensionPixelSize(R.dimen.widget_image_width);
                final int height = context.getResources().getDimensionPixelSize(R.dimen.widget_item_height);

                try {
                    Bitmap resource = Glide.with(context)
                            .using(new InternalUrlLoader(context))
                            .load(meal.getImageUrl())
                            .asBitmap()
                            .centerCrop()
                            .into(width, height)
                            .get();

                    //Image
                    remoteViews.setImageViewBitmap(R.id.image, resource);

                    Palette palette = Palette.from(resource)
                            .setRegion(resource.getWidth() / 4,
                                    resource.getHeight() / 4,
                                    resource.getWidth() / 4 * 3,
                                    resource.getHeight() / 4 * 3)
                            .generate();

                    Palette.Swatch swatch = ColorUtils.parsePalette(palette, meal.getColor());

                    //Background
                    remoteViews.setInt(R.id.background, "setBackgroundColor",
                            android.support.v4.graphics.ColorUtils.setAlphaComponent(
                                    swatch.getRgb(), Math.round(0xFF * .87f)));

                    //Name
                    remoteViews.setTextColor(R.id.name, swatch.getBodyTextColor());

                    //Description
                    remoteViews.setTextColor(R.id.description, swatch.getTitleTextColor());

                    //Price
                    remoteViews.setTextColor(R.id.price, swatch.getBodyTextColor());

                    //Image
                    remoteViews.setImageViewBitmap(R.id.image, resource);

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

            } else {
                Palette.Swatch swatch = new Palette.Swatch(meal.getColor(), 0);

                //Background
                remoteViews.setInt(R.id.background, "setBackgroundColor",
                        android.support.v4.graphics.ColorUtils.setAlphaComponent(
                                swatch.getRgb(), Math.round(0xFF * .87f)));

                //Name
                remoteViews.setInt(R.id.name, "setMaxLines", 1);
                remoteViews.setTextColor(R.id.name, swatch.getBodyTextColor());

                //Description
                remoteViews.setViewVisibility(R.id.description, View.VISIBLE);
                remoteViews.setTextViewText(R.id.description, meal.getDescription());
                remoteViews.setTextColor(R.id.description, swatch.getTitleTextColor());

                //Price
                remoteViews.setTextColor(R.id.price, swatch.getBodyTextColor());

                //Image
                remoteViews.setViewVisibility(R.id.image, View.GONE);
            }

            return remoteViews;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(context.getApplicationContext().getPackageName(),
                    R.layout.widget_loading_view);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public long getItemId(int position) {
            return menu.get(position).getId();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
