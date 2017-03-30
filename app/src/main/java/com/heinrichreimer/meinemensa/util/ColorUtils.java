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

package com.heinrichreimer.meinemensa.util;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.graphics.Palette;
import android.view.MenuItem;
import android.widget.ImageView;

public class ColorUtils {
    private ColorUtils() {
    }

    public static void tintImageView(ImageView view, @ColorInt int color) {
        final Drawable drawable = view.getDrawable();
        if (drawable != null) {
            final Drawable wrapped = DrawableCompat.wrap(drawable);
            wrapped.mutate();
            DrawableCompat.setTint(wrapped, color);
            view.setImageDrawable(drawable);
        }
    }

    public static void tintMenuItemIcon(MenuItem item, @ColorInt int color) {
        final Drawable drawable = item.getIcon();
        if (drawable != null) {
            final Drawable wrapped = DrawableCompat.wrap(drawable);
            wrapped.mutate();
            DrawableCompat.setTint(wrapped, color);
            item.setIcon(drawable);
        }
    }

    public static
    @ColorInt
    int getStatusBarColor(@ColorInt int color) {
        return android.support.v4.graphics.ColorUtils.blendARGB(color, Color.BLACK, 0.2f);
    }

    public static
    @ColorInt
    int getPastelColor(@ColorInt int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        float intensity = hsv[2];
        return android.support.v4.graphics.ColorUtils.blendARGB(color, Color.parseColor("#fef5ee"), intensity * 0.3f);
    }

    public static boolean isLight(@ColorInt int color) {
        return android.support.v4.graphics.ColorUtils.calculateLuminance(color) >= 0.5;
    }

    public static Palette.Swatch parsePalette(Palette palette, @ColorInt int fallback) {
        if (palette == null) {
            return new Palette.Swatch(fallback, 0);
        } else {
            Palette.Swatch swatch = palette.getLightMutedSwatch();
            if (swatch == null) {
                swatch = palette.getDarkMutedSwatch();
            }
            if (swatch == null) {
                swatch = palette.getMutedSwatch();
            }
            if (swatch == null) {
                swatch = palette.getLightVibrantSwatch();
            }
            if (swatch == null) {
                swatch = palette.getDarkVibrantSwatch();
            }
            if (swatch == null) {
                swatch = palette.getVibrantSwatch();
            }
            if (swatch != null) {
                return swatch;
            } else {
                return new Palette.Swatch(fallback, 0);
            }
        }
    }
}
