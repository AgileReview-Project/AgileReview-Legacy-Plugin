package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.wizards.noreviewsource.NoReviewSourceWizard;

/**
 * Handler for switching the Review Source Project in the Review Explorer. This Handler simply opens the preferences dialog.
 */
public class SwitchReviewSourceProjectHandler extends AbstractHandler {
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        //TODO get this from Properties (externalize Strings?)
        NoReviewSourceWizard dialog = new NoReviewSourceWizard();
        WizardDialog wDialog = new WizardDialog(HandlerUtil.getActiveShell(event), dialog);
        wDialog.setBlockOnOpen(true);
        wDialog.open();
        
        return null;
    }
    
}