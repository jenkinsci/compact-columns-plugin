/* SPDX-License-Identifier: MIT
 *
 * Copyright (c) 2020, Tobias Gruetzmacher
 */
package com.robestone.hudson.compactcolumns;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.views.JobColumn;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.MockFolder;

public class CompactColumnsPresentationTest {
  @ClassRule public static JenkinsRule j = new JenkinsRule();

  /** Compares the text of the standard name column with our custom name column. */
  @Test
  public void testJobColumnText() throws Exception {
    MockFolder folder = j.createFolder("folder1");
    folder.createProject(FreeStyleProject.class, "folderJob");
    j.createFreeStyleProject("rootJob");

    createTestView();

    JenkinsRule.WebClient wc = j.createWebClient();
    HtmlPage page = wc.goTo("view/testView/");
    DomElement table = page.getElementById("projectstatus");
    for (DomElement row : table.getElementsByTagName("tr")) {
      if (!row.getAttribute("class").contains("header")) {
        validateRow(row);
      }
    }
  }

  private void createTestView() throws IOException {
    ListView v = new ListView("testView");
    v.setRecurse(true);
    v.setIncludeRegex(".*");
    v.setColumns(
        Arrays.asList(
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
        assertThat(link.getTextContent(), is(equalTo(origName)));
      }
    }
  }

  @Test
  @Issue("SECURITY-1837")
  public void testTooltipIsEscaped() throws Exception {
    FreeStyleProject p = j.createFreeStyleProject("proj1");
    p.setDescription("<i/onmouseover=confirm(1)>test");

    createTestView();
    HtmlPage page = j.createWebClient().goTo("view/testView/");

    List<HtmlAnchor> links =
        page.getAnchors().stream()
            .filter(a -> a.getHrefAttribute().endsWith("/proj1/") && a.hasAttribute("tooltip"))
            .collect(Collectors.toList());
    assertThat(links, hasSize(2));
    for (HtmlAnchor link : links) {
      String tooltip = link.getAttribute("tooltip");
      // The default formatter just escapes all HTML
      assertThat(tooltip, not(containsString("<i")));
      assertThat(tooltip, startsWith("&lt;i"));
    }
  }
}
