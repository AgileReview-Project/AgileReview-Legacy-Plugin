package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.ISourceProviderService;

import de.tukl.cs.softech.agilereview.plugincontrol.SourceProvider;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * Handler for activating the in the ReviewExplorer selected review.
 * Active when: ReviewExplorer is activePart
 * Enabled when: exactly one MultipleReviewWrapper is selected
 */
public class ActivateReviewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "\"Activate in ReviewExplorer selected review\" triggered");
		
		ISelection sel1 = HandlerUtil.getCurrentSelection(event);
		if (sel1 != null){
			if (!sel1.isEmpty())
			{	
				if (sel1 instanceof IStructuredSelection)
				{
					Object o = ((IStructuredSelection) sel1).getFirstElement();
					if (o instanceof MultipleReviewWrapper){
						MultipleReviewWrapper wrap = (MultipleReviewWrapper)o;
						if (!PropertiesManager.getInstance().isReviewOpen(wrap.getReviewId()))
						{
							if (!MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Activate", "In order to activate a review, it has to be open. Do you want to open the selected review now?"))
							{
								// MessageDialog.openWarning(null, "Warning: Could not activate review", "Only open reviews can be activated");
								PluginLogger.logWarning("ReviewExplorer", "activateSelectedReview", "Could not activate review: closed review is selected");
								return null;
							}
							// Execute open/close command
							IHandlerService handlerService = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
							try {
								handlerService.executeCommand("de.tukl.cs.softech.agilereview.views.reviewexplorer.openClose", null);
							} catch (ExecutionException e) {
								PluginLogger.logError(this.getClass().toString(), "execute", "Problems occured executing command \"de.tukl.cs.softech.agilereview.views.reviewexplorer.openCloseSelectedReview\"", e);
							} catch (NotDefinedException e) {
								PluginLogger.logError(this.getClass().toString(), "execute", "Command \"de.tukl.cs.softech.agilereview.views.reviewexplorer.openCloseSelectedReview\" is not defined", e);
							} catch (NotEnabledException e) {
								PluginLogger.logError(this.getClass().toString(), "execute", "Command \"de.tukl.cs.softech.agilereview.views.reviewexplorer.openCloseSelectedReview\" is not enabled", e);
							} catch (NotHandledException e) {
								PluginLogger.logError(this.getClass().toString(), "execute", "Command \"de.tukl.cs.softech.agilereview.views.reviewexplorer.openCloseSelectedReview\" is not handled", e);
							}
						}
						
						PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW, wrap.getReviewId());
						ViewControl.refreshViews(ViewControl.REVIEW_EXPLORER);
						
						//update environment variable
						ISourceProviderService isps = (ISourceProviderService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISourceProviderService.class);
						SourceProvider sp = (SourceProvider) isps.getSourceProvider(SourceProvider.IS_ACTIVE_REVIEW);
						sp.setVariable(SourceProvider.IS_ACTIVE_REVIEW, true);
					}
				}
			}
		}
		return null;
	}

}