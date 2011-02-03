package de.tukl.cs.softech.agilereview.wizard;

import java.io.IOException;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import agileReview.softech.tukl.de.PersonInChargeDocument.PersonInCharge;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.export.XSLExport;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;

/**
 * Provides a wizard for creating a new Review via the NewWizard
 */
public class ExportReviewDataWizard extends Wizard implements IWorkbenchWizard {

	/**
	 * The first and sole page of the wizard 
	 */
	ExportReviewDataWizardPage page1;
	
	/**
	 * creates a new wizard
	 */
	public ExportReviewDataWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * adds all needed pages to the wizard
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		page1 = new ExportReviewDataWizardPage();
		addPage(page1);
	}


	/**
	 * Execute the actual wizard command after all information was collected
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() 
	{
		return true;
	}

	/**
	 * not needed
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) { /* Do nothing */ }

}
