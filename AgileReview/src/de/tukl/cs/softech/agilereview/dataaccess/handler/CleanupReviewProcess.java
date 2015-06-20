package de.tukl.cs.softech.agilereview.dataaccess.handler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.annotations.TagCleaner;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.plugincontrol.ExceptionHandler;
import de.tukl.cs.softech.agilereview.plugincontrol.exceptions.NoReviewSourceFolderException;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;

/**
 * Class that performs the cleanup process
 */
public class CleanupReviewProcess implements IRunnableWithProgress {

	/**
	 * the review to clean
	 */
	private final Review review;
	/**
	 * delete (true) or keep (false) comments
	 */
	private final boolean deleteComments;
	/**
	 * process only closed comments during cleanup
	 */
	private boolean onlyClosedComments;

	/**
	 * Constructor of the Cleanup process
	 *
	 * @param review
	 *            the review to clean (remove tags)
	 * @param deleteComments
	 *            indicates whether to delete (true) or keep (false) comments
	 * @param onlyClosedComments 
	 */
	public CleanupReviewProcess(Review review, boolean deleteComments, boolean onlyClosedComments) {
		this.review = review;
		this.deleteComments = deleteComments;
		this.onlyClosedComments = onlyClosedComments;
	}

	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		ReviewAccess ra = ReviewAccess.getInstance();

		monitor.beginTask("Performing cleanup: ", 100);
		monitor.worked(0);

		monitor.subTask("Loading related comments ...");
		List<Comment> comments = ra.getComments(this.review.getId());
		monitor.worked(10);

		monitor.subTask("Deleting comments ...");
		float progressStep = 90f / comments.size();
		int i = 0;
		for (Comment c : comments) {
			String key = ra.generateCommentKey(c);
			if (!onlyClosedComments || c.getStatus() == 1) {
				TagCleaner.removeTag(new Path(ReviewAccess.computePath(c)), key);
				if (this.deleteComments) {
					try {
						ReviewAccess.getInstance().deleteComment(c);	
					} catch (IOException e) {
						PluginLogger.logError(this.getClass().toString(), "execute", "IOException occured while deleting a comment in ReviewAccess: " + c, e);
					} catch (NoReviewSourceFolderException e) {
						ExceptionHandler.handleNoReviewSourceFolderException();
					}
				}
			}
			i++;
			monitor.worked(Math.round(i * progressStep) + 10);
		}
		monitor.worked(100);
		monitor.done();
	}
}