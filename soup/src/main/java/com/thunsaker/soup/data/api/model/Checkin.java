package com.thunsaker.soup.data.api.model;

import com.google.gson.JsonObject;

public class Checkin {
	public String id;
	public String createdAt;
	public String type;

	public boolean isMayor;
	public boolean isPrivate;
    public String visibility;
	public String shout;
	public int timeZone;
	public CompactVenue venue;
    public VenueLikes likes;
    public boolean like;
	public CheckinPhotosResponse photos;
	public CheckinCommentsResponse comments;
    public CheckinPostsResponse posts;
    public FoursquareSource source;

	public static Checkin GetCheckinFromJson(JsonObject jsonObject) {
		try {
			if(jsonObject.has("venue")) {
				Checkin myCheckin = new Checkin();
				myCheckin.id = jsonObject.get("id") != null ? jsonObject.get("id").getAsString() : "";
				myCheckin.createdAt = jsonObject.get("createdAt") != null ? jsonObject.get("createdAt").getAsString() : "";
				myCheckin.type = jsonObject.get("type") != null ? jsonObject.get("type").getAsString() : "";
				myCheckin.isMayor = jsonObject.get("isMayor") != null && jsonObject.get("isMayor").getAsBoolean();
				myCheckin.isPrivate = jsonObject.get("private") != null && jsonObject.get("private").getAsBoolean();
                myCheckin.visibility = jsonObject.get("visibility") != null ? jsonObject.get("visibility").getAsString() : "";
				myCheckin.shout = jsonObject.get("shout") != null ? jsonObject.get("shout").getAsString() : "";
				myCheckin.timeZone = jsonObject.get("timeZoneOffset") != null ? jsonObject.get("timeZoneOffset").getAsInt() : 0;
				myCheckin.photos = null;
				myCheckin.comments = null;
				myCheckin.source = jsonObject.get("source") != null ? FoursquareSource.GetCheckinSourceFromJson(jsonObject.get("source").getAsJsonObject()) : null;
				myCheckin.venue = jsonObject.get("venue") != null ? CompactVenue.ParseCompactVenueFromJson(jsonObject.get("venue").getAsJsonObject()) : null;
                myCheckin.likes = jsonObject.get("likes") != null ? VenueLikes.ParseVenueLikesFromJson(jsonObject.get("likes").getAsJsonObject()) : null;
                myCheckin.like = jsonObject.get("like") != null && jsonObject.get("like").getAsBoolean();
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
