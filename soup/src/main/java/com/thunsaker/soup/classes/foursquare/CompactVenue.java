package com.thunsaker.soup.classes.foursquare;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

public class CompactVenue {
	private String Id;
	private String Name;
	private Contact Contact;
	private com.thunsaker.soup.classes.foursquare.Location Location;
	private String CanonicalUrl;
	private List<Category> Categories;
	private Boolean Verified;
	private VenueStats Stats;
	private String Url;
	private Boolean BeenHere;
/*	private VenueLikes Likes;
	private Menu Menu;
	private BeenHere BeenHere;
	private Specials Specials;
	private HereNow HereNow;
	private Listed listed; */
	private String ReferralId;

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

	public Contact getContact() {
		return Contact;
	}
	public void setContact(Contact contact) {
		Contact = contact;
	}

	public com.thunsaker.soup.classes.foursquare.Location getLocation() {
		return Location;
	}
	public void setLocation(com.thunsaker.soup.classes.foursquare.Location location) {
		Location = location;
	}

	// Canonical Url (Foursquare Url)
	public String getCanonicalUrl() {
		return CanonicalUrl;
	}
	public void setCanonicalUrl(String canonicalUrl) {
		CanonicalUrl = canonicalUrl;
	}

	public List<Category> getCategories() {
		return Categories;
	}
	public void setCategories(List<Category> categories) {
		Categories = categories;
	}

	public Boolean getVerified() {
		return Verified;
	}
	public void setVerified(Boolean verified) {
		Verified = verified;
	}

	public VenueStats getStats() {
		return Stats;
	}
	public void setStats(VenueStats stats) {
		Stats = stats;
	}

	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}

	public String getReferralId() {
		return ReferralId;
	}
	public void setReferralId(String referralId) {
		this.ReferralId = referralId;
	}

	public Boolean getBeenHere() {
		return BeenHere;
	}
	public void setBeenHere(Boolean beenHere) {
		BeenHere = beenHere;
	}

	@Override
	public String toString() {
		return toJson(this);
	}

	public static String toJson(CompactVenue myCompactVenue) {
		Gson gson = new Gson();
		return myCompactVenue != null ? gson.toJson(myCompactVenue, CompactVenue.class) : "";
	}

	public static CompactVenue GetCompactVenueFromJson(String jsonString) {
		Gson gson = new Gson();
		return jsonString != null ? gson.fromJson(jsonString, CompactVenue.class) : null;
	}

	public static CompactVenue ParseCompactVenueFromJson(JsonObject jsonObject) {
		try {
			CompactVenue myVenue = new CompactVenue();

			myVenue.setId(jsonObject.get("id") != null
					? jsonObject.get("id").getAsString()
					: "");
			myVenue.setName(jsonObject.get("name") != null
					? jsonObject.get("name").getAsString()
					: "");
			myVenue.setContact(jsonObject.get("contact") != null
					? com.thunsaker.soup.classes.foursquare.Contact
							.GetContactFromJson(jsonObject.getAsJsonObject("contact"))
					: null);
			myVenue.setLocation(jsonObject.get("location") != null
					? com.thunsaker.soup.classes.foursquare.Location
							.GetLocationFromJson(jsonObject.getAsJsonObject("location"))
					: null);
			myVenue.setCanonicalUrl(jsonObject.get("canonicalUrl") != null
					? jsonObject.get("canonicalUrl").getAsString()
					: "");
			myVenue.setCategories(jsonObject.get("categories") != null
					? Category
							.GetCategoriesFromJson(jsonObject .getAsJsonArray("categories"), false)
					: null);
			myVenue.setVerified(jsonObject.get("verified") != null
					? jsonObject.get("verified").getAsBoolean()
					: false);
			myVenue.setStats(null);
			myVenue.setUrl(jsonObject.get("url") != null
					? jsonObject.get("url").getAsString()
					: "");
			myVenue.setReferralId(jsonObject.get("referralId") != null
					? jsonObject.get("referralId").getAsString()
					: "");
			myVenue.setBeenHere(jsonObject.get("beenHere") != null
					? jsonObject.get("beenHere").getAsJsonObject().get("marked").getAsBoolean()
					: false);

			return myVenue;
		} catch (Exception e) {
			return null;
		}
	}
}