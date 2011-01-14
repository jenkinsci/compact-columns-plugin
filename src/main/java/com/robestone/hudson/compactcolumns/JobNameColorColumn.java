package com.robestone.hudson.compactcolumns;

import hudson.Extension;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.views.JobColumn;

import org.kohsuke.stapler.DataBoundConstructor;

import com.robestone.hudson.compactcolumns.AbstractCompactColumn.AbstractCompactColumnDescriptor;

public class JobNameColorColumn extends JobColumn {

	@DataBoundConstructor
    public JobNameColorColumn() {
    }

	@SuppressWarnings("unchecked")
	public String getStyle(Job job) {
		Result result = null;
		if (job != null) {
			Run run = job.getLastBuild();
			if (run != null) {
				result = run.getResult();
			}
		}
		String color;
		String underline;
		if (result == null) {
			color = "grey";
			underline = AbstractCompactColumn.OTHER_UNDERLINE_STYLE;
		} else if (Result.ABORTED.equals(result)) {
			color = "grey";
			underline = AbstractCompactColumn.OTHER_UNDERLINE_STYLE;
		} else if (Result.FAILURE.equals(result)) {
			color = "red";
			underline = AbstractCompactColumn.FAILED_UNDERLINE_STYLE;
		} else if (Result.NOT_BUILT.equals(result)) {
			color = "grey";
			underline = AbstractCompactColumn.OTHER_UNDERLINE_STYLE;
		} else if (Result.SUCCESS.equals(result)) {
			color = "blue";
			underline = AbstractCompactColumn.STABLE_UNDERLINE_STYLE;
		} else if (Result.UNSTABLE.equals(result)) {
			color = "orange";
			underline = AbstractCompactColumn.UNSTABLE_UNDERLINE_STYLE;
		} else {
			color = "grey";
			underline = AbstractCompactColumn.OTHER_UNDERLINE_STYLE;
		}
		return "color: " + color + "; text-decoration: none; border-bottom: " + underline;
	}

    @Extension
    public static class DescriptorImpl extends AbstractCompactColumnDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.Compact_Column_Job_Name_w_Status_Color();
        }
    }
}
