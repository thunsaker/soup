package com.thunsaker.soup.data.api.model;

public class VenueStats {
	public int checkinsCount;
	public int usersCount;
	public int tipCount;

    public VenueStats() {
    }

    public VenueStats(int checkinsCount, int usersCount, int tipCount) {
        this.checkinsCount = checkinsCount;
        this.usersCount = usersCount;
        this.tipCount = tipCount;
    }
}