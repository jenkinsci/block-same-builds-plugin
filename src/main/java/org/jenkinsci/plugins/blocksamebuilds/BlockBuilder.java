package org.jenkinsci.plugins.blocksamebuilds;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.Cause.UserIdCause;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;

@Extension
public class BlockBuilder extends Builder {

	private String checkPar;

	@Extension
	public static final class BlockBuildDescriptor extends BuildStepDescriptor<Builder> {

		public BlockBuildDescriptor() {
			super();

			load();
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
			save();
			return super.configure(req, json);
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> arg0) {
			return true;
		}

		@Override
		public String getDisplayName() {
			// TODO Auto-generated method stub
			return "Block the running builds with the same parameters.";
		}

	}

	public BlockBuilder() {
		super();
	}

	@DataBoundConstructor
	public BlockBuilder(String checkPar) {
		this();
		this.setCheckPar(checkPar);
	}

	@Override
	public Descriptor<Builder> getDescriptor() {
		// TODO Auto-generated method stub
		return (BlockBuildDescriptor) super.getDescriptor();
	}

	@Override
	public boolean perform(AbstractBuild<?, ?> build, Launcher arg1, BuildListener listener)
			throws InterruptedException, IOException {
		Map<String, String> curBuildVars = build.getBuildVariables();
		List<String> chkVars = new ArrayList<String>(Arrays.asList(getCheckPar().trim().split(",")));
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
						// run.getFullDisplayName(), build.getFullDisplayName()));

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
//							listener.getLogger()
//									.println(MessageFormat.format("[{0}]: [{1}] <---> [{2}]", key, curVarVal, varVal));
							if (!curVarVal.equalsIgnoreCase(varVal)) {
								hasTheSameBuild = false;
								break;
							}
						}

						isExisted |= hasTheSameBuild;
						if (isExisted && isTheSameUsr) {
							targetBuildUrl = run.getUrl();
							listener.getLogger()
									.println(MessageFormat.format(
											"The job[{0}] which has the same parameters triggered by user[{1}] is running!\nAborting this job...",
											Jenkins.getInstance().getRootUrl() + targetBuildUrl, curUsr));
							build.getExecutor().interrupt(Result.UNSTABLE);
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

		return true;
	}

	public String getCheckPar() {
		return checkPar;
	}

	public void setCheckPar(String checkPar) {
		this.checkPar = checkPar;
	}

}
