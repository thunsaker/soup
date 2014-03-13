package com.thunsaker.soup.classes.foursquare;

import com.google.gson.JsonObject;

public class FoursquareListItem {
	private String Id;
	private String CreatedAt;
	private FoursquareImage Photo;
	private CompactVenue Venue;

	public String getId() {
		return Id;
	}

	public void setId(String id) {
		Id = id;
	}

	public String getCreatedAt() {
		return CreatedAt;
	}

	public void setCreatedAt(String createdAt) {
		CreatedAt = createdAt;
	}

	public FoursquareImage getPhoto() {
		return Photo;
	}

	public void setPhoto(FoursquareImage photo) {
		Photo = photo;
	}

	public CompactVenue getVenue() {
		return Venue;
	}

	public void setVenue(CompactVenue venue) {
		Venue = venue;
	}

	public static FoursquareListItem ParseFoursquareListItemFromJson(
			JsonObject jsonObject) {
		try {
			FoursquareListItem myFoursquareListItem = new FoursquareListItem();

			myFoursquareListItem
					.setId(jsonObject.get("id") != null ? jsonObject.get("id")
							.getAsString() : "");

			myFoursquareListItem
					.setCreatedAt(jsonObject.get("createdAt") != null ? jsonObject
							.get("createdAt").getAsString() : "");

			myFoursquareListItem
					.setPhoto(jsonObject.get("photo") != null ? FoursquareImage
							.GetFoursquareImageFromJson(jsonObject.get("photo")
									.getAsJsonObject()) : null);

			myFoursquareListItem
					.setVenue(jsonObject.get("venue") != null ? CompactVenue
							.ParseCompactVenueFromJson(jsonObject.get("venue")
									.getAsJsonObject()) : null);

			return myFoursquareListItem;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
