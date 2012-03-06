package de.tukl.cs.softech.agilereview.plugincontrol.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.plugincontrol.ExceptionHandler;
import de.tukl.cs.softech.agilereview.plugincontrol.exceptions.NoReviewSourceFolderException;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;

/**
 * Handler for the "refresh" (F5) command for our review
 */
public class RefreshHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "Refresh triggered");
		ReviewAccess ra = ReviewAccess.getInstance();
		// Refill the database
		try {
			ra.fillDatabaseForOpenReviews();
		
			// Test if active review may have vanished
			String activeReview = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
			if (!ra.reviewExists(activeReview))	{
				if (!ra.isReviewLoaded(activeReview)) {
					// Active review has vanished --> deactivate it
					PropertiesManager.getPreferences().setToDefault(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
				}
			}
		} catch (NoReviewSourceFolderException e) {
			ExceptionHandler.handleNoReviewSourceFolderException();
		}
		
		ViewControl.refreshViews(ViewControl.ALL_VIEWS, true);

		// Return must be null (see API)
		return null;
	}

}