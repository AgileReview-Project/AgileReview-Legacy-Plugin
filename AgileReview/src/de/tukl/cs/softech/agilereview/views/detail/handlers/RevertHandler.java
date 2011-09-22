package de.tukl.cs.softech.agilereview.views.detail.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.detail.DetailView;

/**
 * Handler for revert command of the DetailView
 */
public class RevertHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(ViewControl.isOpen(DetailView.class)) {
			DetailView.getInstance().revert();
		}
		return null;
	}

}