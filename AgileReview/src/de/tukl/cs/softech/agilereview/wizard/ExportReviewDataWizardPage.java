package de.tukl.cs.softech.agilereview.wizard;

import java.awt.Checkbox;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import agileReview.softech.tukl.de.ReviewDocument.Review;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * The single page of the NewReview Wizard
 */
public class ExportReviewDataWizardPage extends WizardPage implements SelectionListener {

	private boolean pathSelected = false;
	private boolean reviewsSelected = false;
	private Label responsibility;
	private Label description;
	/**
	 * The textfield containing the path
	 */
	private Text pathText;
	/**
	 * represents the reviews selected in the checkboxtreeview
	 */
	private HashMap<String, Review> reviews = new HashMap<String, Review>();
	private ArrayList<String> selectedReviews = new ArrayList<String>();
	private Composite parent;
	
	/**
	 * @return the path selected for exporting review data
	 */
	public String getPath() {
		return pathText.getText();
	}

	/**
	 * @return the ids of the reviews currently selected in the checkboxtreeviewer
	 */
	public ArrayList<String> getSelectedReviewIDs() {
		return selectedReviews;
	}

	/**
	 * Creates a new page
	 */
	protected ExportReviewDataWizardPage() {
		super("ExportReviewDataWizard1");
		setTitle("Export Review Data");
		setDescription("This wizard exports the data of selected AgileReviews to an XLS/XLSX-File.");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		this.parent = parent;
		
		for (Review review : ReviewAccess.getInstance().getAllReviews()) {
			reviews.put(review.getId(), review);
		}
		
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		
		Label pathLabel = new Label(container, SWT.NULL);
		pathLabel.setText("Export location:");
		
		pathText = new Text(container, SWT.BORDER | SWT.SINGLE);
		pathText.setEditable(false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		pathText.setLayoutData(gd);
		
		Button browseButton = new Button(container, SWT.NULL);
		browseButton.setText("Browse...");
		browseButton.addSelectionListener(this);
		
		GridData rlGD = new GridData(GridData.FILL_HORIZONTAL);
		rlGD.horizontalSpan = 3;
		Label spacer = new Label(container, SWT.NULL);
		spacer.setText("");
		spacer.setLayoutData(rlGD);
		
		Label reviewLabel = new Label(container, SWT.NULL);
		reviewLabel.setText("Select AgileReviews to export:");
		reviewLabel.setLayoutData(rlGD);

		
		CheckboxTreeViewer cbtreeviewer = new CheckboxTreeViewer(container);
		cbtreeviewer.setContentProvider(new ExportTreeViewContentProvider());
		cbtreeviewer.setLabelProvider(new LabelProvider());
		cbtreeviewer.setInput(ReviewAccess.getInstance().getAllReviews());
		GridData tvGridData = new GridData(GridData.FILL_BOTH);
		tvGridData.verticalSpan = 2;
		tvGridData.horizontalSpan = 2;
		cbtreeviewer.getTree().setLayoutData(tvGridData);
		cbtreeviewer.getTree().addSelectionListener(this);

		GridData resGridData = new GridData(GridData.FILL_HORIZONTAL);
		resGridData.horizontalSpan = 1;
		
		responsibility = new Label(container, SWT.NULL);
		responsibility.setText("Responsibility:");
		responsibility.setLayoutData(resGridData);
		
		GridData descGridData = new GridData(GridData.FILL_BOTH);
		descGridData.horizontalSpan = 1;
		description = new Label(container, SWT.WRAP);
		
		description.setText("Description:");
		description.setLayoutData(descGridData);
		
		// Required to avoid an error in the system
		setControl(container);
		setPageComplete(false);
	}

	/* not yet used
	 *  (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (e.widget instanceof Button) {
			DirectoryDialog dlg = new DirectoryDialog(new Shell());

			// Set the initial filter path according
			// to anything they've selected or typed in
			dlg.setFilterPath(pathText.getText());

			// Change the title bar text
			dlg.setText("AgileReview Export");

			// Customizable message displayed in the dialog
			dlg.setMessage("Select export location");

			// Calling open() will open and run the dialog.
			// It will return the selected directory, or
			// null if user cancels
			String dir = dlg.open();
			if (dir != null) {
				// Set the text box to the new selection
				pathText.setText(dir);
			}
			if (!pathText.getText().isEmpty()) {
				pathSelected = true;
			}
			
		} else if (e.widget instanceof Tree) {
			
			if (((Tree) e.widget).getSelection().length == 1) {
				TreeItem selectedTI = (TreeItem) e.item;
				String selectedReviewID = selectedTI.getText();
				if (selectedTI.getChecked()) {
					if (!selectedReviews.contains(selectedReviewID)) {
						selectedReviews.add(selectedReviewID);
					}
				} else if (selectedReviews.contains(selectedReviewID)) {
					selectedReviews.remove(selectedReviewID);
				}
				responsibility.setText("Responsibility: "+reviews.get(selectedReviewID).getPersonInCharge().getName());
				description.setText("Description:\n"+reviews.get(selectedReviewID).getDescription());
				
				reviewsSelected = selectedReviews.size()>0;
				
			}
	
		}
		
		setPageComplete(reviewsSelected && pathSelected);
		
	}
	
}