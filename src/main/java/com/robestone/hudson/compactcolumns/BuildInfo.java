package com.robestone.hudson.compactcolumns;

import hudson.model.Run;
import hudson.util.ColorPalette;
import java.awt.Color;
import java.util.Locale;

/** @author jacob robertson */
public class BuildInfo implements Comparable<BuildInfo> {

  /** Orange is yellowish, and shows up better on the webpage. */
  public static final String UNSTABLE_COLOR = toColorString(new Color(255, 165, 0));

  private static String STABLE_COLOR;
  public static final String OTHER_COLOR = toColorString(ColorPalette.GREY);
  public static final String FAILED_COLOR = toColorString(ColorPalette.RED);

  /** Work-around to check whether the palette has been changed. */
  private static final Color BLUE_FROM_PALETTE = new Color(0x72, 0x9F, 0xCF);

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

  public BuildInfo(
      Run<?, ?> run,
      String color,
      String underlineStyle,
      long buildTime,
      String status,
      String urlPart,
      boolean isLatestBuild) {
    this.run = run;
    this.color = color;
    this.underlineStyle = underlineStyle;
    this.buildTime = buildTime;
    this.status = status;
    this.urlPart = urlPart;
    this.isLatestBuild = isLatestBuild;
  }

  public static String getUnstableColorString() {
    return UNSTABLE_COLOR;
  }

  public static String getFailedColorString() {
    return FAILED_COLOR;
  }

  public static String getStableColorString() {
    if (STABLE_COLOR == null) {
      STABLE_COLOR = toColorString(getStableColor());
    }
    return STABLE_COLOR;
  }

  static Color getStableColor() {
    // determine whether to use the Jenkins palette or our own
    boolean isPaletteStandard = BLUE_FROM_PALETTE.equals(ColorPalette.BLUE);
    if (isPaletteStandard) {
      // we don't like the standard palette for blue, so we return a "better blue"
      return Color.BLUE;
    } else {
      // since someone has changed the palette under the covers, we will honor that
      return ColorPalette.BLUE;
    }
  }

  static String toColorString(Color color) {
    StringBuilder buf = new StringBuilder("#");
    try {
      String hex = Integer.toHexString(color.getRGB() & 0x00ffffff);
      String zeroes = "000000";
      int len = zeroes.length() - hex.length();
      buf.append(zeroes.substring(0, len));
      buf.append(hex);
    } catch (Throwable t) {
      buf.append(t.getMessage());
    }
    return buf.toString();
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
    return new Integer(that.run.number).compareTo(this.run.number);
  }

  public String getTextDecoration() {
    if (underlineStyle == null) {
      return "underline";
    } else {
      return "none";
    }
  }
}
