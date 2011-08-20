package de.tukl.cs.softech.agilereview.wizards.noreviewsource;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard for choosing a AgileReview Source Project if non is existent
 */
public class NoReviewSourceWizard extends Wizard implements IWizard {/*?|0000004 + 0000006|Thilo|c1|?*/

	/**
	 * The first and sole page of the wizard 
	 */
	private NoReviewSourceWizardPage page1;
	
	/**
	 * The name of the project created by this wizard
	 */
	private String chosenProjectName = null;
	
	/**
	 * Standard constructor
	 */
	public NoReviewSourceWizard() {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("No AgileReview Source Project");
	}

	
	/**
	 * adds all needed pages to the wizard
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		page1 = new NoReviewSourceWizardPage();
		addPage(page1);
	}
	
	@Override
	public boolean performFinish() {
		chosenProjectName = page1.getReviewSourceName();
		return chosenProjectName != null;
	}
	
	@Override
	public boolean performCancel() {
		return MessageDialog.openConfirm(getShell(), "Cancel", "Are you sure you want to cancel? Agilereview will not work until you create and choose an AgileReview Source Folder.");
	}
	
	/**
	 * Returns the name of the project chosen to be the current AgileReview Source Project
	 * @return name of the project chosen
	 */
	public String getChosenProjectName(){
		return chosenProjectName;
	}

}
