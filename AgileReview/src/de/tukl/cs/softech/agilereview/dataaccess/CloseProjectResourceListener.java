package de.tukl.cs.softech.agilereview.dataaccess;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;

/**
 * This class handles the case of closing the active review source folder.
 */
public class CloseProjectResourceListener implements IResourceChangeListener {
	
	/**
	 * This boolean indicates whether there was a PRE_CLOSE event before a POST_BUILD
	 */
	private boolean closedBefore = false;
	/**
	 * This IPath is unequal to null iff there was a PRE_DELETE event before a POST_BUILD
	 */
	private IPath deletedProjectPath = null;
	
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				if(event.getType() == IResourceChangeEvent.PRE_CLOSE) {
					closedBefore = true;
				}
				
				if(event.getType() == IResourceChangeEvent.POST_BUILD && closedBefore) {
					ReviewAccess ra = ReviewAccess.getInstance();
					if(ra.getCurrentSourceFolder() != null) {
						//check whether the project was one of the closed project
						IResourceDelta[] deltaArr = event.getDelta().getAffectedChildren();
						if(deltaArr.length > 0) {
							for(IResourceDelta delta : deltaArr) {
								if (ra.getCurrentSourceFolder().equals(delta.getResource())) {
									
									Shell currentShell = Display.getDefault().getActiveShell();
									String msg = "You closed the current 'Agile Review Source Project'.\n" +
											"As this will lead to a crash of AgileReview the project will be reopened.";
									MessageDialog.openWarning(currentShell, "Warning: AgileReview Source Project", msg);
									
									try {
										ra.getCurrentSourceFolder().open(null);
									} catch (CoreException e) {
										PluginLogger.logError(this.getClass().toString(), "resourceChanged", "An exception occured while reopening the closed source project", e);
									}
									break;
								}
							}
						}
					}
					closedBefore = false;
				}
				
				if(event.getType() == IResourceChangeEvent.PRE_DELETE) {
					ReviewAccess ra = ReviewAccess.getInstance();
					IProject currProject = ra.getCurrentSourceFolder();
					if(currProject != null) {
						if(currProject.equals(event.getResource())) {
							deletedProjectPath = currProject.getLocation();
						}
					}
				}
				
				if(event.getType() == IResourceChangeEvent.POST_BUILD && deletedProjectPath != null) {
					ReviewAccess ra = ReviewAccess.getInstance();
					if(ra.getCurrentSourceFolder() != null) {
						//check whether the project was one of the closed project
						IResourceDelta[] deltaArr = event.getDelta().getAffectedChildren();
						if(deltaArr.length > 0) {
							for(IResourceDelta delta : deltaArr) {
								if (ra.getCurrentSourceFolder().equals(delta.getResource())) {
									
									Shell currentShell = Display.getDefault().getActiveShell();
									String msg = "You deleted the current 'Agile Review Source Project' from your internal explorer.\n" +
											"As this will lead to a crash of AgileReview the project will be reimported automatically.";
									MessageDialog.openWarning(currentShell, "Warning: AgileReview Source Project", msg);
									
									try {
										IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(
												new Path(deletedProjectPath+"/.project"));
										IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
										project.create(description, null);
										project.open(null);
									} catch (CoreException e) {
										PluginLogger.logError(this.getClass().toString(), "resourceChanged", "An exception occured while reimporting the closed source project", e);
									}
									break;
								}
							}
						}
					}
					deletedProjectPath = null;
				}
			}
		});
	}
}