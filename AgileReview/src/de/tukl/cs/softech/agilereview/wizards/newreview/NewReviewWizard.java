package de.tukl.cs.softech.agilereview.wizards.newreview;

import java.io.IOException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import agileReview.softech.tukl.de.PersonInChargeDocument.PersonInCharge;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;

/**
 * Provides a wizard for creating a new Review via the NewWizard
 */
public class NewReviewWizard extends Wizard implements INewWizard {

	/**
	 * The first and sole page of the wizard 
	 */
	private NewReviewWizardPage page1;
	
	/**
	 * creates a new wizard
	 */
	public NewReviewWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("New Review");
	}
	
	/**
	 * adds all needed pages to the wizard
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		page1 = new NewReviewWizardPage();
		addPage(page1);
	}

	/**
	 * Execute the actual wizard command after all information was collected
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() 
	{
		boolean result = false;
		try 
		{
			Review newRev = ReviewAccess.getInstance().createNewReview(this.page1.getReviewID());
			if (newRev!=null) {
				newRev.setReferenceId(this.page1.getReviewReference());
				newRev.setDescription(this.page1.getReviewDescription());
				PersonInCharge piC = PersonInCharge.Factory.newInstance();
				piC.setName(this.page1.getReviewResponsibility());
				newRev.setPersonInCharge(piC);
				
				if (ViewControl.isOpen(ReviewExplorer.class)){
					ReviewExplorer.getInstance().addReview(newRev);
				}
				result = true;
			}
		} catch (IOException e) 
		{
			PluginLogger.logError(this.getClass().toString(), "performFinish", "Exception thrown while created a new Review", e);
		}
		
		return result;
	}

	/**
	 * not needed
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) { 
		if (ViewControl.getInstance().shouldSwitchPerspective()) {
			ViewControl.getInstance().switchPerspective();
		}
	}
}
