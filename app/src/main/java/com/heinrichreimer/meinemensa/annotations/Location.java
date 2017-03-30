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

package com.heinrichreimer.meinemensa.annotations;

import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.support.annotation.StringRes;
import android.util.SparseArray;
import android.util.SparseIntArray;

import com.google.android.gms.maps.model.LatLng;
import com.heinrichreimer.meinemensa.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.heinrichreimer.meinemensa.annotations.Location.HARZMENSA;
import static com.heinrichreimer.meinemensa.annotations.Location.HEIDEMENSA;
import static com.heinrichreimer.meinemensa.annotations.Location.MENSA_BERNBURG;
import static com.heinrichreimer.meinemensa.annotations.Location.MENSA_BURG;
import static com.heinrichreimer.meinemensa.annotations.Location.MENSA_DESSAU;
import static com.heinrichreimer.meinemensa.annotations.Location.MENSA_FRANCKESCHE_STIFTUNGEN;
import static com.heinrichreimer.meinemensa.annotations.Location.MENSA_KOETHEN;
import static com.heinrichreimer.meinemensa.annotations.Location.MENSA_MERSEBURG;
import static com.heinrichreimer.meinemensa.annotations.Location.MENSA_NEUWERK;
import static com.heinrichreimer.meinemensa.annotations.Location.MENSA_TULPE;
import static com.heinrichreimer.meinemensa.annotations.Location.WEINBERGMENSA;

@IntDef({HARZMENSA, WEINBERGMENSA, MENSA_KOETHEN, MENSA_BERNBURG, MENSA_NEUWERK,
        MENSA_TULPE, MENSA_BURG, MENSA_DESSAU, MENSA_FRANCKESCHE_STIFTUNGEN,
        MENSA_MERSEBURG, HEIDEMENSA})
@Retention(RetentionPolicy.SOURCE)
public @interface Location {
    int HARZMENSA = 3;
    int WEINBERGMENSA = 5;
    int MENSA_KOETHEN = 7;
    int MENSA_BERNBURG = 8;
    int MENSA_NEUWERK = 9;
    int MENSA_TULPE = 10;
    int MENSA_BURG = 12;
    int MENSA_DESSAU = 13;
    int MENSA_FRANCKESCHE_STIFTUNGEN = 14;
    int MENSA_MERSEBURG = 16;
    int HEIDEMENSA = 17;
    int[] ALL = {HARZMENSA, WEINBERGMENSA, MENSA_KOETHEN, MENSA_BERNBURG, MENSA_NEUWERK,
            MENSA_TULPE, MENSA_BURG, MENSA_DESSAU, MENSA_FRANCKESCHE_STIFTUNGEN,
            MENSA_MERSEBURG, HEIDEMENSA};

    class Converter {
        private static final SparseIntArray ID_TO_LOCATION = new SparseIntArray();

        static {
            ID_TO_LOCATION.put(R.id.menu_item_location_harzmensa, HARZMENSA);
            ID_TO_LOCATION.put(R.id.menu_item_location_weinbergmensa, WEINBERGMENSA);
            ID_TO_LOCATION.put(R.id.menu_item_location_mensa_koethen, MENSA_KOETHEN);
            ID_TO_LOCATION.put(R.id.menu_item_location_mensa_bernburg, MENSA_BERNBURG);
            ID_TO_LOCATION.put(R.id.menu_item_location_mensa_neuwerk, MENSA_NEUWERK);
            ID_TO_LOCATION.put(R.id.menu_item_location_mensa_tulpe, MENSA_TULPE);
            ID_TO_LOCATION.put(R.id.menu_item_location_mensa_burg, MENSA_BURG);
            ID_TO_LOCATION.put(R.id.menu_item_location_mensa_dessau, MENSA_DESSAU);
            ID_TO_LOCATION.put(R.id.menu_item_location_mensa_franckesche_stiftungen, MENSA_FRANCKESCHE_STIFTUNGEN);
            ID_TO_LOCATION.put(R.id.menu_item_location_mensa_merseburg, MENSA_MERSEBURG);
            ID_TO_LOCATION.put(R.id.menu_item_location_heidemensa, HEIDEMENSA);
        }

        private static final SparseIntArray LOCATION_TO_ID = new SparseIntArray();

        static {
            LOCATION_TO_ID.put(HARZMENSA, R.id.menu_item_location_harzmensa);
            LOCATION_TO_ID.put(WEINBERGMENSA, R.id.menu_item_location_weinbergmensa);
            LOCATION_TO_ID.put(MENSA_KOETHEN, R.id.menu_item_location_mensa_koethen);
            LOCATION_TO_ID.put(MENSA_BERNBURG, R.id.menu_item_location_mensa_bernburg);
            LOCATION_TO_ID.put(MENSA_NEUWERK, R.id.menu_item_location_mensa_neuwerk);
            LOCATION_TO_ID.put(MENSA_TULPE, R.id.menu_item_location_mensa_tulpe);
            LOCATION_TO_ID.put(MENSA_BURG, R.id.menu_item_location_mensa_burg);
            LOCATION_TO_ID.put(MENSA_DESSAU, R.id.menu_item_location_mensa_dessau);
            LOCATION_TO_ID.put(MENSA_FRANCKESCHE_STIFTUNGEN, R.id.menu_item_location_mensa_franckesche_stiftungen);
            LOCATION_TO_ID.put(MENSA_MERSEBURG, R.id.menu_item_location_mensa_merseburg);
            LOCATION_TO_ID.put(HEIDEMENSA, R.id.menu_item_location_heidemensa);
        }

        private static final SparseIntArray LOCATION_TO_STRING = new SparseIntArray();

        static {
            LOCATION_TO_STRING.put(HARZMENSA, R.string.menu_item_location_harzmensa);
            LOCATION_TO_STRING.put(WEINBERGMENSA, R.string.menu_item_location_weinbergmensa);
            LOCATION_TO_STRING.put(MENSA_KOETHEN, R.string.menu_item_location_mensa_koethen);
            LOCATION_TO_STRING.put(MENSA_BERNBURG, R.string.menu_item_location_mensa_bernburg);
            LOCATION_TO_STRING.put(MENSA_NEUWERK, R.string.menu_item_location_mensa_neuwerk);
            LOCATION_TO_STRING.put(MENSA_TULPE, R.string.menu_item_location_mensa_tulpe);
            LOCATION_TO_STRING.put(MENSA_BURG, R.string.menu_item_location_mensa_burg);
            LOCATION_TO_STRING.put(MENSA_DESSAU, R.string.menu_item_location_mensa_dessau);
            LOCATION_TO_STRING.put(MENSA_FRANCKESCHE_STIFTUNGEN, R.string.menu_item_location_mensa_franckesche_stiftungen);
            LOCATION_TO_STRING.put(MENSA_MERSEBURG, R.string.menu_item_location_mensa_merseburg);
            LOCATION_TO_STRING.put(HEIDEMENSA, R.string.menu_item_location_heidemensa);
        }

        private static final SparseArray<LatLng[]> LOCATION_TO_LAT_LNG = new SparseArray<>();

        static {
            LOCATION_TO_LAT_LNG.put(HARZMENSA, new LatLng[]{new LatLng(51.489667, 11.967568)});
            LOCATION_TO_LAT_LNG.put(WEINBERGMENSA, new LatLng[]{new LatLng(51.498618, 11.943504)});
            LOCATION_TO_LAT_LNG.put(MENSA_KOETHEN, new LatLng[]{new LatLng(51.746091, 11.983500), new LatLng(51.754041, 11.966399)});
            LOCATION_TO_LAT_LNG.put(MENSA_BERNBURG, new LatLng[]{new LatLng(51.824184, 11.710069)});
            LOCATION_TO_LAT_LNG.put(MENSA_NEUWERK, new LatLng[]{new LatLng(51.490966, 11.957390)});
            LOCATION_TO_LAT_LNG.put(MENSA_TULPE, new LatLng[]{new LatLng(51.486891, 11.968947)});
            LOCATION_TO_LAT_LNG.put(MENSA_BURG, new LatLng[]{new LatLng(51.502989, 11.954635)});
            LOCATION_TO_LAT_LNG.put(MENSA_DESSAU, new LatLng[]{new LatLng(51.840943, 12.230493)});
            LOCATION_TO_LAT_LNG.put(MENSA_FRANCKESCHE_STIFTUNGEN, new LatLng[]{new LatLng(51.477959, 11.971428)});
            LOCATION_TO_LAT_LNG.put(MENSA_MERSEBURG, new LatLng[]{new LatLng(51.343284, 11.976064)});
            LOCATION_TO_LAT_LNG.put(HEIDEMENSA, new LatLng[]{new LatLng(51.496509, 11.933994)});
        }

        @SuppressWarnings("WrongConstant")
        @Location
        public static int toLocation(@IdRes int id) {
            return ID_TO_LOCATION.get(id, HARZMENSA);
        }

        @IdRes
        public static int toId(@Location int location) {
            return LOCATION_TO_ID.get(location, R.id.menu_item_location_harzmensa);
        }

        @StringRes
        public static int toString(@Location int location) {
            return LOCATION_TO_STRING.get(location, R.string.menu_item_location_harzmensa);
        }

        public static LatLng[] toLatLng(@Location int location) {
            return LOCATION_TO_LAT_LNG.get(location, new LatLng[]{new LatLng(51.4896383, 11.9653673)});
        }

        public static boolean isLocation(int possibleLocation) {
            return LOCATION_TO_ID.indexOfKey(possibleLocation) >= 0;
        }

        public static boolean isId(@IdRes int possibleId) {
            return ID_TO_LOCATION.indexOfKey(possibleId) >= 0;
        }
    }
}