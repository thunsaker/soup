package com.thunsaker.soup.classes.foursquare;

import com.google.gson.JsonObject;

import java.util.List;

public class Checkin {
	private String Id;
	private String CreatedDate;
	private String Type;
	
	private boolean IsMayor;
	private boolean IsPrivate;
	private String Shout;
	private int TimeZone;
	private CompactVenue Venue;
	private List<Object> Photos;
	private List<Object> Comments;
	private Object Source;
	
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		Id = id;
	}
	
	public String getCreatedDate() {
		return CreatedDate;
	}
	public void setCreatedDate(String createdDate) {
		CreatedDate = createdDate;
	}
	
	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}
	
	
	public boolean isIsMayor() {
		return IsMayor;
	}
	public void setIsMayor(boolean isMayor) {
		IsMayor = isMayor;
	}
	
	public boolean isIsPrivate() {
		return IsPrivate;
	}
	public void setIsPrivate(boolean isPrivate) {
		IsPrivate = isPrivate;
	}
	
	
	public String getShout() {
		return Shout;
	}
	public void setShout(String shout) {
		Shout = shout;
	}
	
	public int getTimeZone() {
		return TimeZone;
	}
	public void setTimeZone(int timeZone) {
		TimeZone = timeZone;
	}
	
	public CompactVenue getVenue() {
		return Venue;
	}
	public void setVenue(CompactVenue venue) {
		Venue = venue;
	}
	
	public List<Object> getPhotos() {
		return Photos;
	}
	public void setPhotos(List<Object> photos) {
		Photos = photos;
	}
	
	public List<Object> getComments() {
		return Comments;
	}
	public void setComments(List<Object> comments) {
		Comments = comments;
	}
	
	public Object getSource() {
		return Source;
	}
	public void setSource(Object source) {
		Source = source;
	}
	
	public static Checkin GetCheckinFromJson(JsonObject jsonObject) {
		try {
			if(jsonObject.has("venue")) {
				Checkin myCheckin = new Checkin();
				myCheckin.setId(jsonObject.get("id") != null ? jsonObject.get("id").getAsString() : "");
				myCheckin.setCreatedDate(jsonObject.get("createdAt") != null ? jsonObject.get("createdAt").getAsString() : "");
				myCheckin.setType(jsonObject.get("type") != null ? jsonObject.get("type").getAsString() : "");
				myCheckin.setIsMayor(jsonObject.get("isMayor") != null ? jsonObject.get("isMayor").getAsBoolean() : false);
				myCheckin.setIsPrivate(jsonObject.get("private") != null ? jsonObject.get("private").getAsBoolean() : false);
				myCheckin.setShout(jsonObject.get("shout") != null ? jsonObject.get("shout").getAsString() : "");
				myCheckin.setTimeZone(jsonObject.get("timeZoneOffset") != null ? jsonObject.get("timeZoneOffset").getAsInt() : 0);
				myCheckin.setPhotos(null);
				myCheckin.setComments(null);
				myCheckin.setSource(jsonObject.get("source") != null ? CheckinSource.GetCheckinSourceFromJson(jsonObject.get("source").getAsJsonObject()) : null);
				myCheckin.setVenue(jsonObject.get("venue") != null ? CompactVenue.ParseCompactVenueFromJson(jsonObject.get("venue").getAsJsonObject()) : null);
				return myCheckin;
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
