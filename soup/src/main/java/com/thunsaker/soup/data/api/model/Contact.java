package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonObject;

public class Contact {
	public String phone;
	public String formattedPhone;
	public String twitter;

	public static Contact GetContactFromJson(JsonObject jsonObject) {
		try {
			Contact myContact = new Contact();
			myContact.phone = jsonObject.get("phone") != null ? jsonObject.get("phone").getAsString() : "";
			myContact.formattedPhone = jsonObject.get("formattedPhone") != null ? jsonObject.get("formattedPhone").getAsString() : "";
			myContact.twitter = jsonObject.get("twitter") != null ? jsonObject.get("twitter").getAsString() : "";
			return myContact;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}