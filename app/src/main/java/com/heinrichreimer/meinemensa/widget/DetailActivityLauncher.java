package com.heinrichreimer.meinemensa.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.heinrichreimer.meinemensa.app.DetailActivity;
import com.heinrichreimer.meinemensa.app.DetailActivityNoImage;

public class DetailActivityLauncher extends BroadcastReceiver {

    public static final String EXTRA_ID = "com.heinrichreimer.meinemensa.ID";
    public static final String EXTRA_HAS_IMAGE = "com.heinrichreimer.meinemensa.HAS_IMAGE";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("DetailActivityLauncher", "Launching detail activity...");
        if (intent == null) {
            return;
        }
        Bundle extras = intent.getExtras();
        if (!extras.containsKey(EXTRA_ID) || !extras.containsKey(EXTRA_HAS_IMAGE)) {
            return;
        }

        Intent launchIntent;
        if (extras.getBoolean(EXTRA_HAS_IMAGE)) {
            launchIntent = new Intent(context, DetailActivityNoImage.class);
            launchIntent.putExtra(DetailActivityNoImage.EXTRA_ID, extras.getLong(EXTRA_ID));
        } else {
            launchIntent = new Intent(context, DetailActivity.class);
            launchIntent.putExtra(DetailActivity.EXTRA_ID, extras.getLong(EXTRA_ID));
        }
        context.startActivity(launchIntent);
    }
}
