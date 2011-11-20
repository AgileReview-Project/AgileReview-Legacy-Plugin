package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

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
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.AbstractMultipleWrapper;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * Handler for deleting comments based on the selection in the ReviewExplorer
 * Active when: ReviewExplorer is activePart
 * Enabled when: Arbitrary number of AbstractMulipleWrappers are selected
 */
public class DeleteHandler extends AbstractHandler {
	
	/**
	 * Instance of PropertiesManager
	 */
	private PropertiesManager pm = PropertiesManager.getInstance();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "\"Delete in ReviewExplorer\" triggered");
		ReviewAccess ra = ReviewAccess.getInstance();
		ISelection sel1 = HandlerUtil.getCurrentSelection(event);
		if (sel1 != null){
			// Check for not empty and if user really wants to deleted the selected reviews
			if (!sel1.isEmpty() && !MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Review Explorer - Delete", "Are you sure you want to delete all comments of this selection?"))
			{
				return null;
			}		
			if (sel1 instanceof IStructuredSelection) {
				IStructuredSelection structSel1 = (IStructuredSelection)sel1;
				for (Object o: structSel1.toArray())
				{	
					if (o instanceof AbstractMultipleWrapper)
					{
						AbstractMultipleWrapper wrap = (AbstractMultipleWrapper)o;
						if (wrap instanceof MultipleReviewWrapper)
						{
							// if the review is not open, open it, so its comments (and tags) will be deleted
							if (!ra.isReviewLoaded(wrap.getReviewId())){
									ra.loadReviewComments(wrap.getReviewId());
								}
							}
						// Delete comments of this review from TableView and from database
						for (Comment c : ra.getComments(wrap.getReviewId(), wrap.getPath()))
						{
							if (ViewControl.isOpen(CommentTableView.class)){
								CommentTableView.getInstance().deleteComment(c);
							}
							try {
								ra.deleteComment(c);
							} catch (IOException e) {
								PluginLogger.logError(this.getClass().toString(), "execute", "IOEXception while deleting comment "+c, e);
							}
						}
						
						// The following is only considered if a whole review should be deleted
						if (wrap instanceof MultipleReviewWrapper)
						{
							// Delete the selected review from ReviewExplorer
							if(ViewControl.isOpen(ReviewExplorer.class)) {
								ReviewExplorer.getInstance().deleteReview((MultipleReviewWrapper)wrap);
							}
							ra.deleteReview(wrap.getReviewId());
							// Check if this was the active review
							if (PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW).equals(wrap.getReviewId()))
							{
								PropertiesManager.getPreferences().setToDefault(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
							}
							// Remove this review from the list of open reviews (regardless if it was open or not)
							pm.removeFromOpenReviews(wrap.getReviewId());
						}
						// Refresh ReviewExplorer
						ViewControl.refreshViews(ViewControl.REVIEW_EXPLORER);
					}
				}
			}
		}		
		return null;
	}

}