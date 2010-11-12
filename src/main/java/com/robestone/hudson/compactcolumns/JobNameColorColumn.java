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
		if (result == null) {
			color = "grey";
		} else if (Result.ABORTED.equals(result)) {
			color = "grey";
		} else if (Result.FAILURE.equals(result)) {
			color = "red";
		} else if (Result.NOT_BUILT.equals(result)) {
			color = "grey";
		} else if (Result.SUCCESS.equals(result)) {
			color = "blue";
		} else if (Result.UNSTABLE.equals(result)) {
			color = "orange";
		} else {
			color = "grey";
		}
		return "color: " + color;
	}

    @Extension
    public static class DescriptorImpl extends AbstractCompactColumnDescriptor {
        @Override
        public String getDisplayName() {
            return "Compact Column: Job Name w/ Status Color";
        }
    }
}
