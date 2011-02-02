package de.tukl.cs.softech.agilereview.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * The single page of the NewReview Wizard
 */
public class NewReviewWizardPage extends WizardPage implements KeyListener {

	/**
	 * the text field for retrieving the id
	 */
	private Text id;
	/**
	 * the text field for retrieving the reference
	 */
	private Text reference;
	/**
	 * the text field for retrieving the responsible person
	 */
	private Text responsibility;
	/**
	 * the text field for retrieving the description
	 */
	private Text description;
	/**
	 * Label to show an message to the user, if the reviewId is invlaid
	 */
	private Label lValid;
	
	/**
	 * Creates a new page
	 */
	protected NewReviewWizardPage() {
		super("NewReviewWizard1");
		setTitle("New Review");
		setDescription("This wizard creates a new AgileReview.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 2;
		Label lReviewId = new Label(container, SWT.NULL);
		lReviewId.setText("Review-ID*:");

		id = new Text(container, SWT.BORDER | SWT.SINGLE);
		id.setText("");
		id.addKeyListener(this);
		
		// external reference
		Label lReference = new Label(container, SWT.NULL);
		lReference.setText("Reference");
		reference = new Text(container, SWT.BORDER | SWT.SINGLE);
		
		// responsibility
		Label lResponsibility = new Label(container, SWT.NULL);
		lResponsibility.setText("Responsibility");
		responsibility = new Text(container, SWT.BORDER | SWT.SINGLE);
		
		// description
		Label lDescription = new Label(container, SWT.NULL);
		lDescription.setText("Description");
		//Text descTextField = new Text(container, SWT.BORDER | SWT.SINGLE);
		description = new Text(container, SWT.BORDER | SWT.H_SCROLL | SWT.MULTI);
		
		// not valid label + check
		lValid = new Label(container, SWT.NULL);
		lValid.setText("Review-ID is mandatory and has to be set");
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		id.setLayoutData(gd);
		reference.setLayoutData(gd);
		responsibility.setLayoutData(gd);
		description.setLayoutData(new GridData(GridData.FILL_BOTH));
		GridData gdValid = new GridData(GridData.FILL_HORIZONTAL);
		gdValid.horizontalSpan = 2;
		lValid.setLayoutData(gdValid);
		// Required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
	}
	
	/**
	 * @return the review ID entered
	 */
	public String getReviewID() {
		return this.id.getText();
	}
	
	/**
	 * @return the review reference entered
	 */
	public String getReviewReference() {
		return this.reference.getText();
	}
	
	/**
	 * @return the responsible person entered
	 */
	public String getReviewResponsibility() {
		return this.responsibility.getText();
	}
	
	/**
	 * @return the review description entered
	 */
	public String getReviewDescription() {
		return this.description.getText();
	}


	@Override
	public void keyPressed(KeyEvent e) {/* Do nothing */}

	@Override
	public void keyReleased(KeyEvent e) {
		String validMessage = PropertiesManager.getInstance().isValid(id.getText());
		if (!id.getText().isEmpty()) {
			if (validMessage == null){
				setPageComplete(true);
				lValid.setText("");
			}
			else {
				setPageComplete(false);
				lValid.setText(validMessage);
			}
		}
		else {
			setPageComplete(false);
			lValid.setText("Review-ID is mandatory and has to be set");
		}	
		
		lValid.redraw();
	}
}