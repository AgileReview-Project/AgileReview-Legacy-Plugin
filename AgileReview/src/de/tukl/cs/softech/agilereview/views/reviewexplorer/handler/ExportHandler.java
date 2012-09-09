package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import java.util.HashSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;
import de.tukl.cs.softech.agilereview.wizards.export.ExportReviewDataWizard;

/**
 * Handler for exporting selected reviews in the ReviewExplorer
 */
public class ExportHandler extends AbstractHandler {
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        PluginLogger.log(this.getClass().toString(), "execute", "\"Export in ReviewExplorer\" triggered");
        
        ISelection sel = HandlerUtil.getCurrentSelection(event);
        if (!sel.isEmpty() && sel instanceof IStructuredSelection) {
            HashSet<String> selReviewIds = new HashSet<String>();
            for (Object o : ((IStructuredSelection) sel).toArray()) {
                if (o instanceof MultipleReviewWrapper) {
                    selReviewIds.add(((MultipleReviewWrapper) o).getReviewId());
                }
            }
            
            // Call export wizard with parameter
            WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), new ExportReviewDataWizard(selReviewIds));
            dialog.open();
        }
        
        return null;
    }
    
}
