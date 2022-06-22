/*
 * The MIT License
 *
 * Copyright (c) 2009, Sun Microsystems, Inc., Jesse Glick
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.robestone.hudson.compactcolumns;

import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.views.ListViewColumnDescriptor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.chrono.Chronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** @author jacob robertson */
public abstract class AbstractStatusesColumn extends AbstractCompactColumn {

  public static final String OTHER_UNDERLINE_STYLE = "1px dashed";
  public static final String UNSTABLE_UNDERLINE_STYLE = "1px dashed";
  public static final String STABLE_UNDERLINE_STYLE = "0px solid";
  public static final String FAILED_UNDERLINE_STYLE = "1px solid";

  // copied from hudson.Util because they were private
  private static final long ONE_SECOND_MS = 1000;
  private static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;
  private static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;
  static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;
  private static final long ONE_MONTH_MS = 30 * ONE_DAY_MS;
  private static final long ONE_YEAR_MS = 365 * ONE_DAY_MS;
  private transient TimeAgoType timeAgoType;
  private String timeAgoTypeString;

  public AbstractStatusesColumn(String colorblindHint, String timeAgoTypeString) {
    super(colorblindHint);
    this.timeAgoTypeString = timeAgoTypeString;
    setTimeAgoType();
  }

  public static List<BuildInfo> getBuilds(
      Job<?, ?> job,
      Locale locale,
      boolean isFailedShownOnlyIfLast,
      boolean isUnstableShownOnlyIfLast,
      boolean isOnlyShowLastStatus,
      boolean isShowColorblindUnderlineHint,
      TimeAgoType timeAgoType,
      int hideDays) {
    List<BuildInfo> builds = new ArrayList<BuildInfo>();

    addNonNull(
        builds,
        getLastFailedBuild(
            job,
            locale,
            isFailedShownOnlyIfLast,
            isShowColorblindUnderlineHint,
            true,
            timeAgoType));
    addNonNull(
        builds,
        getLastUnstableBuild(
            job,
            locale,
            isUnstableShownOnlyIfLast,
            isShowColorblindUnderlineHint,
            builds.isEmpty(),
            timeAgoType));
    addNonNull(
        builds,
        getLastStableBuild(
            job, locale, isShowColorblindUnderlineHint, builds.isEmpty(), timeAgoType));

    if (builds.isEmpty()) {
      BuildInfo aborted =
          createBuildInfo(
              getLastAbortedBuild(job),
              BuildInfo.getOtherColor(),
              OTHER_UNDERLINE_STYLE,
              getAbortedMessage(),
              null,
              job,
              locale,
              isShowColorblindUnderlineHint,
              true,
              timeAgoType);
      addNonNull(builds, aborted);
    }

    Collections.sort(builds);

    List<BuildInfo> filtered = new ArrayList<BuildInfo>();
    long now = System.currentTimeMillis();
    long maxDiff = hideDays * ONE_DAY_MS;

    for (int i = 0; i < builds.size(); i++) {
      BuildInfo info = builds.get(i);
      boolean show = true;
      if (hideDays > 0) {
        long time = info.getBuildTime();
        long diff = now - time;
        show = (diff <= maxDiff);
      }
      if (filtered.isEmpty() || show) {
        filtered.add(info);
        if (isOnlyShowLastStatus) {
          break;
        }
      }
    }

    builds = filtered;
    for (int i = 0; i < builds.size(); i++) {
      BuildInfo info = builds.get(i);
      info.setFirst(i == 0);
      info.setMultipleBuilds(builds.size() > 1);
      assignTimeAgoString(info, locale, timeAgoType);
    }

    return builds;
  }

  /**
   * @param onlyIfLastCompleted When the statuses aren't sorted, we only show the last failed when
   *     it is also the latest completed build.
   */
  public static BuildInfo getLastFailedBuild(
      Job<?, ?> job,
      Locale locale,
      boolean onlyIfLastCompleted,
      boolean isShowColorblindUnderlineHint,
      boolean isFirst,
      TimeAgoType timeAgoType) {
    Run<?, ?> lastFailedBuild = job.getLastFailedBuild();
    Run<?, ?> lastCompletedBuild = job.getLastCompletedBuild();
    if (lastFailedBuild == null) {
      return null;
    } else if (!onlyIfLastCompleted || (lastCompletedBuild.number == lastFailedBuild.number)) {
      return createBuildInfo(
          job.getLastFailedBuild(),
          BuildInfo.getFailedColor(),
          FAILED_UNDERLINE_STYLE,
          getFailedMessage(),
          "lastFailedBuild",
          job,
          locale,
          isShowColorblindUnderlineHint,
          isFirst,
          timeAgoType);
    } else {
      return null;
    }
  }

  public static BuildInfo getLastStableBuild(
      Job<?, ?> job,
      Locale locale,
      boolean isShowColorblindUnderlineHint,
      boolean isFirst,
      TimeAgoType timeAgoType) {
    return createBuildInfo(
        job.getLastStableBuild(),
        BuildInfo.getStableColor(),
        STABLE_UNDERLINE_STYLE,
        getStableMessage(),
        "lastStableBuild",
        job,
        locale,
        isShowColorblindUnderlineHint,
        isFirst,
        timeAgoType);
  }

  public static BuildInfo getLastUnstableBuild(
      Job<?, ?> job,
      Locale locale,
      boolean isUnstableShownOnlyIfLast,
      boolean isShowColorblindUnderlineHint,
      boolean isFirst,
      TimeAgoType timeAgoType) {
    Run<?, ?> lastUnstable = job.getLastUnstableBuild();
    if (lastUnstable == null) {
      return null;
    }

    Run<?, ?> lastCompleted = job.getLastCompletedBuild();
    boolean isLastCompleted =
        (lastCompleted != null && lastCompleted.number == lastUnstable.number);
    if (isUnstableShownOnlyIfLast && !isLastCompleted) {
      return null;
    }

    return createBuildInfo(
        lastUnstable,
        BuildInfo.getUnstableColor(),
        UNSTABLE_UNDERLINE_STYLE,
        getUnstableMessage(),
        String.valueOf(lastUnstable.number),
        job,
        locale,
        isShowColorblindUnderlineHint,
        isFirst,
        timeAgoType);
  }

  private static void addNonNull(List<BuildInfo> builds, BuildInfo info) {
    if (info != null) {
      builds.add(info);
    }
  }

  private static Run<?, ?> getLastAbortedBuild(Job<?, ?> job) {
    Run<?, ?> latest = job.getLastBuild();
    int i = 0;
    while (latest != null && i++ < 20) {
      if (latest.getResult() == Result.ABORTED) {
        return latest;
      }
      latest = latest.getPreviousBuild();
    }
    return null;
  }

  private static void assignTimeAgoString(BuildInfo info, Locale locale, TimeAgoType timeAgoType) {
    String timeAgoString =
        getTimeAgoString(locale, info.getBuildTime(), info.isMultipleBuilds(), timeAgoType);
    info.setTimeAgoString(timeAgoString);
  }

  private static BuildInfo createBuildInfo(
      Run<?, ?> run,
      String color,
      String underlineStyle,
      String status,
      String urlPart,
      Job<?, ?> job,
      Locale locale,
      boolean isShowColorblindUnderlineHint,
      boolean isFirst,
      TimeAgoType timeAgoType) {
    if (run != null) {
      long buildTime = run.getTime().getTime();
      if (urlPart == null) {
        urlPart = String.valueOf(run.number);
      }
      Run<?, ?> latest = job.getLastCompletedBuild();
      if (latest == null) {
        latest = job.getLastBuild();
      }
      if (!isShowColorblindUnderlineHint) {
        underlineStyle = null;
      }
      BuildInfo build =
          new BuildInfo(
              run, color, underlineStyle, buildTime, status, urlPart, run.number == latest.number);
      return build;
    }
    return null;
  }

  protected static String getTimeAgoString(
      Locale locale, long timestamp, boolean isMultiple, TimeAgoType timeAgoType) {
    if (timeAgoType == TimeAgoType.DIFF) {
      long now = System.currentTimeMillis();
      float diff = now - timestamp;
      String stime = getShortTimestamp(diff);
      return stime;
    } else {
      if (timeAgoType == TimeAgoType.PREFER_DATE_TIME && !isMultiple) {
        return getBuildTimeString(timestamp, locale, true, true, true);
      } else {
        Calendar nowCal = Calendar.getInstance();
        nowCal.setTimeInMillis(System.currentTimeMillis());
        Calendar thenCal = Calendar.getInstance();
        thenCal.setTimeInMillis(timestamp);

        int nowDay = nowCal.get(Calendar.DAY_OF_YEAR);
        int thenDay = thenCal.get(Calendar.DAY_OF_YEAR);

        boolean isToday = (nowDay == thenDay);
        if (isToday) {
          return getBuildTimeString(timestamp, locale, false, true, false);
        } else {
          return getBuildTimeString(timestamp, locale, true, false, false);
        }
      }
    }
  }

  protected static String getBuildTimeString(long timeMs, Locale locale) {
    return getBuildTimeString(timeMs, locale, true, true, false);
  }

  protected static String getBuildTimeString(
      long timeMs, Locale locale, boolean addDate, boolean addTime, boolean useDefaultFormat) {
    // FIXME: System time zone vs. browser time zone
    LocalDateTime time =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(timeMs), ZoneId.systemDefault());

    if (addTime && addDate && useDefaultFormat) {
      return formatDateTime(time, locale);
    } else {
      StringBuilder buf = new StringBuilder();
      if (addTime) {
        buf.append(formatTime(time, locale));
      }
      if (addDate) {
        if (buf.length() > 0) {
          buf.append(", ");
        }
        buf.append(formatDate(time, locale));
      }
      return buf.toString();
    }
  }

  /** I want to use 4-digit years (for clarity), and that doesn't work out of the box... */
  protected static String formatDate(TemporalAccessor date, Locale locale) {
    return getFormatter(Chronology.from(date), locale, FormatStyle.SHORT, null).format(date);
  }

  protected static String formatDateTime(TemporalAccessor date, Locale locale) {
    return getFormatter(Chronology.from(date), locale, FormatStyle.SHORT, FormatStyle.SHORT)
        .format(date);
  }

  protected static String formatTime(TemporalAccessor date, Locale locale) {
    return getFormatter(Chronology.from(date), locale, null, FormatStyle.SHORT).format(date);
  }

  private static DateTimeFormatter getFormatter(
      Chronology chronology, Locale locale, FormatStyle dateStyle, FormatStyle timeStyle) {
    String pattern =
        DateTimeFormatterBuilder.getLocalizedDateTimePattern(
            dateStyle, timeStyle, chronology, locale);
    if (!pattern.contains("yyyy")) {
      pattern = pattern.replace("yy", "yyyy");
    }
    return new DateTimeFormatterBuilder().appendPattern(pattern).toFormatter(locale);
  }

  /**
   * Avoids having "2 days 3 hours" and instead does "2.1 days".
   *
   * <p>Additional strategy details:
   *
   * <ul>
   *   <li>&lt; 1 sec = 0 sec
   *   <li>&lt; 10 of anything = x.y of that (scale 1)
   *   <li>&gt;= 10 of anything = x (scale 0)
   * </ul>
   */
  protected static String getShortTimestamp(float time) {
    String ts;
    float number;
    if (time >= ONE_YEAR_MS) {
      number = getRoundedNumber(time / ONE_YEAR_MS);
      ts = Messages.util_year(number);
    } else if (time >= ONE_MONTH_MS) {
      number = getRoundedNumber(time / ONE_MONTH_MS);
      ts = Messages.util_month(number);
    } else if (time >= ONE_DAY_MS) {
      number = getRoundedNumber(time / ONE_DAY_MS);
      ts = Messages.util_day(number);
    } else if (time >= ONE_HOUR_MS) {
      number = getRoundedNumber(time / ONE_HOUR_MS);
      ts = Messages.util_hour(number);
    } else if (time >= ONE_MINUTE_MS) {
      number = getRoundedNumber(time / ONE_MINUTE_MS);
      ts = Messages.util_minute(number);
    } else if (time >= ONE_SECOND_MS) {
      number = getRoundedNumber(time / ONE_SECOND_MS);
      ts = Messages.util_second(number);
    } else {
      ts = Messages.util_second(0);
    }
    return ts;
  }

  protected static float getRoundedNumber(float number) {
    int scale;
    if (number >= 10) {
      scale = 0;
    } else {
      scale = 1;
    }
    return new BigDecimal(number).setScale(scale, BigDecimal.ROUND_HALF_DOWN).floatValue();
  }

  public static final String getAbortedMessage() {
    return Messages.ballColor_Aborted();
  }

  public static final String getFailedMessage() {
    return Messages.ballColor_Failed();
  }

  public static final String getUnstableMessage() {
    return Messages.ballColor_Unstable();
  }

  public static final String getBuildDescriptionToolTip(BuildInfo build, Locale locale) {
    StringBuilder buf = new StringBuilder();
    buf.append("<b><u>");
    buf.append(Messages.buildNumber());
    buf.append(build.getRun().number);
    buf.append(build.getLatestBuildString(locale));
    buf.append("</u></b>\n");
    buf.append(
        "<ul class=\"jenkins-!-margin-0 jenkins-!-margin-top-1 jenkins-!-padding-0\" style=\"list-style-position: inside\">");
    buf.append("<li>");
    buf.append(build.getBuiltAt(locale));
    buf.append("</li>");
    buf.append("<li>");
    buf.append(build.getStartedAgo(locale));
    buf.append("</li>");
    buf.append("<li>");
    buf.append(build.getLastedDuration(locale));
    buf.append("</li>");
    buf.append("<li><b>");
    buf.append(build.getStatus());
    buf.append("</b></li>");
    buf.append("</ul>");
    return buf.toString();
  }

  public static final String getStableMessage() {
    String message = Messages.run_summary_stable();
    if (message != null && message.length() > 1) {
      // this logic is here solely so I can re-use the "stable" messages, but make it capitalized
      char c = message.charAt(0);
      if (Character.isLowerCase(c)) {
        c = Character.toUpperCase(c);
        message = c + message.substring(1);
      }
    }
    return message;
  }

  Object readResolve() {
    setTimeAgoType();
    return this;
  }

  private void setTimeAgoType() {
    if (timeAgoTypeString == null) {
      timeAgoTypeString = TimeAgoType.DIFF.toString();
    }
    timeAgoType = TimeAgoType.valueOf(timeAgoTypeString);
  }

  public String getColumnSortData(Job<?, ?> job) {
    List<BuildInfo> builds = getBuilds(job, Locale.getDefault());
    if (builds.isEmpty()) {
      return "0";
    }
    BuildInfo latest = builds.get(0);
    return String.valueOf(latest.getBuildTime());
  }

  public int getHideDays() {
    return 0;
  }

  public boolean isBuildsEmpty(Job<?, ?> job) {
    // TODO -- make much more efficient
    return getBuilds(job, Locale.getDefault()).isEmpty();
  }

  public List<BuildInfo> getBuilds(Job<?, ?> job, Locale locale) {
    return getBuilds(
        job,
        locale,
        isFailedShownOnlyIfLast(),
        isUnstableShownOnlyIfLast(),
        isOnlyShowLastStatus(),
        isShowColorblindUnderlineHint(),
        timeAgoType,
        getHideDays());
  }

  protected abstract boolean isFailedShownOnlyIfLast();

  protected abstract boolean isUnstableShownOnlyIfLast();

  public boolean isOnlyShowLastStatus() {
    return false;
  }

  public final String getToolTip(BuildInfo build, Locale locale) {
    return getBuildDescriptionToolTip(build, locale);
  }

  public String getTimeAgoTypeString() {
    return timeAgoTypeString;
  }

  public static enum TimeAgoType {
    DIFF,
    PREFER_DATES,
    PREFER_DATE_TIME
  }

  public abstract static class AbstractCompactColumnDescriptor extends ListViewColumnDescriptor {
    @Override
    public boolean shownByDefault() {
      return false;
    }
  }
}
