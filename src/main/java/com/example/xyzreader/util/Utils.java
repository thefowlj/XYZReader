package com.example.xyzreader.util;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
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

    /**
     * From Android source code
     * Copyright (C) 2006 The Android Open Source Project
     *
     * This method is only supported in API level 24+, but it's purely a mathematical algorithm for
     * calculating the luminance of a color, so it has no dependencies necessary from lower APIs.
     *
     * Returns the relative luminance of a color.
     * <p>
     * Assumes sRGB encoding. Based on the formula for relative luminance
     * defined in WCAG 2.0, W3C Recommendation 11 December 2008.
     *
     * @return a value between 0 (darkest black) and 1 (lightest white)
     */
    public static float luminance(@ColorInt int color) {
        double red = Color.red(color) / 255.0;
        red = red < 0.03928 ? red / 12.92 : Math.pow((red + 0.055) / 1.055, 2.4);
        double green = Color.green(color) / 255.0;
        green = green < 0.03928 ? green / 12.92 : Math.pow((green + 0.055) / 1.055, 2.4);
        double blue = Color.blue(color) / 255.0;
        blue = blue < 0.03928 ? blue / 12.92 : Math.pow((blue + 0.055) / 1.055, 2.4);
        return (float) ((0.2126 * red) + (0.7152 * green) + (0.0722 * blue));
    }
}
