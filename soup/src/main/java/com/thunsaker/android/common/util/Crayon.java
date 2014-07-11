package com.thunsaker.android.common.util;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Crayon {
    /*
        http://stackoverflow.com/questions/12408431/how-can-i-get-the-average-colour-of-an-image
     */
    public static int getAverageColor(Bitmap bitmap) {
        return getAverageColor(bitmap, 255);
    }

    public static int getAverageColor(Bitmap bitmap, int alpha) {
        long red = 0;
        long green = 0;
        long blue = 0;
        long pixelCount = 0;

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int c = bitmap.getPixel(x, y);
                pixelCount++;
                red += Color.red(c);
                green += Color.green(c);
                blue += Color.blue(c);
            }
        }

        int redAverage = (int)(red / pixelCount);
        int greenAverage = (int)(green / pixelCount);
        int blueAverage = (int)(blue / pixelCount);

        return Color.argb(alpha, redAverage, greenAverage, blueAverage);
    }
}
