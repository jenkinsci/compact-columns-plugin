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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.robestone.hudson.compactcolumns.AbstractStatusesColumn.TimeAgoType;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CompactColumnsTest {

    private static final TimeZone SYSTEM_TIMEZONE = TimeZone.getDefault();
    private static final TimeZone TEST_TIMEZONE = TimeZone.getTimeZone("GMT-5:00");
    private static final Locale SYSTEM_LOCALE = Locale.getDefault();
    private static final long TEST_TIME = 1277416568304L;
    private static final LocalDateTime TEST_LOCALTIME =
            LocalDateTime.ofInstant(Instant.ofEpochMilli(TEST_TIME), TEST_TIMEZONE.toZoneId());

    @Before
    public void setSystemSettings() {
        TimeZone.setDefault(TEST_TIMEZONE);
        Locale.setDefault(Locale.US);
    }

    @After
    public void resetSystemSettings() {
        TimeZone.setDefault(SYSTEM_TIMEZONE);
        Locale.setDefault(SYSTEM_LOCALE);
    }

    @Test
    public void testDateFormats() {
        doTestDateFormats(Locale.US, equalTo("6/24/2010"));
        doTestDateFormats(Locale.GERMAN, equalTo("24.06.2010"));
    }

    public void doTestDateFormats(Locale locale, Matcher<String> expect) {
        String output = AbstractStatusesColumn.formatDate(TEST_LOCALTIME, locale);
        assertThat(output, expect);
    }

    @Test
    public void testDateTimeFormats() {
        doTestDateTimeFormats(Locale.US, anyOf(equalTo("6/24/2010 4:56 PM"), equalTo("6/24/2010, 4:56 PM")));
        doTestDateTimeFormats(Locale.GERMAN, anyOf(equalTo("24.06.2010 16:56"), equalTo("24.06.2010, 16:56")));
    }

    public void doTestDateTimeFormats(Locale locale, Matcher expect) {
        String output = AbstractStatusesColumn.formatDateTime(TEST_LOCALTIME, locale);
        assertThat(output, expect);
    }

    /** Shows that all locale handling will be okay. */
    @Test
    public void testNoBadLocale() {
        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            String s = AbstractStatusesColumn.getBuildTimeString(TEST_TIME, locale);
            assertNotNull(s);
        }
    }

    @Test
    public void testShowDate() {
        doTestShowDate(Locale.GERMAN, "16:56", "24.06.2010");
        doTestShowDate(Locale.US, "4:56 PM", "6/24/2010");
    }

    private void doTestShowDate(Locale locale, String expectTime, String expectDate) {
        String expectDateTime = expectTime + ", " + expectDate;
        String expectDateTimeLong = expectDate + " " + expectTime;
        String expectDateTimeLongAlt = expectDate + ", " + expectTime;

        String ago;

        ago = AbstractStatusesColumn.getTimeAgoString(locale, TEST_TIME, false, TimeAgoType.PREFER_DATE_TIME);
        assertThat(ago, is(anyOf(equalTo(expectDateTimeLong), equalTo(expectDateTimeLongAlt))));

        ago = AbstractStatusesColumn.getTimeAgoString(locale, TEST_TIME, true, TimeAgoType.PREFER_DATE_TIME);
        assertEquals(expectDate, ago);

        ago = AbstractStatusesColumn.getTimeAgoString(locale, TEST_TIME, true, TimeAgoType.PREFER_DATES);
        assertEquals(expectDate, ago);

        // can't easily test this format with current API, but can do a negative test
        ago = AbstractStatusesColumn.getTimeAgoString(locale, TEST_TIME, false, TimeAgoType.DIFF);
        assertFalse(expectDate.equals(ago));
        assertFalse(expectDateTime.equals(ago));
        assertFalse(expectTime.equals(ago));
    }

    @Test
    public void testLocalizeDate() {
        doTestLocalizeDate(Locale.ENGLISH, equalTo("4:56 PM, 6/24/2010"));
        doTestLocalizeDate(Locale.GERMAN, equalTo("16:56, 24.06.2010"));
        doTestLocalizeDate(Locale.CANADA, anyOf(equalTo("4:56 PM, 24/06/2010"), equalTo("4:56 p.m., 2010-06-24")));
    }

    private void doTestLocalizeDate(Locale locale, Matcher expect) {
        String formatted = AbstractStatusesColumn.getBuildTimeString(TEST_TIME, locale);
        assertThat(formatted, expect);
    }

    /** This just shows the weird way the Hudson.util is working. */
    @Test
    public void testTime() {
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
        assertEquals(expect, found);
    }

    @Test
    @Ignore("Broken on newer versions of Jenkins, because this test tries to work with subclasses of Job and Run")
    public void testGetBuilds() {
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
        assertEquals(String.valueOf(-time), job._getRuns().firstKey().toString());
        List<BuildInfo> builds = col.getBuilds(job, Locale.US);
        assertEquals(expectToShow.length(), builds.size());
        for (int i = 0; i < builds.size(); i++) {
            char c = expectToShow.charAt(i);
            BuildInfo build = builds.get(i);
            switch (c) {
                case 'S':
                    assertEquals("Stable", build.getStatus());
                    break;
                case 'U':
                    assertEquals("Unstable", build.getStatus());
                    break;
                case 'F':
                    assertEquals("Failed", build.getStatus());
                    break;
                case 'A':
                    assertEquals("Aborted", build.getStatus());
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
