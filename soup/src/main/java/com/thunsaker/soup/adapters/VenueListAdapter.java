package com.thunsaker.soup.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.FoursquareImage;

import java.util.List;

public class VenueListAdapter extends ArrayAdapter<CompactVenue> {
    public List<CompactVenue> mItems;
    private LayoutInflater mInflater;
    public int mResource;

    public VenueListAdapter(Context context, List<CompactVenue> listItems) {
        this(context, R.layout.list_venue_item, listItems);
    }

    public VenueListAdapter(Context context, int resource, List<CompactVenue> listItems) {
        super(context, resource, listItems);
        mInflater = LayoutInflater.from(context);
        mItems = listItems;
        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            v = mInflater.inflate(mResource, null);
        }
        try {
            final CompactVenue venue = mItems.get(position);
            if (venue != null) {
                final String myVenueName = venue.name != null ? venue.name : "";
                final String myVenueAddress = venue.location.address != null
                        ? venue.location.address : "";

                final TextView nameTextView = (TextView) v.findViewById(R.id.textViewVenueName);
                if (nameTextView != null)
                    nameTextView.setText(myVenueName);

                final TextView addressTextView =
                        (TextView) v.findViewById(R.id.textViewVenueAddress);
                if (addressTextView != null)
                    addressTextView.setText(myVenueAddress);

                final ImageView primaryCategoryImageView =
                        (ImageView) v.findViewById(R.id.imageViewVenueCategory);
                List<Category> myCategories = venue.categories;
                if (myCategories != null) {
                    Category primaryCategory = myCategories.get(0) != null
                            ? myCategories.get(0)
                            : null;
                    if (primaryCategoryImageView != null && primaryCategory != null) {
                        String imageUrl =
                                primaryCategory.icon
                                        .getFoursquareLegacyImageUrl(
                                                FoursquareImage.SIZE_MEDIANO);
                        UrlImageViewHelper.setUrlDrawable(
                                primaryCategoryImageView,
                                imageUrl,
                                R.drawable.foursquare_generic_category_icon);
                    }
                } else {
                    primaryCategoryImageView.setImageResource(
                            R.drawable.foursquare_generic_category_icon);
                }

                final TextView distanceTextView =
                        (TextView) v.findViewById(R.id.textViewDistance);
                distanceTextView.setText(venue.location.distance + " m");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return v;
    }
}