/* SPDX-License-Identifier: MIT
 * SPDX-FileCopyrightText: © 2009, Sun Microsystems, Inc., Jesse Glick
 * SPDX-FileCopyrightText: © 2024 Tobias Gruetzmacher
 */
package com.robestone.hudson.compactcolumns;

import static org.assertj.core.api.Assertions.assertThat;

import com.robestone.hudson.compactcolumns.AbstractStatusesColumn.TimeAgoType;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class CompactColumnsTest {

    // Unicode Character 'NARROW NO-BREAK SPACE' (U+202F)
    private static final char NARROW_NBSP = '\u202F';
    private static final TimeZone SYSTEM_TIMEZONE = TimeZone.getDefault();
    private static final TimeZone TEST_TIMEZONE = TimeZone.getTimeZone("GMT-5:00");
    private static final Locale SYSTEM_LOCALE = Locale.getDefault();
    private static final long TEST_TIME = 1277416568304L;
    private static final LocalDateTime TEST_LOCALTIME =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), TEST_TIMEZONE.toZoneId());

    @BeforeEach
    void setSystemSettings() {
        TimeZone.setDefault(TEST_TIMEZONE);
        Locale.setDefault(Locale.US);
    }

    @AfterEach
    void resetSystemSettings() {
        TimeZone.setDefault(SYSTEM_TIMEZONE);
        Locale.setDefault(SYSTEM_LOCALE);
    }

    @Test
    void dateFormats() {
        assertThat(Locale.US).extracting(this::formatDate).isEqualTo("6/24/2010");
        assertThat(Locale.GERMAN).extracting(this::formatDate).isEqualTo("24.06.2010");
    }

    private String formatDate(Locale locale) {
        return AbstractStatusesColumn.formatDate(TEST_LOCALTIME, locale);
    }

    @Test
    void dateTimeFormats() {
        assertThat(formatDateTime(Locale.US))
                .containsAnyOf("6/24/2010, 4:56" + NARROW_NBSP + "PM", "6/24/2010, 4:56 PM");
        assertThat(formatDateTime(Locale.GERMAN)).containsAnyOf("24.06.2010 16:56", "24.06.2010, 16:56");
    }

    private String formatDateTime(Locale locale) {
        return AbstractStatusesColumn.formatDateTime(TEST_LOCALTIME, locale);
    }

    /** Shows that all locale handling will be okay. */
    @Test
    void noBadLocale() {
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            String s = AbstractStatusesColumn.getBuildTimeString(TEST_TIME, locale);
            assertThat(s).isNotNull();
        }
    }

    @Test
    void showDate() {
        doTestShowDate(Locale.GERMAN, "16:56", "24.06.2010");
        doTestShowDate(Locale.US, "4:56 PM", "6/24/2010");
    }

    private void doTestShowDate(Locale locale, String expectTime, String expectDate) {
        String expectDateTime = expectTime + ", " + expectDate;
        Set<String> expectDateTimeLong = new HashSet<>();
        for (String sep : Arrays.asList(" ", ", ")) {
            expectDateTimeLong.add(expectDate + sep + expectTime);
            if (expectTime.contains(" PM")) {
                expectDateTimeLong.add(expectDate + sep + expectTime.replace(' ', NARROW_NBSP));
            }
        }

        String ago;

        ago = AbstractStatusesColumn.getTimeAgoString(locale, TEST_TIME, false, TimeAgoType.PREFER_DATE_TIME);
        assertThat(ago).containsAnyOf(expectDateTimeLong.toArray(String[]::new));

        ago = AbstractStatusesColumn.getTimeAgoString(locale, TEST_TIME, true, TimeAgoType.PREFER_DATE_TIME);
        assertThat(ago).isEqualTo(expectDate);

        ago = AbstractStatusesColumn.getTimeAgoString(locale, TEST_TIME, true, TimeAgoType.PREFER_DATES);
        assertThat(ago).isEqualTo(expectDate);

        // can't easily test this format with current API, but can do a negative test
        ago = AbstractStatusesColumn.getTimeAgoString(locale, TEST_TIME, false, TimeAgoType.DIFF);
        assertThat(ago).isNotEqualTo(expectDate);
        assertThat(ago).isNotEqualTo(expectDateTime);
        assertThat(ago).isNotEqualTo(expectTime);
    }

    @Test
    void localizeDate() {
        assertThat(formatBuildTimeString(Locale.ENGLISH))
                .containsAnyOf("4:56" + NARROW_NBSP + "PM, 6/24/2010", "4:56 PM, 6/24/2010");
        assertThat(formatBuildTimeString(Locale.GERMAN)).isEqualTo("16:56, 24.06.2010");
        assertThat(formatBuildTimeString(Locale.CANADA))
                .containsAnyOf("4:56" + NARROW_NBSP + "p.m., 2010-06-24", "4:56 p.m., 2010-06-24");
    }

    private String formatBuildTimeString(Locale locale) {
        return AbstractStatusesColumn.getBuildTimeString(TEST_TIME, locale);
    }

    /** This just shows the weird way the Hudson.util is working. */
    @Test
    void time() {
        doTestTime(0, "0 sec");
        doTestTime(500, "0 sec");
        doTestTime(999, "0 sec");
        doTestTime(1000, "1 sec");
        doTestTime(1001, "1 sec");
        doTestTime(1500, "1.5 sec");
        doTestTime(1730, "1.7 sec");
        doTestTime(17300, "17 sec");
        doTestTime(1999, "2 sec");
        doTestTime(66000, "1.1 min");
        doTestTime(60000, "1 min");
        doTestTime(360000, "6 min");
        doTestTime(LastSuccessAndFailedColumn.ONE_DAY_MS + 1, "1 day");
        doTestTime(LastSuccessAndFailedColumn.ONE_DAY_MS * 1.5f, "1.5 days");
        doTestTime(LastSuccessAndFailedColumn.ONE_DAY_MS * 10.5f, "10 days");
    }

    private void doTestTime(float diff, String expect) {
        String found = AbstractStatusesColumn.getShortTimestamp(diff);
        assertThat(found).isEqualTo(expect);
    }

    @Test
    @Disabled("Broken on newer versions of Jenkins, because this test tries to work with subclasses of Job and Run")
    void getBuilds() {
        doTestBuilds("SSFFUFUS", "SU", "SF", "SFU");
        doTestBuilds("FSSFFUFUS", "FSU", "FS", "FSU");
        doTestBuilds("FSSFF", "FS", "FS", "FS");
        doTestBuilds("FSS", "FS", "FS", "FS");
        doTestBuilds("F", "F", "F", "F");
        doTestBuilds("FFF", "F", "F", "F");
        doTestBuilds("SFFF", "S", "SF", "SF");
        doTestBuilds("UFF", "U", "UF", "UF");
        doTestBuilds("FFUU", "FU", "F", "FU");
        doTestBuilds("USF", "US", "USF", "USF");
        doTestBuilds("AAUSFAA", "US", "SF", "USF");
        doTestBuilds("USAF", "US", "USF", "USF");
        doTestBuilds("A", "A", "A", "A");
    }

    private void doTestBuilds(
            String buildsSpec,
            String expectForLastStableAndUnstable,
            String expectForLastSuccessAndFailed,
            String expectForAllStatuses) {
        doTestBuilds(buildsSpec, expectForLastStableAndUnstable, new LastStableAndUnstableColumn());
        doTestBuilds(buildsSpec, expectForLastSuccessAndFailed, new LastSuccessAndFailedColumn());
        doTestBuilds(buildsSpec, expectForAllStatuses, new AllStatusesColumn(null, false, null, 0));
    }

    /**
     * @param buildsSpec most recent build first
     * @param expectToShow most recent status first
     */
    private void doTestBuilds(String buildsSpec, String expectToShow, AbstractStatusesColumn col) {
        TestJob job = new TestJob();
        TestRun previous = null;
        long time = 1000;
        for (int i = 0; i < buildsSpec.length(); i++) {
            char c = buildsSpec.charAt(i);
            Result result = null;
            switch (c) {
                case 'S':
                    result = Result.SUCCESS;
                    break;
                case 'U':
                    result = Result.UNSTABLE;
                    break;
                case 'F':
                    result = Result.FAILURE;
                    break;
                case 'A':
                    result = Result.ABORTED;
                    break;
            }
            TestRun run = new TestRun(job, time - i, result);
            job.addRun(run);
            if (previous != null) {
                previous.setPrevious(run);
            }
            previous = run;
        }
        assertThat(job._getRuns().firstKey()).hasToString(String.valueOf(-time));
        List<BuildInfo> builds = col.getBuilds(job, Locale.US);
        assertThat(builds).hasSize(expectToShow.length());
        for (int i = 0; i < builds.size(); i++) {
            char c = expectToShow.charAt(i);
            BuildInfo build = builds.get(i);
            switch (c) {
                case 'S':
                    assertThat(build.getStatus()).isEqualTo("Stable");
                    break;
                case 'U':
                    assertThat(build.getStatus()).isEqualTo("Unstable");
                    break;
                case 'F':
                    assertThat(build.getStatus()).isEqualTo("Failed");
                    break;
                case 'A':
                    assertThat(build.getStatus()).isEqualTo("Aborted");
                    break;
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static class TestRun extends Run {
        public TestRun(Job job, long timestamp, Result result) {
            super(job, timestamp);
            this.number = (int) timestamp;
            this.result = result;
        }

        public void setPrevious(Run previous) {
            this.previousBuild = previous;
        }

        @Override
        public boolean isBuilding() {
            return false;
        }

        @Override
        public String toString() {
            return "tj:" + number;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static class TestJob extends Job {
        private SortedMap runs = new TreeMap();

        public TestJob() {
            super(null, null);
        }

        public void addRun(Run run) {
            runs.put(-run.number, run);
        }

        @Override
        protected SortedMap _getRuns() {
            return runs;
        }

        @Override
        public boolean isBuildable() {
            return false;
        }

        @Override
        protected void removeRun(Run run) {}
    }
}
