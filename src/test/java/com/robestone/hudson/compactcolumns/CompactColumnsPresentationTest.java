/* SPDX-License-Identifier: MIT
 * SPDX-FileCopyrightText: © 2020 Tobias Gruetzmacher
 */
package com.robestone.hudson.compactcolumns;

import static org.assertj.core.api.Assertions.assertThat;

import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.views.JobColumn;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.htmlunit.html.DomElement;
import org.htmlunit.html.DomNode;
import org.htmlunit.html.HtmlAnchor;
import org.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class CompactColumnsPresentationTest {

    /** Compares the text of the standard name column with our custom name column. */
    @Test
    void jobColumnText(JenkinsRule j) throws Exception {
        MockFolder folder = j.createFolder("folder1");
        folder.createProject(FreeStyleProject.class, "folderJob");
        j.createFreeStyleProject("rootJob");

        createTestView(j);

        JenkinsRule.WebClient wc = j.createWebClient();
        HtmlPage page = wc.goTo("view/testView/");
        DomElement table = page.getElementById("projectstatus");
        for (DomNode node : table.querySelectorAll("tbody tr")) {
            DomElement row = (DomElement) node;
            if (!row.getAttribute("class").contains("header")) {
                validateRow(row);
            }
        }
    }

    private void createTestView(JenkinsRule j) throws IOException {
        ListView v = new ListView("testView");
        v.setRecurse(true);
        v.setIncludeRegex(".*");
        v.setColumns(Arrays.asList(
                new JobColumn(),
                new JobNameColumn(),
                new JobNameColorColumn(true, true, true, AbstractCompactColumn.colorblindHint_none)));
        j.jenkins.addView(v);
    }

    private void validateRow(DomElement row) {
        String origName = null;
        for (DomElement link : row.getElementsByTagName("a")) {
            if (origName == null) {
                origName = link.getTextContent();
            } else {
                assertThat(link.getTextContent()).isEqualTo(origName);
            }
        }
    }

    @Test
    @Issue("SECURITY-1837")
    void tooltipIsEscaped(JenkinsRule j) throws Exception {
        FreeStyleProject p = j.createFreeStyleProject("proj1");
        p.setDescription("<i/onmouseover=confirm(1)>test");

        createTestView(j);
        HtmlPage page = j.createWebClient().goTo("view/testView/");

        List<HtmlAnchor> links = page.getAnchors().stream()
                .filter(a -> a.getHrefAttribute().endsWith("/proj1/") && a.hasAttribute("tooltip"))
                .collect(Collectors.toList());
        assertThat(links).hasSize(2);
        for (HtmlAnchor link : links) {
            String tooltip = link.getAttribute("tooltip");
            // The default formatter just escapes all HTML
            assertThat(tooltip).doesNotContain("<i");
            assertThat(tooltip).startsWith("&lt;i");
        }
    }
}
