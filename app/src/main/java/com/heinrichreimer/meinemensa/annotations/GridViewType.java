package com.heinrichreimer.meinemensa.annotations;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.heinrichreimer.meinemensa.annotations.GridViewType.IMAGE;
import static com.heinrichreimer.meinemensa.annotations.GridViewType.NO_IMAGE;

@IntDef({IMAGE, NO_IMAGE})
@Retention(RetentionPolicy.SOURCE)
public @interface GridViewType {
    int IMAGE = 1;
    int NO_IMAGE = 2;
}