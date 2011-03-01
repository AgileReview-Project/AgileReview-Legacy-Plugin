package de.tukl.cs.softech.agilereview.plugincontrol;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

/**
 * Dummy class for the AgileReview project nature
 */
public class AgileReviewNature implements IProjectNature {

	@Override
	public void configure() throws CoreException {/* Do nothing*/}

	@Override
	public void deconfigure() throws CoreException {/* Do nothing*/}

	@Override
	public IProject getProject() {
		/* Do nothing*/
		return null;
	}

	@Override
	public void setProject(IProject project) {/* Do nothing*/}

}
