package com.thunsaker.soup.data;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thunsaker.soup.R;

public class CategoryItemView extends LinearLayout {
	public CategoryItemView(Context context) {
		super(context);
	}

	public CategoryItemView(Context context, String categoryName, String categoryIconUrl, Boolean isPrimary) {
		super(context);

		final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.list_category_item, this);

		final TextView myCategoryName = (TextView)getChildAt(1);
		myCategoryName.setText(categoryName);

		final ToggleButton myCategoryIsPrimary = (ToggleButton)getChildAt(1);
		myCategoryIsPrimary.setChecked(isPrimary);

		final ImageView myCategoryIcon = (ImageView)getChildAt(3);
		if(categoryIconUrl != "")
			UrlImageViewHelper.setUrlDrawable(myCategoryIcon, categoryIconUrl, R.drawable.foursquare_generic_category_icon);
		else
			myCategoryIcon.setImageResource(R.drawable.generic_category_icon);
	}
}