package com.thunsaker.soup.pro;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {
	public static final String SOUP_PACKAGE_NAME = "com.thunsaker.soup";
	public static final String SOUP_PRO_PACKAGE_NAME = "com.thunsaker.soup.pro";
	public static final String SOUP_PRO_LAUNCHER_ACTIVITY = "com.thunsaker.soup.pro.MainActivity";

	public static final String DIALOG_HIDE_SOUP_CONFIRM = "HIDE_SOUP";
	public static boolean isInstalled = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		try {
	        PackageManager manager = getApplicationContext().getPackageManager();

	        Button mLaunchSoupButton = (Button) findViewById(R.id.buttonGetSoup);
	        Button mHideSoupButton = (Button) findViewById(R.id.buttonHideSoup);

	        if(manager.checkSignatures(SOUP_PACKAGE_NAME, SOUP_PRO_PACKAGE_NAME) == PackageManager.SIGNATURE_MATCH) {
	        	((TextView) findViewById(R.id.textViewSoupInstruction)).setText(R.string.just_key);
	        	mLaunchSoupButton.setText(R.string.open_soup);
	        	isInstalled = true;
	        } else {
	        	((TextView) findViewById(R.id.textViewSoupInstruction)).setText(R.string.download_soup);
	        	mLaunchSoupButton.setText(R.string.get_soup);
	        	isInstalled = false;
	        }

        	findViewById(R.id.imageButtonSoupBowl).setOnClickListener(soupClickListener);
        	mLaunchSoupButton.setOnClickListener(soupClickListener);
        	mHideSoupButton.setOnClickListener(soupHideClickListener);
	    } catch (Exception e) {
	        // Expected exception that occurs if the package is not present.
	    	e.printStackTrace();
	    }
	}

	public void openSoup() {
		if(isInstalled) {
			startActivity(new Intent(getPackageManager().getLaunchIntentForPackage(SOUP_PACKAGE_NAME)));
		} else {
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.thunsaker.soup")));
		}
	}

	public OnClickListener soupClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			openSoup();
		}
	};

	public OnClickListener soupHideClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
		    DialogFragment newFragment = new HideSoupConfirmDialogFragment();
		    newFragment.show(getSupportFragmentManager(), DIALOG_HIDE_SOUP_CONFIRM);
		}
	};

	public static class HideSoupConfirmDialogFragment extends DialogFragment {
	    @Override
	    public Dialog onCreateDialog(Bundle savedInstanceState) {
	        // Use the Builder class for convenient dialog construction
	        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage(R.string.hide_soup_confim)
	               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                	   ComponentName componentToDisable = new ComponentName(SOUP_PRO_PACKAGE_NAME, SOUP_PRO_LAUNCHER_ACTIVITY);
		           			getActivity().getPackageManager().setComponentEnabledSetting(
		           					componentToDisable,
		           					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		           					PackageManager.DONT_KILL_APP);
		           			Toast.makeText(getActivity().getApplicationContext(), "Soup Pro has been hidden.", Toast.LENGTH_SHORT).show();
		           			getActivity().finish();
	                   }
	               })
	               .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	                   public void onClick(DialogInterface dialog, int id) {
	                   }
	               });

	        return builder.create();
	    }
	}
}