package de.tukl.cs.softech.agilereview.plugincontrol.refactoring;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.PlatformUI;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;

/**
 * The ResourceChangeListener listens for move and rename refactorings in order to manage 
 * the consistency of all concerned comments
 */
public class ResourceChangeListener implements IResourceChangeListener, IResourceDeltaVisitor {
	
	/**
	 * Path from which a resource is moved
	 */
	private String oldPath = "";
	/**
	 * Path to which a resource is moved
	 */
	private String newPath = "";
	/**
	 * Shows the status of refactoring of every event 
	 */
	private boolean refactoringDone = false;

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		oldPath = "";
		newPath = "";
		refactoringDone = false;
		if((event.getType() & IResourceChangeEvent.POST_CHANGE) != 0) {
			try {
				event.getDelta().accept(this);
			} catch (CoreException e) {
				PluginLogger.logError(this.getClass().toString(), "resourceChanged", "CoreException occured during acceptance test of event delta", e);
			}
		}
	}

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException{
		
		switch(delta.getKind()) {
		case IResourceDelta.ADDED:
			if((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0 && oldPath.isEmpty()) {
				oldPath = delta.getMovedFromPath().toOSString();
			}
			break;
		case IResourceDelta.REMOVED:
			if((delta.getFlags() & IResourceDelta.MOVED_TO) != 0 && newPath.isEmpty()) {
				newPath = delta.getMovedToPath().toOSString();
			}
			break;
		}
		
		if(!oldPath.isEmpty() && !newPath.isEmpty() && !refactoringDone) {
			PluginLogger.log(this.getClass().toString(), "visit", ">oldPath="+oldPath+"< - >newPath="+newPath+"< - type="+delta.getResource().getType());
			try {
				ReviewAccess.getInstance().refactorPath(oldPath, newPath, delta.getResource().getType());
			} catch (IOException e) {
				PluginLogger.logError(this.getClass().toString(), "visit", "IOException occured during refactoring Path in ReviewAccess", e);
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error while refactoring AgileReview comments", "An error occured while reading/writing the involved comment storage files (1)");
			} catch (XmlException e) {
				PluginLogger.logError(this.getClass().toString(), "visit", "XmlException occured during refactoring Path in ReviewAccess", e);
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error while refactoring AgileReview comments", "An error occured while reading/writing the involved comment storage files (2)");
			}
			refactoringDone = true;
		}
		
		return true;
	}
}
