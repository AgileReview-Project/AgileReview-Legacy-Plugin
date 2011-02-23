package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.wizards.newreview.NewReviewWizard;

/**
 * Handler for Creating a new review
 */
public class CreateNewReviewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	
		PluginLogger.log(this.getClass().toString(), "execute", "New Review Handler triggered");
		
		WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), new NewReviewWizard());
		dialog.open();
		
		return null;
	}

}
