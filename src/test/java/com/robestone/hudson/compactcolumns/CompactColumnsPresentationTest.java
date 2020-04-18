/* SPDX-License-Identifier: MIT
 *
 * Copyright (c) 2020, Tobias Gruetzmacher
 */
package com.robestone.hudson.compactcolumns;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.model.FreeStyleProject;
import hudson.model.ListView;
import hudson.views.JobColumn;
import java.util.Arrays;
import org.junit.ClassRule;
import org.junit.Test;
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

    ListView v = new ListView("testView");
    v.setRecurse(true);
    v.setIncludeRegex(".*");
    v.setColumns(
        Arrays.asList(
            new JobColumn(),
            new JobNameColumn(),
            new JobNameColorColumn(true, true, true, AbstractCompactColumn.colorblindHint_none)));
    j.jenkins.addView(v);

    JenkinsRule.WebClient wc = j.createWebClient();
    HtmlPage page = wc.goTo("view/testView/");
    DomElement table = page.getElementById("projectstatus");
    for (DomElement row : table.getElementsByTagName("tr")) {
      if (!row.getAttribute("class").contains("header")) {
        validateRow(row);
      }
    }
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
}
