package com.heinrichreimer.meinemensa.annotations;

import android.support.annotation.IdRes;
import android.support.annotation.IntDef;
import android.util.SparseIntArray;

import com.heinrichreimer.meinemensa.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.heinrichreimer.meinemensa.annotations.PriceCategory.EMPLOYEES;
import static com.heinrichreimer.meinemensa.annotations.PriceCategory.GUESTS;
import static com.heinrichreimer.meinemensa.annotations.PriceCategory.STUDENTS;

@IntDef({STUDENTS, EMPLOYEES, GUESTS})
@Retention(RetentionPolicy.SOURCE)
public @interface PriceCategory {
    int STUDENTS = 1;
    int EMPLOYEES = 2;
    int GUESTS = 3;
    int[] ALL = {STUDENTS, EMPLOYEES, GUESTS};

    class Converter {
        private static final SparseIntArray ID_TO_PRICE_CATEGORY = new SparseIntArray(3);

        static {
            ID_TO_PRICE_CATEGORY.put(R.id.menu_item_price_students, STUDENTS);
            ID_TO_PRICE_CATEGORY.put(R.id.menu_item_price_employees, EMPLOYEES);
            ID_TO_PRICE_CATEGORY.put(R.id.menu_item_price_guests, GUESTS);
        }

        private static final SparseIntArray PRICE_CATEGORY_TO_ID = new SparseIntArray(3);

        static {
            PRICE_CATEGORY_TO_ID.put(STUDENTS, R.id.menu_item_price_students);
            PRICE_CATEGORY_TO_ID.put(EMPLOYEES, R.id.menu_item_price_employees);
            PRICE_CATEGORY_TO_ID.put(GUESTS, R.id.menu_item_price_guests);
        }

        @SuppressWarnings("WrongConstant")
        @PriceCategory
        public static int toPriceCategory(@IdRes int id) {
            return ID_TO_PRICE_CATEGORY.get(id, STUDENTS);
        }

        @IdRes
        public static int toId(@PriceCategory int category) {
            return PRICE_CATEGORY_TO_ID.get(category, R.id.menu_item_price_students);
        }

        public static boolean isPriceCategory(int possibleCategory) {
            return PRICE_CATEGORY_TO_ID.indexOfKey(possibleCategory) >= 0;
        }

        public static boolean isId(@IdRes int possibleId) {
            return ID_TO_PRICE_CATEGORY.indexOfKey(possibleId) >= 0;
        }
    }
}