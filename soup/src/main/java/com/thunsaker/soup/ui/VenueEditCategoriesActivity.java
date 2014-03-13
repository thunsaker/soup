package com.thunsaker.soup.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import com.thunsaker.soup.R;
import com.thunsaker.soup.FoursquareHelper;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.classes.foursquare.Category;
import com.thunsaker.soup.classes.foursquare.CompactVenue;
import com.thunsaker.soup.classes.foursquare.FoursquareImage;
import com.thunsaker.soup.classes.foursquare.Venue;
import com.thunsaker.soup.util.foursquare.VenueEndpoint;

import java.util.ArrayList;
import java.util.List;

/*
 * Created by @thunsaker
 */
public class VenueEditCategoriesActivity extends ActionBarActivity {
	private boolean useLogo = true;
	private boolean showHomeUp = true;

	private static final int ADD_CATEGORY_REQUEST = 0;
	public static Venue currentVenue;
	private CompactVenue currentCompactVenue;
	public static List<Category> originalCategories = new ArrayList<Category>();
	private List<Category> updatedCategories = null;

	public LinearLayout mLinearLayoutProgressBar = null;
	public LinearLayout mLinearLayout = null;
	public TextView mAddCategoryTextView = null;
	boolean hasModified = false;
	int level = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		setContentView(R.layout.activity_venue_edit_categories);
		ActionBar ab = getSupportActionBar();
		ab.setDisplayHomeAsUpEnabled(showHomeUp);
		ab.setDisplayUseLogoEnabled(useLogo);

		setProgressBarVisibility(false);
		setProgressBarIndeterminate(true);

		level = Integer.parseInt(
                PreferencesHelper.getFoursquareSuperuserLevel(getApplicationContext()));

		mLinearLayoutProgressBar = (LinearLayout)findViewById(R.id.linearLayoutProgressBarWrapper);
		mLinearLayout = (LinearLayout)findViewById(R.id.linearLayoutVenueCategoriesWrapper);
		mAddCategoryTextView = (TextView) findViewById(R.id.textViewCategoryAddTitle);

		if(getIntent().hasExtra(VenueDetailFragment.VENUE_EDIT_EXTRA)) {
			currentCompactVenue =
                    CompactVenue.GetCompactVenueFromJson(
                            getIntent().getExtras()
                                    .get(VenueDetailFragment.VENUE_EDIT_EXTRA).toString());
		}

		if(VenueDetailFragment.currentVenue != null) {
			currentVenue = VenueDetailFragment.currentVenue;
			originalCategories = new ArrayList<Category>();
			if(currentVenue.getCategories() != null && currentVenue.getCategories().size() > 0)
				originalCategories.addAll(currentVenue.getCategories());
		} else {
			if(mLinearLayoutProgressBar != null && mLinearLayout != null) {
				mLinearLayoutProgressBar.setVisibility(View.VISIBLE);
				mLinearLayout.setVisibility(View.GONE);
			}
			setProgressBarVisibility(true);
			new VenueEndpoint.GetVenue(getApplicationContext(),
                    currentCompactVenue.getId(), this,
                    FoursquareHelper.CALLER_SOURCE_EDIT_CATEGORIES).execute();
		}

		LoadCurrentCategories(currentVenue.getCategories());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(hasModified && level >= FoursquareHelper.SUPERUSER.SU1) {
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
			originalCategories.addAll(currentVenue.getCategories());
			updatedCategories = null;
			LoadCurrentCategories(originalCategories);
			hasModified = false;
			supportInvalidateOptionsMenu();
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void SaveUpdatedCategories() {
		Venue myModifiedVenue = new Venue();
		myModifiedVenue = currentVenue;
		myModifiedVenue.setCategories(null);
		if(updatedCategories.size() > 0) {
			myModifiedVenue.setCategories(updatedCategories);

			setProgressBarVisibility(true);
			new VenueEndpoint.EditVenue(getApplicationContext(),
                    currentVenue.getId(), myModifiedVenue, this, false, true).execute();
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

			List<Category> categoriesToLoad = null;
			categoriesToLoad = new ArrayList<Category>();
			if(myCategories != null && myCategories.size() > 0) {
				categoriesToLoad.addAll(myCategories);

				if(categoriesToLoad != null && categoriesToLoad.size() > 0) {
					for (Category cat : categoriesToLoad) {
						if(cat != null) {
							LinearLayout myCategoryLinearLayout = null;
							ToggleButton myCategoryPrimaryToggleButton = null;
							ImageButton myCategoryDeleteImageButton = null;

							if(cat.getPrimary()) {
								myCategoryLinearLayout =
                                        (LinearLayout)findViewById(
                                                R.id.linearLayoutCategoryWrapper1);
								myCategoryPrimaryToggleButton =
                                        (ToggleButton)findViewById(
                                                R.id.toggleButtonPrimary1);
								myCategoryDeleteImageButton =
                                        (ImageButton) findViewById(
                                                R.id.imageViewCategoryDelete1);

								if(level >= FoursquareHelper.SUPERUSER.SU1) {
									myCategoryPrimaryToggleButton.setOnClickListener(
                                            new OnClickListener() {
										@Override
										public void onClick(View v) {
											ToggleButton myToggle = (ToggleButton)v;
											myToggle.setChecked(true);
										}
									});

                                    myCategoryDeleteImageButton.setVisibility(View.VISIBLE);
                                    myCategoryDeleteImageButton
                                            .setOnClickListener(new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    RemoveCategory(0);
                                                }
                                            });
								} else {
									myCategoryDeleteImageButton.setVisibility(View.GONE);
									myCategoryPrimaryToggleButton
                                            .setOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View v) {
											ToggleButton myToggle = (ToggleButton)v;
											myToggle.setChecked(true);
										}
									});
								}
								primarySet = true;
							} else {
								if(secondarySet) {
									myCategoryLinearLayout =
                                            (LinearLayout)findViewById(
                                                    R.id.linearLayoutCategoryWrapper3);
									myCategoryPrimaryToggleButton =
                                            (ToggleButton)findViewById(
                                                    R.id.toggleButtonPrimary3);
									myCategoryDeleteImageButton =
                                            (ImageButton) findViewById(
                                                    R.id.imageViewCategoryDelete3);

									if(level >= FoursquareHelper.SUPERUSER.SU1) {
										myCategoryPrimaryToggleButton
                                                .setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												ToggleButton myToggle = (ToggleButton)v;
												if(myToggle.isChecked()) {
													MakePrimary(2);
												}
											}
										});
										myCategoryDeleteImageButton.setVisibility(View.VISIBLE);
										myCategoryDeleteImageButton
                                                .setOnClickListener(new OnClickListener() {
											@Override
												public void onClick(View v) {
													RemoveCategory(2);
												}
											});
									} else {
										myCategoryDeleteImageButton.setVisibility(View.GONE);
										myCategoryPrimaryToggleButton
                                                .setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												ToggleButton myToggle = (ToggleButton)v;
												myToggle.setChecked(false);
											}
										});
									}
									tertiarySet = true;
								} else {
									myCategoryLinearLayout =
                                            (LinearLayout)findViewById(
                                                    R.id.linearLayoutCategoryWrapper2);
									myCategoryPrimaryToggleButton =
                                            (ToggleButton)findViewById(
                                                    R.id.toggleButtonPrimary2);
									myCategoryDeleteImageButton =
                                            (ImageButton) findViewById(
                                                    R.id.imageViewCategoryDelete2);

									if(level >= FoursquareHelper.SUPERUSER.SU1) {
										myCategoryPrimaryToggleButton
                                                .setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												ToggleButton myToggle = (ToggleButton)v;
												if(myToggle.isChecked()) {
													MakePrimary(1);
												}
											}
										});
										myCategoryDeleteImageButton.setVisibility(View.VISIBLE);
										myCategoryDeleteImageButton.setOnClickListener(new OnClickListener() {
											@Override
												public void onClick(View v) {
													RemoveCategory(1);
												}
											});
									} else {
										myCategoryDeleteImageButton.setVisibility(View.GONE);
										myCategoryPrimaryToggleButton.setOnClickListener(new OnClickListener() {
											@Override
											public void onClick(View v) {
												ToggleButton myToggle = (ToggleButton)v;
												myToggle.setChecked(false);
											}
										});
									}
									secondarySet = true;
								}
							}

							if(myCategoryLinearLayout != null) {

								final String categoryName =
                                        cat.getName() != null ? cat.getName() : "";
								final String categoryIconUrl =
                                        cat.getIcon() != null ? cat.getIcon().getFoursquareLegacyImageUrl(FoursquareImage.SIZE_MEDIANO) : "";
								final Boolean isPrimary =
                                        cat.getPrimary() != null ? cat.getPrimary() : false;

								((TextView) myCategoryLinearLayout.getChildAt(2))
                                        .setText(categoryName);
								((ToggleButton) myCategoryLinearLayout.getChildAt(0))
                                        .setChecked(isPrimary);

								final ImageView myCategoryIcon =
                                        (ImageView)myCategoryLinearLayout.getChildAt(1);
								if(categoryIconUrl != "")
									UrlImageViewHelper
                                            .setUrlDrawable(myCategoryIcon,
                                                    categoryIconUrl,
                                                    R.drawable.foursquare_generic_category_icon);
								else
									myCategoryIcon
                                            .setImageResource(R.drawable.generic_category_icon);

								myCategoryLinearLayout.setVisibility(View.VISIBLE);
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
				((LinearLayout)findViewById(R.id.linearLayoutCategoryWrapper1))
                        .setVisibility(View.GONE);
			if(!secondarySet)
				((LinearLayout)findViewById(R.id.linearLayoutCategoryWrapper2))
                        .setVisibility(View.GONE);
			if(!tertiarySet)
				((LinearLayout)findViewById(R.id.linearLayoutCategoryWrapper3))
                        .setVisibility(View.GONE);

			if(level >= FoursquareHelper.SUPERUSER.SU1) {
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
		String newPrimaryId = tempCategory.getId();
		tempCategory.setPrimary(true);
		tempSortList.add(tempCategory);

		for (Category c : updatedCategories) {
			if(!c.getId().equals(newPrimaryId)) {
				c.setPrimary(false);
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
			wasPrimary = myCategory.getPrimary();
			updatedCategories.remove(myCategory);
		}

		if(wasPrimary && updatedCategories.size() > 0)
			updatedCategories.get(0).setPrimary(true);

		LoadCurrentCategories(updatedCategories);
	}

	protected void AddCategory(Category myCategory) {
		if(updatedCategories == null) {
			updatedCategories = new ArrayList<Category>();
			if(currentVenue.getCategories() != null && currentVenue.getCategories().size() > 0)
				updatedCategories.addAll(currentVenue.getCategories());
		}

		if(updatedCategories.size() == 0) {
			myCategory.setPrimary(true);
			supportInvalidateOptionsMenu();
			updatedCategories.add(myCategory);
		} else {
            for (Category cat : updatedCategories) {
                if(cat.getId().equals(myCategory.getId().trim())) {
//                    Crouton.makeText(this,
//                            String.format(getString(R.string.edit_category_add_already_added),
//                                    myCategory.getName()), Style.ALERT).show();

                    Toast.makeText(this,
                            String.format(getString(R.string.edit_category_add_already_added),
                                    myCategory.getName()), Toast.LENGTH_SHORT).show();
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