package com.heinrichreimer.meinemensa.databinding;

import android.content.res.ColorStateList;
import android.databinding.BindingAdapter;
import android.support.annotation.ColorInt;
import android.support.v4.view.ViewCompat;
import android.support.v7.graphics.Palette;
import android.view.View;
import android.widget.TextView;

public class BindingAdapters {
    private BindingAdapters() {
    }

    @BindingAdapter("backgroundTint")
    public static void setBackgroundTint(View view, @ColorInt int color) {
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(color));
    }

    @BindingAdapter("bodyTextColor")
    public static void setBodyTextColor(TextView textView, @ColorInt int background) {
        textView.setTextColor(new Palette.Swatch(background, 0).getBodyTextColor());
    }

    @BindingAdapter("titleTextColor")
    public static void setTitleTextColor(TextView textView, @ColorInt int background) {
        textView.setTextColor(new Palette.Swatch(background, 0).getTitleTextColor());
    }
}
