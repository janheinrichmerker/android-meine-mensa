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

import com.heinrichreimer.meinemensa.annotations.PriceCategory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Same as {@link Price} but detached from any {@link io.realm.Realm} instance
 * so that it can be used in other {@link Thread}
 */

//TODO Use Realm#copyFromRealm() instead.
public class UnlinkedPrice {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final DecimalFormat GERMAN_NUMBER_FORMAT = (DecimalFormat) NumberFormat.getInstance(Locale.GERMAN);

    static {
        GERMAN_NUMBER_FORMAT.setMinimumFractionDigits(2);
        GERMAN_NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    private int students; //in Euro-cents
    private int employees; //in Euro-cents
    private int guests; //in Euro-cents

    public UnlinkedPrice(Price price) {
        try {
            BigDecimal decimal = new BigDecimal(GERMAN_NUMBER_FORMAT.parse(price.forStudents()).toString());
            decimal = decimal.multiply(HUNDRED);
            this.students = decimal.intValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            BigDecimal decimal = new BigDecimal(GERMAN_NUMBER_FORMAT.parse(price.forEmployees()).toString());
            decimal = decimal.multiply(HUNDRED);
            this.employees = decimal.intValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            BigDecimal decimal = new BigDecimal(GERMAN_NUMBER_FORMAT.parse(price.forGuests()).toString());
            decimal = decimal.multiply(HUNDRED);
            this.guests = decimal.intValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public String forCategory(@PriceCategory int category) {
        switch (category) {
            case PriceCategory.STUDENTS:
                return GERMAN_NUMBER_FORMAT.format(BigDecimal.valueOf(students)
                        .divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
            case PriceCategory.EMPLOYEES:
                return GERMAN_NUMBER_FORMAT.format(BigDecimal.valueOf(employees)
                        .divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
            case PriceCategory.GUESTS:
            default:
                return GERMAN_NUMBER_FORMAT.format(BigDecimal.valueOf(guests)
                        .divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
        }
    }

    public String forStudents() {
        return GERMAN_NUMBER_FORMAT.format(BigDecimal.valueOf(students)
                .divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
    }

    public String forEmployees() {
        return GERMAN_NUMBER_FORMAT.format(BigDecimal.valueOf(employees)
                .divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
    }

    public String forGuests() {
        return GERMAN_NUMBER_FORMAT.format(BigDecimal.valueOf(guests)
                .divide(HUNDRED, 2, BigDecimal.ROUND_HALF_UP));
    }
}