package de.tukl.cs.softech.agilereview.wizards.export;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;

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
	public ExportReviewDataWizard(Set<String> selectedReviews) {
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
	public boolean performFinish() {
		try {
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(ExportReviewDataWizard.this.getShell());
			pmd.open();
			pmd.run(true, false, new XSLExport(page1.getSelectedReviews(), page1.getTemplatePath(), page1.getExportPath()));
			pmd.close();
		} catch (InvocationTargetException e) {
			PluginLogger.logError(this.getClass().toString(),"performFinish", "InvocationTargetException", e);
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error while exporting Reviews", "An Eclipse internal error occured!\nRetry and please report the bug when it occurs again.\nCode:1");
		} catch (InterruptedException e) {
			PluginLogger.logError(this.getClass().toString(),"performFinish", "InterruptedException", e);
			MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error while exporting Reviews", "An Eclipse internal error occured!\nRetry and please report the bug when it occurs again.\nCode:2");
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
