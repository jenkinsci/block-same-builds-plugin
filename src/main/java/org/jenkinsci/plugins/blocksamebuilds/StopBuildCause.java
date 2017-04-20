package org.jenkinsci.plugins.blocksamebuilds;

import jenkins.model.CauseOfInterruption;

public class StopBuildCause extends CauseOfInterruption {

	private String reason;

	public StopBuildCause() {
	}

	public StopBuildCause(String reason) {
		this.reason = reason;
	}

	@Override
	public String getShortDescription() {
		// TODO Auto-generated method stub
		return this.reason;
	}

}
