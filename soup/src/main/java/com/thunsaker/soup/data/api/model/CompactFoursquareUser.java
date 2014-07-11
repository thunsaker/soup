package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonObject;

public class CompactFoursquareUser {
	private String Id;
	private String FirstName;
	private String LastName;
	private String Gender;
	private String Relationship;
	private FoursquareImage Photo;
	private String Type;

	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}

	public String getFirstName() {
		return FirstName;
	}
	public void setFirstName(String firstName) {
		FirstName = firstName;
	}

	public String getLastName() {
		return LastName;
	}
	public void setLastName(String lastName) {
		LastName = lastName;
	}

	public String getGender() {
		return Gender;
	}
	public void setGender(String gender) {
		Gender = gender;
	}

	public String getRelationship() {
		return Relationship;
	}
	public void setRelationship(String relationship) {
		Relationship = relationship;
	}

	public FoursquareImage getPhoto() {
		return Photo;
	}
	public void setPhoto(FoursquareImage photo) {
		Photo = photo;
	}

	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}

	public static CompactFoursquareUser GetCompactFoursquareUserFromJson(JsonObject jsonObject) {
		try {
			CompactFoursquareUser myUser = new CompactFoursquareUser();
			myUser.setId(jsonObject.get("id") != null ? jsonObject.get("id").getAsString() : "");
			myUser.setFirstName(jsonObject.get("firstName") != null ? jsonObject.get("firstName").getAsString() : "");
			myUser.setLastName(jsonObject.get("lastName") != null ? jsonObject.get("lastName").getAsString() : "");
			myUser.setGender(jsonObject.get("gender") != null ? jsonObject.get("gender").getAsString() : "");
			myUser.setRelationship(jsonObject.get("relationship") != null ? jsonObject.get("relationship").getAsString() : "");
			myUser.setPhoto(jsonObject.get("photo") != null ? FoursquareImage.GetFoursquareImageFromJson(jsonObject.get("photo").getAsJsonObject()) : null);
			myUser.setType(jsonObject.get("type") != null ? jsonObject.get("type").getAsString() : "");
			return myUser;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}