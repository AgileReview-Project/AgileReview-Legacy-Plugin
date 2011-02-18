package de.tukl.cs.softech.agilereview.views.commenttable.handler;

import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;

/**
 * Handler for adding a new Comment
 */
public class AddNewCommentHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "Command \"Add new Comment\" triggered");
		IContextService contextService = (IContextService)PlatformUI.getWorkbench().getService(IContextService.class);
		for (Object o :contextService.getActiveContextIds())
		{
			System.out.println(o);
		}
		
		String activeReview = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
		if (!activeReview.isEmpty()) {
			try	{
				String pathToFile = "";
				
				IEditorPart part = HandlerUtil.getActiveEditor(event);
				if(part != null) {
					if ( part instanceof IEditorPart ) {
						IEditorInput input = part.getEditorInput();
						if (input != null && input instanceof FileEditorInput) {
							pathToFile = ((FileEditorInput)input).getFile().getFullPath().toOSString().replaceFirst(Pattern.quote(System.getProperty("file.separator")), "");
						}
					}
					String user = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.AUTHOR_NAME);
					Comment newComment = ReviewAccess.getInstance().createNewComment(activeReview , user, pathToFile);
					// TODO Hiervon noch was auslagern (Parser, etc)
					if(ViewControl.isOpen(CommentTableView.class)) {
						CommentTableView.getInstance().addComment(newComment);
					}
					// Refresh the Review Explorer
					if(ViewControl.isOpen(ReviewExplorer.class)) {
						ReviewExplorer.getInstance().refresh();
					}
					// TODO: Hier saven nötig? Gespeichert wird ja automatisch beim Focusverlust im DetailView
					ReviewAccess.getInstance().save();
				} else {
					// no open editor
					MessageDialog.openWarning(null, "Warning: No open file", "Please open a file in an editor before adding comments!");
					PluginLogger.logWarning(this.getClass().toString(), "addNewComment", "No open editor!");
				}
			} catch (IOException e) {
				PluginLogger.logError(this.getClass().toString(), "addNewComment", "IOException occured while creating a new comment in ReviewAccess", e);
			}

		} else {
			MessageDialog.openWarning(null, "Warning: No active review", "Please activate a review before adding comments!");
			PluginLogger.logWarning(this.getClass().toString(), "addNewComment", "No active review!");
		}

		return null;
	}

}
