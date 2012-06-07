package de.tukl.cs.softech.agilereview.views.detail.handlers;

import java.io.IOException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.handlers.HandlerUtil;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.plugincontrol.ExceptionHandler;
import de.tukl.cs.softech.agilereview.plugincontrol.exceptions.NoReviewSourceFolderException;
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
    private final PropertiesManager pm = PropertiesManager.getInstance();
    
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        if (ViewControl.isOpen(DetailView.class)) {
            Object o = DetailView.getInstance().getContent();
            if (o instanceof Review) {
                deleteReview(event, (Review) o);
            } else if (o instanceof Comment) {
                deleteComment(event, (Comment) o);
            }
        }
        return null;
    }
    
    /**
     * Deletes a review
     * @param event HandlerEvent of the current handler call
     * @param review to be deleted
     * @return true, if the deletion was successful<br>false, otherwise
     */
    private boolean deleteReview(ExecutionEvent event, Review review) {
        if (!MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Review Details - Delete", "Are you sure you want to delete this review?")) { return false; }
        
        ReviewAccess ra = ReviewAccess.getInstance();
        try {
            // If necessary load review before deleting stuff (so comments and tags will be deleted)
            if (!ra.isReviewLoaded(review.getId())) {
                ra.loadReviewComments(review.getId());
            }
            
            // Delete the selected review from ReviewExplorer
            if (ViewControl.isOpen(ReviewExplorer.class)) {
                ReviewExplorer.getInstance().deleteReview(new MultipleReviewWrapper(review, review.getId()));
            }
            ra.deleteReview(review.getId());
            // Check if this was the active review
            if (PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW).equals(review.getId())) {
                PropertiesManager.getPreferences().setToDefault(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
            }
            // Remove this review from the list of open reviews (regardless if it was open or not)
            pm.removeFromOpenReviews(review.getId());
        } catch (NoReviewSourceFolderException e) {
            ExceptionHandler.handleNoReviewSourceFolderException();
        }
        return true;
    }
    
    /**
     * Deletes a comment
     * @param event HandlerEvent of the current handler call
     * @param comment to be deleted
     * @return true, if the deletion was successful<br>false, otherwise
     */
    private boolean deleteComment(ExecutionEvent event, Comment comment) {
        String keySeparator = pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
        String commentTag = comment.getReviewID() + keySeparator + comment.getAuthor() + keySeparator + comment.getId();
        
        if (!MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Comment Details - Delete", "Are you sure you want to delete comment \""
                + commentTag + "\"?")) { return false; }
        
        ReviewAccess ra = ReviewAccess.getInstance();
        try {
            if (ViewControl.isOpen(CommentTableView.class)) {/*?|r121|Malte|c0|*/
                CommentTableView.getInstance().deleteComment(comment);
            }
            ra.deleteComment(comment);/*|r121|Malte|c0|?*/
        } catch (IOException e) {
            PluginLogger.logError(this.getClass().toString(), "execute", "IOException occured while deleting a comment in ReviewAccess: " + comment,
                    e);
        } catch (NoReviewSourceFolderException e) {
            ExceptionHandler.handleNoReviewSourceFolderException();
        }
        
        // Refresh Review Explorer
        ViewControl.refreshViews(ViewControl.REVIEW_EXPLORER);
        return true;
    }
}