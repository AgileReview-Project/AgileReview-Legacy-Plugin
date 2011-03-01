package de.tukl.cs.softech.agilereview.dataaccess;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * Handles save commands
 */
public class SaveHandler extends AbstractHandler {
	
	/**
	 * Save command id
	 */
	public static final String SAVE_COMMAND_ID = "de.tukl.cs.softech.agilereview.save";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "\"Save\" handler triggered");
		if(ViewControl.isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().refreshTable();
		}
		try {
			ReviewAccess.getInstance().save();
		} catch (IOException e) {
			PluginLogger.logError(this.getClass().toString(), "execute", "Error occured while saving comments", e);
		}
		return null;
	}

}
