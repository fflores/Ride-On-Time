/*
 * Copyright (c) 2010, Sony Ericsson Mobile Communication AB. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 *
 *    * Redistributions of source code must retain the above copyright notice, this 
 *      list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *    * Neither the name of the Sony Ericsson Mobile Communication AB nor the names
 *      of its contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.boullevard.rideontime;

import com.boullevard.zoom.DynamicZoomControl;
import com.boullevard.zoom.ImageZoomView;
import com.boullevard.zoom.LongPressZoomListener;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Activity for zoom tutorial 4
 */
public class SubwayMapActivity extends Activity
{

	/** Constant used as menu item id for resetting zoom state */
	private static final int MENU_ID_RESET = 0;

	/** Image zoom view */
	private ImageZoomView mZoomView;

	/** Zoom control */
	private DynamicZoomControl mZoomControl;

	/** Decoded bitmap image */
	private Bitmap mBitmap;

	/** On touch listener for zoom view */
	private LongPressZoomListener mZoomListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.subway_map);

		mZoomControl = new DynamicZoomControl();
		
		mBitmap = decodeImage(R.drawable.subwaymap);

		mZoomListener = new LongPressZoomListener(getApplicationContext());
		mZoomListener.setZoomControl(mZoomControl);

		mZoomView = (ImageZoomView) findViewById(R.id.zoomview);
		
		mZoomView.setZoomState(mZoomControl.getZoomState());
		mZoomView.setImage(mBitmap);
		mZoomView.setOnTouchListener(mZoomListener);

		mZoomControl.setAspectQuotient(mZoomView.getAspectQuotient());

		resetZoomState();
		
	}

	private Bitmap decodeImage(int f) {
		Bitmap b = null;

		// Decode image size
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		o.inDither = false;
		o.inPurgeable = true;
		o.inTempStorage=new byte[32 * 2074];
		BitmapFactory.decodeResource(getResources(), f, o);
	
		final int IMAGE_MAX_SIZE = 2100;
		
		int scale = 1;
		if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
			scale = (int) Math.pow(2, (int) Math.round(Math.log(IMAGE_MAX_SIZE
					/ (double) Math.max(o.outHeight, o.outWidth))
					/ Math.log(0.5)));
		}

		// Decode with inSampleSize
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		o2.inDither = false;
		o2.inPurgeable = true;
		o2.inTempStorage=new byte[32 * 2074];

		//System.gc();
		b = BitmapFactory.decodeResource(getResources(), f, o2);
		b.recycle();
		
		return b;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		mBitmap.recycle();
		mZoomView.setOnTouchListener(null);
		mZoomControl.getZoomState().deleteObservers();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, MENU_ID_RESET, 2, R.string.menu_reset);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ID_RESET:
				resetZoomState();
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * Reset zoom state and notify observers
	 */
	private void resetZoomState() {
		mZoomControl.getZoomState().setPanX(0.5f);
		mZoomControl.getZoomState().setPanY(0.5f);
		mZoomControl.getZoomState().setZoom(1f);
		mZoomControl.getZoomState().notifyObservers();
	}

}
