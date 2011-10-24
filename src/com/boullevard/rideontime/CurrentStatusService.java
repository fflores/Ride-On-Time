package com.boullevard.rideontime;

import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class CurrentStatusService extends Service
{
	private static final String TAG = "CurrentStatusService";

	public static final String UPDATE_NEW_ITEM = "Update_New_Item";
	public static final String UPDATE_TIMESTAMP = "Update_TimeStamp";
	public static final String UPDATE_END = "Update_End";

	// private ArrayList<StatusItem> statusArray = new ArrayList<StatusItem>();
	private CurrentStatusLookupTask lastLookup = null;

	@Override
	public void onCreate() {
		Log.w(TAG, "onCreate");
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.w(TAG, "onStart");
		refreshSchedule();
	}

	@Override
	public void onDestroy() {
		Log.w(TAG, "onDestroy");
		super.onDestroy();
		lastLookup.cancel(true);
		lastLookup = null;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.w(TAG, "onBind");
		return null;
	}


	private class CurrentStatusLookupTask extends AsyncTask<Void, Void, Void>
	{

		@Override
		protected Void doInBackground(Void... params) {

			try {
				/** Handling XML */
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXParser sp = spf.newSAXParser();
				XMLReader xr = sp.getXMLReader();

				/** Send URL to parse XML Tags */
				URL sourceUrl = new URL(getString(R.string.mta_feed));

				/** Create handler to handle XML Tags ( extends DefaultHandler ) */
				MyXMLHandler myXMLHandler = new MyXMLHandler();
				xr.setContentHandler(myXMLHandler);
				xr.parse(new InputSource(sourceUrl.openStream()));
				
			} catch (Exception e) {
				//Log.w(TAG, "XML Pasing Excpetion = " + e.getMessage());
			}
			
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//Log.w(TAG, "onPostExecute");
			stopSelf();
			announceUpdateEnd();
		}
	}

	private void refreshSchedule() {
		//Log.w(TAG, "refreshStatusSchedule");
		if (lastLookup == null || lastLookup.getStatus().equals(AsyncTask.Status.FINISHED)) {
			lastLookup = new CurrentStatusLookupTask();
			lastLookup.execute((Void[]) null);
		}
	}

	private void announceNewHeader(String category) {
		Intent intent = new Intent(UPDATE_NEW_ITEM);
		intent.putExtra("category", category);
		intent.putExtra("header", "true");
		sendBroadcast(intent);
		intent = null;
	}


	private void announceNewStatusItem(ArrayList<String> arr) {
		// Add the new quake to our list of statusArray.
		
		Intent intent = new Intent(UPDATE_NEW_ITEM);
		intent.putExtra("line", arr.get(0));
		intent.putExtra("status", arr.get(1));
		intent.putExtra("statusTxt", arr.get(2));
		intent.putExtra("date", arr.get(3));
		intent.putExtra("time", arr.get(4));
		intent.putExtra("category", arr.get(5));
		intent.putExtra("header", "false");
		sendBroadcast(intent);
		intent = null;
	}

	private void announceTimeStamp(String currentTimeStamp) {
		Intent intent = new Intent(UPDATE_TIMESTAMP);
		intent.putExtra("timestamp", currentTimeStamp);
		sendBroadcast(intent);
		intent = null;
	}

	private void announceUpdateEnd() {
		Intent intent = new Intent(UPDATE_END);
		sendBroadcast(intent);
		intent = null;
	}
	
	public class MyXMLHandler extends DefaultHandler
	{
		private static final String TAG = "MyXMLHandler";
		String currentValue = null;
		String currentCategory = null;
		String buffer = "";
		String currentQName = null;
		ArrayList<String> arrayList = new ArrayList<String>();
		
		Pattern pattern;
	   
	   @Override
	   public void startDocument() throws SAXException {
			 pattern = Pattern.compile("000000");
		} 
	   
	   @Override
	   public void endDocument() throws SAXException {
	       // Some sort of finishing up work
	   	currentValue = null;
			currentCategory = null;
			buffer = "";
			currentQName = null;
			arrayList = null;
	   } 
	   
	   @Override
	   public void startElement(String namespaceURI, String localName, String qName, 
	           Attributes atts) throws SAXException {
	   	
	   	if (localName.equals("subway") || localName.equals("bus") || localName.equals("BT")
	   		|| localName.equals("LIRR") || localName.equals("MetroNorth")) {
	   		announceNewHeader(localName);
	   		currentCategory = localName;
	   	}
	   	
	   	if(localName.equals("text"))
	   		buffer = "";
	   } 
	   
	   @Override
	   public void characters(char ch[], int start, int length) {
	       currentValue = new String(ch, start, length);
	       buffer += currentValue;
	   } 
	   
	   @Override
	   public void endElement(String namespaceURI, String localName, String qName) 
	   throws SAXException {
	   	if(localName.equals("timestamp"))
	   		announceTimeStamp(currentValue);
	   	
	   	if(localName.equals("name") || localName.equals("status") ||
	   		localName.equals("Date") || localName.equals("Time")) {
	   		arrayList.add(currentValue);
	   	}
	   	
	   	if (localName.equals("text")) {
	   		Matcher matcher = pattern.matcher(buffer);
				
				if(matcher.find()) {
					String output = matcher.replaceAll("ffffff");
					arrayList.add(output);					
				}else {
					arrayList.add(buffer);
				}
			}
	   	
	   	if (localName.equals("line")) {
	   		arrayList.add(currentCategory);
	   		announceNewStatusItem(arrayList);
	   		arrayList.clear();
		   }
	   }
	}
}
