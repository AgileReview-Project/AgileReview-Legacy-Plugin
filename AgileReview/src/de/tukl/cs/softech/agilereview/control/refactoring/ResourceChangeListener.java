package de.tukl.cs.softech.agilereview.control.refactoring;

import java.io.IOException;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

import de.tukl.cs.softech.agilereview.control.ReviewAccess;

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
			} catch (final CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean visit(final IResourceDelta delta) throws CoreException {
		
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
			System.out.println(">"+oldPath+"< - >"+newPath+"< - "+delta.getResource().getType());
			try {
				ReviewAccess.getInstance().refactorPath(oldPath, newPath, delta.getResource().getType());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			refactoringDone = true;
		}
		
		return true;
	}
}
