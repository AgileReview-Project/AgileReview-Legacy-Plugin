package de.tukl.cs.softech.agilereview.plugincontrol.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * Compute and reveal the next visible comment in the document
 */
public class NextCommentInListHandler extends AbstractHandler {
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        PluginLogger.log(this.getClass().toString(), "execute", "\"Show Next Comment In List\" triggered");
        
        if (ViewControl.isOpen(CommentTableView.class)) {
            CommentTableView.getInstance().selectNextComment();
        }
        
        return null;
    }
    
}
