package com.thunsaker.soup.data.api.model;

public class VenueBeenHere extends BaseCountClass<Object> {
    public boolean marked;

    public VenueBeenHere() {
    }

    public VenueBeenHere(int count, boolean marked) {
        this.count = count;
        this.marked = marked;
    }
}
