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

import hudson.Extension;
import org.kohsuke.stapler.DataBoundConstructor;

/** @author jacob robertson */
public class AllStatusesColumn extends AbstractStatusesColumn {

  private boolean onlyShowLastStatus;
  private int hideDays;

  @DataBoundConstructor
  public AllStatusesColumn(
      String colorblindHint, boolean onlyShowLastStatus, String timeAgoTypeString, int hideDays) {
    super(colorblindHint, timeAgoTypeString);
    this.onlyShowLastStatus = onlyShowLastStatus;
    this.hideDays = hideDays;
  }

  public int getHideDays() {
    return hideDays;
  }

  @Override
  protected boolean isFailedShownOnlyIfLast() {
    return false;
  }

  @Override
  protected boolean isUnstableShownOnlyIfLast() {
    return false;
  }

  public boolean isOnlyShowLastStatus() {
    return onlyShowLastStatus;
  }

  @Extension
  public static class AllStatusesColumnDescriptor extends AbstractCompactColumnDescriptor {
    @Override
    public String getDisplayName() {
      return Messages.Compact_Column_Statuses_w_Options();
    }

    @Override
    public String getHelpFile() {
      return "/plugin/compact-columns/all-statuses-column.html";
    }
  }
}
