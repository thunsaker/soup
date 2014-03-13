package com.thunsaker.soup.classes.foursquare;

import com.google.gson.JsonObject;

public class FoursquareImage {
	public static Integer SIZE_EXTRA_GRANDE = 88;
	public static Integer SIZE_GRANDE = 64;
	public static Integer SIZE_MEDIANO = 44;
	public static Integer SIZE_PEQUENO = 32;
	public static String IMAGE_SIZE_PREFIX = "bg_%s";
	public static String IMAGE_SIZE_PLACEHOLDER = "%sx%s";
	
	private String Id;
	private String Prefix;
	private String Suffix;
	private Integer Height;
	private Integer Width;
	private String Visibility;
	
	public FoursquareImage() {
		
	}
	
	public FoursquareImage(String pre, String suff) {
		setPrefix(pre);
		setSuffix(suff);
	}
	
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	
	public String getPrefix() {
		return Prefix;
	}
	public void setPrefix(String prefix) {
		Prefix = prefix;
	}
	
	public String getSuffix() {
		return Suffix;
	}
	public void setSuffix(String suffix) {
		Suffix = suffix;
	}
	
	public Integer getHeight() {
		return Height;
	}
	public void setHeight(Integer height) {
		Height = height;
	}

	public Integer getWidth() {
		return Width;
	}
	public void setWidth(Integer width) {
		Width = width;
	}

	public String getVisibility() {
		return Visibility;
	}

	public void setVisibility(String visibility) {
		Visibility = visibility;
	}

	@Override
	public String toString() {
		return getFoursquareImageUrl(SIZE_PEQUENO);
	};	
	
	public String getFoursquareImageUrl(Integer size) {
		return String.format("%s%s%s", getPrefix(), String.format(IMAGE_SIZE_PLACEHOLDER, size, size), getSuffix());
	}
	
	public String getFoursquareImageUrl() {
		return String.format("%s%s%s", getPrefix(), String.format(IMAGE_SIZE_PLACEHOLDER, getHeight(), getWidth()), getSuffix());
	}
	
	public String getFoursquareLegacyImageUrl(Integer size) {
		return String.format("%s%s%s", getPrefix(), String.format(IMAGE_SIZE_PREFIX, size), getSuffix());
	}
	
	public static FoursquareImage GetFoursquareImageFromJson(JsonObject jsonObject) {
		try {
			if(jsonObject != null) {
				FoursquareImage myFoursquareImage = new FoursquareImage();
				myFoursquareImage.setId(jsonObject.get("id") != null ? jsonObject.get("id").getAsString() : "");
				myFoursquareImage.setPrefix(jsonObject.get("prefix") != null ? jsonObject.get("prefix").getAsString() : "");
				myFoursquareImage.setSuffix(jsonObject.get("suffix") != null ? jsonObject.get("suffix").getAsString() : "");
				myFoursquareImage.setHeight(jsonObject.get("height") != null ? jsonObject.get("height").getAsInt() : 0);
				myFoursquareImage.setWidth(jsonObject.get("width") != null ? jsonObject.get("width").getAsInt() : 0);
				myFoursquareImage.setVisibility(jsonObject.get("visibility") != null ? jsonObject.get("visibility").getAsString() : "");
				return myFoursquareImage;
			} else {
				return null;
			}
		} catch(Exception e) {
			return null;
		}
	}
}