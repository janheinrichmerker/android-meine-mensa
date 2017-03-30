/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.heinrichreimer.meinemensa.parse;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Xml;

import com.heinrichreimer.meinemensa.annotations.Location;
import com.heinrichreimer.meinemensa.model.Meal;
import com.heinrichreimer.meinemensa.model.Price;
import com.heinrichreimer.meinemensa.util.ColorUtils;

import org.joda.time.DateTime;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class MenuParser {

    private static final Pattern INTEGER_PATTERN = Pattern.compile("[0-9]+");
    private static final Pattern DECIMAL_PATTERN = Pattern.compile("[0-9]+,[0-9]+");
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#([A-F0-9]{8}|[A-F0-9]{6})$", Pattern.CASE_INSENSITIVE);
    private static final String IMAGE_THUMBNAIL_SUFFIX = "_gadget.png";
    private static final Pattern IMAGE_PATH_PATTERN = Pattern.compile("https?+://+[A-Za-z0-9-._~:/?#\\[\\]@!$&'()*+,;=%]+(jpe?g|png|gif)", Pattern.CASE_INSENSITIVE);
    private static final String[] IMAGE_PLACEHOLDER_URLS = {
            "http://meine-mensa.de/mediathek/1314719149_e31f525acabd23968a1fd204e0b822a8.jpg",
            "http://meine-mensa.de/mediathek/1242228528_faff828197dc2e6a2f714f816ca473b9.jpg",
            "http://meine-mensa.de/mediathek/1317797040_c4725cdebc15d556c06b73e83d7310cb.jpg"
    };
    private static final String[] VEGETARIAN_WHITE_LIST = {
            "vegetarisch",
            "vegan",
            "soja"
    };
    private static final String[] VEGETARIAN_BLACK_LIST = {
            "fleisch",
            "fisch",
            "schwein",
            "rind",
            "kaninchen",
            "hase",
            "lamm",
            "hahn",
            "huhn",
            "pute",
            "wurst",
            "hähnchen",
            "hühnchen",
            "hühner",
            "forelle",
            "lachs",
            "scholle",
            "garnele",
            "kassler",
            "kasseler",
            "würstchen",
            "knacker",
            "speck",
            "filet",
            "steak",
            "braten",
            "roulade",
            "roullade",
            "schnitzel",
            "cordon bleu",
            "schaschlik",
            "geschnetzeltes",
            "gulasch",
            "kotelett",
            "bbq",
            "burger",
            "hamburger",
            "frikadelle",
            "klopse",
            "rippchen",
            "carne",
            "bolognese",
            "carbonara"
    };

    private static final String NAMESPACE = null;

    private final XmlPullParser parser;
    private final DateTime date;
    @Location
    private final int location;

    private MenuParser(XmlPullParser parser, DateTime date, @Location int location) {
        this.parser = parser;
        this.date = new DateTime(date.getYear(), date.getMonthOfYear(), date.getDayOfMonth(), 0, 0);
        this.location = location;
    }

    @Nullable
    public static MenuParser parse(XmlPullParser parser, DateTime date, @Location int location) {
        return new MenuParser(parser, date, location);
    }

    @Nullable
    public static MenuParser parse(InputStream in, String inputEncoding, DateTime date, @Location int location) {
        XmlPullParser parser;
        try {
            parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, inputEncoding);
            parser.nextTag();
            return new MenuParser(parser, date, location);
        } catch (XmlPullParserException | IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    public static MenuParser parse(String in, String inputEncoding, DateTime date, @Location int location) {
        XmlPullParser parser;
        try {
            InputStream stream = new ByteArrayInputStream(in.getBytes(inputEncoding));
            try {
                parser = Xml.newPullParser();
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                parser.setInput(stream, inputEncoding);
                parser.nextTag();
                return new MenuParser(parser, date, location);
            } catch (XmlPullParserException | IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                try {
                    stream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NonNull
    public List<Meal> readMenu() {
        List<Meal> entries = new ArrayList<>();

        // Search for <data> tags. These wrap the beginning/end of an Atom document.
        try {
            parser.require(XmlPullParser.START_TAG, NAMESPACE, "data");

            while (parser.next() != XmlPullParser.END_TAG) {
                if (parser.getEventType() != XmlPullParser.START_TAG) {
                    continue;
                }
                String name = parser.getName();
                // Starts by looking for the <counter> tag. This tag repeats inside of <data> for each
                // article in the feed.
                if (name.equals("counter")) {
                    try {
                        entries.add(readMeal());
                    } catch (IOException | XmlPullParserException ignored) {
                    }
                } else {
                    skip();
                }
            }

        } catch (XmlPullParserException | IOException e) {
            return entries;
        }

        return entries;
    }

    private Meal readMeal() throws IOException, XmlPullParserException {
        Meal.Builder meal = new Meal.Builder();

        parser.require(XmlPullParser.START_TAG, NAMESPACE, "counter");


        String color = parser.getAttributeValue(null, "bg_color");
        if (HEX_COLOR_PATTERN.matcher(color).matches()) {
            meal.color(ColorUtils.getPastelColor(Color.parseColor(color)));
        }
        String id = parser.getAttributeValue(null, "foodplan_id");
        if (INTEGER_PATTERN.matcher(id).matches()) {
            meal.id(Long.parseLong(id));
        }
        String name = parser.getAttributeValue(null, "food_name");
        if (!TextUtils.isEmpty(name)) {
            name = name.replace("Komponente", "Beilage");
            if (!TextUtils.isEmpty(name)) {
                meal.name(name);
            }
        }
        String description = parser.getAttributeValue(null, "food_name2");
        if (!TextUtils.isEmpty(description)) {
            meal.description(description);
        }
        String imageUrl = parser.getAttributeValue(null, "img_url");
        if (!TextUtils.isEmpty(imageUrl)) {
            if (imageUrl.endsWith(IMAGE_THUMBNAIL_SUFFIX)) {
                imageUrl = imageUrl.substring(0, imageUrl.length() - IMAGE_THUMBNAIL_SUFFIX.length());
            }
            boolean isPlaceholder = false;
            for (String imagePlaceholderUrl : IMAGE_PLACEHOLDER_URLS) {
                if (imagePlaceholderUrl.equals(imageUrl)) {
                    isPlaceholder = true;
                    break;
                }
            }
            if (!isPlaceholder && IMAGE_PATH_PATTERN.matcher(imageUrl).matches()) {
                imageUrl = imageUrl.replace(IMAGE_THUMBNAIL_SUFFIX, "");
                meal.imageUrl(imageUrl);
            }
        }
        meal.date(date);
        meal.location(location);

        if ("Tagessuppe".equals(name) && !TextUtils.isEmpty(description)) {
            meal.name(description);
            meal.description(name);
        }

        Price.Builder price = new Price.Builder();
        String priceStudents = parser.getAttributeValue(null, "food_price_cat_1");
        if (DECIMAL_PATTERN.matcher(priceStudents).matches()) {
            price.forStudents(priceStudents);
        }
        String priceEmployees = parser.getAttributeValue(null, "food_price_cat_2");
        if (DECIMAL_PATTERN.matcher(priceEmployees).matches()) {
            price.forEmployees(priceEmployees);
        }
        String priceGuests = parser.getAttributeValue(null, "food_price_cat_3");
        if (DECIMAL_PATTERN.matcher(priceGuests).matches()) {
            price.forGuests(priceGuests);
        }
        meal.price(price.build());

        boolean vegetarian = false;
        String nameLowerCase = name.toLowerCase();
        String descriptionLowerCase = description.toLowerCase();
        for (String search : VEGETARIAN_WHITE_LIST) {
            if (nameLowerCase.contains(search) || descriptionLowerCase.contains(search)) {
                vegetarian = true;
                break;
            }
        }
        if (vegetarian) {
            meal.vegetarian(true);
        } else {
            vegetarian = true;
            for (String search : VEGETARIAN_BLACK_LIST) {
                if (nameLowerCase.contains(search) || descriptionLowerCase.contains(search)) {
                    vegetarian = false;
                    break;
                }
            }
            meal.vegetarian(vegetarian);
        }

        while (true) {
            if (parser.nextTag() == XmlPullParser.END_TAG) break;
            // Intentionally break; consumes any remaining sub-tags.
        }
        return meal.build();
    }

    /**
     * Skips tags the parser isn't interested in. Uses depth to handle nested tags. i.e.,
     * if the next tag after a START_TAG isn't a matching END_TAG, it keeps going until it
     * finds the matching END_TAG (as indicated by the value of "depth" being 0).
     */
    private void skip() throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}