package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.wizards.newreview.NewReviewWizard;
import de.tukl.cs.softech.agilereview.wizards.noreviewsource.NoReviewSourceWizard;

/**
 * Handler for Creating a new review
 */
public class CreateNewReviewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
	
		PluginLogger.log(this.getClass().toString(), "execute", "New Review Handler triggered");
		
		// If no valid source exists, give the user the chance to get a valid one
		boolean valid = ReviewAccess.getInstance().isCurrentSourceValid();/*?|r108|Peter Reuter|c5|*/
		if (!valid) {
			String msg = "In order to create a review, a valid 'AgileReview Source Project' is needed.\n" +
					"You can now choose (or create) an 'AgileReview Source Project'.";
			MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "", msg);
			NoReviewSourceWizard dialog = new NoReviewSourceWizard();
			WizardDialog wDialog = new WizardDialog(HandlerUtil.getActiveShell(event), dialog);
			wDialog.setBlockOnOpen(true);
			valid = wDialog.open() == Window.OK;
		} 
		if (valid) {
			WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), new NewReviewWizard());
			dialog.open();
		}/*|r108|Peter Reuter|c5|?*/
		
		return null;
	}

}