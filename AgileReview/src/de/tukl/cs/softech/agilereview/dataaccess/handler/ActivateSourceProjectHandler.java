package de.tukl.cs.softech.agilereview.dataaccess.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PlatformUIUtil;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;

/**
 * Handler for activating the currently selected AgileReview Source project.
 */
public class ActivateSourceProjectHandler extends AbstractHandler {
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        PluginLogger.log(this.getClass().toString(), "execute", "'Activate Review Source Project' triggered in Package-Explorer");
        try {
            ISelection sel = HandlerUtil.getCurrentSelection(event);
            if (sel instanceof StructuredSelection) {
                IStructuredSelection strucSel = (IStructuredSelection) sel;
                Object o = strucSel.getFirstElement();
                if (o instanceof IProject) {
                    IProject projectSel = (IProject) o;
                    if (projectSel.hasNature(PropertiesManager.getInstance().getInternalProperty(
                            PropertiesManager.INTERNAL_KEYS.AGILEREVIEW_NATURE))) {
                        // Activate the source folder
                        ReviewAccess.getInstance().loadReviewSourceProject(projectSel.getName());
                        // Trigger refresh
                        ViewControl.refreshViews(ViewControl.ALL_VIEWS, true);
                    }
                }
            }
            
        } catch (CoreException e) { // CoreException presented to the user via the ExecutionException
            throw new ExecutionException("Closed or non-existent project selected", e);
        }
        return null;
    }
    
    @Override
    public boolean isEnabled() {
        // Only enabled when the only selected source project is not the current one
        ISelection selection = PlatformUIUtil.getActivePage().getSelection();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection strucSel = (IStructuredSelection) selection;
            if (((IStructuredSelection) selection).size() == 1) {
                Object o = strucSel.getFirstElement();
                if (o instanceof IProject) {
                    return super.isEnabled() && !((IProject) o).getName().equals(PropertiesManager.getPreferences().getString(
                            PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER));
                }
            }
        }
        return true;
    }
    
}