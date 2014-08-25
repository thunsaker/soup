package com.thunsaker.soup.data.api.model;

import java.util.List;

public class GetVenueHoursResponse extends FoursquareResponse {
    public VenueHoursResponse response;

    public class VenueHoursResponse {
        public VenueHoursResponseHours hours;
        public VenueHoursResponsePopular popular;

        public class VenueHoursResponseHours {
            public List<VenueHoursTimeFrameResponseDayList> timeframes;
        }

        public class VenueHoursResponsePopular extends VenueHoursResponseHours { }
    }

    public class GetVenueResponseHours {
        public String status;
        public boolean isOpen;
        public List<VenueHoursTimeFrameResponse> timeframes;
    }

    public class VenueHoursTimeFrameResponseDayList {
        public List<Integer> days;
        public boolean includesToday;
        public List<VenueHoursTimeFrameOpen> open;
        public Object[] segments;
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