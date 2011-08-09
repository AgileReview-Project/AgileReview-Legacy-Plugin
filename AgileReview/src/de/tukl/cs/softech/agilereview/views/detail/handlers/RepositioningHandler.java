package de.tukl.cs.softech.agilereview.views.detail.handlers;

import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.detail.DetailView;

/**
 * This handler handles the repositioning action of the comment detail view
 */
public class RepositioningHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (ViewControl.isOpen(DetailView.class)) {
			Object content = DetailView.getInstance().getContent();
			if(content instanceof Comment) {
				Comment comment = (Comment)content;
				if(ViewControl.isOpen(CommentTableView.class)) {
					CommentTableView ctv = CommentTableView.getInstance();
					try {
						IEditorPart editor;
						if((editor = HandlerUtil.getActiveEditor(event)) != null) {
							if(ctv.openEditorContains(comment)) {
								//comment stays in same file
								ctv.relocateComment(comment);
							} else {
								//comment has to move to new file
								IEditorInput input = editor.getEditorInput();
								if (input != null && input instanceof FileEditorInput) {
									ReviewAccess ra = ReviewAccess.getInstance();
									//create new comment in new file
									String pathToNewFile = ((FileEditorInput)input).getFile().getFullPath().toOSString().replaceFirst(Pattern.quote(System.getProperty("file.separator")), "");
									Comment newComment = ra.createNewComment(comment.getReviewID() , comment.getAuthor(), pathToNewFile);
									ctv.addComment(newComment);
									//delete old comment
									ra.deleteComment(comment);
									ctv.deleteComment(comment);
									//refresh views
									ViewControl.refreshViews(ViewControl.REVIEW_EXPLORER);
								} else {
									MessageDialog.openError(null, "Comment Detail - Repositioning", "You cannot relocate this comment as the currently opened editor is not yet supported!");
								}
							}
						} else {
							MessageDialog.openError(null, "Comment Detail - Repositioning", "You cannot relocate this comment as there is no editor opened at the moment!");
						}
					} catch (IOException e) {
						PluginLogger.log(this.getClass().toString(), "execute", "IOException when trying to create the new moved comment or deleting the old one", e);
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}
}
