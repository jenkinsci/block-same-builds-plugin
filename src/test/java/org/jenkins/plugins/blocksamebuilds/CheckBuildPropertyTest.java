package org.jenkins.plugins.blocksamebuilds;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.jenkinsci.plugins.blocksamebuilds.CheckBuildJobProperty;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import hudson.model.BooleanParameterDefinition;
import hudson.model.BooleanParameterValue;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.Result;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.tasks.BatchFile;
import hudson.tasks.Shell;

public class CheckBuildPropertyTest {
	@Rule
	public JenkinsRule j = new JenkinsRule();

//	@Test
//	public void first() throws Exception {
//		FreeStyleProject project = j.createFreeStyleProject();
//		if (SystemUtils.IS_OS_WINDOWS) {
//			project.getBuildersList().add(new BatchFile("echo test"));
//		} else {
//			project.getBuildersList().add(new Shell("sleep 20"));
//		}
//
//		ParameterDefinition pd1 = new StringParameterDefinition("param1", "1");
//		ParameterDefinition pd2 = new BooleanParameterDefinition("param2", false, "");
//
//		project.addProperty(new ParametersDefinitionProperty(pd1, pd2));
//		project.addProperty(new CheckBuildJobProperty("", true));
//
//		FreeStyleBuild build = project.scheduleBuild2(0).get();
//		// FreeStyleBuild build2 = project.scheduleBuild2(5).get();
//		// FreeStyleBuild build3 = project.scheduleBuild2(10).get();
//		// FreeStyleBuild build4 = project.scheduleBuild2(15).get();
//
//		j.assertBuildStatus(Result.SUCCESS, build);
//		// j.assertBuildStatus(Result.NOT_BUILT, build2);
//		// j.assertBuildStatus(Result.NOT_BUILT, build3);
//		// j.assertBuildStatus(Result.NOT_BUILT, build4);
//	}

}
