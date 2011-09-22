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

/**
 * Handler for the "refresh" (F5) command for our review
 */
public class RefreshHandler extends AbstractHandler {
	
	/**
	 * Instance of ReviewAccess
	 */
	private  ReviewAccess ra = ReviewAccess.getInstance();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		PluginLogger.log(this.getClass().toString(), "execute", "Refresh triggered");
		// Refill the database
		try {
			ra.fillDatabaseForOpenReviews();
		} catch (XmlException e) {
			PluginLogger.logError(this.getClass().toString(), "execute", "XMLException is thrown", e);
		} catch (IOException e) {
			PluginLogger.logError(this.getClass().toString(), "execute", "IOException is thrown", e);
		}
		
		// Test if active review may have vanished
		String activeReview = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
		if (!ra.reviewExists(activeReview))
		{
			if (!ra.isReviewLoaded(activeReview))
			{
				// Active review has vanished --> deactivate it
				PropertiesManager.getPreferences().setToDefault(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
			}
		}
		
		ViewControl.refreshViews(ViewControl.ALL_VIEWS, true);

		// Return must be null (see API)
		return null;
	}

}