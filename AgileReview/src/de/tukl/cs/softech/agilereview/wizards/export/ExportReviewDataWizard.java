package de.tukl.cs.softech.agilereview.wizards.export;

import java.io.IOException;
import java.util.Set;

import net.sf.jxls.exception.ParsePropertyException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

import agileReview.softech.tukl.de.ReviewDocument.Review;

import de.tukl.cs.softech.agilereview.export.XSLExport;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;

/**
 * Provides a wizard for creating a new Review via the NewWizard
 */
public class ExportReviewDataWizard extends Wizard implements IWorkbenchWizard {

	/**
	 * The first and sole page of the wizard 
	 */
	private ExportReviewDataWizardPage page1 = new ExportReviewDataWizardPage();
	
	/**
	 * creates a new wizard
	 */
	public ExportReviewDataWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * 
	 * @param selectedReviews
	 */
	public ExportReviewDataWizard(Set<Review> selectedReviews) {
		super();
		setNeedsProgressMonitor(true);
		page1.setSelectedReviews(selectedReviews);
	}
	
	/**
	 * adds all needed pages to the wizard
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		addPage(page1);
	}

	/**
	 * Execute the actual wizard command after all information was collected
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() 
	{
		try {
			XSLExport.exportReviews(page1.getSelectedReviews(), page1.getTemplatePath(), page1.getExportPath());
		} catch (ParsePropertyException e) {
			PluginLogger.logError(this.getClass().toString(),"performFinish", "ParsePropertyException", e);
		} catch (InvalidFormatException e) {
			PluginLogger.logError(this.getClass().toString(),"performFinish", "InvalidFormatExceptionException", e);
		} catch (IOException e) {
			PluginLogger.logError(this.getClass().toString(),"performFinish", "IOException", e);
		}
		return true;
	}

	/**
	 * not needed
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) { /* Do nothing */ }

}
