package de.tukl.cs.softech.agilereview.wizards.noreviewsource;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;

/**
 * Wizard for choosing a AgileReview Source Project if non is existent
 */
public class NoReviewSourceWizard extends Wizard implements IWizard {

	/**
	 * The first and sole page of the wizard 
	 */
	private NoReviewSourceWizardPage page1;
	
	/**
	 * The name of the project created by this wizard
	 */
	private String chosenProjectName = null;
	/**
	 * Indicates, if the project should directly be set as 'AgileReview Source Project'
	 */
	private boolean setDirectly = true;
	
	/**
	 * Standard constructor. Equal to NoReviewSourceWizard(true)
	 */
	public NoReviewSourceWizard() {
		this(true);
	}

	/**
	 * Constructor for this wizard
	 * @param setDirectly if <code>true</code> the project will be set as 'AgileReview Source Project' directly, else the name will only be provided
	 */
	public NoReviewSourceWizard(boolean setDirectly) {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("No AgileReview Source Project");
		this.setDirectly = setDirectly;
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
		boolean result = chosenProjectName != null;
		if (result && setDirectly) {
			if (ReviewAccess.createAndOpenReviewProject(chosenProjectName)) {
				ReviewAccess.getInstance().loadReviewSourceProject(chosenProjectName);
			}
			ViewControl.refreshViews(ViewControl.ALL_VIEWS, true);
		}
		return result;
	}
	
	@Override
	public boolean performCancel() {
		MessageDialogWithToggle md = MessageDialogWithToggle.openOkCancelConfirm(getShell(), "Cancel", "Are you sure you want to cancel? Agilereview will not work until you create and choose an AgileReview Source Folder.", "Do not ask me on Ecilpse startup.", Activator.getDefault().getPreferenceStore().getString(PropertiesManager.EXTERNAL_KEYS.ASK_FOR_REVIEW_FOLDER).equals(MessageDialogWithToggle.ALWAYS), Activator.getDefault().getPreferenceStore(), PropertiesManager.EXTERNAL_KEYS.ASK_FOR_REVIEW_FOLDER);/*?|r108|Peter Reuter|c3|*/
		return md.getReturnCode()==MessageDialogWithToggle.OK ? true : false;/*|r108|Peter Reuter|c3|?*/
	}
	
	/**
	 * Returns the name of the project chosen to be the current AgileReview Source Project
	 * @return name of the project chosen
	 */
	public String getChosenProjectName(){
		return chosenProjectName;
	}

}