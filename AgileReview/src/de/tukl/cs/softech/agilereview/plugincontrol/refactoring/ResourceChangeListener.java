package de.tukl.cs.softech.agilereview.plugincontrol.refactoring;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.Display;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

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
				// TODO Auto-generated catch block
				PluginLogger.logError(this.getClass().toString(), "resourceChanged", "CoreException occured during acceptance test of event delta", e);
			}
		}
	}

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException{
		
		switch(delta.getKind()) {
		case IResourceDelta.ADDED:
			if((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
				oldPath = delta.getMovedFromPath().toOSString();
				System.out.println("oldPath="+delta.getMovedFromPath().toOSString());
			}
			break;
		case IResourceDelta.REMOVED:
			if((delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
				newPath = delta.getMovedToPath().toOSString();
				System.out.println("newPath="+delta.getMovedToPath().toOSString());
			}
			break;
		}
		
		if(!oldPath.equals("") && !newPath.equals("") && !refactoringDone) {
			PluginLogger.log(this.getClass().toString(), "visit", ">oldPath="+oldPath+"< - >newPath="+newPath+"< - type="+delta.getResource().getType());
			try 
			{
				// Do the refactoring
				ReviewAccess.getInstance().refactorPath(oldPath, newPath, delta.getResource().getType());
				// Refresh the TableView (has to be done in the UI-Thread)
				Display.getDefault().asyncExec(new Runnable(){
					@Override
					public void run() {
						if(ViewControl.isOpen(CommentTableView.class)) {
							CommentTableView.getInstance().resetComments();
						}
					}
				});
			}
			catch (IOException e) {
				PluginLogger.logError(this.getClass().toString(), "visit", "IOException occured during refactoring Path in ReviewAccess", e);
			} catch (XmlException e) {
				PluginLogger.logError(this.getClass().toString(), "visit", "XmlException occured during refactoring Path in ReviewAccess", e);
			} catch (SWTException e) {
				PluginLogger.logError(this.getClass().toString(), "visit", "SWTException occured during asynchronous execution in the UI-Thread", e);
			}
			refactoringDone = true;
		}
		
		return true;
	}
}
