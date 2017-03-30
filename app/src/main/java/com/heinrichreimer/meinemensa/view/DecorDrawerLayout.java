package com.heinrichreimer.meinemensa.view;

import android.content.Context;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;

public class DecorDrawerLayout extends DrawerLayout {
    public DecorDrawerLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public DecorDrawerLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DecorDrawerLayout(Context context) {
        super(context);
    }
}
