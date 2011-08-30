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
	
	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				ReviewAccess ra = ReviewAccess.getInstance();
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
				
				if(event.getType() == IResourceChangeEvent.POST_BUILD && closedBefore) {
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
										break;
									} else {
										// Show NoAgileReviewSourceProject wizard
										NoReviewSourceWizard dialog = new NoReviewSourceWizard();
										WizardDialog wDialog = new WizardDialog(currentShell, dialog);
										wDialog.setBlockOnOpen(true);
										if (wDialog.open() == Window.OK) {
											String projectName = dialog.getChosenProjectName();		
											if (ReviewAccess.createAndOpenReviewProject(projectName)) {
												PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, projectName);
												ra.loadReviewSourceProject(projectName);
											}
										}
									}
								}
							}
						}
					}
					closedBefore = false;
				}
				
				if(event.getType() == IResourceChangeEvent.PRE_DELETE) {
					IProject currProject = ra.getCurrentSourceFolder();
					if(currProject != null) {
						if(currProject.equals(event.getResource())) {
							deletedProjectPath = currProject.getLocation();
							ra.unloadCurrentReviewSourceProject();
						}
					}
				}
				
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
										NoReviewSourceWizard dialog = new NoReviewSourceWizard();
										WizardDialog wDialog = new WizardDialog(currentShell, dialog);
										wDialog.setBlockOnOpen(true);
										if (wDialog.open() == Window.OK) {
											String projectName = dialog.getChosenProjectName();		
											if (ReviewAccess.createAndOpenReviewProject(projectName)) {
												PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, projectName);
												ra.loadReviewSourceProject(projectName);
											}
										}
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
											break;
										} else {
											// Show NoAgileReviewSourceProject wizard
											NoReviewSourceWizard dialog1 = new NoReviewSourceWizard();
											WizardDialog wDialog1 = new WizardDialog(currentShell, dialog1);
											wDialog1.setBlockOnOpen(true);
											if (wDialog1.open() == Window.OK) {
												String projectName = dialog1.getChosenProjectName();		
												if (ReviewAccess.createAndOpenReviewProject(projectName)) {
													PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, projectName);
													ra.loadReviewSourceProject(projectName);
												}
											}
										}
									}
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