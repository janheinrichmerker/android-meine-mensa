package com.heinrichreimer.meinemensa.model;

import android.text.TextUtils;

import com.heinrichreimer.meinemensa.annotations.Location;

import org.joda.time.DateTime;

import io.realm.annotations.PrimaryKey;

//TODO Use Realm#copyFromRealm() instead.
public class UnlinkedMeal {

    @PrimaryKey
    private long id;
    private int color;
    private String name;
    private String description;
    private String imageUrl;
    private UnlinkedPrice price;
    @Location
    private int location;
    private long date;

    public UnlinkedMeal(Meal meal) {
        this.id = meal.getId();
        this.color = meal.getColor();
        this.name = meal.getName();
        this.description = meal.getDescription();
        this.imageUrl = meal.getImageUrl();
        this.price = new UnlinkedPrice(meal.getPrice());
        this.location = meal.getLocation();
        this.date = meal.getDate().getMillis();
    }

    public long getId() {
        return id;
    }

    public int getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean hasDescription() {
        return !TextUtils.isEmpty(description);
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean hasImage() {
        return !TextUtils.isEmpty(imageUrl);
    }

    public UnlinkedPrice getPrice() {
        return price;
    }

    @Location
    public int getLocation() {
        return location;
    }

    public DateTime getDate() {
        return new DateTime(date);
    }
}