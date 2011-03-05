package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * Handler for opening or closing the reviews selected in the ReviewExplorer
 * Active when: ReviewExplorer is activePart
 * Enabled when: arbitrary number of MultipleReviewWrappers are selected
 */
public class OpenCloseReviewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "\"Open/Close in ReviewExplorer selected review\" triggered");
		
		ISelection sel1 = HandlerUtil.getCurrentSelection(event);
		if (sel1 != null){
			if (sel1 instanceof IStructuredSelection)
			{
				for (Object o: ((IStructuredSelection)sel1).toArray())
				{	
					if (o instanceof MultipleReviewWrapper)
					{
						MultipleReviewWrapper selectedWrap = (MultipleReviewWrapper)o;
						String reviewId = selectedWrap.getReviewId();
						if (selectedWrap.isOpen())
						{	
							// Review is open --> close it
							PluginLogger.log(this.getClass().toString(), "openCloseReview", "Review "+selectedWrap.getReviewId()+" will be closed");
							selectedWrap.setOpen(false);
							ReviewAccess.getInstance().unloadReviewComments(reviewId);
							PropertiesManager.getInstance().removeFromOpenReviews(reviewId);
							
							// Test if active review may have vanished
							String activeReview = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
							if (activeReview.equals(reviewId))
							{
								if (!ReviewAccess.getInstance().isReviewLoaded(reviewId))
								{
									// Active review has vanished --> deactivate it
									PropertiesManager.getPreferences().setToDefault(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
								}
							}
						}
						else
						{
							// Review is closed --> open it
							PluginLogger.log(this.getClass().toString(), "openCloseReview", "Review "+selectedWrap.getReviewId()+" will be opened");
							selectedWrap.setOpen(true);
							try 
							{
								ReviewAccess.getInstance().loadReviewComments(reviewId);
							} catch (XmlException e) {
								PluginLogger.logError(this.getClass().toString(), "execute", "Review "+selectedWrap.getReviewId()+" could not be opened", e);				
							} catch (IOException e) {
								PluginLogger.logError(this.getClass().toString(), "execute", "Review "+selectedWrap.getReviewId()+" could not be opened", e);
							}
							PropertiesManager.getInstance().addToOpenReviews(reviewId);
						}	
					}
				}
				if(ViewControl.isOpen(ReviewExplorer.class)) {
					ReviewExplorer.getInstance().refresh();
					ReviewExplorer.getInstance().validateExplorerSelection();
				}
				if(ViewControl.isOpen(CommentTableView.class)) {
					CommentTableView.getInstance().resetComments();
				}
			}
		}
		return null;
	}
}
