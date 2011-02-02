package de.tukl.cs.softech.agilereview.plugincontrol;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.detail.DetailView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;

/**
 * Handler for the "refresh" (F5) command for our review
 */
public class RefreshHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		PluginLogger.log(this.getClass().toString(), "execute", "Refresh triggered");
		// Refill the database
		try {
			ReviewAccess.getInstance().fillDatabaseForOpenReviews();
		} catch (XmlException e) {
			PluginLogger.logError(this.getClass().toString(), "execute", "XMLException is thrown", e);
		} catch (IOException e) {
			PluginLogger.logError(this.getClass().toString(), "execute", "IOException is thrown", e);
		}
		
		// Test if active review may have vanished
		String activeReview = PropertiesManager.getInstance().getExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
		if (!activeReview.isEmpty())
		{
			if (!ReviewAccess.getInstance().isReviewLoaded(activeReview))
			{
				// Active review has vanished --> deactivate it
				PropertiesManager.getInstance().setExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW, "");
			}
		}
		
		if(ViewControl.isOpen(ReviewExplorer.class)) {
			ReviewExplorer.getInstance().refreshInput();
		}
		if(ViewControl.isOpen(DetailView.class)) {
			DetailView.getInstance().changeParent(DetailView.EMPTY);
		}
		if(ViewControl.isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().resetComments();
		}


		// Return must be null (see API)
		return null;
	}

}
