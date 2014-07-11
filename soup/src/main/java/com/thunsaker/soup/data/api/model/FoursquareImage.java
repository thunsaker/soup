package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonObject;

public class FoursquareImage {
	public static Integer SIZE_EXTRA_GRANDE = 88;
	public static Integer SIZE_GRANDE = 64;
	public static Integer SIZE_MEDIANO = 44;
	public static Integer SIZE_PEQUENO = 32;
	public static String IMAGE_SIZE_PREFIX = "bg_%s";
	public static String IMAGE_SIZE_PLACEHOLDER = "%sx%s";

	public String id;
	public String prefix;
	public String suffix;
	public Integer height;
	public Integer width;
	public String visibility;

	public FoursquareImage() {

	}

	public FoursquareImage(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

//	public String getId() {
//		return Id;
//	}
//	public void setId(String id) {
//		Id = id;
//	}
//
//	public String getPrefix() {
//		return Prefix;
//	}
//	public void setPrefix(String prefix) {
//		Prefix = prefix;
//	}
//
//	public String getSuffix() {
//		return Suffix;
//	}
//	public void setSuffix(String suffix) {
//		Suffix = suffix;
//	}
//
//	public Integer getHeight() {
//		return Height;
//	}
//	public void setHeight(Integer height) {
//		Height = height;
//	}
//
//	public Integer getWidth() {
//		return Width;
//	}
//	public void setWidth(Integer width) {
//		Width = width;
//	}
//
//	public String getVisibility() {
//		return Visibility;
//	}
//
//	public void setVisibility(String visibility) {
//		Visibility = visibility;
//	}

	@Override
	public String toString() {
		return getFoursquareImageUrl(SIZE_PEQUENO);
	};

	public String getFoursquareImageUrl(Integer size) {
		return String.format("%s%s%s", prefix, String.format(IMAGE_SIZE_PLACEHOLDER, size, size), suffix);
	}

	public String getFoursquareImageUrl() {
		return String.format("%s%s%s", prefix, String.format(IMAGE_SIZE_PLACEHOLDER, height, width), suffix);
	}

	public String getFoursquareLegacyImageUrl(Integer size) {
		return String.format("%s%s%s", prefix, String.format(IMAGE_SIZE_PREFIX, size), suffix);
	}

	public static FoursquareImage GetFoursquareImageFromJson(JsonObject jsonObject) {
		try {
			if(jsonObject != null) {
				FoursquareImage myFoursquareImage = new FoursquareImage();
				myFoursquareImage.id = jsonObject.get("id") != null ? jsonObject.get("id").getAsString() : "";
				myFoursquareImage.prefix = jsonObject.get("prefix") != null ? jsonObject.get("prefix").getAsString() : "";
				myFoursquareImage.suffix = jsonObject.get("suffix") != null ? jsonObject.get("suffix").getAsString() : "";
				myFoursquareImage.height = jsonObject.get("height") != null ?  jsonObject.get("height").getAsInt() : 0;
				myFoursquareImage.width = jsonObject.get("width") != null ? jsonObject.get("width").getAsInt() : 0;
				myFoursquareImage.visibility = jsonObject.get("visibility") != null ? jsonObject.get("visibility").getAsString() : "";
				return myFoursquareImage;
			} else {
				return null;
			}
		} catch(Exception e) {
			return null;
		}
	}
}