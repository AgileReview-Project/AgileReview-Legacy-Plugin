package de.tukl.cs.softech.agilereview.views.commenttable.handler;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * Handler for deleting comments from the CommentTableView
 */
public class DeleteCommentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "\"Delete comment\" triggered");
		ISelection sel = HandlerUtil.getCurrentSelection(event);
		if (sel != null){
			// Check for not empty and if user really wants to deleted the selected reviews	
			if (sel instanceof IStructuredSelection) {
				IStructuredSelection structSel = (IStructuredSelection)sel;
				if (!MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Comments Summary - Delete", "Are you sure you want to delete the comments currently selected in the Comment Summary?"))
				{
					return null;
				}
				for (Object o: structSel.toArray())
				{	
					if (o instanceof Comment) {
						Comment c = (Comment)o;
						if(ViewControl.isOpen(CommentTableView.class)) {
							CommentTableView.getInstance().deleteComment(c);
						}
						try {
							ReviewAccess.getInstance().deleteComment(c);
						} catch (IOException e) {
							PluginLogger.logError(this.getClass().toString(), "execute", "IOException occured while deleting a comment in ReviewAccess: "+c, e);
						}
						// Refresh the Review Explorer
						ViewControl.refreshViews(ViewControl.REVIEW_EXPLORER);
					}
				}
			}
		}
		return null;
	}

}