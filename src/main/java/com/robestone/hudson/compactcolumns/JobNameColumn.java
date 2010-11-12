package com.robestone.hudson.compactcolumns;

import hudson.Extension;
import hudson.views.JobColumn;

import org.kohsuke.stapler.DataBoundConstructor;

import com.robestone.hudson.compactcolumns.AbstractCompactColumn.AbstractCompactColumnDescriptor;

public class JobNameColumn extends JobColumn {

	@DataBoundConstructor
    public JobNameColumn() {
    }

    @Extension
    public static class DescriptorImpl extends AbstractCompactColumnDescriptor {
        @Override
        public String getDisplayName() {
            return "Compact Column: Job Name";
        }
    }
}
