package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import org.eclipse.core.commands.ExecutionEvent;

import de.tukl.cs.softech.agilereview.annotations.ColorManager;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * Handler for linking the explorer and the CommentTableView
 * since eclipse 3.5 this can more easier be done via "HandlerUtil.toggleCommandState(event.getCommand())"
 */
public class LinkExplorerToggleHandler extends ToggleHandler {

	@Override
	protected void executeToggle(ExecutionEvent event, boolean checked) {
		PluginLogger.log(this.getClass().toString(), "executeToggle", "\"Link Explorer\" triggered");
		if(ViewControl.isOpen(CommentTableView.class)) {
			if (!checked) {
				CommentTableView.getInstance().removeSelectionFilter();
			} else {
				CommentTableView.getInstance().addSelectionFilter();
			}
			ColorManager.resetColorScheme();/*?|r59|Malte|c11|?*/
		}
	}

}