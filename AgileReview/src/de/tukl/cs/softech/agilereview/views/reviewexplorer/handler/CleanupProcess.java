package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.annotations.TagCleaner;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.plugincontrol.ExceptionHandler;
import de.tukl.cs.softech.agilereview.plugincontrol.exceptions.NoReviewSourceFolderException;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * Class that performs the cleanup process
 */
public class CleanupProcess implements IRunnableWithProgress {/*?|r120|Malte|c1|?*/
    
    /**
     * Instance of ReviewAccess
     */
    private static ReviewAccess ra = ReviewAccess.getInstance();/*?|r120|Thilo|c0|?*/
    /**
     * the review to clean
     */
    private final Review review;
    /**
     * delete (true) or keep (false) comments
     */
    private final boolean deleteComments;
    
    /**
     * Constructor of the Cleanup process
     * @param review the review to clean (remove tags)
     * @param deleteComments indicates whether to delete (true) or keep (false) comments
     */
    public CleanupProcess(Review review, boolean deleteComments) {
        this.review = review;
        this.deleteComments = deleteComments;
    }
    
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("Performing cleanup: ", 100);
        monitor.worked(0);
        
        monitor.subTask("Loading related comments ...");
        List<Comment> comments = ra.getComments(review.getId());
        monitor.worked(10);
        
        monitor.subTask("Deleting comments ...");
        float progressStep = 90f / comments.size();
        int i = 0;
        String key;
        for (final Comment c : comments) {
            key = generateCommentKey(c);
            if (deleteComments) {
                if (ViewControl.isOpen(CommentTableView.class)) {
                    Display.getDefault().syncExec(new Runnable() {
                        @Override
                        public void run() {
                            CommentTableView.getInstance().deleteComment(c);
                        }
                    });
                } else {
                    TagCleaner.removeTag(new Path(ReviewAccess.computePath(c)), key, false);
                }
                try {
                    ReviewAccess.getInstance().deleteComment(c);
                } catch (IOException e) {
                    PluginLogger.logError(this.getClass().toString(), "execute",
                            "IOException occured while deleting a comment in ReviewAccess: " + c, e);
                } catch (NoReviewSourceFolderException e) {
                    ExceptionHandler.handleNoReviewSourceFolderException();
                }
            } else {
                TagCleaner.removeTag(new Path(ReviewAccess.computePath(c)), key, false);
            }
            i++;
            monitor.worked(Math.round(i * progressStep) + 10);
        }
        monitor.worked(100);
        monitor.done();
    }
    
    /**
     * Generates the comment key for the given comment in the following scheme: reviewID|author|commendID
     * @param comment which comment key should be generated
     * @return comment key
     */
    private String generateCommentKey(Comment comment) {
        String keySeparator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
        String commentTag = comment.getReviewID() + keySeparator + comment.getAuthor() + keySeparator + comment.getId();
        return commentTag;
    }
}