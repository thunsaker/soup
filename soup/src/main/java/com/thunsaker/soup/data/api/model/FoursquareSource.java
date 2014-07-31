package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonObject;

public class FoursquareSource {
	public String name;
	public String url;

	public static FoursquareSource GetCheckinSourceFromJson(JsonObject jsonObject) {
		try {
			FoursquareSource myFoursquareSource = new FoursquareSource();
			if(jsonObject != null) {
				myFoursquareSource.name = jsonObject.get("name") != null ? jsonObject.get("name").getAsString() : "";
				myFoursquareSource.url = jsonObject.get("url") != null ? jsonObject.get("url").getAsString() : "";
			}

			return myFoursquareSource;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}