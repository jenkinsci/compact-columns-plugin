package com.robestone.hudson.compactcolumns;

import com.robestone.hudson.compactcolumns.AbstractStatusesColumn.AbstractCompactColumnDescriptor;
import com.robestone.hudson.compactcolumns.AbstractStatusesColumn.TimeAgoType;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import jenkins.model.Jenkins;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;

public class JobNameColorColumn extends AbstractCompactColumn {

    private final boolean showColor;
    private final boolean showDescription;
    private final boolean showLastBuild;

    @DataBoundConstructor
    public JobNameColorColumn(
            boolean showColor, boolean showDescription, boolean showLastBuild, String colorblindHint) {
        super(colorblindHint);
        this.showColor = showColor;
        this.showDescription = showDescription;
        this.showLastBuild = showLastBuild;
    }

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
            color = BuildInfo.getOtherColor();
            underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
        } else if (Result.ABORTED.equals(result)) {
            color = BuildInfo.getOtherColor();
            underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
        } else if (Result.FAILURE.equals(result)) {
            color = BuildInfo.getFailedColor();
            underline = AbstractStatusesColumn.FAILED_UNDERLINE_STYLE;
        } else if (Result.NOT_BUILT.equals(result)) {
            color = BuildInfo.getOtherColor();
            underline = AbstractStatusesColumn.OTHER_UNDERLINE_STYLE;
        } else if (Result.SUCCESS.equals(result)) {
            color = BuildInfo.getStableColor();
            underline = AbstractStatusesColumn.STABLE_UNDERLINE_STYLE;
        } else if (Result.UNSTABLE.equals(result)) {
            color = BuildInfo.getUnstableColor();
            underline = AbstractStatusesColumn.UNSTABLE_UNDERLINE_STYLE;
        } else {
            color = BuildInfo.getOtherColor();
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

    public String getToolTip(Job job, Locale locale) throws IOException {
        StringBuilder tip = new StringBuilder();
        if (showDescription) {
            String desc = Jenkins.get().getMarkupFormatter().translate(job.getDescription());
            if (!desc.isBlank()) {
                tip.append(desc);
            }
        }
        if (showLastBuild) {
            // TODO could me much more efficient if we have a flag to get just the first build (will help
            // with other TODO)
            List<BuildInfo> builds = AbstractStatusesColumn.getBuilds(
                    job, locale, false, false, true, isShowColorblindUnderlineHint(), TimeAgoType.DIFF, 0);
            if (!builds.isEmpty()) {
                BuildInfo build = builds.get(0);
                String desc = AbstractStatusesColumn.getBuildDescriptionToolTip(build, locale);
                if (!desc.isEmpty()) {
                    if (!tip.isEmpty()) {
                        tip.append("<hr/>");
                    }
                    tip.append(desc);
                }
            }
        }
        return (!tip.isEmpty()) ? tip.toString() : null;
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
    @Symbol("compactJobNameColor")
    public static class DescriptorImpl extends AbstractCompactColumnDescriptor {
        public String getColumnDisplayName() {
            return Messages.jobColumn_displayName();
        }

        @Override
        public String getDisplayName() {
            return Messages.Compact_Column_Job_Name_w_Options();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/compact-columns/job-name-color-column.html";
        }
    }
}
