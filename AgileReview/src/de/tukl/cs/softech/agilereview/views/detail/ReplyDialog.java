package de.tukl.cs.softech.agilereview.views.detail;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * The ReplyDialog is a small Wizard to insert new Replys to a document
 */
public class ReplyDialog extends Composite implements Listener {
	
	/**
	 * TextBox to insert the reply text
	 */
	private StyledText replyText;
	/**
	 * "ok" Button
	 */
	private Button okButton;
	
	/**
	 * boolean which indicates whether this wizard was canceled or not
	 */
	private boolean boolSaved = false;
	/**
	 * inserted author text
	 */
	private String strReplyAuthor = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.AUTHOR_NAME);;
	/**
	 * inserted reply text
	 */
	private String strReplyText = "";
	
	/**
	 * Creates a new dialog for entering replies
	 * @param parent
	 * @param style
	 */
	public ReplyDialog(Composite parent, int style) {
		super(parent, style);
		initUI();
	}	
		
	/**
	 * Creates the UI
	 */
	private void initUI() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		this.setLayout(gridLayout);
		
		replyText = new StyledText(this, SWT.PUSH | SWT.V_SCROLL | SWT.BORDER);
		replyText.setWordWrap(true);
		replyText.setEditable(true);
		replyText.setEnabled(true);
		replyText.setSize(50, 50);
		GridData gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.verticalAlignment = GridData.FILL;
	    gridData.horizontalSpan = 2;
	    gridData.grabExcessVerticalSpace = true;
	    gridData.grabExcessHorizontalSpace = true;
	    replyText.setLayoutData(gridData);  
	    
	    okButton = new Button(this, SWT.PUSH);
	    okButton.setText("Save");
	    okButton.addListener(SWT.Selection, this);
	    
	    Button cancelButton = new Button(this, SWT.PUSH);
	    cancelButton.setText("Cancel");
	    cancelButton.addListener(SWT.Selection, this);
	}
	
	/**
	 * Returns whether the Save-Button was pressed or not
	 * @return true for save button, false for cancel button
	 */
	public boolean getSaved() {
		return boolSaved;
	}
	
	/**
	 * Returns the author of the reply
	 * @return reply author
	 */
	public String getReplyAuthor() {
		return strReplyAuthor;
	}
	
	/**
	 * Return the reply's text
	 * @return reply text
	 */
	public String getReplyText(){
		return strReplyText;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		if (event.widget == okButton) {
			strReplyText = replyText.getText().trim();
			if(strReplyText.equals("")) {
				MessageDialog.openInformation(this.getShell(), "Information", 
		        		PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.COMMENT_EMPTY_REPLY_MESSAGE));
			} else {
				boolSaved = true;
				getParent().dispose();
			}
        } else {
        	boolSaved = false;
        	getParent().dispose();
        }        
	}
}

