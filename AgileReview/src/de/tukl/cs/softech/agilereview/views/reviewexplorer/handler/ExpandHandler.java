package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.AbstractMultipleWrapper;

/**
 *	This handler takes care of the command for selective expand of all sub nodes 
 *  of a given node in the ReviewExplorer
 */
public class ExpandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if(selection != null) {
			if(selection instanceof IStructuredSelection) {
				for(Object o : ((IStructuredSelection)selection).toArray()) {
					if(o instanceof AbstractMultipleWrapper) {
						if(ViewControl.isOpen(ReviewExplorer.class)) {
							ReviewExplorer.getInstance().expandAllSubNodes((AbstractMultipleWrapper)o);
						}
					}
				}
			}
		}
		return null;
	}
}
