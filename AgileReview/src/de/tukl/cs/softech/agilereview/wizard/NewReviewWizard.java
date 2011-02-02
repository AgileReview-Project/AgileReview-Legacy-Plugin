package de.tukl.cs.softech.agilereview.wizard;

import java.io.IOException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import agileReview.softech.tukl.de.PersonInChargeDocument.PersonInCharge;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;

/**
 * Provides a wizard for creating a new Review via the NewWizard
 */
public class NewReviewWizard extends Wizard implements IWorkbenchWizard {

	/**
	 * The first and sole page of the wizard 
	 */
	NewReviewWizardPage page1;
	
	/**
	 * creates a new wizard
	 */
	public NewReviewWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/* 
	 * adds all needed pages to the wizard
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		page1 = new NewReviewWizardPage();
		addPage(page1);
	}


	/* 
	 * Execute the actual wizard command after all information was collected
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() 
	{
		boolean result = true;
		try 
		{
			Review newRev = ReviewAccess.getInstance().createNewReview(this.page1.getReviewID());
			newRev.setReferenceId(this.page1.getReviewReference());
			newRev.setDescription(this.page1.getReviewDescription());
			PersonInCharge piC = PersonInCharge.Factory.newInstance();
			piC.setName(this.page1.getReviewResponsibility());
			newRev.setPersonInCharge(piC);
			
			if (ViewControl.isOpen(ReviewExplorer.class)){
				ReviewExplorer.getInstance().addNewReview(newRev);
			}
		} catch (IOException e) 
		{
			PluginLogger.logError(this.getClass().toString(), "performFinish", "Exception thrown while created a new Review", e);
			result = false;
		}
		
		return result;
	}

	/* not needed
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) { /* TODO Auto-generated method stub */ }

}
