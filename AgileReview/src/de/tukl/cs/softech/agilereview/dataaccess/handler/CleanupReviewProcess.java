package de.tukl.cs.softech.agilereview.dataaccess.handler;

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
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

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
	 * ignore open comments during cleanup
	 */
	private boolean ignoreOpenComments;

	/**
	 * Constructor of the Cleanup process
	 *
	 * @param review
	 *            the review to clean (remove tags)
	 * @param deleteComments
	 *            indicates whether to delete (true) or keep (false) comments
	 */
	public CleanupReviewProcess(Review review, boolean deleteComments, boolean ignoreOpenComments) {
		this.review = review;
		this.deleteComments = deleteComments;
		this.ignoreOpenComments = ignoreOpenComments;
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
		String key;
		for (final Comment c : comments) {
			key = ra.generateCommentKey(c);
			if (this.deleteComments && !(ignoreOpenComments && c.getStatus() == 0)) {
				if (ViewControl.isOpen(CommentTableView.class)) {
					Display.getDefault().syncExec(new Runnable() {
						@Override
						public void run() {
							CommentTableView.getInstance().deleteComment(c);
						}
					});
				} else {
					TagCleaner.removeTag(new Path(ReviewAccess.computePath(c)), key);
				}
				try {
					ReviewAccess.getInstance().deleteComment(c);
				} catch (IOException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "IOException occured while deleting a comment in ReviewAccess: " + c, e);
				} catch (NoReviewSourceFolderException e) {
					ExceptionHandler.handleNoReviewSourceFolderException();
				}
			} else {
				TagCleaner.removeTag(new Path(ReviewAccess.computePath(c)), key);
			}
			i++;
			monitor.worked(Math.round(i * progressStep) + 10);
		}
		monitor.worked(100);
		monitor.done();
	}
}