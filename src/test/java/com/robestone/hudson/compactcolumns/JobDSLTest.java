/* SPDX-License-Identifier: MIT
 * SPDX-FileCopyrightText: © 2009, Sun Microsystems, Inc., Jesse Glick
 * SPDX-FileCopyrightText: © 2024 Tobias Gruetzmacher
 */
package com.robestone.hudson.compactcolumns;

import static org.assertj.core.api.Assertions.assertThat;

import hudson.model.View;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import javaposse.jobdsl.dsl.DslScriptLoader;
import javaposse.jobdsl.plugin.JenkinsJobManagement;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class JobDSLTest {

    // This test makes sure that JobDSL scripts using automatically generated names work
    @Test
    void automaticJobDsl(JenkinsRule j) throws IOException {
        doTest("automatic", j);
    }

    // This test makes sure that JobDSL scripts using symbol annotations work
    @Test
    void symbolJobDsl(JenkinsRule j) throws IOException {
        doTest("symbol", j);
    }

    private void doTest(String type, JenkinsRule j) throws IOException {
        JenkinsJobManagement m = new JenkinsJobManagement(
                System.out, Collections.emptyMap(), Paths.get(".").toFile());
        new DslScriptLoader(m).runScript(readResource("/" + type + "JobDSL.groovy"));

        View legacyView = j.jenkins.getView(type + "View");
        assertThat(legacyView.getColumns())
                .hasExactlyElementsOfTypes(
                        AllStatusesColumn.class,
                        JobNameColorColumn.class,
                        JobNameColumn.class,
                        LastStableAndUnstableColumn.class,
                        LastSuccessAndFailedColumn.class);
    }

    private String readResource(String name) throws IOException {
        return IOUtils.toString(JobDSLTest.class.getResourceAsStream(name), StandardCharsets.UTF_8);
    }
}
