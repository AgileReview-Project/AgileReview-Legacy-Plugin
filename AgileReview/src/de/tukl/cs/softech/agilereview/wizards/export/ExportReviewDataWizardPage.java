package de.tukl.cs.softech.agilereview.wizards.export;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
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
public class ExportReviewDataWizardPage extends WizardPage implements SelectionListener, ModifyListener {

	/**
	 * indicates whether a path for exporting are valid or not
	 */
	private boolean pathsValid = false;
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
	 * Label which displays usage errors
	 */
	private Label errorLabel;
	/**
	 * TreeViewer of Reviews to be exported
	 */
	private CheckboxTreeViewer cbtreeviewer;
	/**
	 * a map of all reviews that are currently opened and their ids
	 */
	private HashMap<String, Review> reviews = new HashMap<String, Review>();
	/**
	 * all reviews that are selected in the wizard
	 */
	private HashSet<String> selectedReviewIDs = new HashSet<String>();

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
		templatePathText.addModifyListener(this);
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
		exportPathText.addModifyListener(this);
		
		Button browseButton = new Button(container, SWT.NULL);
		browseButton.setText("Browse...");
		browseButton.setData("path");
		browseButton.addSelectionListener(this);
		
		GridData rlGD = new GridData(GridData.FILL_HORIZONTAL);
		rlGD.horizontalSpan = 3;
		errorLabel = new Label(container, SWT.NULL);
		errorLabel.setText("");
		errorLabel.setAlignment(SWT.CENTER);
		errorLabel.setForeground(new Color(this.getShell().getDisplay(), 255, 0, 0));
		errorLabel.setLayoutData(rlGD);
		
		// spacer to generate some space between path and review selection
		Label spacer = new Label(container, SWT.NULL);
		spacer.setText("");
		spacer.setLayoutData(rlGD);
		
		// ui elements for selecting reviews
		Label reviewLabel = new Label(container, SWT.NULL);
		reviewLabel.setText("Select AgileReviews to export:");
		reviewLabel.setLayoutData(rlGD);
		
		cbtreeviewer = new CheckboxTreeViewer(container);
		cbtreeviewer.setContentProvider(new ExportTreeViewContentProvider());
		cbtreeviewer.setLabelProvider(new ExportTreeViewLabelProvider());
		Collection<Review> allReviews = reviews.values();
		ArrayList<Review> openReviews = new ArrayList<Review>();
		for (Review r : allReviews) {
			if (ReviewAccess.getInstance().isReviewLoaded(r.getId())) {
				openReviews.add(r);
			}
		}
		cbtreeviewer.setInput(openReviews);
		GridData tvGridData = new GridData(GridData.FILL_BOTH);
		tvGridData.verticalSpan = 2;
		tvGridData.horizontalSpan = 2;
		cbtreeviewer.getTree().setLayoutData(tvGridData);
		cbtreeviewer.getTree().addSelectionListener(this);
		
		//select initial reviews
		for(String id : selectedReviewIDs) {
			cbtreeviewer.setChecked(reviews.get(id), true);
		}

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
		
		pathsValid = checkPathValidity();
				
		// Required to avoid an error in the system
		setControl(container);
		setPageComplete(reviewsSelected && pathsValid);
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
		} else if (e.widget instanceof Tree) {
		// selection of reviews changed	
			if (((Tree) e.widget).getSelection().length == 1) {
				TreeItem selectedTI = (TreeItem) e.item;
				String selectedReviewID = selectedTI.getText();
				if (selectedTI.getChecked()) {
					// review not in list and checked, add it
					if (!selectedReviewIDs.contains(selectedReviewID)) {
						selectedReviewIDs.add(selectedReviewID);
					}
				} else if (selectedReviewIDs.contains(selectedReviewID)) {
					// review is in list, but not checked, remove it
					selectedReviewIDs.remove(selectedReviewID);
				}
				// adapt labels for responsibility and description
				responsibility.setText("Responsibility: "+reviews.get(selectedReviewID).getPersonInCharge().getName());
				description.setText("Description:\n"+reviews.get(selectedReviewID).getDescription());
				
				reviewsSelected = selectedReviewIDs.size()>0;
				
			}
	
		}
		
		// page complete if >0 reviews and path selected
		setPageComplete(reviewsSelected && pathsValid);
		
	}

	@Override
	public void modifyText(ModifyEvent e) {
		if(e.getSource().equals(this.templatePathText)) {
			this.pathsValid = checkPathValidity();
		} else if(e.getSource().equals(this.exportPathText)) {
			this.pathsValid = checkPathValidity();
		}
		setPageComplete(reviewsSelected && pathsValid);
	}
	
	/**
	 * Checks whether all paths are valid
	 * @return true, if all paths are valid<br>
	 * false, otherwise
	 */
	private boolean checkPathValidity() {
		File templatePath = new File(templatePathText.getText());
		File exportPath = new File(exportPathText.getText());
		if (templatePath.exists() && exportPath.exists() && !templatePathText.getText().isEmpty() && !exportPathText.getText().isEmpty()) {
			this.errorLabel.setText("");
			return true;
		} else {
			this.errorLabel.setText("One or more of the selected paths do not exist.");
			return false;
		}
	}
	
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
	public HashSet<String> getSelectedReviewIDs() {
		return selectedReviewIDs;
	}
	
	/**
	 * @return the reviews currently selected in the checkboxtreeviewer
	 */
	public ArrayList<Review> getSelectedReviews() {
		ArrayList<Review> result = new ArrayList<Review>();
		for (String id : selectedReviewIDs) {
			result.add(reviews.get(id));
		}
		return result;
	}
	
	/**
	 * Sets the reviews to be selected
	 * @param selectedReviews
	 */
	public void setSelectedReviews(Set<String> selectedReviews) {
		this.selectedReviewIDs.clear();
		this.selectedReviewIDs.addAll(selectedReviews);
	}
}