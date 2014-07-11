package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonObject;

import java.util.List;

public class Checkin {
	public String id;
	public String createdDate;
	public String type;

	public boolean isMayor;
	public boolean isPrivate;
	public String shout;
	public int timeZone;
	public CompactVenue venue;
	public List<Object> photos;
	public List<Object> comments;
	public Object source;

//	public String getId() {
//		return Id;
//	}
//	public void setId(String id) {
//		Id = id;
//	}
//
//	public String getCreatedDate() {
//		return CreatedDate;
//	}
//	public void setCreatedDate(String createdDate) {
//		CreatedDate = createdDate;
//	}
//
//	public String getType() {
//		return Type;
//	}
//	public void setType(String type) {
//		Type = type;
//	}
//
//
//	public boolean isIsMayor() {
//		return IsMayor;
//	}
//	public void setIsMayor(boolean isMayor) {
//		IsMayor = isMayor;
//	}
//
//	public boolean isIsPrivate() {
//		return IsPrivate;
//	}
//	public void setIsPrivate(boolean isPrivate) {
//		IsPrivate = isPrivate;
//	}
//
//
//	public String getShout() {
//		return Shout;
//	}
//	public void setShout(String shout) {
//		Shout = shout;
//	}
//
//	public int getTimeZone() {
//		return TimeZone;
//	}
//	public void setTimeZone(int timeZone) {
//		TimeZone = timeZone;
//	}
//
//	public CompactVenue getVenue() {
//		return Venue;
//	}
//	public void setVenue(CompactVenue venue) {
//		Venue = venue;
//	}
//
//	public List<Object> getPhotos() {
//		return Photos;
//	}
//	public void setPhotos(List<Object> photos) {
//		Photos = photos;
//	}
//
//	public List<Object> getComments() {
//		return Comments;
//	}
//	public void setComments(List<Object> comments) {
//		Comments = comments;
//	}
//
//	public Object getSource() {
//		return Source;
//	}
//	public void setSource(Object source) {
//		Source = source;
//	}

	public static Checkin GetCheckinFromJson(JsonObject jsonObject) {
		try {
			if(jsonObject.has("venue")) {
				Checkin myCheckin = new Checkin();
				myCheckin.id = jsonObject.get("id") != null ? jsonObject.get("id").getAsString() : "";
				myCheckin.createdDate = jsonObject.get("createdAt") != null ? jsonObject.get("createdAt").getAsString() : "";
				myCheckin.type = jsonObject.get("type") != null ? jsonObject.get("type").getAsString() : "";
				myCheckin.isMayor = jsonObject.get("isMayor") != null && jsonObject.get("isMayor").getAsBoolean();
				myCheckin.isPrivate = jsonObject.get("private") != null && jsonObject.get("private").getAsBoolean();
				myCheckin.shout = jsonObject.get("shout") != null ? jsonObject.get("shout").getAsString() : "";
				myCheckin.timeZone = jsonObject.get("timeZoneOffset") != null ? jsonObject.get("timeZoneOffset").getAsInt() : 0;
				myCheckin.photos = null;
				myCheckin.comments = null;
				myCheckin.source = jsonObject.get("source") != null ? CheckinSource.GetCheckinSourceFromJson(jsonObject.get("source").getAsJsonObject()) : null;
				myCheckin.venue = jsonObject.get("venue") != null ? CompactVenue.ParseCompactVenueFromJson(jsonObject.get("venue").getAsJsonObject()) : null;
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
