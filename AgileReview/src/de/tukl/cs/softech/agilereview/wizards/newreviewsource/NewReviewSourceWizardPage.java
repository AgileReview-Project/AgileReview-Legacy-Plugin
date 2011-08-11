package de.tukl.cs.softech.agilereview.wizards.newreviewsource;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * The single page of the NewReview Wizard
 */
public class NewReviewSourceWizardPage extends WizardPage implements ModifyListener {

	/**
	 * the text field for retrieving the id
	 */
	private Text name;
	/**
	 * Checkbox button to determine if folder should be used directly
	 */
	private Button use;

	
	/**
	 * Creates a new page
	 */
	protected NewReviewSourceWizardPage() {
		super("New Review Source Project");
		setTitle("New Review Source Project");
		setDescription("This wizard creates a new Review Source Project.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		
		// Textfield + Label
		Label lReviewId = new Label(container, SWT.NULL);
		lReviewId.setText("Review Source Project-Name*:");
		name = new Text(container, SWT.BORDER | SWT.SINGLE);
		name.setText(PropertiesManager.getPreferences().getDefaultString(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER));
		name.selectAll();
		name.setToolTipText("Review Source Folder -Name must be set.");
		name.addModifyListener(this);
		
		// Check-Box
		use = new Button(container, SWT.CHECK);
		use.setText("use this project after creation");
		use.setSelection(true);
	
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		name.setLayoutData(gd);
		use.setLayoutData(gd);
		// Required to avoid an error in the system
		setControl(container);
		this.modifyText(null);
		setErrorMessage(null);
	}
	
	/**
	 * @return the review ID entered
	 */
	protected String getReviewSourceName() {
		return this.name.getText().trim();
	}
	
	/**
	 * @return the review reference entered
	 */
	protected boolean getUseDirectly() {
		return this.use.getSelection();
	}
	
	
    /**
     * Returns whether this page's controls currently all contain valid 
     * values.
     *
     * @return <code>true</code> if all controls are valid, and
     *   <code>false</code> if at least one is invalid
     */
    private boolean validatePage() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();

        String projectFieldContents = name.getText();
        if (projectFieldContents.equals("")) { //$NON-NLS-1$
            setErrorMessage(null);
            return false;
        }

        IStatus nameStatus = workspace.validateName(projectFieldContents,
                IResource.PROJECT);
        if (!nameStatus.isOK()) {
            setErrorMessage(nameStatus.getMessage());
            return false;
        }
 
        IProject project = workspace.getRoot().getProject(
				projectFieldContents);
        
        if (project.exists()) {
            setErrorMessage("Project does already exist");
            return false;
        }

        setErrorMessage(null);
        setMessage(null);
        return true;
    }
	

	@Override
	public void modifyText(ModifyEvent e) {
		setPageComplete(validatePage());		
	}
}