package com.thunsaker.soup.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.app.SoupApp;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.CompactVenue;
import com.thunsaker.soup.data.api.model.FoursquareImage;
import com.thunsaker.soup.data.api.model.Venue;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/*
 * Created by @thunsaker
 */
public class VenueEditCategoriesActivity extends BaseSoupActivity {
    @Inject
    @ForApplication
    Context mContext;

	private static final int ADD_CATEGORY_REQUEST = 0;
	public static Venue currentVenue;
	private CompactVenue currentCompactVenue;
	public static List<Category> originalCategories = new ArrayList<Category>();
	private List<Category> updatedCategories = null;

    @InjectView(R.id.linearLayoutProgressBarWrapper) LinearLayout mLinearLayoutProgressBar;
    @InjectView(R.id.linearLayoutVenueCategoriesWrapper) LinearLayout mLinearLayout;
    @InjectView(R.id.textViewCategoryAddTitle) TextView mAddCategoryTextView;

    @InjectView(R.id.linearLayoutCategoryWrapper1) LinearLayout mCategoryWrapper1;
    @InjectView(R.id.toggleButtonPrimary1) ToggleButton mCategoryPrimaryToggle1;
    @InjectView(R.id.imageViewCategoryIcon1) ImageView mCategoryIcon1;
    @InjectView(R.id.textViewCategoryName1) TextView mCategoryName1;
    @InjectView(R.id.imageViewCategoryDelete1) ImageButton mDeleteCategory1;

    @InjectView(R.id.linearLayoutCategoryWrapper2) LinearLayout mCategoryWrapper2;
    @InjectView(R.id.toggleButtonPrimary2) ToggleButton mCategoryPrimaryToggle2;
    @InjectView(R.id.imageViewCategoryIcon2) ImageView mCategoryIcon2;
    @InjectView(R.id.textViewCategoryName2) TextView mCategoryName2;
    @InjectView(R.id.imageViewCategoryDelete2) ImageButton mDeleteCategory2;

    @InjectView(R.id.linearLayoutCategoryWrapper3) LinearLayout mCategoryWrapper3;
    @InjectView(R.id.toggleButtonPrimary3) ToggleButton mCategoryPrimaryToggle3;
    @InjectView(R.id.imageViewCategoryIcon3) ImageView mCategoryIcon3;
    @InjectView(R.id.textViewCategoryName3) TextView mCategoryName3;
    @InjectView(R.id.imageViewCategoryDelete3) ImageButton mDeleteCategory3;

	boolean hasModified = false;
	int level = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.activity_venue_edit_categories);

		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setDisplayUseLogoEnabled(true);

		setSupportProgressBarVisibility(false);
        setSupportProgressBarIndeterminate(true);

        ButterKnife.inject(this);

		level = PreferencesHelper.getFoursquareSuperuserLevel(getApplicationContext());

		if(getIntent().hasExtra(VenueDetailFragment.VENUE_EDIT_EXTRA)) {
			currentCompactVenue =
                    CompactVenue.GetCompactVenueFromJson(
                            getIntent().getExtras()
                                    .get(VenueDetailFragment.VENUE_EDIT_EXTRA).toString());
		}

		if(VenueDetailFragment.currentVenue != null) {
			currentVenue = VenueDetailFragment.currentVenue;
			originalCategories = new ArrayList<Category>();
			if(currentVenue.categories != null && currentVenue.categories.size() > 0)
				originalCategories.addAll(currentVenue.categories);
		} else {
			if(mLinearLayoutProgressBar != null && mLinearLayout != null) {
				mLinearLayoutProgressBar.setVisibility(View.VISIBLE);
				mLinearLayout.setVisibility(View.GONE);
			}
			setSupportProgressBarVisibility(true);
            FoursquareTasks mFoursquareTasks = new FoursquareTasks((SoupApp) mContext);
			mFoursquareTasks.new GetVenue(currentCompactVenue.id,
                    FoursquarePrefs.CALLER_SOURCE_EDIT_CATEGORIES).execute();
		}

		LoadCurrentCategories(currentVenue.categories);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(hasModified && level >= FoursquarePrefs.SUPERUSER.SU1) {
			getMenuInflater().inflate(R.menu.activity_venue_edit, menu);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		case R.id.action_save:
			SaveUpdatedCategories();
			break;
		case R.id.action_revert:
//			Crouton.makeText(this, R.string.edit_category_reverting_changes, Style.INFO).show();
            Toast.makeText(this, R.string.edit_category_reverting_changes, Toast.LENGTH_SHORT).show();
			originalCategories = null;
			originalCategories = new ArrayList<Category>();
			originalCategories.addAll(currentVenue.categories);
			updatedCategories = null;
			LoadCurrentCategories(originalCategories);
			hasModified = false;
			supportInvalidateOptionsMenu();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void SaveUpdatedCategories() {
		Venue myModifiedVenue;
		myModifiedVenue = currentVenue;
		myModifiedVenue.categories = null;
		if(updatedCategories.size() > 0) {
			myModifiedVenue.categories = updatedCategories;

			setSupportProgressBarVisibility(true);
			new FoursquareTasks.EditVenue(getApplicationContext(),
                    currentVenue.id, myModifiedVenue, this, false, true).execute();
		} else {
//			Crouton.makeText(getParent(), R.string.edit_category_no_categories, Style.INFO).show();
            Toast.makeText(getApplicationContext(), R.string.edit_category_no_categories, Toast.LENGTH_SHORT).show();
		}
	}

	public void LoadCurrentCategories(List<Category> myCategories) {
		try {
			boolean primarySet = false;
			boolean secondarySet = false;
			boolean tertiarySet = false;

			List<Category> categoriesToLoad;
			categoriesToLoad = new ArrayList<Category>();
			if(myCategories != null && myCategories.size() > 0) {
				categoriesToLoad.addAll(myCategories);

				if(categoriesToLoad.size() > 0) {
					for (Category cat : categoriesToLoad) {
						if(cat != null) {
                            final String categoryName = cat.name != null ? cat.name : "";
                            final String categoryIconUrl = cat.icon != null ? cat.icon.getFoursquareLegacyImageUrl(FoursquareImage.SIZE_MEDIANO) : "";

							if(cat.primary) {
								if(level >= FoursquarePrefs.SUPERUSER.SU1) {
									mCategoryPrimaryToggle1.setOnClickListener(
                                            new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    ToggleButton myToggle = (ToggleButton) v;
                                                    myToggle.setChecked(true);
                                                }
                                            }
                                    );

                                    mDeleteCategory1.setVisibility(View.VISIBLE);
                                    mDeleteCategory1.setOnClickListener(new OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            RemoveCategory(0);
                                        }
                                    });
								} else {
									mDeleteCategory1.setVisibility(View.GONE);
									mCategoryPrimaryToggle1
                                            .setOnClickListener(new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    ToggleButton myToggle = (ToggleButton) v;
                                                    myToggle.setChecked(true);
                                                }
                                            });
								}

                                mCategoryName1.setText(categoryName);
                                mCategoryPrimaryToggle1.setChecked(true);

                                if(!categoryIconUrl.equals(""))
                                    UrlImageViewHelper.setUrlDrawable(mCategoryIcon1, categoryIconUrl, R.drawable.foursquare_generic_category_icon);
                                else
                                    mCategoryIcon1.setImageResource(R.drawable.generic_category_icon);

                                mCategoryWrapper1.setVisibility(View.VISIBLE);

								primarySet = true;
							} else {
								if(secondarySet) {
									if(level >= FoursquarePrefs.SUPERUSER.SU1) {
										mCategoryPrimaryToggle3
                                                .setOnClickListener(new OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        ToggleButton myToggle = (ToggleButton) v;
                                                        if (myToggle.isChecked()) {
                                                            MakePrimary(2);
                                                        }
                                                    }
                                                });
										mDeleteCategory3.setVisibility(View.VISIBLE);
										mDeleteCategory3
                                                .setOnClickListener(new OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        RemoveCategory(2);
                                                    }
                                                });
									} else {
										mDeleteCategory3.setVisibility(View.GONE);
										mCategoryPrimaryToggle3
                                                .setOnClickListener(new OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        ToggleButton myToggle = (ToggleButton) v;
                                                        myToggle.setChecked(false);
                                                    }
                                                });
									}

                                    mCategoryName3.setText(categoryName);
                                    mCategoryPrimaryToggle3.setChecked(false);

                                    if(!categoryIconUrl.equals(""))
                                        UrlImageViewHelper.setUrlDrawable(mCategoryIcon3, categoryIconUrl, R.drawable.foursquare_generic_category_icon);
                                    else
                                        mCategoryIcon3.setImageResource(R.drawable.generic_category_icon);

                                    mCategoryWrapper3.setVisibility(View.VISIBLE);

									tertiarySet = true;
								} else {
									if(level >= FoursquarePrefs.SUPERUSER.SU1) {
										mCategoryPrimaryToggle2
                                                .setOnClickListener(new OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        ToggleButton myToggle = (ToggleButton) v;
                                                        if (myToggle.isChecked()) {
                                                            MakePrimary(1);
                                                        }
                                                    }
                                                });
										mDeleteCategory2.setVisibility(View.VISIBLE);
										mDeleteCategory2.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                RemoveCategory(1);
                                            }
                                        });
									} else {
										mDeleteCategory2.setVisibility(View.GONE);
										mCategoryPrimaryToggle2.setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                ToggleButton myToggle = (ToggleButton) v;
                                                myToggle.setChecked(false);
                                            }
                                        });
									}

                                    mCategoryName2.setText(categoryName);
                                    mCategoryPrimaryToggle2.setChecked(false);

                                    if(!categoryIconUrl.equals(""))
                                        UrlImageViewHelper.setUrlDrawable(mCategoryIcon2, categoryIconUrl, R.drawable.foursquare_generic_category_icon);
                                    else
                                        mCategoryIcon2.setImageResource(R.drawable.generic_category_icon);

                                    mCategoryWrapper2.setVisibility(View.VISIBLE);

									secondarySet = true;
								}
							}
						}
					}
				}
			}

			if(mLinearLayoutProgressBar != null && mLinearLayout != null) {
				mLinearLayoutProgressBar.setVisibility(View.GONE);
				mLinearLayout.setVisibility(View.VISIBLE);
			}

			if(!primarySet)
				mCategoryWrapper1.setVisibility(View.GONE);
			if(!secondarySet)
				mCategoryWrapper2.setVisibility(View.GONE);
			if(!tertiarySet)
				mCategoryWrapper3.setVisibility(View.GONE);

			if(level >= FoursquarePrefs.SUPERUSER.SU1) {
				if(!primarySet || !secondarySet || !tertiarySet)
					ShowHideAddVenueItem(true);
				else
					ShowHideAddVenueItem(false);
			} else {
				ShowHideAddVenueItem(false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void MakePrimary(int i) {
		Integer placeHolder = i;
		List<Category> tempSortList = new ArrayList<Category>();
		if(updatedCategories == null) {
			updatedCategories = new ArrayList<Category>();
			updatedCategories.addAll(originalCategories);
		}

		Category tempCategory = updatedCategories.get(placeHolder);
		String newPrimaryId = tempCategory.id;
		tempCategory.primary = true;
		tempSortList.add(tempCategory);

		for (Category c : updatedCategories) {
			if(!c.id.equals(newPrimaryId)) {
				c.primary = false;
				tempSortList.add(c);
			}
		}

		updatedCategories.clear();
		updatedCategories.addAll(tempSortList);
		hasModified = true;
		supportInvalidateOptionsMenu();

		LoadCurrentCategories(updatedCategories);
	}

	protected void RemoveCategory(Integer item) {
		hasModified = true;
		supportInvalidateOptionsMenu();

		if(updatedCategories == null) {
			updatedCategories = new ArrayList<Category>();
			if(originalCategories != null && originalCategories.size() > 0)
				updatedCategories.addAll(originalCategories);
		}
		boolean wasPrimary = false;

		if(updatedCategories != null && updatedCategories.get(item) != null) {
			Category myCategory = updatedCategories.get(item);
			wasPrimary = myCategory.primary;
			updatedCategories.remove(myCategory);
		}

		if(wasPrimary && updatedCategories.size() > 0)
			updatedCategories.get(0).primary = true;

		LoadCurrentCategories(updatedCategories);
	}

	protected void AddCategory(Category myCategory) {
		if(updatedCategories == null) {
			updatedCategories = new ArrayList<Category>();
			if(currentVenue.categories != null && currentVenue.categories.size() > 0)
				updatedCategories.addAll(currentVenue.categories);
		}

		if(updatedCategories.size() == 0) {
			myCategory.primary = true;
			supportInvalidateOptionsMenu();
			updatedCategories.add(myCategory);
		} else {
            for (Category cat : updatedCategories) {
                if(cat.id.equals(myCategory.id.trim())) {
                    Toast.makeText(this,
                            String.format(getString(R.string.edit_category_add_already_added),
                                    myCategory.name), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
			updatedCategories.add(myCategory);
		}

		hasModified = true;
		supportInvalidateOptionsMenu();
		LoadCurrentCategories(updatedCategories);
	}

	private void ShowHideAddVenueItem(Boolean show) {
		if(show) {
			mAddCategoryTextView.setVisibility(View.VISIBLE);
		} else {
			mAddCategoryTextView.setVisibility(View.GONE);
		}
	}

	public void ShowAddCategory(View myView) {
		Intent addCategoryIntent =
                new Intent(getApplicationContext(), VenueAddCategoryActivity.class);
		startActivityForResult(addCategoryIntent, ADD_CATEGORY_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == VenueEditCategoriesActivity.ADD_CATEGORY_REQUEST) {
			if(resultCode == RESULT_OK) {
				String result = data.getStringExtra(VenueAddCategoryActivity.SELECTED_CATEGORY);
				Category myCategory = Category.GetCategoryFromJson(result);
				if(myCategory != null)
					AddCategory(myCategory);
				else
//					Crouton.makeText(this,
//                            getString(R.string.edit_category_add_error),
//                            Style.ALERT).show();
                    Toast.makeText(this,
                            getString(R.string.edit_category_add_error),
                            Toast.LENGTH_SHORT).show();
			}
		}
	}
}