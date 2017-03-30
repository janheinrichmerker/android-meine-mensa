package com.heinrichreimer.meinemensa.model;

import android.text.TextUtils;

import com.heinrichreimer.meinemensa.annotations.Location;

import org.joda.time.DateTime;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Meal extends RealmObject {
    public static final String ID = "id";
    public static final String COLOR = "color";
    public static final String NAME = "name";
    public static final String DESCRIPTION = "description";
    public static final String IMAGE_URL = "imageUrl";
    public static final String PRICE_STUDENTS = "price." + Price.STUDENTS;
    public static final String PRICE_EMPLOYEES = "price." + Price.EMPLOYEES;
    public static final String PRICE_GUESTS = "price." + Price.GUESTS;
    public static final String LOCATION = "location";
    public static final String DATE = "date";
    public static final String VEGETARIAN = "vegetarian";

    @PrimaryKey
    private long id;
    private int color;
    private String name;
    private String description;
    private String imageUrl;
    private Price price;
    @Location
    private int location;
    private long date;
    private boolean vegetarian;

    public Meal() {
    }

    private Meal(Builder builder) {
        this.id = builder.id;
        this.color = builder.color;
        this.name = builder.name;
        this.description = builder.description;
        this.imageUrl = builder.imageUrl;
        this.price = builder.price;
        this.location = builder.location;
        this.date = builder.date;
        this.vegetarian = builder.vegetarian;
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

    public Price getPrice() {
        return price;
    }

    @Location
    public int getLocation() {
        return location;
    }

    public DateTime getDate() {
        return new DateTime(date);
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public static class Builder {
        private long id;
        private int color;
        private String name;
        private String description;
        private String imageUrl;
        private Price price;
        @Location
        private int location;
        private long date;
        private boolean vegetarian;

        public Builder id(long id) {
            this.id = id;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder imageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
            return this;
        }

        public Builder price(Price price) {
            this.price = price;
            return this;
        }

        public Builder location(@Location int location) {
            this.location = location;
            return this;
        }

        public Builder date(DateTime date) {
            this.date = date.getMillis();
            return this;
        }

        public Builder vegetarian(boolean vegetarian) {
            this.vegetarian = vegetarian;
            return this;
        }

        public Meal build() {
            return new Meal(this);
        }
    }
}