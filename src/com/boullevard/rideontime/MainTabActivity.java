package com.boullevard.rideontime;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

public class MainTabActivity extends TabActivity
{
	private static final String TAG = "HelloTabWidget";
	
	/** Called when the activity is first created. */
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		//Log.w(TAG, "onCreate");

		Resources res = getResources(); // Resource object to get Drawables
		TabHost tabHost = getTabHost(); // The activity TabHost
		TabHost.TabSpec spec; // Resusable TabSpec for each tab
		Intent intent; // Reusable Intent for each tab

		// Create an Intent to launch an Activity for the tab (to be reused)
		intent = new Intent().setClass(this, CurrentStatusActivity.class);

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("artists").setIndicator("Status",
				res.getDrawable(R.drawable.ic_tab_status)).setContent(intent);
		tabHost.addTab(spec);

		
		// Do the same for the other tabs
		//intent = new Intent().setClass(this, SubwayMapActivity.class);
		intent = new Intent().setClass(this, SubwayMapActivity.class);
		spec = tabHost.newTabSpec("albums").setIndicator("Subway Map",
				res.getDrawable(R.drawable.ic_tab_map)).setContent(intent);
		tabHost.addTab(spec);

		// Custom background for tab buttons
		TabWidget tabWidget = getTabWidget();
		for (int i = 0; i < tabWidget.getChildCount(); i++) {
			RelativeLayout tabLayout = (RelativeLayout) tabWidget.getChildAt(i);
			tabLayout.setBackgroundDrawable(res.getDrawable(R.drawable.tab_indicator));
		}

		setupTabStrips(tabHost);
		tabHost.setCurrentTab(0);
	}

	private void setupTabStrips(TabHost tabHost) {

		/*
		 * Hack to get rid of bottom stripe
		 */
		TabWidget tw = getTabWidget();
		Field mBottomLeftStrip;
		Field mBottomRightStrip;

		try {
			mBottomLeftStrip = tw.getClass().getDeclaredField("mBottomLeftStrip");
			mBottomRightStrip = tw.getClass().getDeclaredField("mBottomRightStrip");

			if (!mBottomLeftStrip.isAccessible()) {
				mBottomLeftStrip.setAccessible(true);
			}

			if (!mBottomRightStrip.isAccessible()) {
				mBottomRightStrip.setAccessible(true);
			}

			mBottomLeftStrip.set(tw, getResources().getDrawable(R.drawable.blank));
			mBottomRightStrip.set(tw, getResources().getDrawable(R.drawable.blank));

		} catch (java.lang.NoSuchFieldException e) {
			// possibly 2.2 and above device
			try {
				Method stripEnabled = tw.getClass().getDeclaredMethod("setStripEnabled",
						boolean.class);
				stripEnabled.invoke(tw, false);

			} catch (Exception e1) {
				e1.printStackTrace();
			}
		} catch (Exception e) {
			//Log.e("setUpTabs", "Failed removing tabui bottom strip", e);
		}
	}

}