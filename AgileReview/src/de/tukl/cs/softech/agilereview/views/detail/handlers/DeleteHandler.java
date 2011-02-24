package de.tukl.cs.softech.agilereview.views.detail.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.detail.DetailView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * Handles refresh events for detail views
 */
public class DeleteHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(ViewControl.isOpen(DetailView.class)) {
			Object o = DetailView.getInstance().getContent();	
			if(o instanceof Review) {
				if (!MessageDialog.openConfirm(null, "Review Details - Delete", "Are you sure you want to delete this review?"))
				{
					return null;
				}
				Review r = (Review)o;
				// Delete the selected review from ReviewExplorer
				if(ViewControl.isOpen(ReviewExplorer.class)) {
					ReviewExplorer.getInstance().deleteReview(new MultipleReviewWrapper(r, r.getId()));
				}
				ReviewAccess.getInstance().deleteReview(r.getId());
				// Check if this was the active review
				if (PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW).equals(r.getId()))
				{
					PropertiesManager.getPreferences().setToDefault(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
				}
				// Remove this review from the list of open reviews (regardless if it was open or not)
				PropertiesManager.getInstance().removeFromOpenReviews(r.getId());
			} else if(o instanceof Comment) {
				Comment c = (Comment)o;
				String keySeparator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
				String commentTag = c.getReviewID()+keySeparator+c.getAuthor()+keySeparator+c.getId();
				
				if (!MessageDialog.openConfirm(null, "Comment Details - Delete", "Are you sure you want to delete comment \""+commentTag+"\"?"))
				{
					return null;
				}
				if(ViewControl.isOpen(CommentTableView.class)) {
					CommentTableView.getInstance().deleteComment(c);
				}
				try {
					ReviewAccess.getInstance().deleteComment(c);
				} catch (IOException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "IOException occured while deleting a comment in ReviewAccess: "+c, e);
				}
				// Refresh the Review Explorer
				if(ViewControl.isOpen(ReviewExplorer.class)) {
					ReviewExplorer.getInstance().refresh();
				}
			}
		}
		return null;
	}

}
