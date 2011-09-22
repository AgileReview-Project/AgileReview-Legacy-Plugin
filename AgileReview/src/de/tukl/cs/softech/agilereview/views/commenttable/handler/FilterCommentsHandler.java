package de.tukl.cs.softech.agilereview.views.commenttable.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * Handler for jumping to the filter comment field in the CommentTableView
 */
public class FilterCommentsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "Command \"Filter comments\" triggered");
		if(ViewControl.isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().focusFilterField();
		}
		return null;
	}

}