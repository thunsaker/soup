package com.thunsaker.soup.util;

import android.content.Context;

import com.thunsaker.soup.R;

public class CategoryUtil {
    public static int getCategoryColor(Context myContext, String categoryKey) {
        int color;
        if (categoryKey.equals("a") || categoryKey.equals("m") || categoryKey.equals("k")) {
            color = myContext.getResources().getColor(R.color.category_art);
        } else if (categoryKey.equals("b") || categoryKey.equals("n")) {
            color = myContext.getResources().getColor(R.color.category_school);
        } else if (categoryKey.equals("c") || categoryKey.equals("o") || categoryKey.equals("l")) {
            color = myContext.getResources().getColor(R.color.category_event);
        } else if (categoryKey.equals("d") || categoryKey.equals("p")) {
            color = myContext.getResources().getColor(R.color.category_food);
        } else if (categoryKey.equals("d") || categoryKey.equals("q")) {
            color = myContext.getResources().getColor(R.color.category_night);
        } else if (categoryKey.equals("e") || categoryKey.equals("r") || categoryKey.equals("x")) {
            color = myContext.getResources().getColor(R.color.category_out);
        } else if (categoryKey.equals("f") || categoryKey.equals("s")) {
            color = myContext.getResources().getColor(R.color.category_work);
        } else if (categoryKey.equals("g") || categoryKey.equals("t")) {
            color = myContext.getResources().getColor(R.color.category_house);
        } else if (categoryKey.equals("h") || categoryKey.equals("u")) {
            color = myContext.getResources().getColor(R.color.category_shop);
        } else if (categoryKey.equals("i") || categoryKey.equals("v") || categoryKey.equals("z")) {
            color = myContext.getResources().getColor(R.color.category_travel);
        } else { // j & z
            color = myContext.getResources().getColor(R.color.soup_red_light);
        }

        return color;
    }
}
