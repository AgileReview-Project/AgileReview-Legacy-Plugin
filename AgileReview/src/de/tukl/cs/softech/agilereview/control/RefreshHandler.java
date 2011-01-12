package de.tukl.cs.softech.agilereview.control;

import java.io.IOException;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.view.DetailView;
import de.tukl.cs.softech.agilereview.view.ReviewExplorer;

/**
 * Handler for the "refresh" (F5) command for our review
 */
public class RefreshHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		System.out.println("Refresh triggered");
		// Refill the database
		try {
			ReviewAccess.getInstance().fillDatabaseForOpenReviews();
		} catch (XmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Test if active review may have vanished
		String activeReview = PropertiesManager.getInstance().getActiveReview();
		if (!activeReview.isEmpty())
		{
			if (!ReviewAccess.getInstance().isReviewLoaded(activeReview))
			{
				// Active review has vanished --> deactivate it
				PropertiesManager.getInstance().setActiveReview("");
			}
		}
		
		// Refresh the other views
//		try 
//		{
			ReviewExplorer.getInstance().refreshInput();
			DetailView.getInstance().changeParent(DetailView.EMPTY);
			// XXX Noch keine besseren Weg dafür
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().hideView(CommentTableView.getInstance());
//			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("de.tukl.cs.softech.agilereview.view.commenttableview.view");
//		} catch (PartInitException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// Return must be null (see API)
		return null;
	}

}
