package com.robestone.hudson.compactcolumns;

import java.util.Locale;

import hudson.model.Run;

public class BuildInfo implements Comparable<BuildInfo> {

	private Run<?, ?> run;
	private String color;
	private String underlineStyle;
	private String timeAgoString;
	private long buildTime;
	private String status;
	private String urlPart;
	private boolean isFirst;
	private boolean isLatestBuild;
	private boolean multipleBuilds;
	
	public BuildInfo(Run<?, ?> run, String color, String underlineStyle, String timeAgoString,
			long buildTime, String status, String urlPart,
			boolean isLatestBuild) {
		this.run = run;
		this.color = color;
		this.underlineStyle = underlineStyle;
		this.timeAgoString = timeAgoString;
		this.buildTime = buildTime;
		this.status = status;
		this.urlPart = urlPart;
		this.isLatestBuild = isLatestBuild;
	}
	public Run<?, ?> getRun() {
		return run;
	}
	public String getColor() {
		return color;
	}
	public String getTimeAgoString() {
		return timeAgoString;
	}
	public String getStatus() {
		return status;
	}
	public String getUrlPart() {
		return urlPart;
	}
	public boolean isFirst() {
		return isFirst;
	}
	public boolean isLatestBuild() {
		return isLatestBuild;
	}
	public long getBuildTime() {
		return buildTime;
	}
	
	// ----
	
	public String getLatestBuildString(Locale locale) {
    	if (isLatestBuild) {
    		return " (" + Messages.latestBuild() + ")";
    	} else {
    		return "";
    	}
	}
	public String getStartedAgo(Locale locale) {
		return Messages._startedAgo(timeAgoString).toString(locale);
	}
	public String getBuiltAt(Locale locale) {
		String time = AbstractCompactColumn.getBuildTimeString(buildTime, locale);
		return Messages._builtAt(time).toString(locale);
	}
	public String getLastedDuration(Locale locale) {
		return Messages._lastedDuration(run.getDurationString()).toString(locale);
	}
	public String getFontWeight() {
    	if (isLatestBuild && multipleBuilds) {
    		return "bold";
    	} else {
    		return "normal";
    	}
	}
	public String getUnderlineStyle() {
		return underlineStyle;
	}
	public void setFirst(boolean first) {
		this.isFirst = first;
	}
	public void setMultipleBuilds(boolean multipleBuilds) {
		this.multipleBuilds = multipleBuilds;
	}
	/**
	 * Sort by build number.
	 */
	public int compareTo(BuildInfo that) {
		return new Integer(that.run.number).compareTo(this.run.number);
	}
}