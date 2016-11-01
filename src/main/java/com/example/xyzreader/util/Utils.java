package com.example.xyzreader.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.graphics.Palette;

/**
 * Utility class
 */

public class Utils {

    public static int MUTED_DARK_COLOR_DEFAULT = 0xFF424242; //Grey 800
    public static float LIGHTEN_ALPHA = 0.25f;
    public static float DARKEN_CHANGE = 0.25f;

    /**
     * Given an rgb color integer, darken the color by a factor between 0 and 1
     * @param rgb red-green-blue color integer
     * @param change float value between 0 an 1 representing the relative percentage to darken the
     *               given color
     * @return darkend color integer
     */
    public static int darkenColor(int rgb, float change) {
        float[] hsv = new float[3];
        Color.colorToHSV(rgb, hsv);
        hsv[2] -= hsv[2] * change;
        return Color.HSVToColor(hsv);
    }

    /**
     * Gets the dominant color generated from a bitmap
     * @param bitmap bitmap image
     * @return dominant color generated from the given bitmap image. Returns the
     * MUTED_DARK_COLOR_DEFAULT constant (Grey 800) if no dominant color can be retrieved.
     */
    public static int getDominantColorFromBitmap(Bitmap bitmap) {
        Palette p;
        try {
            p = Palette.from(bitmap).generate();
        } catch(IllegalArgumentException e) {
            return MUTED_DARK_COLOR_DEFAULT;
        }
        return p.getDominantColor(MUTED_DARK_COLOR_DEFAULT);
    }

    /**
     * Returns color with a modified alpha-channel
     * @param alpha alpha value between 0 and 1, with 0 being transparent and 1 being opaque
     * @param color the color integer
     * @return color with a modified alpha-channel
     */
    public static int alphaColor(float alpha, int color) {
        int maxAlpha = 255;
        alpha = alpha > 1.0f ? 1.0f : alpha;
        alpha = alpha < 0 ? 0 : alpha;
        int relativeAlpha = Math.round(alpha * maxAlpha);
        return Color.argb(relativeAlpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}
