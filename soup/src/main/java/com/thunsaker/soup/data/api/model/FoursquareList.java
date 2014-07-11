package com.thunsaker.soup.data.api.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class FoursquareList {
	private String Id;
	private String Name;
	private String Description;
	private String Type;
	private CompactFoursquareUser User;
	private Boolean IsFollowing;
	private Boolean IsEditable;
	private Boolean IsPublic;
	private Boolean IsCollaborative;
	private String Url;
	private String CanonicalUrl;
	private String CanonicalPath;
	private String CreatedAt;
	private String UpdatedAt;
	private FoursquareImage Photo;
	private Integer VisitedCount;
	private Integer VenueCount;
	private Integer Followers;
	private Integer ListItems;
	private List<FoursquareListItem> Items;

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

	public String getDescription() {
		return Description;
	}
	public void setDescription(String description) {
		Description = description;
	}

	public String getType() {
		return Type;
	}
	public void setType(String type) {
		Type = type;
	}

	public CompactFoursquareUser getUser() {
		return User;
	}
	public void setUser(CompactFoursquareUser compactFoursquareUser) {
		User = compactFoursquareUser;
	}

	public Boolean getIsEditable() {
		return IsEditable;
	}
	public void setIsEditable(Boolean isEditable) {
		IsEditable = isEditable;
	}

	public Boolean getIsPublic() {
		return IsPublic;
	}
	public void setIsPublic(Boolean isPublic) {
		IsPublic = isPublic;
	}

	public Boolean getIsCollaborative() {
		return IsCollaborative;
	}
	public void setIsCollaborative(Boolean isCollaborative) {
		IsCollaborative = isCollaborative;
	}

	public String getUrl() {
		return Url;
	}
	public void setUrl(String url) {
		Url = url;
	}

	public String getCanonicalUrl() {
		return CanonicalUrl;
	}
	public void setCanonicalUrl(String canonicalUrl) {
		CanonicalUrl = canonicalUrl;
	}

	public String getCanonicalPath() {
		return CanonicalPath;
	}
	public void setCanonicalPath(String canonicalPath) {
		CanonicalPath = canonicalPath;
	}

	public String getCreatedAt() {
		return CreatedAt;
	}
	public void setCreatedAt(String createdAt) {
		CreatedAt = createdAt;
	}

	public String getUpdatedAt() {
		return UpdatedAt;
	}
	public void setUpdatedAt(String updatedAt) {
		UpdatedAt = updatedAt;
	}

	public FoursquareImage getPhoto() {
		return Photo;
	}
	public void setPhoto(FoursquareImage photo) {
		Photo = photo;
	}

	public Integer getVisitedCount() {
		return VisitedCount;
	}
	public void setVisitedCount(Integer visitedCount) {
		VisitedCount = visitedCount;
	}

	public Integer getVenueCount() {
		return VenueCount;
	}
	public void setVenueCount(Integer venueCount) {
		VenueCount = venueCount;
	}

	public Boolean getIsFollowing() {
		return IsFollowing;
	}
	public void setIsFollowing(Boolean isFollowing) {
		IsFollowing = isFollowing;
	}

	public Integer getFollowers() {
		return Followers;
	}
	public void setFollowers(Integer followers) {
		Followers = followers;
	}

	public void setListItems(Integer listItems) {
		ListItems = listItems;
	}
	public Integer getListItems() {
		return ListItems;
	}

	public List<FoursquareListItem> getItems() {
		return Items;
	}
	public void setItems(List<FoursquareListItem> items) {
		Items = items;
	}

	public static FoursquareList GetListFromJson(JsonObject jsonObject, String listType) {
		try {
			FoursquareList myList = new FoursquareList();
			String myId = jsonObject.get("id") != null
					? jsonObject.get("id").getAsString() : "";
			myList.setId(myId);
			myList.setType(jsonObject.get("type") != null
					? jsonObject.get("type").getAsString()
							: myId.contains("/todos") ? "todos" : listType);
			myList.setName(jsonObject.get("name") != null
					? jsonObject.get("name").getAsString() : "");
			myList.setDescription(jsonObject.get("description") != null
					? jsonObject.get("description").getAsString() : "");
			myList.setUser(jsonObject.get("user") != null
					? CompactFoursquareUser.GetCompactFoursquareUserFromJson(
							jsonObject.get("user").getAsJsonObject())
							: null);

			myList.setIsFollowing(jsonObject.get("following") != null
					? jsonObject.get("following").getAsBoolean() : false);
			myList.setIsEditable(jsonObject.get("editable") != null
					? jsonObject.get("editable").getAsBoolean() : false);
			myList.setIsPublic(jsonObject.get("public") != null
					? jsonObject.get("public").getAsBoolean() : false);
			myList.setIsCollaborative(jsonObject.get("collaborative") != null
					? jsonObject.get("collaborative").getAsBoolean() : false);

			myList.setUrl(jsonObject.get("url") != null ? jsonObject.get("url")
					.getAsString() : "");
			myList.setCanonicalUrl(jsonObject.get("canonicalUrl") != null
					? jsonObject.get("canonicalUrl").getAsString() : "");
			myList.setCanonicalPath(jsonObject.get("canonicalPath") != null
					? jsonObject.get("canonicalPath").getAsString() : "");

			myList.setCreatedAt(jsonObject.get("createdAt") != null
					? jsonObject.get("createdAt").getAsString() : "");
			myList.setUpdatedAt(jsonObject.get("updatedAt") != null
					? jsonObject.get("updatedAt").getAsString() : "");

			myList.setPhoto(jsonObject.get("photo") != null
					? FoursquareImage.GetFoursquareImageFromJson(
							jsonObject.get("photo").getAsJsonObject())
							: null);

			JsonObject jListItems = new JsonObject();
			jListItems = jsonObject.get("listItems") != null
					? jsonObject.get("listItems").getAsJsonObject() : null;
			if(jListItems != null) {
				myList.setVenueCount(jListItems.get("count") != null
						? jListItems.get("count").getAsInt() : 0);
				myList.setItems(jListItems.get("items") != null
						? FoursquareList.GetListItemsFromJson(jListItems.get("items").getAsJsonArray())
								: null);
			}

			return myList;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static List<FoursquareListItem> GetListItemsFromJson(JsonArray jsonArray) {
		try {
			List<FoursquareListItem> myListItems = new ArrayList<FoursquareListItem>();
			for (JsonElement jListItem : jsonArray) {
				FoursquareListItem myListItem = new FoursquareListItem();
				myListItem = FoursquareListItem.ParseFoursquareListItemFromJson(jListItem.getAsJsonObject());
				myListItems.add(myListItem);
			}
			return myListItems;
		} catch (Exception e) {
			return null;
		}
	}
}