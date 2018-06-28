package de.tukl.cs.softech.agilereview.views.detail.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.detail.CommentDetailView;

/**
 * This handler handles the repositioning action of the comment detail view
 */
public class RelocateHandler extends AbstractHandler {
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (ViewControl.isOpen(CommentDetailView.class)) {
            CommentDetailView.getInstance().relocateComment();
        }
        return null;
    }
}