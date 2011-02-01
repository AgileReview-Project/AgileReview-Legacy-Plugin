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

/**
 * The single page of the NewReview Wizard
 */
public class NewReviewWizard1 extends WizardPage {

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
	 * Creates a new page
	 */
	protected NewReviewWizard1() {
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
		id.addKeyListener(new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (!id.getText().isEmpty()) {
					setPageComplete(true);
				}
			}

		});
		
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
		
		// mandatory hint
		Label lMandatory = new Label(container, SWT.NULL);
		lMandatory.setText("*) Review-ID is mandatory.");
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		id.setLayoutData(gd);
		reference.setLayoutData(gd);
		responsibility.setLayoutData(gd);
		description.setLayoutData(new GridData(GridData.FILL_BOTH));
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

}
