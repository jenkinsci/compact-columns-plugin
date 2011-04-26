package com.robestone.hudson.compactcolumns;

import hudson.Extension;
import hudson.model.Result;
import hudson.model.Job;
import hudson.model.Run;

import java.util.List;
import java.util.Locale;

import org.kohsuke.stapler.DataBoundConstructor;

import com.robestone.hudson.compactcolumns.AbstractStatusesColumn.AbstractCompactColumnDescriptor;

/**
 * @author jacob robertson
 */
public class JobNameOptionsColumn extends AbstractCompactColumn {

	private boolean showColor;
	private boolean showDescription;
	private boolean showLastBuild;
	
	@DataBoundConstructor
	public JobNameOptionsColumn(boolean showColor, boolean showDescription,
			boolean showLastBuild, String colorblindHint) {
		super(colorblindHint);
		this.showColor = showColor;
		this.showDescription = showDescription;
		this.showLastBuild = showLastBuild;
	}

	@SuppressWarnings("rawtypes")
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
			underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
		} else if (Result.ABORTED.equals(result)) {
			color = "grey";
			underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
		} else if (Result.FAILURE.equals(result)) {
			color = "red";
			underline = AbstractStatusesColumn.FAILED_UNDERLINE_STYLE;
		} else if (Result.NOT_BUILT.equals(result)) {
			color = "grey";
			underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
		} else if (Result.SUCCESS.equals(result)) {
			color = "blue";
			underline = AbstractStatusesColumn.STABLE_UNDERLINE_STYLE;
		} else if (Result.UNSTABLE.equals(result)) {
			color = "orange";
			underline = AbstractStatusesColumn.UNSTABLE_UNDERLINE_STYLE;
		} else {
			color = "grey";
			underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
		}
		String style = "";
		if (showColor) {
			style += ("color: " + color + ";");
		}
		if (isShowColorblindUnderlineHint()) {
			style += ("text-decoration: none; border-bottom: " + underline + ";");
		}
		return style;
	}
	@SuppressWarnings("rawtypes")
	public String getToolTip(Job job, Locale locale) {
		String tip = "";
		if (showDescription) {
			tip += job.getDescription();
		}
		if (showLastBuild) {
			if (tip.length() > 0) {
				tip += "<hr/>";
			}
			List<BuildInfo> builds = AbstractStatusesColumn.getBuilds(job, false, false);
			if (!builds.isEmpty()) {
				BuildInfo build = builds.get(0);
				tip += AbstractStatusesColumn.getBuildDescriptionToolTip(build, locale);
			}
		}
		return tip;
	}
    public boolean isShowColor() {
		return showColor;
	}
	public boolean isShowDescription() {
		return showDescription;
	}
	public boolean isShowLastBuild() {
		return showLastBuild;
	}

	@Extension
    public static class DescriptorImpl extends AbstractCompactColumnDescriptor {
        @Override
        public String getDisplayName() {
            return Messages.Compact_Column_Job_Name_w_Options();
        }
    }
}
