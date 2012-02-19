package de.tukl.cs.softech.agilereview.wizards.noreviewsource;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;

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
		MessageDialogWithToggle md = MessageDialogWithToggle.openYesNoQuestion(getShell(), "Cancel Review Source folder selection", "Are you sure you want to cancel? Agilereview will not work until you create and choose an AgileReview Source Folder.", "Do not ask me on Eclipse startup.", PropertiesManager.getPreferences().getBoolean(PropertiesManager.EXTERNAL_KEYS.DO_NOT_ASK_FOR_REVIEW_FOLDER), null, null);/*?|r108|Peter Reuter|c3|*//*?|r108|Peter Reuter|c8|*/
		PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.DO_NOT_ASK_FOR_REVIEW_FOLDER, md.getToggleState());
		return md.getReturnCode()==IDialogConstants.YES_ID;/*|r108|Peter Reuter|c3|?*//*|r108|Peter Reuter|c8|?*/
	}
	
	/**
	 * Returns the name of the project chosen to be the current AgileReview Source Project
	 * @return name of the project chosen
	 */
	public String getChosenProjectName(){
		return chosenProjectName;
	}

}