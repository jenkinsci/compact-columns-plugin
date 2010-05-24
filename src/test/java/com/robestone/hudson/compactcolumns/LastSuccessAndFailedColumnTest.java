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

import com.robestone.hudson.compactcolumns.LastSuccessAndFailedColumn;

import junit.framework.TestCase;

public class LastSuccessAndFailedColumnTest extends TestCase {

	private LastSuccessAndFailedColumn col = new LastSuccessAndFailedColumn();
	
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
		String found = col.getShortTimestamp(diff);
		assertEquals(expect, found);
	}
	
}
