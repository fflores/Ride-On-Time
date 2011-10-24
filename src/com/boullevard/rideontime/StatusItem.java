package com.boullevard.rideontime;

public class StatusItem
{
	//private static final String TAG = "StatusItem";
	
	//For subway line images
	public enum Category { SUBWAY, OTHER };
	
	private String headerName;
	private Boolean headerBool = false;
	private String lineName;
	private String status;
	private String statusText;
	private String date;
	private String time;
	private Category category;

	// line, status, statusText, url, date, time

	public String getLine() {
		return lineName;
	}

	public String getStatus() {
		return status;
	}
	
	public String getStatusText() {
		return statusText;
	}

	public String getTime() {
		return time;
	}

	public String getDate() {
		return date;
	}
	
	public String getHeader() {
		return headerName;
	}

	public Category getCategory() {
		return category;
	}
	
	public Boolean isHeader() {
		return headerBool;
	}
	
	public StatusItem(String _line, String _status, String _statusTxt, String _d, String _t, String _category) {
		lineName = _line;
		status = _status;
		date = _d;
		time = _t;
		
		statusText = "<i>Posted " + date + " " + time + "</i><br/>" + lineName + "<br/>" + status + "<br/><br/>" +  _statusTxt;
		
		if (_category.equals("subway")) {
			category = Category.SUBWAY;
		}else {
			category = Category.OTHER;
		}
	}

	public StatusItem(String _header) {
		headerName = _header;
		headerBool = true;
	}
	
	
	@Override
	public String toString() {
		return lineName + " " + status;
	}
}
