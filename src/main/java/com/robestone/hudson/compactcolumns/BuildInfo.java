package com.robestone.hudson.compactcolumns;

import hudson.model.Run;
import java.util.Locale;
import java.util.Objects;

public class BuildInfo implements Comparable<BuildInfo> {

    private static final String STABLE_COLOR = "var(--success-color, green) !important";
    private static final String UNSTABLE_COLOR = "var(--warning-color, orange) !important";
    private static final String FAILED_COLOR = "var(--error-color, red) !important";
    private static final String OTHER_COLOR = "var(--text-color-secondary, grey) !important";

    private final Run<?, ?> run;
    private String color;
    private String underlineStyle;
    private String timeAgoString;
    private long buildTime;
    private String status;
    private String urlPart;
    private boolean isFirst;
    private boolean isLatestBuild;
    private boolean multipleBuilds;

    public BuildInfo(
            Run<?, ?> run,
            String color,
            String underlineStyle,
            long buildTime,
            String status,
            String urlPart,
            boolean isLatestBuild) {
        this.run = Objects.requireNonNull(run, "BuildInfo needs a run");
        this.color = color;
        this.underlineStyle = underlineStyle;
        this.buildTime = buildTime;
        this.status = status;
        this.urlPart = urlPart;
        this.isLatestBuild = isLatestBuild;
    }

    public static String getStableColor() {
        return STABLE_COLOR;
    }

    public static String getUnstableColor() {
        return UNSTABLE_COLOR;
    }

    public static String getFailedColor() {
        return FAILED_COLOR;
    }

    public static String getOtherColor() {
        return OTHER_COLOR;
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

    public boolean isMultipleBuilds() {
        return multipleBuilds;
    }

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
        String time = AbstractStatusesColumn.getBuildTimeString(buildTime, locale);
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
        if (underlineStyle == null) {
            return "0px";
        }
        return underlineStyle;
    }

    public void setFirst(boolean first) {
        this.isFirst = first;
    }

    public void setMultipleBuilds(boolean multipleBuilds) {
        this.multipleBuilds = multipleBuilds;
    }

    public void setTimeAgoString(String timeAgoString) {
        this.timeAgoString = timeAgoString;
    }
    /** Sort by build number. */
    public int compareTo(BuildInfo that) {
        return Integer.compare(that.run.number, this.run.number);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuildInfo buildInfo = (BuildInfo) o;
        return run.number == buildInfo.run.number;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(run.number);
    }

    public String getTextDecoration() {
        if (underlineStyle == null) {
            return "underline";
        } else {
            return "none";
        }
    }
}
