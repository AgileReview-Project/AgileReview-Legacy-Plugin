package de.tukl.cs.softech.agilereview.views.detail.handlers;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;

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
	
	/**
	 * Instance of PropertiesManager
	 */
	private PropertiesManager pm = PropertiesManager.getInstance();
	/**
	 * Instance of ReviewAccess
	 */
	private ReviewAccess ra = ReviewAccess.getInstance();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(ViewControl.isOpen(DetailView.class)) {
			Object o = DetailView.getInstance().getContent();	
			if(o instanceof Review) {
				if (!MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Review Details - Delete", "Are you sure you want to delete this review?"))
				{
					return null;
				}
				Review r = (Review)o;
				// If necessary load review before deleting stuff (so comments and tags will be deleted)
				if (!ra.isReviewLoaded(r.getId())){
					try {
						ra.loadReviewComments(r.getId());
					} catch (XmlException e) {
						PluginLogger.logError(this.getClass().toString(), "execute", "XmlException while loading comment of closed review "+r.getId(), e);
					} catch (IOException e) {
						PluginLogger.logError(this.getClass().toString(), "execute", "IOEXception while loading comment of closed review "+r.getId(), e);
					}
				}
				
				// Delete the selected review from ReviewExplorer
				if(ViewControl.isOpen(ReviewExplorer.class)) {
					ReviewExplorer.getInstance().deleteReview(new MultipleReviewWrapper(r, r.getId()));
				}
				ra.deleteReview(r.getId());
				// Check if this was the active review
				if (PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW).equals(r.getId()))
				{
					PropertiesManager.getPreferences().setToDefault(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
				}
				// Remove this review from the list of open reviews (regardless if it was open or not)
				pm.removeFromOpenReviews(r.getId());
			} else if(o instanceof Comment) {
				Comment c = (Comment)o;
				String keySeparator = pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
				String commentTag = c.getReviewID()+keySeparator+c.getAuthor()+keySeparator+c.getId();
				
				if (!MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Comment Details - Delete", "Are you sure you want to delete comment \""+commentTag+"\"?"))
				{
					return null;
				}
				if(ViewControl.isOpen(CommentTableView.class)) {
					CommentTableView.getInstance().deleteComment(c);
				}
				try {
					ra.deleteComment(c);
				} catch (IOException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "IOException occured while deleting a comment in ReviewAccess: "+c, e);
				}
				// Refresh the Review Explorer
				ViewControl.refreshViews(ViewControl.REVIEW_EXPLORER);
			}
		}
		return null;
	}

}
