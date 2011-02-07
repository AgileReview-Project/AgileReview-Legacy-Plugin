package de.tukl.cs.softech.agilereview.wizards.export;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
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

	/**
	 * indicates whether a path for exporting has been selected or not
	 */
	private boolean pathsSelected = false;
	/**
	 * indicates if one or more review(s) have been selected for exporting
	 */
	private boolean reviewsSelected = false;
	/**
	 * a label that displays the person in charge of the review that is selected
	 */
	private Label responsibility;
	/**
	 * a label that displays the description of the review that is selected
	 */
	private Label description;
	/**
	 * The textfield containing the path for the export template
	 */
	private Text templatePathText;
	/**
	 * The textfield containing the path
	 */
	private Text exportPathText;
	/**
	 * a map of all reviews that are currently opened and their ids
	 */
	private HashMap<String, Review> reviews = new HashMap<String, Review>();
	/**
	 * all reviews that are selected in the wizard
	 */
	private ArrayList<String> selectedReviews = new ArrayList<String>();
	
	/**
	 * @return the path selected for exporting review data
	 */
	public String getExportPath() {
		return exportPathText.getText();
	}
	
	/**
	 * @return the path selected for export template
	 */
	public String getTemplatePath() {
		return templatePathText.getText();
	}

	/**
	 * @return the ids of the reviews currently selected in the checkboxtreeviewer
	 */
	public ArrayList<String> getSelectedReviewIDs() {
		return selectedReviews;
	}
	
	/**
	 * @return the reviews currently selected in the checkboxtreeviewer
	 */
	public ArrayList<Review> getSelectedReviews() {
		ArrayList<Review> result = new ArrayList<Review>();
		for (String id : selectedReviews) {
			result.add(reviews.get(id));
		}
		return result;
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
		
		//get reviews from ReviewAccess
		for (Review review : ReviewAccess.getInstance().getAllReviews()) {
			reviews.put(review.getId(), review);
		}
		
		// create page ui
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		
		// ui elements for template selection
		Label templateLabel = new Label(container, SWT.NULL);
		templateLabel.setText("Template for XLS export:");
		
		templatePathText = new Text(container, SWT.BORDER | SWT.SINGLE);
		templatePathText.setText(PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.TEMPLATE_PATH));			
		templatePathText.setEditable(false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		templatePathText.setLayoutData(gd);
		Button browseButtonTemplate = new Button(container, SWT.NULL);
		browseButtonTemplate.setText("Browse...");
		browseButtonTemplate.setData("template");
		browseButtonTemplate.addSelectionListener(this);
		
		// ui elements for selecting export path
		Label pathLabel = new Label(container, SWT.NULL);
		pathLabel.setText("XLS export location:");
		
		exportPathText = new Text(container, SWT.BORDER | SWT.SINGLE);
		exportPathText.setText(PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.EXPORT_PATH));	
		exportPathText.setEditable(false);
		exportPathText.setLayoutData(gd);
		
		Button browseButton = new Button(container, SWT.NULL);
		browseButton.setText("Browse...");
		browseButton.setData("path");
		browseButton.addSelectionListener(this);
		// spacer to generate some space between path and review selection
		GridData rlGD = new GridData(GridData.FILL_HORIZONTAL);
		rlGD.horizontalSpan = 3;
		Label spacer = new Label(container, SWT.NULL);
		spacer.setText("");
		spacer.setLayoutData(rlGD);
		
		// ui elements for selecting reviews
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
		
		pathsSelected = !templatePathText.getText().isEmpty() && !exportPathText.getText().isEmpty();
				
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

	/* 
	 * Browse-button was pressed or review selection changed
	 * (non-Javadoc)
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		// browse button pressed
		if (e.widget instanceof Button) {
			// browse button for template pressed
			if (e.widget.getData().equals("template")) {
				FileDialog dlg = new FileDialog(new Shell());

				// Change the title bar text
				dlg.setText("AgileReview Export");
				String[] filterExtensions = {"*.xls*"};
				dlg.setFilterExtensions(filterExtensions);

				// Calling open() will open and run the dialog.
				// It will return the selected directory, or
				// null if user cancels
				String dir = dlg.open();
				if (dir != null) {
					templatePathText.setText(dir);
				}
			} else if (e.widget.getData().equals("path")) {
			// browse button for export path pressed
				DirectoryDialog dlg = new DirectoryDialog(new Shell());

				// Change the title bar text
				dlg.setText("AgileReview Export");

				// Calling open() will open and run the dialog.
				// It will return the selected directory, or
				// null if user cancels
				String dir = dlg.open();
				if (dir != null) {
					exportPathText.setText(dir);
				}
			}
			pathsSelected = !exportPathText.getText().isEmpty() && !templatePathText.getText().isEmpty();
		} else if (e.widget instanceof Tree) {
		// selection of reviews changed	
			if (((Tree) e.widget).getSelection().length == 1) {
				TreeItem selectedTI = (TreeItem) e.item;
				String selectedReviewID = selectedTI.getText();
				if (selectedTI.getChecked()) {
					// review not in list and checked, add it
					if (!selectedReviews.contains(selectedReviewID)) {
						selectedReviews.add(selectedReviewID);
					}
				} else if (selectedReviews.contains(selectedReviewID)) {
					// review is in list, but not checked, remove it
					selectedReviews.remove(selectedReviewID);
				}
				// adapt labels for responsibility and description
				responsibility.setText("Responsibility: "+reviews.get(selectedReviewID).getPersonInCharge().getName());
				description.setText("Description:\n"+reviews.get(selectedReviewID).getDescription());
				
				reviewsSelected = selectedReviews.size()>0;
				
			}
	
		}
		
		// page complete if >0 reviews and path selected
		setPageComplete(reviewsSelected && pathsSelected);
		
	}
	
}