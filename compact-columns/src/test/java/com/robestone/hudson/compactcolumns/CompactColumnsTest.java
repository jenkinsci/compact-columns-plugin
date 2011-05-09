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

import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.TestCase;

public class CompactColumnsTest extends TestCase {

	/**
	 * Shows that all locale handling will be okay.
	 */
	public void testNoBadLocale() {
		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale: locales) {
			String s = AbstractStatusesColumn.getBuildTimeString(1277416568304L, locale);
			assertNotNull(s);
		}
	}
	
	public void testLocalizeDate() {
		long time = 1277416568304L;
		doTestLocalizeDate(time, Locale.ENGLISH, "4:56 PM, 6/24/2010");
		doTestLocalizeDate(time, Locale.GERMAN, "16:56, 24.06.2010");
		doTestLocalizeDate(time, Locale.CANADA, "4:56 PM, 24/06/2010");
	}
	private void doTestLocalizeDate(long time, Locale locale, String expect) {
		String found = AbstractStatusesColumn.getBuildTimeString(time, locale);
		assertEquals(expect, found);
	}
	
	/**
	 * This just shows the weird way the Hudson.util is working.
	 */
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
	
	private void doTestBuilds(String buildsSpec, 
			String expectForLastStableAndUnstable, String expectForLastSuccessAndFailed, String expectForAllStatuses) {
		doTestBuilds(buildsSpec, expectForLastStableAndUnstable, new LastStableAndUnstableColumn());
		doTestBuilds(buildsSpec, expectForLastSuccessAndFailed, new LastSuccessAndFailedColumn());
		doTestBuilds(buildsSpec, expectForAllStatuses, new AllStatusesColumn(null, false, 0));
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
				case 'S': result = Result.SUCCESS; break;
				case 'U': result = Result.UNSTABLE; break;
				case 'F': result = Result.FAILURE; break;
				case 'A': result = Result.ABORTED; break;
			}
			TestRun run = new TestRun(job, time - i, result);
			job.addRun(run);
			if (previous != null) {
				previous.setPrevious(run);
			}
			previous = run;
		}
		assertEquals(String.valueOf(-time), job._getRuns().firstKey().toString());
		List<BuildInfo> builds = col.getBuilds(job);
		assertEquals(expectToShow.length(), builds.size());
		for (int i = 0; i < builds.size(); i++) {
			char c = expectToShow.charAt(i);
			BuildInfo build = builds.get(i);
			switch (c) {
				case 'S': assertEquals("Stable", build.getStatus()); break;
				case 'U': assertEquals("Unstable", build.getStatus()); break;
				case 'F': assertEquals("Failed", build.getStatus()); break;
				case 'A': assertEquals("Aborted", build.getStatus()); break;
			}
		}
	}
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
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
		protected void removeRun(Run run) {
		}
	}
}
