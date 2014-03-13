package com.thunsaker.soup.classes;

/*
 * Created by @thunsaker
 */
public class PickedLocation {
	private Double myLatitude;
	private Double myLongitude;
	private String myName;
	private String myFoursquareVenueId;
	private String myAddressLine;
	private Boolean isFoursquare = false;

	public PickedLocation() { }

	public PickedLocation(Double Latitude, Double Longitude) {
		myLatitude = Latitude;
		myLongitude = Longitude;
		myName = "";
		myFoursquareVenueId = "";
		myAddressLine = "";
		isFoursquare = false;
	}

	public PickedLocation(Double Latitude, Double Longitude, String Name, String FoursquareVenueId, String AddressLine) {
		myLatitude = Latitude;
		myLongitude = Longitude;
		myName = Name;
		myFoursquareVenueId = FoursquareVenueId;
		myAddressLine = AddressLine;
		isFoursquare = true;
	}

	public Double getLatitude() {
		return myLatitude;
	}
	public void setLatitude(Double latitude) {
		myLatitude = latitude;
	}

	public Double getLongitude() {
		return myLongitude;
	}
	public void setLongitude(Double longitude) {
		myLongitude = longitude;
	}

	public String getName() {
		return myName;
	}
	public void setName(String name) {
		myName = name;
	}

	public String getFoursquareVenueId() {
		return myFoursquareVenueId;
	}
	public void setFoursquareVenueId(String foursquareVenueId) {
		myFoursquareVenueId = foursquareVenueId;
		setIsFoursquare(true);
	}

	public String getAddressLine() {
		return myAddressLine;
	}
	public void setAddressLine(String addressLine) {
		myAddressLine = addressLine;
	}

	public Boolean getIsFoursquare() {
		return isFoursquare;
	}
	public void setIsFoursquare(Boolean isFoursquare) {
		this.isFoursquare = isFoursquare;
	}
}
