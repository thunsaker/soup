package com.thunsaker.soup.data.api.model;

import java.util.List;

public class GetVenueHoursResponse extends FoursquareResponse {
    VenueHoursResponse response;

    public class VenueHoursResponse {
        public String status;
        public boolean isOpen;
        public List<VenueHoursTimeFrameResponse> timeframes;
    }

    public class VenueHoursTimeFrameResponse {
        public String days;
        public boolean includesToday;
        public List<VenueHoursTimeFrameOpen> open;
        public Object[] segments;
    }

    public class VenueHoursTimeFrameOpen {
        public String start;
        public String end;
        public String renderedTime;
    }
}