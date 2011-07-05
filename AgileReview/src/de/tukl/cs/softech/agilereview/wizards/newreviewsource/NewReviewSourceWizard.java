package de.tukl.cs.softech.agilereview.wizards.newreviewsource;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;

/**
 * Wizard for creating a new AgileReview source project
 */
public class NewReviewSourceWizard extends Wizard implements INewWizard {

	/**
	 * The first and sole page of the wizard 
	 */
	private NewReviewSourceWizardPage page1;
	
	/**
	 * Constructor
	 */
	public NewReviewSourceWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}
	
	/**
	 * adds all needed pages to the wizard
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		page1 = new NewReviewSourceWizardPage();
		addPage(page1);
	}

	@Override
	public boolean performFinish() {
		String projectName = page1.getReviewSourceName();
		boolean useDirectly = page1.getUseDirectly();
		
		boolean result = ReviewAccess.createAndOpenReviewProject(projectName);
			
		if (useDirectly) {
			PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, projectName);
			if (ReviewAccess.getInstance().updateReviewSourceProject()) {
				ViewControl.refreshViews(ViewControl.ALL_VIEWS, true);
			}
		}
		
		return result;
	}

}
