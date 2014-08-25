package com.thunsaker.soup.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;
import com.thunsaker.android.common.annotations.ForApplication;
import com.thunsaker.soup.PreferencesHelper;
import com.thunsaker.soup.R;
import com.thunsaker.soup.app.BaseSoupActivity;
import com.thunsaker.soup.app.SoupApp;
import com.thunsaker.soup.data.api.model.Category;
import com.thunsaker.soup.data.api.model.FoursquareImage;
import com.thunsaker.soup.data.api.model.Venue;
import com.thunsaker.soup.services.foursquare.FoursquarePrefs;
import com.thunsaker.soup.services.foursquare.FoursquareTasks;
import com.thunsaker.soup.util.Util;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;
import de.greenrobot.event.EventBus;

/*
 * Created by @thunsaker
 */
public class VenueEditCategoriesActivity extends BaseSoupActivity {
    @Inject
    @ForApplication
    Context mContext;

    @Inject
    EventBus mBus;

    protected static final String CATEGORY_DELETE_CONFIRMATION_DIALOG = "CATEGORY_DELETE_CONFIRMATION_DIALOG";

    private static final int ADD_CATEGORY_REQUEST = 0;
    public static Venue currentVenue;
    public static List<Category> originalCategories = new ArrayList<Category>();
    private List<Category> updatedCategories = null;

    @InjectView(R.id.linearLayoutProgressBarWrapper) LinearLayout mLinearLayoutProgressBar;
    @InjectView(R.id.linearLayoutVenueCategoriesWrapper) LinearLayout mMainWrapper;

    @InjectView(R.id.relativeLayoutCategoryAddWrapper) RelativeLayout mAddButtonWrapper;

    @InjectView(R.id.relativeLayoutVenueCategoryWrapper1) RelativeLayout mCategoryWrapper1;
    @InjectView(R.id.relativeLayoutVenueCategory1) RelativeLayout mCategory1;
    @InjectView(R.id.imageViewCategoryIcon1) ImageView mCategoryIcon1;
    @InjectView(R.id.textViewCategoryName1) TextView mCategoryName1;
    @InjectView(R.id.imageViewCategoryIconPrimary1) ImageView mCategoryPrimary1;

    @InjectView(R.id.relativeLayoutVenueCategoryWrapper2) RelativeLayout mCategoryWrapper2;
    @InjectView(R.id.relativeLayoutVenueCategory2) RelativeLayout mCategory2;
    @InjectView(R.id.imageViewCategoryIcon2) ImageView mCategoryIcon2;
    @InjectView(R.id.textViewCategoryName2) TextView mCategoryName2;
    @InjectView(R.id.imageViewCategoryIconPrimary2) ImageView mCategoryPrimary2;

    @InjectView(R.id.relativeLayoutVenueCategoryWrapper3) RelativeLayout mCategoryWrapper3;
    @InjectView(R.id.relativeLayoutVenueCategory3) RelativeLayout mCategory3;
    @InjectView(R.id.imageViewCategoryIcon3) ImageView mCategoryIcon3;
    @InjectView(R.id.textViewCategoryName3) TextView mCategoryName3;
    @InjectView(R.id.imageViewCategoryIconPrimary3) ImageView mCategoryPrimary3;

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
        ab.setIcon(getResources().getDrawable(R.drawable.ic_launcher_white));

        setSupportProgressBarVisibility(false);
        setSupportProgressBarIndeterminate(true);

        ButterKnife.inject(this);

        level = PreferencesHelper.getFoursquareSuperuserLevel(getApplicationContext());

        if (getIntent().hasExtra(VenueDetailFragment.VENUE_EDIT_EXTRA)) {
            currentVenue =
                    Venue.GetVenueFromJson(
                            getIntent().getExtras().get(VenueDetailFragment.VENUE_EDIT_EXTRA).toString());
        }

        if (VenueDetailFragment.currentVenue != null) {
            currentVenue = VenueDetailFragment.currentVenue;
            originalCategories = new ArrayList<Category>();
            if (currentVenue.categories != null && currentVenue.categories.size() > 0)
                originalCategories.addAll(currentVenue.categories);
        } else {
            if (mLinearLayoutProgressBar != null && mMainWrapper != null) {
                mLinearLayoutProgressBar.setVisibility(View.VISIBLE);
                mMainWrapper.setVisibility(View.GONE);
            }

            setSupportProgressBarVisibility(true);
            FoursquareTasks mFoursquareTasks = new FoursquareTasks((SoupApp) mContext);
            mFoursquareTasks.new GetVenue(currentVenue.id, FoursquarePrefs.CALLER_SOURCE_EDIT_CATEGORIES).execute();
        }

        LoadCurrentCategories(currentVenue.categories);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (hasModified && level >= FoursquarePrefs.SUPERUSER.SU1) {
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
        if (updatedCategories.size() > 0) {
            myModifiedVenue.categories = updatedCategories;

            setSupportProgressBarVisibility(true);
            new FoursquareTasks.EditVenue(getApplicationContext(),
                    currentVenue.id, myModifiedVenue, this, false, true).execute();
        } else {
            Toast.makeText(getApplicationContext(), R.string.edit_category_no_categories, Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("NewApi")
    public void LoadCurrentCategories(List<Category> myCategories) {
        try {
            boolean primarySet = false;
            boolean secondarySet = false;
            boolean tertiarySet = false;

            List<Category> categoriesToLoad;
            categoriesToLoad = new ArrayList<Category>();
            if (myCategories != null && myCategories.size() > 0) {
                categoriesToLoad.addAll(myCategories);

                if (categoriesToLoad.size() > 0) {
                    for (Category cat : categoriesToLoad) {
                        if (cat != null) {
                            final String categoryName = cat.name != null ? cat.name : "";
                            final String categoryIconUrl = cat.icon != null ? cat.icon.getFoursquareLegacyImageUrl(FoursquareImage.SIZE_MEDIANO, false) : "";

                            if (cat.primary) {
                                mCategoryName1.setText(categoryName);

                                if (!categoryIconUrl.equals("")) {
                                    UrlImageViewHelper.setUrlDrawable(mCategoryIcon1, categoryIconUrl, R.drawable.foursquare_generic_category_icon);
                                    mCategoryIcon1.setBackgroundColor(Util.GetCategoryColor(categoryName.charAt(0), mContext));
                                } else
                                    mCategoryIcon1.setImageResource(R.drawable.generic_category_icon);

                                mCategoryWrapper1.setVisibility(View.VISIBLE);

                                primarySet = true;
                            } else {
                                if (secondarySet) {
                                    mCategoryName3.setText(categoryName);

                                    if (!categoryIconUrl.equals("")) {
                                        UrlImageViewHelper.setUrlDrawable(mCategoryIcon3, categoryIconUrl, R.drawable.foursquare_generic_category_icon);
                                        mCategoryIcon3.setBackgroundColor(Util.GetCategoryColor(categoryName.charAt(0), mContext));
                                    } else
                                        mCategoryIcon3.setImageResource(R.drawable.generic_category_icon);

                                    mCategoryWrapper3.setVisibility(View.VISIBLE);

                                    tertiarySet = true;
                                } else {
                                    mCategoryName2.setText(categoryName);

                                    if (!categoryIconUrl.equals("")) {
                                        UrlImageViewHelper.setUrlDrawable(mCategoryIcon2, categoryIconUrl, R.drawable.foursquare_generic_category_icon);
                                        mCategoryIcon2.setBackgroundColor(Util.GetCategoryColor(categoryName.charAt(0), mContext));
                                    } else
                                        mCategoryIcon2.setImageResource(R.drawable.generic_category_icon);

                                    mCategoryWrapper2.setVisibility(View.VISIBLE);

                                    secondarySet = true;
                                }
                            }
                        }
                    }
                }
            }

            if (mLinearLayoutProgressBar != null && mMainWrapper != null) {
//                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
//                    mMainWrapper.setAlpha(0f);
//                    mMainWrapper.setVisibility(View.VISIBLE);
//
//                    mMainWrapper.animate()
//                            .alpha(1f)
//                            .setDuration(1000)
//                            .setListener(null);
//
//                    mLinearLayoutProgressBar.animate()
//                            .alpha(0f)
//                            .setDuration(1000)
//                            .setListener(new android.animation.AnimatorListenerAdapter() {
//                                @Override
//                                public void onAnimationEnd(android.animation.Animator animation) {
//                                    mLinearLayoutProgressBar.setVisibility(View.GONE);
//                                }
//                            });
//                } else {
                    mLinearLayoutProgressBar.setVisibility(View.GONE);
                    mLinearLayoutProgressBar.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_out));

                    mMainWrapper.setVisibility(View.VISIBLE);
                    mMainWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
//                }
            }

            if (!primarySet)
                mCategoryWrapper1.setVisibility(View.GONE);
            if (!secondarySet)
                mCategoryWrapper2.setVisibility(View.GONE);
            if (!tertiarySet)
                mCategoryWrapper3.setVisibility(View.GONE);

            if (level >= FoursquarePrefs.SUPERUSER.SU1) {
                if (!primarySet || !secondarySet || !tertiarySet)
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
        if (updatedCategories == null) {
            updatedCategories = new ArrayList<Category>();
            updatedCategories.addAll(originalCategories);
        }

        Category tempCategory = updatedCategories.get(placeHolder);
        String newPrimaryId = tempCategory.id;
        tempCategory.primary = true;
        tempSortList.add(tempCategory);

        for (Category c : updatedCategories) {
            if (!c.id.equals(newPrimaryId)) {
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

        if (updatedCategories == null) {
            updatedCategories = new ArrayList<Category>();
            if (originalCategories != null && originalCategories.size() > 0)
                updatedCategories.addAll(originalCategories);
        }
        boolean wasPrimary = false;

        if (updatedCategories != null && updatedCategories.get(item) != null) {
            Category myCategory = updatedCategories.get(item);
            wasPrimary = myCategory.primary;
            updatedCategories.remove(myCategory);
        }

        if (wasPrimary && updatedCategories.size() > 0)
            updatedCategories.get(0).primary = true;

        LoadCurrentCategories(updatedCategories);
    }

    protected void AddCategory(Category myCategory) {
        if (updatedCategories == null) {
            updatedCategories = new ArrayList<Category>();
            if (currentVenue.categories != null && currentVenue.categories.size() > 0)
                updatedCategories.addAll(currentVenue.categories);
        }

        if (updatedCategories.size() == 0) {
            myCategory.primary = true;
            supportInvalidateOptionsMenu();
            updatedCategories.add(myCategory);
        } else {
            for (Category cat : updatedCategories) {
                if (cat.id.equals(myCategory.id.trim())) {
                    Toast.makeText(this,
                            String.format(getString(R.string.edit_category_add_already_added),
                                    myCategory.name), Toast.LENGTH_SHORT
                    ).show();
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
        if (show) {
            mAddButtonWrapper.setVisibility(View.VISIBLE);
            mAddButtonWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_in));
        } else {
            mAddButtonWrapper.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.fade_out));
            mAddButtonWrapper.setVisibility(View.INVISIBLE);
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
        if (requestCode == VenueEditCategoriesActivity.ADD_CATEGORY_REQUEST) {
            if (resultCode == RESULT_OK) {
                String result = data.getStringExtra(VenueAddCategoryActivity.SELECTED_CATEGORY);
                Category myCategory = Category.GetCategoryFromJson(result);
                if (myCategory != null)
                    AddCategory(myCategory);
                else
                    Toast.makeText(this,
                            getString(R.string.edit_category_add_error),
                            Toast.LENGTH_SHORT).show();
            }
        }
    }

    @OnClick(R.id.imageViewCategoryIconPrimary1)
    public void SetPrimary1() {
        MakePrimary(0);
    }

    @OnClick(R.id.imageViewCategoryIconPrimary2)
    public void SetPrimary2() {
        MakePrimary(1);
    }

    @OnClick(R.id.imageViewCategoryIconPrimary3)
    public void SetPrimary3() {
        MakePrimary(2);
    }

    @OnLongClick(R.id.relativeLayoutVenueCategory1)
    public boolean RemoveCategory1() {
        if(level >= FoursquarePrefs.SUPERUSER.SU1) {
            ConfirmDeleteDialogFragment confirmDeleteDialogFragment =
                    ConfirmDeleteDialogFragment.newInstance(0, mCategoryName1.getText().toString());
            confirmDeleteDialogFragment.show(getSupportFragmentManager(), CATEGORY_DELETE_CONFIRMATION_DIALOG);
            return true;
        }

        return false;
    }

    @OnLongClick(R.id.relativeLayoutVenueCategory2)
    public boolean RemoveCategory2() {
        if(level >= FoursquarePrefs.SUPERUSER.SU1) {
            ConfirmDeleteDialogFragment confirmDeleteDialogFragment =
                    ConfirmDeleteDialogFragment.newInstance(1, mCategoryName2.getText().toString());
            confirmDeleteDialogFragment.show(getSupportFragmentManager(), CATEGORY_DELETE_CONFIRMATION_DIALOG);
            return true;
        }

        return false;
    }

    @OnLongClick(R.id.relativeLayoutVenueCategory3)
    public boolean RemoveCategory3() {
        if(level >= FoursquarePrefs.SUPERUSER.SU1) {
            ConfirmDeleteDialogFragment confirmDeleteDialogFragment =
                    ConfirmDeleteDialogFragment.newInstance(2, mCategoryName3.getText().toString());
            confirmDeleteDialogFragment.show(getSupportFragmentManager(), CATEGORY_DELETE_CONFIRMATION_DIALOG);
            return true;
        }

        return false;
    }

    public static class ConfirmDeleteDialogFragment extends DialogFragment {

        private static String ITEM_TO_DELETE = "ITEM_TO_DELETE";
        private static String ITEM_NAME_TO_DELETE = "ITEM_NAME_TO_DELETE";

        public static ConfirmDeleteDialogFragment newInstance(int item, String name) {
            ConfirmDeleteDialogFragment fragment = new ConfirmDeleteDialogFragment();
            Bundle args = new Bundle();
            args.putInt(ITEM_TO_DELETE, item);
            args.putString(ITEM_NAME_TO_DELETE, name);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (getArguments() != null) {
                final int mItem = getArguments().getInt(ITEM_TO_DELETE);
                String mName = getArguments().getString(ITEM_NAME_TO_DELETE);

                return new AlertDialog.Builder(getActivity())
                        .setMessage(String.format(getString(R.string.dialog_category_delete_confirm), mName))
                        .setPositiveButton(
                                getString(R.string.dialog_yes),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ((VenueEditCategoriesActivity) getActivity()).doRemoveCategory(mItem);
                                    }
                                }
                        )
                        .setNegativeButton(getString(R.string.dialog_no), null)
                        .create();
            } else
                return null;
        }
    }

    public void doRemoveCategory(int item) {
        RemoveCategory(item);
    }
}