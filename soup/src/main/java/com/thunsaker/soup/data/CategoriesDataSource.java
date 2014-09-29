package com.thunsaker.soup.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;

public class CategoriesDataSource {
    private SQLiteDatabase db;
    private CategoriesDb dbHelper;
    private String[] catColumns = { CategoriesDb.COLUMN_ID, CategoriesDb.COLUMN_FOURSQUARE_ID, CategoriesDb.COLUMN_NAME, CategoriesDb.COLUMN_PARENT };

    public CategoriesDataSource (Context context) {
        dbHelper = new CategoriesDb(context);
    }

    public void open() throws SQLException {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public CategoryInfo createCategoryInfo(String categoryId, String name) {
        return this.createCategoryInfo(categoryId, name, "");
    }

    public CategoryInfo createCategoryInfo(String categoryId, String name, String parentId) {
        ContentValues vals = new ContentValues();
        vals.put(CategoriesDb.COLUMN_FOURSQUARE_ID, categoryId);
        vals.put(CategoriesDb.COLUMN_NAME, name);
        vals.put(CategoriesDb.COLUMN_PARENT, parentId);
        long insertId = db.insert(CategoriesDb.TABLE_CATEGORIES, null, vals);
        Cursor cursor = db.query(CategoriesDb.TABLE_CATEGORIES, catColumns, CategoriesDb.COLUMN_ID + " = " + insertId, null, null, null, null);
        cursor.moveToFirst();
        CategoryInfo newCatInfo = cursorToCategory(cursor);
        cursor.close();
        return newCatInfo;
    }

    public void deleteCategoryInfo(String foursquareCategoryId) throws SQLException {
        db.delete(CategoriesDb.TABLE_CATEGORIES, CategoriesDb.COLUMN_FOURSQUARE_ID + " = " + foursquareCategoryId, null);
    }

    private CategoryInfo cursorToCategory(Cursor cursor) {
        CategoryInfo catInfo = new CategoryInfo();
        catInfo.id = cursor.getLong(0);
        catInfo.categoryId = cursor.getString(1);
        catInfo.name = cursor.getString(2);
        // TODO: This may be empty, if no parent is given
        catInfo.parentId = cursor.getString(3);
        return catInfo;
    }
}