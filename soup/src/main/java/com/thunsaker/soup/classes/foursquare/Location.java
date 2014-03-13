package com.thunsaker.soup.classes.foursquare;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonObject;

public class Location {
	private String Address;
	private String CrossStreet;
	private Double Latitude;
	private Double Longitude;
	private Integer Distance;
	private String PostalCode;
	private String City;
	private String State;
	private String Country;
	private String CountryCode;
	
	public String getAddress() {
		return Address;
	}
	public void setAddress(String address) {
		Address = address;
	}
	
	public String getCrossStreet() {
		return CrossStreet;
	}
	public void setCrossStreet(String crossStreet) {
		CrossStreet = crossStreet;
	}
	
	public Double getLatitude() {
		return Latitude;
	}
	public void setLatitude(Double latitude) {
		Latitude = latitude;
	}
	
	public Double getLongitude() {
		return Longitude;
	}
	public void setLongitude(Double longitude) {
		Longitude = longitude;
	}
	
	public Integer getDistance() {
		return Distance;
	}
	public void setDistance(Integer distance) {
		Distance = distance;
	}
	
	public String getPostalCode() {
		return PostalCode;
	}
	public void setPostalCode(String postalCode) {
		PostalCode = postalCode;
	}
	
	public String getCity() {
		return City;
	}
	public void setCity(String city) {
		City = city;
	}
	
	public String getState() {
		return State;
	}
	public void setState(String state) {
		State = state;
	}
	
	public String getCountry() {
		return Country;
	}
	public void setCountry(String country) {
		Country = country;
	}
	
	public String getCountryCode() {
		return CountryCode;
	}
	public void setCountryCode(String countryCode) {
		CountryCode = countryCode;
	}
	
	public static Location GetLocationFromJson(JsonObject jsonObject) {
		Location myLocation = new Location();
		myLocation.setAddress(jsonObject.get("address") != null ? jsonObject.get("address").getAsString() : "");
		myLocation.setCrossStreet(jsonObject.get("crossStreet") != null ? jsonObject.get("crossStreet").getAsString() : "");
		myLocation.setLatitude(jsonObject.get("lat") != null ? jsonObject.get("lat").getAsDouble() : 0);
		myLocation.setLongitude(jsonObject.get("lng") != null ? jsonObject.get("lng").getAsDouble() : 0);
		myLocation.setDistance(jsonObject.get("distance") != null ? jsonObject.get("distance").getAsInt() : 0);
		myLocation.setPostalCode(jsonObject.get("postalCode") != null ? jsonObject.get("postalCode").getAsString() : "");
		myLocation.setCity(jsonObject.get("city") != null ? jsonObject.get("city").getAsString() : "");
		myLocation.setState(jsonObject.get("state") != null ? jsonObject.get("state").getAsString() : "");
		myLocation.setCountry(jsonObject.get("country") != null ? jsonObject.get("country").getAsString() : "");
		myLocation.setCountryCode(jsonObject.get("cc") != null ? jsonObject.get("cc").getAsString() : "");
		return myLocation;
	}
	
	public LatLng getLatLng() {
		return new LatLng(getLatitude(), getLongitude());
	}
	
	public String getLatLngString() {
		return String.format("%s,%s", getLatitude(), getLongitude());
	}
	
	public String getCityStatePostalCode() {
		String city = getCity() != "" ? getCity() : "-";
		String state = getState() != "" ? getState() : "-";
		String postalCode = getPostalCode() != "" ? getPostalCode() : "-";
		
		return String.format("%s, %s %s", city, state, postalCode);
	}
}