package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonObject;

public class CompactFoursquareUser {
	public String id;
    public String firstName;
    public String lastName;
    public String gender;
    public String relationship;
    public FoursquareImage photo;
    public String type;

	public static CompactFoursquareUser GetCompactFoursquareUserFromJson(JsonObject jsonObject) {
		try {
			CompactFoursquareUser myUser = new CompactFoursquareUser();
			myUser.id = jsonObject.get("id") != null ? jsonObject.get("id").getAsString() : "";
			myUser.firstName = jsonObject.get("firstName") != null ? jsonObject.get("firstName").getAsString() : "";
			myUser.lastName = jsonObject.get("lastName") != null ? jsonObject.get("lastName").getAsString() : "";
			myUser.gender = jsonObject.get("gender") != null ? jsonObject.get("gender").getAsString() : "";
			myUser.relationship = jsonObject.get("relationship") != null ? jsonObject.get("relationship").getAsString() : "";
			myUser.photo = jsonObject.get("photo") != null ? FoursquareImage.GetFoursquareImageFromJson(jsonObject.get("photo").getAsJsonObject()) : null;
			myUser.type = jsonObject.get("type") != null ? jsonObject.get("type").getAsString() : "";
			return myUser;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}