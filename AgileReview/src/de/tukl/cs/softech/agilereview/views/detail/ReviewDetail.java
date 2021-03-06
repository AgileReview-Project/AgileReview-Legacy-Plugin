package de.tukl.cs.softech.agilereview.views.detail;

import java.awt.Desktop;
import java.net.URI;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.plugincontrol.SourceProvider;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * The ReviewDetail class describes one detail representation of a Review Object
 */
public class ReviewDetail extends AbstractDetail<Review> implements SelectionListener {
    
    /**
     * TextArea to edit the person in charge of the given Review
     */
    private Text authorInstance;
    /**
     * Label to show the Review Name
     */
    private Text reviewInstance;
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
     * Button for opening the external reference in a browser
     */
    private Button referenceButton;
    
    /**
     * Creates a new ReviewDetail Composite onto the given parent with the specified SWT styles
     * @param parent onto the ReviewDetail Composite will be added
     * @param style with which this Composite will be styled
     */
    protected ReviewDetail(Composite parent, int style) {
        super(parent, style);
    }
    
    /*
     * (non-Javadoc)
     * @see de.tukl.cs.softech.agilereview.view.detail.AbstractDetail#initUI()
     */
    @Override
    protected void initUI() {
        GridLayout gridLayout = new GridLayout();
        int numColumns = 3;
        gridLayout.numColumns = numColumns;
        this.setLayout(gridLayout);
        
        Label review = new Label(this, SWT.PUSH);
        review.setText("Review: ");
        super.bgComponents.add(review);
        
        reviewInstance = new Text(this, SWT.WRAP);
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = numColumns - 1;
        reviewInstance.setLayoutData(gridData);
        reviewInstance.setEditable(false);
        super.bgComponents.add(reviewInstance);
        
        Label refId = new Label(this, SWT.PUSH);
        refId.setText("External reference: ");
        super.bgComponents.add(refId);
        
        reference = new Text(this, SWT.BORDER | SWT.SINGLE);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalSpan = numColumns - 2;
        reference.setLayoutData(gridData);
        reference.addFocusListener(this);
        
        referenceButton = new Button(this, SWT.PUSH);
        referenceButton.setImage(PropertiesManager.getInstance().getIcon(PropertiesManager.INTERNAL_KEYS.ICONS.BROWSE));
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.END;
        referenceButton.setLayoutData(gridData);
        referenceButton.addSelectionListener(this);
        referenceButton.setToolTipText("Interpret \"External reference\" as URI and open it");
        super.bgComponents.add(referenceButton);
        
        Label author = new Label(this, SWT.PUSH);
        author.setText("Responsibility: ");
        super.bgComponents.add(author);
        
        authorInstance = new Text(this, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = numColumns - 1;
        authorInstance.setLayoutData(gridData);
        authorInstance.addFocusListener(this);
        authorInstance.addModifyListener(this);
        
        Label status = new Label(this, SWT.PUSH);
        status.setText("Status: ");
        super.bgComponents.add(status);
        
        statusDropDown = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.PUSH);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = numColumns - 1;
        statusDropDown.setLayoutData(gridData);
        statusDropDown.addFocusListener(this);
        statusDropDown.addModifyListener(this);
        super.bgComponents.add(statusDropDown);
        
        Sash sash = new Sash(this, SWT.PUSH);
        sash.setVisible(false);
        
        Label caption = new Label(this, SWT.PUSH);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = numColumns;
        caption.setLayoutData(gridData);
        caption.setText("Description:");
        super.bgComponents.add(caption);
        
        txt = new StyledText(this, SWT.PUSH | SWT.V_SCROLL | SWT.BORDER);
        txt.setVisible(true);
        txt.setWordWrap(true);
        txt.setEditable(true);
        txt.setEnabled(true);
        txt.addFocusListener(this);
        txt.addModifyListener(this);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.verticalSpan = 5;
        gridData.horizontalSpan = numColumns;
        gridData.grabExcessVerticalSpace = true;
        gridData.grabExcessHorizontalSpace = true;
        txt.setLayoutData(gridData);
        
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
        if (!(newStr = this.authorInstance.getText().trim()).equals(this.editedObject.getPersonInCharge().getName())) {
            this.editedObject.getPersonInCharge().setName(newStr);
            result = true;
        }
        if (!(newStr = this.reference.getText().trim()).equals(this.editedObject.getReferenceId())) {
            this.editedObject.setReferenceId(newStr);
            result = true;
        }
        if (this.statusDropDown.getSelectionIndex() != this.editedObject.getStatus()) {
            this.editedObject.setStatus(this.statusDropDown.getSelectionIndex());
            result = true;
        }
        if (!(newStr = this.txt.getText().trim()).equals(this.editedObject.getDescription())) {
            this.editedObject.setDescription(super.convertLineBreaks(newStr));
            result = true;
        }
        return result;
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
        for (int i = 0; i < levels.length; i++) {
            statusDropDown.add(levels[i]);
        }
    }
    
    /**
     * Fills the contents of a given Review object into the ReviewDetailView
     * @param reviewWrapper MultipleReviewWrapper which data should be displayed in the view
     */
    protected void fillContents(MultipleReviewWrapper reviewWrapper) {
        if (reviewWrapper != null) {
            Review review = reviewWrapper.getWrappedReview();
            if (review != null) {
                fillContents(review);
            }
        }
    }
    
    /*
     * (non-Javadoc)
     * @see de.tukl.cs.softech.agilereview.view.detail.AbstractDetail#fillContents(java.lang.Object)
     */
    @Override
    protected void fillContents(Review review) {
        if (review != null) {
            this.backupObject = (Review) review.copy();
            this.editedObject = review;
            this.reference.setText(review.getReferenceId());
            this.authorInstance.setText(review.getPersonInCharge().getName());
            this.authorInstance.setToolTipText(review.getPersonInCharge().getName());
            this.reviewInstance.setText(review.getId());
            this.reviewInstance.setToolTipText(review.getId());
            
            if (review.getDescription() != null) {
                this.txt.setText(review.getDescription());
            } else {
                this.txt.setText("");
            }
            statusDropDown.select(review.getStatus());
        }
        //set revertable to false because it was set from the ModificationListener while inserting inital content
        ISourceProviderService isps = (ISourceProviderService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(
                ISourceProviderService.class);
        SourceProvider sp = (SourceProvider) isps.getSourceProvider(SourceProvider.REVERTABLE);
        sp.setVariable(SourceProvider.REVERTABLE, false);
    }
    
    @Override
    public void widgetSelected(SelectionEvent e) {
        try {
            URI uri = new URI(this.reference.getText());
            if (Desktop.isDesktopSupported()) {
                if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(uri);
                }
            } else {
                PluginLogger.logWarning(this.getClass().toString(), "widgetSelected", "\"java.awt.Desktop\" not supported by OS");
            }
        } catch (Exception ex) {
            PluginLogger.logError(this.getClass().toString(), "widgetSelected", "Can not open \"" + this.reference.getText()
                    + "\": It may not be a valid URI", ex);
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Invalid URI",
                    "External Reference is an unvalid URI:\n" + ex.getLocalizedMessage());
        }
    }
    
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }
    
    @Override
    protected Color determineBackgroundColor() {
        String prop = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.DEFAULT_REVIEW_COLOR);
        String[] rgb = prop.split(",");
        return new Color(PlatformUI.getWorkbench().getDisplay(), Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
    }
}
