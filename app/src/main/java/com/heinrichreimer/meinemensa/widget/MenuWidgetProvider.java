package com.heinrichreimer.meinemensa.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MenuWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.w("MenuWidgetProvider", "onUpdate method called");

        Intent intent = new Intent(context, UpdateMenuWidgetService.class);
        intent.setAction(UpdateMenuWidgetService.ACTION_UPDATE);
        context.startService(intent);
    }
}
