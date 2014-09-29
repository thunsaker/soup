package com.thunsaker.soup.data;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class CategoriesDb extends SQLiteAssetHelper {
    private static final String DATABASE_NAME = "categories.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FOURSQUARE_ID = "foursquareId";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PARENT = "parentId";
    // Consider adding the category color here.

    private static final String DATABASE_CREATE =
            String.format("create table %s (%s integer primary key autoincrement, %s text not null, %s text not null, %s);",
                    TABLE_CATEGORIES, COLUMN_ID, COLUMN_FOURSQUARE_ID, COLUMN_NAME, COLUMN_PARENT);

    public CategoriesDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}
