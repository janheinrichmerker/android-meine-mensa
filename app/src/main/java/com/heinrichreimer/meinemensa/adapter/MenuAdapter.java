package com.heinrichreimer.meinemensa.adapter;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.support.annotation.ColorInt;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.heinrichreimer.meinemensa.R;
import com.heinrichreimer.meinemensa.annotations.GridViewType;
import com.heinrichreimer.meinemensa.annotations.Location;
import com.heinrichreimer.meinemensa.annotations.PriceCategory;
import com.heinrichreimer.meinemensa.app.DetailActivity;
import com.heinrichreimer.meinemensa.app.DetailActivityNoImage;
import com.heinrichreimer.meinemensa.databinding.GridItemBinding;
import com.heinrichreimer.meinemensa.databinding.GridItemNoImageBinding;
import com.heinrichreimer.meinemensa.model.Meal;
import com.heinrichreimer.meinemensa.network.InternalUrlLoader;
import com.heinrichreimer.meinemensa.util.ColorUtils;
import com.heinrichreimer.meinemensa.util.GlideUtils;
import com.heinrichreimer.meinemensa.util.PreferencesUtils;

import org.joda.time.DateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmRecyclerViewAdapter;
import io.realm.RealmResults;
import io.realm.Sort;

public class MenuAdapter extends RealmRecyclerViewAdapter<Meal, RecyclerView.ViewHolder> {

    @IntDef({UpdateReason.PRICE_CATEGORY})
    @Retention(RetentionPolicy.SOURCE)
    private @interface UpdateReason {
        int PRICE_CATEGORY = 1;
    }

    private static final String DEBUG_TAG = "MenuAdapter";

    private
    @Location
    int[] locations;
    private
    @PriceCategory
    int priceCategory;
    private DateTime date;

    private MenuAdapter(Context context, OrderedRealmCollection<Meal> data, Builder builder) {
        super(context, data, true);
        this.locations = builder.locations;
        this.priceCategory = builder.priceCategory;
        this.date = builder.date;

        setHasStableIds(true);
    }

    @Override
    @GridViewType
    public int getItemViewType(int position) {
        if (getData() == null) {
            return GridViewType.IMAGE;
        }
        Meal meal = getData().get(position);
        if (meal == null) {
            return GridViewType.IMAGE;
        }
        return meal.hasImage() ? GridViewType.IMAGE : GridViewType.NO_IMAGE;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, @GridViewType int viewType) {
        switch (viewType) {
            case GridViewType.NO_IMAGE:
                return new ViewHolderNoImage(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.grid_item_no_image, parent, false));
            case GridViewType.IMAGE:
            default:
                return new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.grid_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder uncastHolder, int position, List<Object> payloads) {
        if (payloads.contains(UpdateReason.PRICE_CATEGORY)) {
            if (uncastHolder instanceof ViewHolderNoImage) {
                ViewHolderNoImage holder = (ViewHolderNoImage) uncastHolder;
                holder.binding.setPriceCategory(priceCategory);
                holder.binding.executePendingBindings();
            } else if (uncastHolder instanceof ViewHolder) {
                ViewHolder holder = (ViewHolder) uncastHolder;
                holder.binding.setPriceCategory(priceCategory);
                holder.binding.executePendingBindings();
            }
        } else {
            onBindViewHolder(uncastHolder, position);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder uncastHolder, int position) {
        final Context context = uncastHolder.itemView.getContext();
        final Meal meal = getItem(position);
        if (meal == null) {
            return;
        }

        if (uncastHolder instanceof ViewHolderNoImage) {
            final ViewHolderNoImage holder = (ViewHolderNoImage) uncastHolder;

            holder.binding.setMeal(meal);
            holder.binding.setPriceCategory(priceCategory);
            holder.binding.executePendingBindings();

            holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailActivityNoImage.class);
                    intent.putExtra(DetailActivityNoImage.EXTRA_ID, meal.getId());
                    context.startActivity(intent);
                }
            });

            Palette.Swatch swatch = new Palette.Swatch(ColorUtils.getStatusBarColor(meal.getColor()), 0);
            @ColorInt final int textPrimary = swatch.getBodyTextColor();
            @ColorInt final int textSecondary = swatch.getTitleTextColor();
            ColorUtils.tintImageView(holder.binding.vegetarian, textPrimary);
            holder.binding.name.setTextColor(textPrimary);
            holder.binding.description.setTextColor(textSecondary);
            holder.binding.price.setTextColor(textPrimary);
        } else if (uncastHolder instanceof ViewHolder) {
            final ViewHolder holder = (ViewHolder) uncastHolder;

            holder.binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra(DetailActivity.EXTRA_ID, meal.getId());
                    context.startActivity(intent);
                }
            });

            holder.binding.setMeal(meal);
            holder.binding.setPriceCategory(priceCategory);
            holder.binding.executePendingBindings();

            holder.binding.progress.setVisibility(View.VISIBLE);

            RequestListener<String, GlideDrawable> listener = new RequestListener<String, GlideDrawable>() {
                @Override
                public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                    holder.binding.progress.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                    holder.binding.progress.setVisibility(View.GONE);
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
                                    final Palette.Swatch swatch = ColorUtils.parsePalette(palette, meal.getColor());
                                    holder.binding.scrim.setBackgroundColor(swatch.getRgb());
                                    holder.binding.name.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            holder.binding.name.setTextColor(swatch.getBodyTextColor());
                                        }
                                    });
                                    ColorUtils.tintImageView(holder.binding.vegetarian, swatch.getBodyTextColor());
                                    holder.binding.price.setTextColor(swatch.getBodyTextColor());
                                }
                            });
                    return false;
                }
            };

            Glide.with(context)
                    .using(new InternalUrlLoader(context))
                    .load(meal.getImageUrl())
                    .listener(listener)
                    .crossFade()
                    .centerCrop()
                    .into(holder.binding.image);
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        GridItemBinding binding;

        private ViewHolder(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

    private class ViewHolderNoImage extends RecyclerView.ViewHolder {
        GridItemNoImageBinding binding;

        private ViewHolderNoImage(View itemView) {
            super(itemView);
            binding = DataBindingUtil.bind(itemView);
        }
    }

    public UpdateBuilder update(Context context) {
        return new UpdateBuilder(this, context);
    }

    public static class Builder extends AbstractUpdateBuilder {
        private Builder(Context context) {
            super(context);
        }

        @NonNull
        public static Builder with(Context context) {
            return new Builder(context);
        }

        @Override
        public MenuAdapter build() {
            RealmResults<Meal> results = getResults();
            return new MenuAdapter(context, results, this);
        }
    }

    public static class UpdateBuilder extends AbstractUpdateBuilder {
        private MenuAdapter adapter;

        private UpdateBuilder(MenuAdapter adapter, Context context) {
            super(context);
            this.adapter = adapter;
        }

        @Override
        public MenuAdapter build() {
            if (date.equals(adapter.date) && locations == adapter.locations &&
                    priceCategory != adapter.priceCategory) {
                adapter.priceCategory = priceCategory;
                adapter.notifyItemRangeChanged(0, adapter.getItemCount(), UpdateReason.PRICE_CATEGORY);
                return adapter;
            }

            RealmResults<Meal> results = getResults();

            adapter.updateData(results);
            adapter.locations = locations;
            adapter.priceCategory = priceCategory;
            adapter.date = date;

            return adapter;
        }
    }

    public abstract static class AbstractUpdateBuilder {
        protected Context context;
        protected Realm realm;
        protected
        @Location
        int[] locations;
        protected
        @PriceCategory
        int priceCategory;
        protected
        @NonNull
        DateTime date = DateTime.now();
        protected boolean vegetarianOnly;

        private AbstractUpdateBuilder(Context context) {
            this.context = context;
            this.realm = Realm.getDefaultInstance();
            this.locations = PreferencesUtils.getLocations(context);
            this.priceCategory = PreferencesUtils.getPriceCategory(context);
            this.date = PreferencesUtils.getNextWeekdayDate(context);
            this.vegetarianOnly = PreferencesUtils.isVegetarianOnly(context);
        }

        public AbstractUpdateBuilder realm(Realm realm) {
            this.realm = realm;
            return this;
        }

        public AbstractUpdateBuilder locations(@Location int[] locations) {
            this.locations = locations;
            return this;
        }

        public AbstractUpdateBuilder priceCategory(int priceCategory) {
            this.priceCategory = priceCategory;
            return this;
        }

        public AbstractUpdateBuilder date(@NonNull DateTime date) {
            this.date = date;
            return this;
        }

        public AbstractUpdateBuilder vegetarianOnly(boolean vegetarianOnly) {
            this.vegetarianOnly = vegetarianOnly;
            return this;
        }

        protected RealmResults<Meal> getResults() {
            if (realm == null) {
                throw new IllegalArgumentException("Realm must not be null.");
            }
            if (!PriceCategory.Converter.isPriceCategory(priceCategory)) {
                throw new IllegalArgumentException("Price category is not valid.");
            }

            DateTime day = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0);

            Log.d(DEBUG_TAG, "Loading menu from Realm (locations = " + Arrays.toString(locations) + "; date = " + day.getMillis() + "; vegetarianOnly = " + vegetarianOnly + ")...");

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

            RealmResults<Meal> results;
            results = query.findAllSortedAsync(Meal.NAME, Sort.ASCENDING);
            /*TODO enable this once https://github.com/realm/realm-java/issues/672 is implemented and remove line above
            switch (priceCategory) {
                case PriceCategory.STUDENTS:
                    results = query.findAllSortedAsync(Meal.PRICE_STUDENTS, Sort.DESCENDING,
                            Meal.NAME, Sort.ASCENDING);
                    break;
                case PriceCategory.EMPLOYEES:
                    results = query.findAllSortedAsync(Meal.PRICE_EMPLOYEES, Sort.DESCENDING,
                            Meal.NAME, Sort.ASCENDING);
                    break;
                case PriceCategory.GUESTS:
                default:
                    results = query.findAllSortedAsync(Meal.PRICE_GUESTS, Sort.DESCENDING,
                            Meal.NAME, Sort.ASCENDING);
                    break;
            }
            */
            return results;
        }

        public abstract MenuAdapter build();
    }
}
