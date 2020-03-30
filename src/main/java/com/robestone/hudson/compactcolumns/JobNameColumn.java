package com.robestone.hudson.compactcolumns;

import com.robestone.hudson.compactcolumns.AbstractStatusesColumn.AbstractCompactColumnDescriptor;
import hudson.Extension;
import hudson.views.JobColumn;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

/** @author jacob robertson */
public class JobNameColumn extends JobColumn {

  @DataBoundConstructor
  public JobNameColumn() {}

  @Extension
  @Symbol("compactJobName")
  public static class DescriptorImpl extends AbstractCompactColumnDescriptor {
    public String getColumnDisplayName() {
      return hudson.views.Messages.JobColumn_DisplayName();
    }

    @Override
    public String getDisplayName() {
      return Messages.Compact_Column_Job_Name();
    }
  }
}
