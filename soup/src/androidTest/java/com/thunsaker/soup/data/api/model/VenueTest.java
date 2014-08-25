package com.thunsaker.soup.data.api.model;

import android.test.InstrumentationTestCase;

import java.util.ArrayList;
import java.util.List;

public class VenueTest extends InstrumentationTestCase {
    public void testConvertCompactVenueToVenue() throws Exception {
        // Arrange
        CompactVenue compact = new CompactVenue();
        compact.id = "4b52689af964a520d97b27e3";
        compact.name = "Fry's Food Store";
        compact.contact = new Contact("6239076160", "(623) 907-6160", "frysfoodstores");
        compact.location = new Location("11425 W Buckeye Rd", "at S Avondale Blvd", 33.43418112561641, -112.30458498001099, 2271, "85323", "Avondale", "AZ", "United States", "US", null);
        List<Category> compactCategoryList = new ArrayList<Category>();
        compactCategoryList.add(new Category("52f2ab2ebcbc57f1066b8b46", "Supermarket", "Supermarkets", "Supermarket",
                new FoursquareImage("https://ss1.4sqi.net/img/categories_v2/shops/food_grocery_",".png"), true));
        compact.categories = compactCategoryList;
        compact.verified = true;
        compact.url = "http://www.frysfood.com";
//        compact.stats = new VenueStats(3470, 564, 14);
//        compact.beenHere = new VenueBeenHere(79, false);
//        compact.storeId = "89";
//        compact.referralId = "v-1407797198";

        Venue expected = new Venue();
        expected.id = "4b52689af964a520d97b27e3";
        expected.name = "Fry's Food Store";
        expected.contact = new Contact("6239076160", "(623) 907-6160", "frysfoodstores");
        expected.location = new Location("11425 W Buckeye Rd", "at S Avondale Blvd", 33.43418112561641, -112.30458498001099, 2271, "85323", "Avondale", "AZ", "United States", "US", null);
        List<Category> venueCategoryList = new ArrayList<Category>();
        venueCategoryList.add(new Category("52f2ab2ebcbc57f1066b8b46", "Supermarket", "Supermarkets", "Supermarket",
                new FoursquareImage("https://ss1.4sqi.net/img/categories_v2/shops/food_grocery_",".png"), true));
        expected.categories = venueCategoryList;
        expected.verified = true;
        expected.url = "http://www.frysfood.com";
//        expected.stats = new VenueStats(3470, 564, 14);
//        expected.beenHere = new VenueBeenHere(79, false);
//        expected.storeId = "89";
//        expected.referralId = "v-1407797198";

        // Act
        Venue actual = Venue.ConvertCompactVenueToVenue(compact);

        // Assert
        assertEquals(expected.id, actual.id);
        assertEquals(expected.name, actual.name);
        assertEquals(expected.location.address, actual.location.address);
        assertEquals(expected.location.postalCode, actual.location.postalCode);
        assertEquals(expected.contact.phone, actual.contact.phone);
        assertNotNull(expected.categories);
        assertEquals(expected.categories.get(0).id, actual.categories.get(0).id);
        assertEquals(expected.categories.get(0).name, actual.categories.get(0).name);
        assertEquals(expected.url, actual.url);
    }
}