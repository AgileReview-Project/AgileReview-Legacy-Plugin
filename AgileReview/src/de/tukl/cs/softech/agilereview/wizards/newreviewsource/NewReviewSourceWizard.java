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
	 * Specifies whether the useDirectly check-box is initially selected or not (default: true)
	 */
	private boolean bUseDirectlyInitial = true;
	/*?|0000004 + 0000006|Malte|c12|*/
	/**
	 * Specifies whether the useDirectly check-box is enabled or not (default: false)
	 */
	private boolean bFixUseDirectly = false;/*|0000004 + 0000006|Malte|c12|?*/
	/**
	 * The name of the project created by this wizard
	 */
	private String createdProjectName = null;
	
	/**
	 * Constructor with arguments for customized calling
	 * @param useDirectlyInitial specifies whether the useDirectly check-box is initially selected or not
	 * @param fixUseDirectly specifies whether the useDirectly check-box is enabled or not
	 */
	public NewReviewSourceWizard(boolean useDirectlyInitial, boolean fixUseDirectly) {
		super();
		setNeedsProgressMonitor(true);
		setWindowTitle("New AgileReview Source Project");
		this.bUseDirectlyInitial = useDirectlyInitial;
		this.bFixUseDirectly = fixUseDirectly;
	}
	/**
	 * Empty constructor for calling from eclipse
	 */
	public NewReviewSourceWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override/*?|0000004 + 0000006|Malte|c11|*/
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

	}/*|0000004 + 0000006|Malte|c11|?*/
	
	/**
	 * adds all needed pages to the wizard
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		super.addPages();
		page1 = new NewReviewSourceWizardPage(bUseDirectlyInitial, bFixUseDirectly);
		addPage(page1);
	}

	@Override
	public boolean performFinish() {
		String projectName = page1.getReviewSourceName();
		boolean useDirectly = page1.getUseDirectly();
		
		boolean result = ReviewAccess.createAndOpenReviewProject(projectName);
		if (result) {
			createdProjectName = projectName;
		}
		
		if (useDirectly) {
			PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, projectName);
			if (ReviewAccess.getInstance().updateReviewSourceProject()) {
				ViewControl.refreshViews(ViewControl.ALL_VIEWS, true);
			}
		}
		
		return result;
	}
	/**
	 * Returns the "result" of this wizard: The name of the created project
	 * @return name of the created project or null if project was not created
	 */
	public String getCreatedProjectName() {
		return createdProjectName;
	}

}