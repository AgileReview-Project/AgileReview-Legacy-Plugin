package de.tukl.cs.softech.agilereview.dataaccess;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

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
    private volatile Set<IResource> closedBefore = new HashSet<IResource>();
    /**
     * This IPath is unequal to null iff there was a PRE_DELETE event before a POST_BUILD
     */
    private volatile HashMap<IResource, IPath> deletedProjectPath = new HashMap<IResource, IPath>();
    /**
     * Variable to save the old SourceProject between a PRE and a POST event
     */
    private IProject oldSourceProject = null;
    
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
                wDialog.open();
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
                if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
                    closedBefore.add(event.getResource());
                    if (oldSourceProject == null) {
                        oldSourceProject = ra.getCurrentSourceFolder();
                    }
                    // Remove active nature, if needed
                    if (oldSourceProject != null) {
                        if (oldSourceProject.equals(event.getResource())) {
                            oldSourceProject = ra.unloadCurrentReviewSourceProject();
                        }
                    }
                }
                
                ////////////////
                // POST_CLOSE //
                ////////////////
                if (event.getType() == IResourceChangeEvent.POST_BUILD && !closedBefore.isEmpty()) {
                    if (oldSourceProject != null) {
                        //check whether the project was one of the closed project
                        for (IResource r : closedBefore) {
                            if (oldSourceProject.equals(r)) {
                                Shell currentShell = Display.getDefault().getActiveShell();
                                String msg = "You closed the currently used 'Agile Review Source Project'.\n"
                                        + "Do you want to reopen it to avoid a crash of AgileReview?";
                                if (MessageDialog.openQuestion(currentShell, "Warning: AgileReview Source Project", msg)) {
                                    try {
                                        oldSourceProject.open(null); // TODO use progressmonitor?
                                        ra.loadReviewSourceProject(oldSourceProject.getName());
                                    } catch (final CoreException e) {
                                        PluginLogger.logError(this.getClass().toString(), "resourceChanged",
                                                "An exception occured while reopening the closed source project", e);
                                        Display.getDefault().syncExec(new Runnable() {
                                            @Override
                                            public void run() {
                                                MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could open project", e
                                                        .getLocalizedMessage());
                                            }
                                        });
                                    }
                                } else {
                                    // Show NoAgileReviewSourceProject wizard
                                    showNoSourceProjectWizard();
                                }
                                break;
                            }
                        }
                    }
                    closedBefore.clear();
                }
                
                ////////////////
                // PRE_DELETE //
                ////////////////
                if (event.getType() == IResourceChangeEvent.PRE_DELETE) {
                    if (oldSourceProject == null) {
                        oldSourceProject = ra.getCurrentSourceFolder();
                    }
                    
                    if (oldSourceProject != null) {
                        if (oldSourceProject.equals(event.getResource())) {
                            deletedProjectPath.put(oldSourceProject, oldSourceProject.getLocation());
                            oldSourceProject = ra.unloadCurrentReviewSourceProject();
                        }
                    }
                }
                
                /////////////////
                // POST_DELETE //
                /////////////////
                if (event.getType() == IResourceChangeEvent.POST_BUILD && !deletedProjectPath.isEmpty()) {
                    if (oldSourceProject != null) {
                        //check whether the project was one of the closed project
                        for (Entry<IResource, IPath> entry : deletedProjectPath.entrySet()) {
                            if (oldSourceProject.equals(entry.getKey())) {
                                Shell currentShell = Display.getDefault().getActiveShell();
                                // Check in file system, if file still exists
                                if (!entry.getValue().toFile().exists()) {
                                    String msg = "You deleted the current 'Agile Review Source Project' from disk.\n"
                                            + "Please choose an other 'AgileReview Source Project' for AgileReview to stay functional";
                                    MessageDialog.openWarning(currentShell, "'Agile Review Source Project' deleted", msg);
                                    // Show NoAgileReviewSourceProject wizard
                                    showNoSourceProjectWizard();
                                } else {
                                    String msg = "You deleted the current 'Agile Review Source Project' from your internal explorer.\n"
                                            + "Do you want to re-import it directly to avoid a crash of AgileReview?";
                                    if (MessageDialog.openQuestion(currentShell, "Warning: AgileReview Source Project", msg)) {
                                        try {
                                            IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(
                                                    new Path(entry.getValue() + "/.project"));
                                            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(description.getName());
                                            project.create(description, null); //TODO use progress monitor here and one line below?
                                            project.open(null);
                                            ra.loadReviewSourceProject(project.getName());
                                        } catch (final CoreException e) {
                                            PluginLogger.logError(this.getClass().toString(), "resourceChanged",
                                                    "An exception occured while reimporting the closed source project", e);
                                            Display.getDefault().syncExec(new Runnable() {
                                                @Override
                                                public void run() {
                                                    MessageDialog.openError(Display.getDefault().getActiveShell(),
                                                            "AgileReview: Could not import project", e.getLocalizedMessage());
                                                }
                                            });
                                        }
                                    } else {
                                        // Show NoAgileReviewSourceProject wizard
                                        showNoSourceProjectWizard();
                                    }
                                }
                                break;
                            }
                        }
                    }
                    deletedProjectPath.clear();
                }
                
                ////////////////
                // POST_BUILD //
                ////////////////
                if (event.getType() == IResourceChangeEvent.POST_BUILD && deletedProjectPath.isEmpty() && closedBefore.isEmpty()) {
                    for (IProject p : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
                        try {
                            if (p.hasNature(PropertiesManager.getInstance().getInternalProperty(
                                    PropertiesManager.INTERNAL_KEYS.ACTIVE_AGILEREVIEW_NATURE))
                                    && ra.getCurrentSourceFolder() != null && !ra.getCurrentSourceFolder().equals(p)) {
                                ReviewAccess.setProjectNatures(p, new String[] { PropertiesManager.getInstance().getInternalProperty(
                                        PropertiesManager.INTERNAL_KEYS.AGILEREVIEW_NATURE) });
                                // update decorator
                                Display.getDefault().asyncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (PlatformUI.getWorkbench() == null) {
                                        }
                                        PlatformUI.getWorkbench().getDecoratorManager().update("de.tukl.cs.softech.agilereview.active_decorator");
                                    }
                                });
                            }
                        } catch (CoreException e) {/* We are not interested in closed or non existent projects*/
                        }
                    }
                }
            }
        });
    }
}