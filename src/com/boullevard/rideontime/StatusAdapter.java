package com.boullevard.rideontime;

import java.util.List;

import com.boullevard.rideontime.StatusItem.Category;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StatusAdapter extends ArrayAdapter<StatusItem>
{
	private static final String TAG = "StatusAdapter";

	int resource;

	public StatusAdapter(Context context, int _resource, List<StatusItem> objects) {
		super(context, _resource, objects);
		// TODO Auto-generated constructor stub
		resource = _resource;

		//Log.w(TAG, "StatusAdapter init");
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		StatusItem item = getItem(position);
		String status = item.getStatus();

		// If status is null then assumption is it's a header or
		// if "GOOD SERVICE" should be disabled
		if (item.isHeader() || status.equals("GOOD SERVICE"))
			return false;
		else
			return true;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//Log.w(TAG, "getView : " + Integer.toString(position));
		
		ViewHolder holder;
		StatusItem item = getItem(position);

		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			convertView = vi.inflate(resource, null);

			holder = new ViewHolder();
			holder.lineTextView = (TextView) convertView.findViewById(R.id.line_id);
			holder.lineImageView = (ImageView) convertView.findViewById(R.id.line_id_image);
			holder.statusView = (TextView) convertView.findViewById(R.id.status_id);
			holder.headerView = (TextView) convertView.findViewById(R.id.header);
			convertView.setTag(holder);
			
			holder.statusView.setTextColor(convertView.getResources().getColor(R.color.red));
			
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		
		// Check header
		if (item.isHeader()) {
			String header = item.getHeader();
			initHeader(holder, header);
		}else {
			String line = item.getLine();
			String status = item.getStatus();
			initTransitItem(holder, item, line, status, convertView);
		}
		
		return convertView;
	}
	
	private void initHeader(ViewHolder holder, String header) {
		holder.headerView.setVisibility(TextView.VISIBLE);
		holder.headerView.setText(header);

		holder.lineImageView.setVisibility(TextView.GONE);
		holder.lineTextView.setVisibility(TextView.GONE);
		holder.statusView.setVisibility(TextView.GONE);
	}
	
	private void initTransitItem(ViewHolder holder, StatusItem item, String line, String status, View convertView) {
		holder.headerView.setVisibility(TextView.GONE);
		Resources resource = convertView.getResources();
		
		Category category = item.getCategory();
		switch (category) {
			case SUBWAY:
				String resID = "subway_" + line.toLowerCase();
				int resources = resource.getIdentifier(resID, "drawable", "com.boullevard.rideontime");
				holder.lineImageView.setImageResource(resources);
				holder.lineImageView.setVisibility(TextView.VISIBLE);
				holder.lineTextView.setVisibility(TextView.INVISIBLE);
				break;
			default:
				holder.lineImageView.setVisibility(TextView.GONE);
				holder.lineTextView.setVisibility(TextView.VISIBLE);
				holder.lineTextView.setText(line);
				break;
		}

		holder.statusView.setVisibility(TextView.VISIBLE);
		holder.statusView.setText(status);
		
		//Set text color for different status
		if (status.equals(resource.getString(R.string.mta_status_good)))
			holder.statusView.setTextColor(resource.getColor(R.color.green));
		
		if(status.equals(resource.getString(R.string.mta_status_delay)) || status.equals(resource.getString(R.string.mta_status_planned))) 
			holder.statusView.setTextColor(resource.getColor(R.color.red));
		
		if(status.equals(resource.getString(R.string.mta_status_service)))
			holder.statusView.setTextColor(resource.getColor(R.color.orange));
	}
	
	static class ViewHolder
	{
		TextView lineTextView;
		ImageView lineImageView;
		TextView statusView;
		TextView headerView;
	}
}
