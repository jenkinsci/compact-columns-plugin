package com.robestone.hudson.compactcolumns;

import hudson.Extension;

import org.kohsuke.stapler.DataBoundConstructor;

import com.robestone.hudson.compactcolumns.AbstractStatusesColumn.AbstractCompactColumnDescriptor;

/**
 * @author jacob robertson
 */
public class JobNameColorColumn extends JobNameOptionsColumn {

	@DataBoundConstructor
    public JobNameColorColumn() {
		super(true, true, false, null);
    }

    @Extension
    public static class DescriptorImpl extends AbstractCompactColumnDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.Compact_Column_Job_Name_w_Status_Color();
        }
    }
}
