package com.thunsaker.soup.data.api.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Category {
	public String id;
    public String name;
    public String pluralName;
    public String shortName;
    public FoursquareImage icon;
    public boolean primary;
    public List<Category> categories;

    public Category() {}

    public Category(String id, String name, String pluralName, String shortName, FoursquareImage icon, boolean primary) {
        this(id, name, pluralName, shortName, icon, primary, null);
    }

    public Category(String id, String name, String pluralName, String shortName, FoursquareImage icon, boolean primary, List<Category> categories) {
        this.id = id;
        this.name = name;
        this.pluralName = pluralName;
        this.shortName = shortName;
        this.icon = icon;
        this.primary = primary;
        this.categories = categories;
    }

    @Override
	public String toString() {
		Gson myGson = new Gson();
		return myGson.toJson(this);
	}

	public static List<Category> GetCategoriesFromJson(JsonArray jsonArray, Boolean checkForSubcategories) {
		try {
			List<Category> myCategories = new ArrayList<Category>();
			Category myTempCategory;
			if(jsonArray != null) {
				for (JsonElement element : jsonArray) {
                    myTempCategory = GetCategoryFromJson(element);

					// Check for categories
					if(checkForSubcategories) {
						List<Category> mySubcategories = new ArrayList<Category>();
						JsonObject jObjectSubcategories = element.getAsJsonObject();
						if(jObjectSubcategories.get("categories") != null) {
							JsonArray jArraySubcategories = jObjectSubcategories.getAsJsonArray("categories");
							for (JsonElement jElementSubcategory : jArraySubcategories) {
								Category myTempSubcategory = GetCategoryFromJson(jElementSubcategory);

								List<Category> mySubSubcategories = new ArrayList<Category>();
								JsonObject jObjectSubSubcategories = jElementSubcategory.getAsJsonObject();
								if(jObjectSubSubcategories.get("categories") != null) {
									JsonArray jArraySubSubcategories = jObjectSubSubcategories.getAsJsonArray("categories");
									for (JsonElement jElementSubSubcategory : jArraySubSubcategories) {
										Category myTempSubSubcategory = GetCategoryFromJson(jElementSubSubcategory);
										mySubSubcategories.add(myTempSubSubcategory);
									}
									myTempSubcategory.categories = mySubSubcategories;
								}
								mySubcategories.add(myTempSubcategory);
							}
						}
						myTempCategory.categories = mySubcategories;
					}
					myCategories.add(myTempCategory);
				}
			}
			if(myCategories.size() > 0)
				return myCategories;
			else
				return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Category GetCategoryFromJson(JsonElement jsonElementToConvert) {
		try {
			Category cat = new Category();
			if(jsonElementToConvert != null) {
				JsonObject jcat = jsonElementToConvert.getAsJsonObject();
				cat.id = jcat.get("id") != null ? jcat.get("id").getAsString() : "";
				cat.name = jcat.get("name") != null ? jcat.get("name").getAsString() : "";
				cat.pluralName = jcat.get("pluralName") != null ? jcat.get("pluralName").getAsString() : "";
				cat.shortName = jcat.get("shortName") != null ? jcat.get("shortName").getAsString() : "";
                cat.icon = FoursquareImage.GetFoursquareImageFromJson(jcat.get("icon") != null ? jcat.get("icon").getAsJsonObject() : null);
				cat.primary = jcat.get("primary") != null && jcat.get("primary").getAsBoolean();
				return cat;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Category GetCategoryFromJson(String jsonStringToConvert) {
		Gson gson = new Gson();
		return jsonStringToConvert != null ? gson.fromJson(jsonStringToConvert, Category.class) : null;
	}
}