package com.boullevard.rideontime;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CurrentStatusActivity extends Activity
{
	private static final String TAG = "CurrentStatusActivity";
	private static final Boolean DEBUG_BITMAP_ISSUE = true;
	static final private int STATUS_DIALOG = 1;

	private ListView statusListView;
	private StatusAdapter aa;
	private ArrayList<StatusItem> statusArray = new ArrayList<StatusItem>();
	private TextView currentUpdateView;
	private StatusItem selectedStatusItem;
	private ImageButton updateButton;

	private CurrentStatusReceiver receiver;
	private NetworkConnectionReceiver netWorkReceiver;
	private ComponentName service;
	private Intent serviceIntent;
	private ProgressDialog progressDialog;

	@Override
	public void onCreate(Bundle icicle) {
		Log.w(TAG, "onCreate");
		// Debug.startMethodTracing("rideontime");

		super.onCreate(icicle);

		setContentView(R.layout.status_main);

		/** ********* BLOCK NOT EXECUTED IF IN DEBUG BITMAP MODE **********/
		if (DEBUG_BITMAP_ISSUE) {

			updateButton = (ImageButton) this.findViewById(R.id.update_btn);
			updateButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					checkNetworkConnectivity();
				}
			});

			currentUpdateView = (TextView) this.findViewById(R.id.current_update_id);

			statusListView = (ListView) this.findViewById(R.id.statusListView);
			statusListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView _av, View _v, int _index, long arg3) {
					selectedStatusItem = statusArray.get(_index);
					showDialog(STATUS_DIALOG);
				}
			});

			int layoutID = R.layout.listviewitem;
			aa = new StatusAdapter(this, layoutID, statusArray);
			statusListView.setAdapter(aa);

			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage("Loading...");
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(true);

			receiver = new CurrentStatusReceiver();

			// Debug.stopMethodTracing();
			netWorkReceiver = new NetworkConnectionReceiver();
		}

		// ********* END BITMAP ISSUE DEBUG **********/
	}

	@Override
	protected void onStart() {
		Log.w(TAG, "onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.w(TAG, "onResume");

		registerReceiver(receiver, new IntentFilter(CurrentStatusService.UPDATE_NEW_ITEM));
		registerReceiver(receiver, new IntentFilter(CurrentStatusService.UPDATE_TIMESTAMP));
		registerReceiver(receiver, new IntentFilter(CurrentStatusService.UPDATE_END));

		// register network receiver

		registerReceiver(netWorkReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));

		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.w(TAG, "onPause");

		/*
		 * ********* REMOVE ONCE BITMAP ISSUE RESOLVED *********
		 */
		if (DEBUG_BITMAP_ISSUE) {

			if (serviceIntent != null) {
				stopService(serviceIntent);
				serviceIntent = null;
			}

			unregisterReceiver(receiver);
			unregisterReceiver(netWorkReceiver);
		}

		/*  ******** REMOVE ONCE BITMAP ISSUE RESOLVED ********** */

		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.w(TAG, "onStop");

		progressDialog.hide();
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.w(TAG, "onDestroy");
		if (serviceIntent != null)
			stopService(serviceIntent);

		super.onDestroy();
	}

	// /////////////////////////
	// DIALOGS
	// /////////////////////////
	private void refreshSchedule() {

		if (serviceIntent != null) {
			// Log.w(TAG, "stopService");
			stopService(new Intent(this, service.getClass()));
		}

		serviceIntent = new Intent(this, CurrentStatusService.class);
		service = startService(serviceIntent);
		progressDialog.show();

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case STATUS_DIALOG:
				LayoutInflater li = LayoutInflater.from(this);
				View statusDetailsView = li.inflate(R.layout.status_details, null);

				AlertDialog.Builder statusDialog = new AlertDialog.Builder(this);
				statusDialog.setTitle("Status Update");
				statusDialog.setView(statusDetailsView);
				return statusDialog.create();
		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
			case STATUS_DIALOG:
				AlertDialog statusDialog = (AlertDialog) dialog;
				statusDialog.setTitle(R.string.mta_title_dialog);
				TextView tv = (TextView) statusDialog
						.findViewById(R.id.statusDetailsTextView);
				tv.setText(Html.fromHtml(selectedStatusItem.getStatusText()));
				Linkify.addLinks(tv, Linkify.WEB_URLS);
				break;
		}
	}

	private void addNewStatusItem(Intent intent) {
		// Add the new quake to our list of statusArray.
		StatusItem _statusItem = null;

		if (intent.getStringExtra("header").equals("true")) {

			String cat = intent.getStringExtra("category");
			if (cat.equals("subway"))
				cat = getString(R.string.mta_title_subway);
			if (cat.equals("bus"))
				cat = getString(R.string.mta_title_bus);
			if (cat.equals("BT"))
				cat = getString(R.string.mta_title_bt);
			if (cat.equals("LIRR"))
				cat = getString(R.string.mta_title_lirr);
			if (cat.equals("MetroNorth"))
				cat = getString(R.string.mta_title_metro);

			_statusItem = new StatusItem(cat);
		} else {
			_statusItem = new StatusItem(intent.getStringExtra("line"), intent
					.getStringExtra("status"), intent.getStringExtra("statusTxt"), intent
					.getStringExtra("date"), intent.getStringExtra("time"), intent
					.getStringExtra("category"));
		}
		statusArray.add(_statusItem);
	}

	private void onUpdateTimeStamp(Intent intent) {
		statusArray.clear();
		currentUpdateView.setText(getString(R.string.current_update_text) + " "
				+ intent.getStringExtra("timestamp"));
	}

	public class CurrentStatusReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(CurrentStatusService.UPDATE_NEW_ITEM))
				addNewStatusItem(intent);

			if (intent.getAction().equals(CurrentStatusService.UPDATE_TIMESTAMP))
				onUpdateTimeStamp(intent);

			if (intent.getAction().equals(CurrentStatusService.UPDATE_END)) {
				aa.notifyDataSetChanged();
				progressDialog.dismiss();
			}

		}
	}

	/*
	 * Calls checkNetworkConnectivity() any time a network change occurs to
	 * double check if connection for wifi or mobile exists
	 */
	public class NetworkConnectionReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
				checkNetworkConnectivity();
			} else {
				// Log.w(TAG,
				// "NetworkCOnnectionReceiver Called CONNECTION AVAILABLE");
				if (statusArray.size() == 0)
					refreshSchedule();
			}
		}
	}

	/*
	 * Checks network connectivity if no networks available tell user with Toast
	 * otherwise load data
	 */
	private void checkNetworkConnectivity() {
		ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		NetworkInfo mobileInfo = connectivity
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

		if (wifiInfo.isConnected() || mobileInfo.isConnected()) {
			// Log.w(TAG, "NETWORK CONNECTION AVAILABLE");
			refreshSchedule();
		} else {
			// Log.w(TAG, "NO NETWORK CONNECION");
			Context context = getApplicationContext();
			String msg = "No Network Connection at the moment";
			Toast toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, 0, 0);
			toast.show();
		}
	}
}
