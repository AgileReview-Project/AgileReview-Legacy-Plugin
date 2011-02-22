package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.detail.DetailView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;
import de.tukl.cs.softech.agilereview.wizards.newreview.NewReviewWizard;

/**
 * Handler for Creating a new review
 */
public class CreateNewReviewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (ViewControl.isOpen(CommentTableView.class) || ViewControl.isOpen(DetailView.class) || ViewControl.isOpen(ReviewExplorer.class)) {
			if (ViewControl.getInstance().shouldSwitchPerspective()) {
				ViewControl.getInstance().switchPerspective();
			}
		}
		
		PluginLogger.log(this.getClass().toString(), "execute", "New Review Handler triggered");
		
		WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), new NewReviewWizard());
		dialog.open();
		
		return null;
	}

}
