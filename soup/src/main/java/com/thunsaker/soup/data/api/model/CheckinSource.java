package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonObject;

public class CheckinSource {
	private String Name;
	private String Url;

	public String getName() {
		return Name;
	}
	public void setName(String name) {
		Name = name;
	}

	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}

	public static CheckinSource GetCheckinSourceFromJson(JsonObject jsonObject) {
		try {
			CheckinSource myCheckinSource = new CheckinSource();
			if(jsonObject != null) {
				myCheckinSource.setName(jsonObject.get("name") != null ? jsonObject.get("name").getAsString() : "");
				myCheckinSource.setUrl(jsonObject.get("url") != null ? jsonObject.get("url").getAsString() : "");
			}

			return myCheckinSource;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}