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

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import com.heinrichreimer.meinemensa.R;
import com.heinrichreimer.meinemensa.app.DetailActivity;
import com.heinrichreimer.meinemensa.app.DetailActivityNoImage;
import com.heinrichreimer.meinemensa.app.MainActivity;
import com.heinrichreimer.meinemensa.app.MapActivity;

import java.util.LinkedList;
import java.util.Queue;

import uk.co.samuelwall.materialtaptargetprompt.MaterialTapTargetPrompt;

public class IntroUtils {

    private static final String KEY_SHOWN_MAIN_INTRO = "com.heinrichreimer.meinemensa.SHOWN_MAIN_INTRO";
    private static final String KEY_SHOWN_DETAIL_INTRO = "com.heinrichreimer.meinemensa.SHOWN_DETAIL_INTRO";
    private static final String KEY_SHOWN_MAP_INTRO = "com.heinrichreimer.meinemensa.SHOWN_MAP_INTRO";

    private IntroUtils() {
    }

    public static void showIntro(@NonNull final Activity activity) {
        final ViewTreeObserver viewTreeObserver = activity.getWindow().getDecorView().getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (activity instanceof MainActivity) {
                    showMainIntro(activity);
                } else if (activity instanceof DetailActivity || activity instanceof DetailActivityNoImage) {
                    showDetailIntro(activity);
                } else if (activity instanceof MapActivity) {
                    showMapIntro(activity);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this);
                } else {
                    //noinspection deprecation
                    viewTreeObserver.removeGlobalOnLayoutListener(this);
                }
            }
        });
    }

    private static void showMainIntro(@NonNull Activity activity) {
        new IntroBuilder(activity, KEY_SHOWN_MAIN_INTRO)
                .addTarget(new MaterialTapTargetPrompt.Builder(activity)
                        .setTarget(R.id.menu_item_location)
                        .setPrimaryText(R.string.title_main_intro_location)
                        .setSecondaryText(R.string.description_main_intro_location))
                .addTarget(new MaterialTapTargetPrompt.Builder(activity)
                        .setTarget(R.id.menu_item_date)
                        .setPrimaryText(R.string.title_main_intro_date)
                        .setSecondaryText(R.string.description_main_intro_date))
                .addTarget(new MaterialTapTargetPrompt.Builder(activity)
                        .setTarget(R.id.menu_item_overflow)
                        .setIcon(R.drawable.ic_more)
                        .setPrimaryText(R.string.title_main_intro_more)
                        .setSecondaryText(R.string.description_main_intro_more))
                .show();
    }

    private static void showDetailIntro(@NonNull Activity activity) {
        new IntroBuilder(activity, KEY_SHOWN_DETAIL_INTRO)
                .addTarget(new MaterialTapTargetPrompt.Builder(activity)
                        .setTarget(R.id.menu_item_directions)
                        .setPrimaryText(R.string.title_detail_intro_directions)
                        .setSecondaryText(R.string.description_detail_intro_directions))
                .show();
    }

    private static void showMapIntro(@NonNull Activity activity) {
        new IntroBuilder(activity, KEY_SHOWN_MAP_INTRO)
                .addTarget(new MaterialTapTargetPrompt.Builder(activity)
                        .setTarget(R.id.menu_item_nearest_location)
                        .setPrimaryText(R.string.title_map_intro_more_nearest_location)
                        .setSecondaryText(R.string.description_map_intro_more_nearest_location))
                .show();
    }

    private static class IntroBuilder {
        @NonNull
        private final Activity activity;
        private final String preferencesKey;
        @NonNull
        final Queue<MaterialTapTargetPrompt.Builder> targets = new LinkedList<>();

        private IntroBuilder(@NonNull Activity activity, String preferencesKey) {
            this.activity = activity;
            this.preferencesKey = preferencesKey;
        }

        private IntroBuilder addTarget(MaterialTapTargetPrompt.Builder target) {
            if (target.isTargetSet()) {
                targets.add(target);
            }
            return this;
        }

        private void show() {
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
            if (preferences.getBoolean(preferencesKey, false)) {
                return;
            }

            final int oldSystemUiVisibility = activity.getWindow().getDecorView().getSystemUiVisibility();
            final int oldStatusBarColor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                oldStatusBarColor = activity.getWindow().getStatusBarColor();
            } else {
                oldStatusBarColor = 0;
            }

            MaterialTapTargetPrompt.OnHidePromptListener listener =
                    new MaterialTapTargetPrompt.OnHidePromptListener() {
                        @Override
                        public void onHidePrompt(MotionEvent event, boolean tappedTarget) {
                            if (targets.isEmpty()) {
                                preferences.edit().putBoolean(preferencesKey, true).apply();

                                activity.getWindow().getDecorView().setSystemUiVisibility(oldSystemUiVisibility);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    activity.getWindow().setStatusBarColor(oldStatusBarColor);
                                }
                            } else {
                                targets.remove().show();
                            }
                        }

                        @Override
                        public void onHidePromptComplete() {
                        }
                    };

            for (MaterialTapTargetPrompt.Builder target : targets) {
                target.setOnHidePromptListener(listener)
                        .setPrimaryTextColourFromRes(R.color.text_color_primary_inverse)
                        .setSecondaryTextColourFromRes(R.color.text_color_secondary_inverse)
                        .setBackgroundColourFromRes(R.color.background_tap_target)
                        .setFocalColourFromRes(R.color.color_primary)
                        .setAutoDismiss(true)
                        .setAutoFinish(true)
                        .setCaptureTouchEventOutsidePrompt(true);
            }

            if (targets.isEmpty()) {
                preferences.edit().putBoolean(preferencesKey, true).apply();
            } else {
                targets.remove().show();

                int newSystemUiVisibility = oldSystemUiVisibility;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    newSystemUiVisibility &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
                }

                activity.getWindow().getDecorView().setSystemUiVisibility(newSystemUiVisibility);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.background_tap_target_dark));
                }
            }
        }
    }
}
