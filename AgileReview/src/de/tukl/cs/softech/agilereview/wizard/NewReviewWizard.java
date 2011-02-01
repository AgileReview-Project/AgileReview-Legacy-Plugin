package de.tukl.cs.softech.agilereview.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * Provides a wizard for creating a new Review via the NewWizard
 */
public class NewReviewWizard extends Wizard implements IWorkbenchWizard {

	/**
	 * The first and sole page of the wizard 
	 */
	NewReviewWizard1 page1;
	
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
		page1 = new NewReviewWizard1();
		addPage(page1);
	}


	/* 
	 * Execute the actual wizard command after all information was collected
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
//		try {
//			Review newReview = ReviewAccess.getInstance().createNewReview(page1.getReviewID());
//			newReview.setDescription(page1.getDescription());
//			PersonInCharge responsibility = PersonInCharge.Factory.newInstance();
//			responsibility.setName(page1.getReviewResponsibility());
//			newReview.setReferenceId(page1.getReviewReference());
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return true;
	}

	/* not needed
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) { /* TODO Auto-generated method stub */ }

}
