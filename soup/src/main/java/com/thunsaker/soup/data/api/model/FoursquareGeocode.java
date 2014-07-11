package com.thunsaker.soup.data.api.model;

/**
 * Created by 20462660 on 7/9/2014.
 */
public class FoursquareGeocode {
    public String what;
    public String where;
    public FoursquareGeocodeFeature feature;
    public Object[] parents;

    private class FoursquareGeocodeFeature {
        public String cc;
        public String name;
        public String displayName;
        public String matchedName;
        public String highlightedName;
        public int woeType;
        public String slug;
        public String id;
        public FoursquareGeocodeGeometry geometry;
    }

    private class FoursquareGeocodeGeometry {
        public FoursquareGeocode.Center center;
        public FoursquareGeocode.Bounds bounds;

    }

    public class Center {
    }

    public class Bounds {
        public Bounds.NorthEast ne;
        public Bounds.SouthWest sw;

        public class SouthWest extends FoursquareLatLng { }

        public class NorthEast extends FoursquareLatLng { }
    }
}
