package org.jenkinsci.plugins.blocksamebuilds;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.JobProperty;
import hudson.model.JobPropertyDescriptor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Cause.UserIdCause;
import hudson.util.FormValidation;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class CheckBuildJobProperty extends JobProperty<Job<?, ?>> {

	public CheckBuildJobProperty() {
		super();
		this.checkPars = "";
	}

	@DataBoundConstructor
	public CheckBuildJobProperty(String checkPars, boolean on) {
		super();
		this.checkPars = checkPars;
		this.on = on;
	}

	private boolean on;
	private String checkPars;

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public String getCheckPars() {
		return checkPars;
	}

	public void setCheckPars(String checkPars) {
		this.checkPars = checkPars;
	}

	private static final Logger logger = Logger.getLogger(CheckBuildJobProperty.class.getName());

	@Override
	public boolean prebuild(AbstractBuild<?, ?> build, BuildListener listener) {

		Map<String, String> curBuildVars = build.getBuildVariables();
		List<String> chkVars = new ArrayList<String>(Arrays.asList(getCheckPars().trim().split(",")));
		boolean isAuthenticated = Jenkins.getAuthentication().isAuthenticated();

		try {
			if (chkVars.contains("")) {
				chkVars.removeAll(Arrays.asList(""));
			}

			if (chkVars.isEmpty()) { // if the check_vars setting is empty,
										// bring
				// all parameters by default
				chkVars.addAll(curBuildVars.keySet());
			}

			// remove the wrong keys in check_vars setting
			if (!curBuildVars.keySet().containsAll(chkVars)) {
				chkVars.retainAll(curBuildVars.keySet());
			}

			RunList<?> builds = build.getProject().getBuilds();
			String curBuildId = build.getId();
			String targetBuildUrl = "";

			UserIdCause uIdCause = build.getCause(UserIdCause.class);
			if (null != uIdCause) {
				String curUsr = (isAuthenticated) ? uIdCause.getUserId() : "anonymous";
				Boolean isExisted = false;

				for (Run<?, ?> run : builds) {
					if (run.isBuilding()) {

						// skip checking current build
						if (curBuildId.equalsIgnoreCase(run.getId())) {
							continue;
						}

						// listener.getLogger().println(MessageFormat.format("compare
						// {0} with {1}",
						// run.getFullDisplayName(),
						// build.getFullDisplayName()));

						Boolean hasTheSameBuild = true;
						Build<?, ?> b = (Build<?, ?>) run;
						Map<String, String> buildVars = b.getBuildVariables();

						UserIdCause uid = run.getCause(UserIdCause.class);
						Boolean isTheSameUsr = false;
						if (null != uid) {
							String runner = (isAuthenticated) ? uid.getUserId() : "anonymous";
							isTheSameUsr = runner.equalsIgnoreCase(curUsr);
						}

						for (String key : chkVars) {
							String varVal = buildVars.get(key).trim();
							String curVarVal = curBuildVars.get(key).trim();
							// listener.getLogger()
							// .println(MessageFormat.format("[{0}]: [{1}] <--->
							// [{2}]", key, curVarVal, varVal));
							if (!curVarVal.equalsIgnoreCase(varVal)) {
								hasTheSameBuild = false;
								break;
							}
						}

						isExisted |= hasTheSameBuild;
						if (isExisted && isTheSameUsr) {
							targetBuildUrl = run.getUrl();
							String msg = MessageFormat.format(
									"The job[{0}] which has the same parameters triggered by user[{1}] is running!",
									Jenkins.getInstance().getRootUrl() + targetBuildUrl, curUsr);
							listener.getLogger().println(msg + "\nAborting this job...");
							build.getExecutor().interrupt(Result.NOT_BUILT, new StopBuildCause("Reason: " + msg));
							break;
						}
					}

				}
			}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			listener.getLogger().println(MessageFormat.format("Fail to check parameter:\n{0}", sw.toString()));
		}

		return super.prebuild(build, listener);
	}

	@Override
	public DescriptorImpl getDescriptor() {
		return (DescriptorImpl) super.getDescriptor();
	}

	@Extension
	public static final class DescriptorImpl extends JobPropertyDescriptor {

		@Override
		public JobProperty<?> newInstance(StaplerRequest req, JSONObject formData) throws FormException {
			if (formData.optBoolean("on")) {
				return (CheckBuildJobProperty) super.newInstance(req, formData);
			}
			return null;
		}

		@Override
		public boolean isApplicable(Class<? extends Job> jobType) {
			return true;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			save();
			return super.configure(req, json);
		}

		public DescriptorImpl() {
		}

		public FormValidation doCheckName(@QueryParameter String value) throws IOException, ServletException {
			return FormValidation.ok();
		}

		public String getDisplayName() {
			return "Block Parameters";
		}

	}

}
