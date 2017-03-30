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