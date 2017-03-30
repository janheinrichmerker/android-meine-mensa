package com.heinrichreimer.meinemensa.network;

import android.content.Context;

import com.bumptech.glide.load.model.stream.BaseGlideUrlLoader;

import java.util.regex.Pattern;

public class InternalUrlLoader extends BaseGlideUrlLoader<String> {

    private static final double HYPOT_GADGET = Math.hypot(290, 232);
    private static final double HYPOT_MEDIUM = Math.hypot(450, 360);

    private static final Pattern IMAGE_INTERNAL_PATH_PATTERN = Pattern.compile("https?+://+meine-mensa\\.de/mediathek/[A-Za-z0-9-._~:/?#\\[\\]@!$&'()*+,;=%]+(jpe?g|png|gif)", Pattern.CASE_INSENSITIVE);

    public InternalUrlLoader(Context context) {
        super(context);
    }

    @Override
    protected String getUrl(String model, int width, int height) {
        double hypot = Math.hypot(width, height);
        if (IMAGE_INTERNAL_PATH_PATTERN.matcher(model).matches()) {
            if (hypot <= HYPOT_GADGET) {
                return model + "_gadget.png";
            }
            if (hypot <= HYPOT_MEDIUM) {
                return model + "_medium.png";
            }
        }
        return model;
    }
}