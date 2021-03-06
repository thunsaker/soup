package com.thunsaker.soup.data.api.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

public class Location {
    public String address;
    public String crossStreet;
    public double latitude;
    public double longitude;
    public Integer distance;
    public String postalCode;
    public String city;
    public String state;
    public String country;
    public String countryCode;
    public String[] formattedAddress;

    public Location() {}

    public Location(String address, String crossStreet, double latitude, double longitude, Integer distance, String postalCode, String city, String state, String country, String countryCode, String[] formattedAddress) {
        this.address = address;
        this.crossStreet = crossStreet;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.postalCode = postalCode;
        this.city = city;
        this.state = state;
        this.country = country;
        this.countryCode = countryCode;
        this.formattedAddress = formattedAddress;
    }

	public static Location GetLocationFromJson(JsonObject jsonObject) {
		Location myLocation = new Location();
		myLocation.address = jsonObject.get("address") != null ? jsonObject.get("address").getAsString() : "";
		myLocation.crossStreet = jsonObject.get("crossStreet") != null ? jsonObject.get("crossStreet").getAsString() : "";
		myLocation.latitude = jsonObject.get("lat") != null ? jsonObject.get("lat").getAsDouble() : 0;
		myLocation.longitude = jsonObject.get("lng") != null ? jsonObject.get("lng").getAsDouble() : 0;
		myLocation.distance = jsonObject.get("distance") != null ? jsonObject.get("distance").getAsInt() : 0;
		myLocation.postalCode = jsonObject.get("postalCode") != null ? jsonObject.get("postalCode").getAsString() : "";
		myLocation.city = jsonObject.get("city") != null ? jsonObject.get("city").getAsString() : "";
		myLocation.state = jsonObject.get("state") != null ? jsonObject.get("state").getAsString() : "";
		myLocation.country = jsonObject.get("country") != null ? jsonObject.get("country").getAsString() : "";
		myLocation.countryCode = jsonObject.get("cc") != null ? jsonObject.get("cc").getAsString() : "";
		return myLocation;
	}

    public static Location GetLocationFromFoursquareLocationResponse(FoursquareLocationResponse response) {
        Location myLocation = new Location();
        myLocation.address = response.address != null ? response.address : "";
        myLocation.crossStreet = response.crossStreet != null ? response.crossStreet : "";
        myLocation.latitude = response.lat;
        myLocation.longitude = response.lng;
        myLocation.distance = response.distance;
        myLocation.postalCode = response.postalCode != null ? response.postalCode : "";
        myLocation.city = response.city != null ? response.city : "";
        myLocation.state = response.state != null ? response.state : "";
        myLocation.country = response.country != null ? response.country : "";
        myLocation.countryCode = response.cc != null ? response.cc : "";
        return myLocation;
    }

	public LatLng getLatLng() {
		return new LatLng(latitude, longitude);
	}

	public String getLatLngString() {
		return String.format("%s,%s", latitude, longitude);
	}

	public String getCityStatePostalCode() {
		String city = !this.city.equals("") ? this.city : "-";
		String state = !this.state.equals("") ? this.state : "-";
		String postalCode = !this.postalCode.equals("") ? this.postalCode : "-";

		return String.format("%s, %s %s", city, state, postalCode);
	}
}