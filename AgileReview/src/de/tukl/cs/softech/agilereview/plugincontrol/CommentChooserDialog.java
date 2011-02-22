package de.tukl.cs.softech.agilereview.plugincontrol;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * The ReplyDialog is a small Wizard to insert new Replys to a document
 */
public class CommentChooserDialog extends Composite implements Listener, KeyListener {
	
	/**
	 * TextBox to insert the reply text
	 */
	private Combo replyText;
	/**
	 * "ok" Button
	 */
	private Button okButton;
	
	/**
	 * boolean which indicates whether this wizard was canceled or not
	 */
	private boolean boolSaved = false;
	/**
	 * inserted reply text
	 */
	private String strReplyText = "";
	/**
	 * Tags from which the user can choose
	 */
	private String[] argsArr;
	
	/**
	 * Creates a new dialog for entering replies
	 * @param parent
	 * @param style
	 * @param args Comment tags from which to choose
	 */
	public CommentChooserDialog(Composite parent, int style, String[] args) {
		super(parent, style);
		this.argsArr = args;
		initUI();
	}	
		
	/**
	 * Creates the UI
	 */
	private void initUI() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		this.setLayout(gridLayout);
		this.addKeyListener(this);
		
		replyText = new Combo(this, SWT.PUSH | SWT.V_SCROLL | SWT.BORDER | SWT.READ_ONLY);
		replyText.setItems(this.argsArr);
		replyText.select(0);
		replyText.setFocus();
		
		GridData gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.verticalAlignment = GridData.FILL;
	    gridData.horizontalSpan = 2;
	    gridData.grabExcessVerticalSpace = true;
	    gridData.grabExcessHorizontalSpace = true;
	    replyText.setLayoutData(gridData);  
	    
	    okButton = new Button(this, SWT.PUSH);
	    okButton.setText("Ok");
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

	@Override
	public void keyPressed(KeyEvent e) {

		
	}

	@Override
	public void keyReleased(KeyEvent e) {/* Do nothing */}
}

