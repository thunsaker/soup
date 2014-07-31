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
    public List<Category> subcategories;

//	public String getId() {
//		return Id;
//	}
//	public void setId(String id) {
//		Id = id;
//	}
//
//	public String getName() {
//		return Name;
//	}
//	public void setName(String name) {
//		Name = name;
//	}
//
//	public String getPluralName() {
//		return PluralName;
//	}
//	public void setPluralName(String pluralName) {
//		PluralName = pluralName;
//	}
//
//	public String getShortName() {
//		return ShortName;
//	}
//	public void setShortName(String shortName) {
//		ShortName = shortName;
//	}
//
//	public FoursquareImage getIcon() {
//		return Icon;
//	}
//	public void setIcon(FoursquareImage icon) {
//		Icon = icon;
//	}
//
//	public Boolean getPrimary() {
//		return Primary;
//	}
//	public void setPrimary(Boolean primary) {
//		Primary = primary;
//	}
//
//	public List<Category> getSubcategories() {
//		return Subcategories;
//	}
//	public void setSubcategories(List<Category> subcategories) {
//		Subcategories = subcategories;
//	}

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

					// Check for subcategories
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
									myTempSubcategory.subcategories = mySubSubcategories;
								}
								mySubcategories.add(myTempSubcategory);
							}
						}
						myTempCategory.subcategories = mySubcategories;
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