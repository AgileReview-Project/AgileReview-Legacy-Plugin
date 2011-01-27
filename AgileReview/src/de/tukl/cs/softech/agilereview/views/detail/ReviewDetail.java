package de.tukl.cs.softech.agilereview.views.detail;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Text;

import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.explorer.wrapper.MultipleReviewWrapper;

/**
 * The ReviewDetail class describes one detail representation of a Review Object
 */
public class ReviewDetail extends AbstractDetail<Review> {
	
	/**
	 * TextArea to edit the person in charge of the given Review
	 */
	private Text authorInstance;
	/**
	 * Label to show the Review Name
	 */
	private Label reviewInstance;
	/**
	 * ComboBox to provide a choice for the Review status
	 */
	private Combo statusDropDown;
	/**
	 * TextBox to represent the Comment description in a modifiable way
	 */
	private StyledText txt;
	/**
	 * TextArea to edit an external reference to this Review
	 */
	private Text reference;

	/**
	 * Creates a new ReviewDetail Composite onto the given parent with the specified SWT styles
	 * @param parent onto the ReviewDetail Composite will be added
	 * @param style with which this Composite will be styled
	 */
	public ReviewDetail(Composite parent, int style) {
		super(parent, style);
	}

	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.view.detail.AbstractDetail#initUI()
	 */
	@Override
	protected void initUI() {
		GridLayout gridLayout = new GridLayout();
		int numColumns = 2;
		gridLayout.numColumns = numColumns;
		this.setLayout(gridLayout);
		
		Label review = new Label(this, SWT.PUSH);
		review.setText("Review: ");
	    
	    reviewInstance = new Label(this, SWT.PUSH);
	    GridData gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    reviewInstance.setLayoutData(gridData);
	    
	    Label refId = new Label(this, SWT.PUSH);
	    refId.setText("External reference: ");
	    
	    reference = new Text(this, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    reference.setLayoutData(gridData);
	    reference.addFocusListener(this);

	    Label author = new Label(this, SWT.PUSH);
	    author.setText("Responsibility: ");
	    
	    authorInstance = new Text(this, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    authorInstance.setLayoutData(gridData);
	    authorInstance.addFocusListener(this);
	    
	    Label status = new Label(this, SWT.PUSH);
	    status.setText("Status: ");
	    
	    statusDropDown = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.PUSH);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    statusDropDown.setLayoutData(gridData);
	    statusDropDown.addFocusListener(this);
	    
	    new Sash(this, SWT.PUSH);
	    
	    Label caption = new Label(this, SWT.PUSH);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns;
	    caption.setLayoutData(gridData);
	    caption.setText("Description:");
	    
	    txt = new StyledText(this, SWT.PUSH | SWT.V_SCROLL | SWT.BORDER);
	    txt.setVisible(true);
		txt.setWordWrap(true);
		txt.setEditable(true);
		txt.setEnabled(true);
	    txt.addFocusListener(this);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.verticalAlignment = GridData.FILL;
	    gridData.verticalSpan = 5;
	    gridData.horizontalSpan = numColumns;
	    gridData.grabExcessVerticalSpace = true;
	    gridData.grabExcessHorizontalSpace = true;
	    txt.setLayoutData(gridData);

	    /*Button delButton = new Button(this, SWT.PUSH);
	    delButton.setText("Delete");
	    delButton.setData("delete_review");
	    delButton.addListener(SWT.Selection, CommentController.getInstance());*/
	    
	    Composite g = new Composite(this, SWT.NONE);
	    GridLayout glayout = new GridLayout(3, false);
		g.setLayout(glayout);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.END;
	    gridData.horizontalSpan = numColumns;
	    g.setLayoutData(gridData);
	    
	    revertButton = new Button(g, SWT.PUSH);
	    //listener and settings will be set in AbstractDetail
	    
	    saveButton = new Button(g, SWT.PUSH);
	    //listener and settings will be set in AbstractDetail
	    
	    setPropertyConfigurations();
	}

	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.view.detail.AbstractDetail#saveChanges()
	 */
	@Override
	protected boolean saveChanges() {
		boolean result = false;
		String newStr;
		if(!(newStr = this.authorInstance.getText().trim()).equals(this.editedObject.getPersonInCharge().getName())) {
			this.editedObject.getPersonInCharge().setName(newStr);
			result = true;
		} if(!(newStr = this.reference.getText().trim()).equals(this.editedObject.getReferenceId())) {
			this.editedObject.setReferenceId(newStr);
			result = true;
		} else if(this.statusDropDown.getSelectionIndex() != this.editedObject.getStatus()) {
			this.editedObject.setStatus(this.statusDropDown.getSelectionIndex());
			result = true;
		} else if(!(newStr = this.txt.getText().trim()).equals(this.editedObject.getDescription())) {
			this.editedObject.setDescription(newStr);
			result = true;
		}
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.view.detail.AbstractDetail#fillContents(java.lang.Object)
	 */
	public void fillContents(Review review) {
		if(review != null) {
			this.backupObject = (Review)review.copy();
			this.editedObject = review;
			this.reference.setText(review.getReferenceId());
			this.authorInstance.setText(review.getPersonInCharge().getName());
			this.reviewInstance.setText(review.getId());
			//review.getDescription() == null is the case in a new created Comment
			if(review.getDescription() != null) {
				this.txt.setText(review.getDescription());
			} else {
				this.txt.setText("");
			}
			statusDropDown.select(review.getStatus());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.view.detail.AbstractDetail#setFocus()
	 */
	@Override
	public boolean setFocus() {
		return this.txt.setFocus();
	}
	
	/**
	 * Sets the levels for the status and priority configuration of a comment.
	 */
	private void setPropertyConfigurations() {
		PropertiesManager pm = PropertiesManager.getInstance();				
		String value = pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.REVIEW_STATUS);
		String[] levels = value.split(",");
		statusDropDown.removeAll();
		for(int i = 0; i < levels.length; i++) {
			statusDropDown.add(levels[i]);
		}
	}
	
	/**
	 * Function which fills the contents of a given Review object
	 * into the ReviewDetailView
	 * @param reviewWrapper MultipleReviewWrapper which data should be displayed in the view
	 */
	public void fillContents(MultipleReviewWrapper reviewWrapper) {
		if(reviewWrapper != null) {
			Review review = reviewWrapper.getWrappedReview();
			if(review != null) {
				fillContents(review);
			}
		}
	}

}
