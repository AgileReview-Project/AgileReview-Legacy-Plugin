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
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.wizards.noreviewsource.NoReviewSourceWizard;

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
	
	/**
	 * Displays the NoAgileReviewSourceProject wizard
	 */
	private void showNoSourceProjectWizard() {
		// Has to be done in new thread, as the resourceChanged method blocks the building of the workspace
		Display.getDefault().asyncExec(new Runnable() {								
			@Override
			public void run() {
				NoReviewSourceWizard dialog = new NoReviewSourceWizard();
				WizardDialog wDialog = new WizardDialog(Display.getDefault().getActiveShell(), dialog);
				wDialog.setBlockOnOpen(true);
				if (wDialog.open() == Window.OK) {
					String projectName = dialog.getChosenProjectName();		
					if (ReviewAccess.createAndOpenReviewProject(projectName)) {
						PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, projectName);
						ReviewAccess.getInstance().loadReviewSourceProject(projectName);
					}
				}
			}
		});
	}
	
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
			Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				ReviewAccess ra = ReviewAccess.getInstance();
				
				///////////////
				// PRE_CLOSE //
				///////////////
				if(event.getType() == IResourceChangeEvent.PRE_CLOSE) {
					closedBefore = true;
					// Remove active nature, if needed
					IProject currSource = ra.getCurrentSourceFolder();
					if(currSource != null) {
						if (currSource.equals(event.getResource())){
							// now remove active nature
							ra.unloadCurrentReviewSourceProject();
						}
					}
				}
				
				////////////////
				// POST_CLOSE //
				////////////////
				if(event.getType() == IResourceChangeEvent.POST_BUILD && closedBefore) {
					closedBefore = false;
					if(ra.getCurrentSourceFolder() != null) {
						//check whether the project was one of the closed project
						IResourceDelta[] deltaArr = event.getDelta().getAffectedChildren();
						if(deltaArr.length > 0) {
							for(IResourceDelta delta : deltaArr) {
								if (ra.getCurrentSourceFolder().equals(delta.getResource())) {
									Shell currentShell = Display.getDefault().getActiveShell();
									String msg = "You closed the currently used 'Agile Review Source Project'.\n" +
									"Do you want to reopen it to avoid a crash of AgileReview?";
									if (MessageDialog.openQuestion(currentShell, "Warning: AgileReview Source Project", msg)) {
										try {
											ra.getCurrentSourceFolder().open(null); // TODO use progressmonitor?
											ra.loadReviewSourceProject(ra.getCurrentSourceFolder().getName());
										} catch (CoreException e) {
											PluginLogger.logError(this.getClass().toString(), "resourceChanged", "An exception occured while reopening the closed source project", e);
										}
									} else {
										// Show NoAgileReviewSourceProject wizard
										showNoSourceProjectWizard();
									}
									break;
								}
							}
						}
					}
				}
				
				////////////////
				// PRE_DELETE //
				////////////////
				if(event.getType() == IResourceChangeEvent.PRE_DELETE) {
					IProject currProject = ra.getCurrentSourceFolder();
					if(currProject != null) {
						if(currProject.equals(event.getResource())) {
							deletedProjectPath = currProject.getLocation();
							ra.unloadCurrentReviewSourceProject();
						}
					}
				}
				
				/////////////////
				// POST_DELETE //
				/////////////////
				if(event.getType() == IResourceChangeEvent.POST_BUILD && deletedProjectPath != null) {
					if(ra.getCurrentSourceFolder() != null) {
						//check whether the project was one of the closed project
						IResourceDelta[] deltaArr = event.getDelta().getAffectedChildren();
						if(deltaArr.length > 0) {
							for(IResourceDelta delta : deltaArr) {
								if (ra.getCurrentSourceFolder().equals(delta.getResource())) {
									Shell currentShell = Display.getDefault().getActiveShell();
									// Check in file system, if file still exists
									if (!deletedProjectPath.toFile().exists()) {
										String msg = "You deleted the current 'Agile Review Source Project' from disk.\n" +
										"Please choose an other 'AgileReview Source Project' for AgileReview to stay functional";
										MessageDialog.openWarning(currentShell, "'Agile Review Source Project' deleted", msg);
										// Show NoAgileReviewSourceProject wizard
										deletedProjectPath = null; // needed for correct wizard behavior
										showNoSourceProjectWizard();
									} else {
										String msg = "You deleted the current 'Agile Review Source Project' from your internal explorer.\n" +
												"Do you want to re-import it directly to avoid a crash of AgileReview?";
										if (MessageDialog.openQuestion(currentShell, "Warning: AgileReview Source Project", msg)) {
											try {
												IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(
														new Path(deletedProjectPath+"/.project"));
												IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
												project.create(description, null); //TODO use progressmontiro here and one line below?
												project.open(null);
												ra.loadReviewSourceProject(project.getName());
											} catch (CoreException e) {
												PluginLogger.logError(this.getClass().toString(), "resourceChanged", "An exception occured while reimporting the closed source project", e);
											}
										} else {
											// Show NoAgileReviewSourceProject wizard
											deletedProjectPath = null; // needed for correct wizard behavior
											showNoSourceProjectWizard();
										}
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