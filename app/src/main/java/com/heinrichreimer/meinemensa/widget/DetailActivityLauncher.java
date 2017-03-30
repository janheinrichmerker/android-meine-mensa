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
