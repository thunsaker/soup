package com.thunsaker.soup.classes.foursquare;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Category {
	private String Id;
	private String Name;
	private String PluralName;
	private String ShortName;
	private FoursquareImage Icon;
	private Boolean Primary;
	private List<Category> Subcategories;
	
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}
	
	public String getPluralName() {
		return PluralName;
	}
	public void setPluralName(String pluralName) {
		PluralName = pluralName;
	}
	
	public String getShortName() {
		return ShortName;
	}
	public void setShortName(String shortName) {
		ShortName = shortName;
	}
	
	public FoursquareImage getIcon() {
		return Icon;
	}
	public void setIcon(FoursquareImage icon) {
		Icon = icon;
	}
	
	public Boolean getPrimary() {
		return Primary;
	}
	public void setPrimary(Boolean primary) {
		Primary = primary;
	}
	
	public List<Category> getSubcategories() {
		return Subcategories;
	}
	public void setSubcategories(List<Category> subcategories) {
		Subcategories = subcategories;
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
					myTempCategory = new Category();
					myTempCategory = GetCategoryFromJson(element);
					
					// Check for subcategories
					if(checkForSubcategories) {
						List<Category> mySubcategories = new ArrayList<Category>();
						JsonObject jObjectSubcategories = element.getAsJsonObject();
						if(jObjectSubcategories.get("categories") != null) {
							JsonArray jArraySubcategories = jObjectSubcategories.getAsJsonArray("categories");
							for (JsonElement jElementSubcategory : jArraySubcategories) {
								Category myTempSubcategory = new Category();
								myTempSubcategory = GetCategoryFromJson(jElementSubcategory);
								
								List<Category> mySubSubcategories = new ArrayList<Category>();
								JsonObject jObjectSubSubcategories = jElementSubcategory.getAsJsonObject();
								if(jObjectSubSubcategories.get("categories") != null) {
									JsonArray jArraySubSubcategories = jObjectSubSubcategories.getAsJsonArray("categories");
									for (JsonElement jElementSubSubcategory : jArraySubSubcategories) {
										Category myTempSubSubcategory = new Category();
										myTempSubSubcategory = GetCategoryFromJson(jElementSubSubcategory);
										mySubSubcategories.add(myTempSubSubcategory);
									}
									myTempSubcategory.setSubcategories(mySubSubcategories);
								}
								mySubcategories.add(myTempSubcategory);
							}
						}
						myTempCategory.setSubcategories(mySubcategories);
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
				cat.setId(jcat.get("id") != null ? jcat.get("id").getAsString() : "");
				cat.setName(jcat.get("name") != null ? jcat.get("name").getAsString() : "");
				cat.setPluralName(jcat.get("pluralName") != null ? jcat.get("pluralName").getAsString() : "");
				cat.setShortName(jcat.get("shortName") != null ? jcat.get("shortName").getAsString() : "");
				FoursquareImage catImage = new FoursquareImage();
				catImage = FoursquareImage.GetFoursquareImageFromJson(jcat.get("icon") != null ? jcat.get("icon").getAsJsonObject() : null);
				cat.setIcon(catImage);
				cat.setPrimary(jcat.get("primary") != null ? jcat.get("primary").getAsBoolean() : false);
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