package org.jenkinsci.plugins.blocksamebuilds;

import java.io.IOException;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Descriptor;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
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
	public boolean perform(AbstractBuild<?, ?> arg0, Launcher arg1, BuildListener arg2)
			throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		return super.perform(arg0, arg1, arg2);
	}

	public String getCheckPar() {
		return checkPar;
	}

	public void setCheckPar(String checkPar) {
		this.checkPar = checkPar;
	}

}
