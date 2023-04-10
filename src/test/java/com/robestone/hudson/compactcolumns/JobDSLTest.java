/*
 * The MIT License
 *
 * Copyright (c) 2009, Sun Microsystems, Inc., Jesse Glick
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.robestone.hudson.compactcolumns;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.iterableWithSize;

import hudson.model.View;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collections;
import javaposse.jobdsl.dsl.DslScriptLoader;
import javaposse.jobdsl.plugin.JenkinsJobManagement;
import org.apache.commons.io.IOUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class JobDSLTest {
    @ClassRule
    public static JenkinsRule j = new JenkinsRule();

    @Test
    // This test makes sure that JobDSL scripts using automatically generated names work
    public void testAutomaticJobDsl() throws IOException {
        doTest("automatic");
    }

    @Test
    // This test makes sure that JobDSL scripts using symbol annotations work
    public void testSymbolJobDsl() throws IOException {
        doTest("symbol");
    }

    private void doTest(String type) throws IOException {
        JenkinsJobManagement m = new JenkinsJobManagement(
                System.out, Collections.emptyMap(), Paths.get(".").toFile());
        new DslScriptLoader(m).runScript(readResource("/" + type + "JobDSL.groovy"));

        View legacyView = j.jenkins.getView(type + "View");
        assertThat(legacyView.getColumns(), iterableWithSize(5));
        assertThat(
                legacyView.getColumns(),
                contains(
                        instanceOf(AllStatusesColumn.class),
                        instanceOf(JobNameColorColumn.class),
                        instanceOf(JobNameColumn.class),
                        instanceOf(LastStableAndUnstableColumn.class),
                        instanceOf(LastSuccessAndFailedColumn.class)));
    }

    private String readResource(String name) throws IOException {
        return IOUtils.toString(JobDSLTest.class.getResourceAsStream(name), StandardCharsets.UTF_8);
    }
}
