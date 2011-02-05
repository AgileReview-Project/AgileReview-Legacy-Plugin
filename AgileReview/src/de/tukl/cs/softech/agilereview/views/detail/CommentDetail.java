package de.tukl.cs.softech.agilereview.views.detail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReplyDocument.Reply;
import de.tukl.cs.softech.agilereview.plugincontrol.CommentController;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * The CommentDetail class describes one detail representation of a Comment Object
 */
public class CommentDetail extends AbstractDetail<Comment> {
	
	/**
	 * Label to show the author of the shown Comment
	 */
	private Label authorInstance;
	/**
	 * TextField to represent the recipient in a modifiable way
	 */
	private Text recipientText;
	/**
	 * TextBox to represent the Comment description in a modifiable way
	 */
	private StyledText txt;
	/**
	 * ComboBox to provide a choice for the Comment priority
	 */
	private Combo priorityDropDown;
	/**
	 * ComboBox to provide a choice for the Comment status
	 */
	private Combo statusDropDown;
	/**
	 * TextBox to represent the Replys of a Comment
	 */
	private StyledText replys;

	/**
	 * Creates the CommentDetail Composite and creates the initial UI
	 * @param parent on which this component should be added
	 * @param style in which this component should be displayed
	 */
	protected CommentDetail(Composite parent, int style) {
		super(parent, style);
	}

	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.view.detail.AbstractDetail#initUI()
	 */
	protected void initUI() {
		GridLayout gridLayout = new GridLayout();
		int numColumns = 2;
		gridLayout.numColumns = numColumns;
		this.setLayout(gridLayout);

	    Label author = new Label(this, SWT.PUSH);
	    author.setText("Author: ");
	    
	    authorInstance = new Label(this, SWT.PUSH);
	    GridData gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    authorInstance.setLayoutData(gridData);
	    
	    Label status = new Label(this, SWT.PUSH);
	    status.setText("Status: ");
	    
	    statusDropDown = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.PUSH);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    statusDropDown.setLayoutData(gridData);
	    statusDropDown.addFocusListener(this);	    
	    
	    Label priority = new Label(this, SWT.PUSH);
	    priority.setText("Priority: ");
	    
	    priorityDropDown = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.PUSH);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    priorityDropDown.setLayoutData(gridData);
	    priorityDropDown.addFocusListener(this);
	    	
	    Label recipient = new Label(this, SWT.PUSH);
	    recipient.setText("Recipient: ");
	    
	    recipientText = new Text(this, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    recipientText.setLayoutData(gridData);
	    recipientText.addFocusListener(this);
	    
	    new Sash(this, SWT.PUSH);
	    
	    Label caption = new Label(this, SWT.PUSH);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns;
	    caption.setLayoutData(gridData);
	    caption.setText("Description / Replys:");
	    
	    SashForm texts = new SashForm(this, SWT.VERTICAL);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.verticalAlignment = GridData.FILL;
	    gridData.verticalSpan = 5;
	    gridData.horizontalSpan = numColumns;
	    gridData.grabExcessVerticalSpace = true;
	    gridData.grabExcessHorizontalSpace = true;
	    texts.setLayoutData(gridData);
	    
	    txt = new StyledText(texts, SWT.PUSH | SWT.V_SCROLL | SWT.BORDER);
	    txt.setVisible(true);
		txt.setWordWrap(true);
		txt.setEditable(true);
		txt.setEnabled(true);
	    txt.addFocusListener(this);
	    
	    replys = new StyledText(texts, SWT.PUSH | SWT.V_SCROLL | SWT.BORDER);
	    replys.setVisible(true);
	    replys.setEditable(false);
	    replys.setWordWrap(true);
	    replys.addFocusListener(this);
	    
	    Button replyButton = new Button(this, SWT.PUSH);
	    replyButton.setText("new Reply");
	    replyButton.setData("reply");
	    replyButton.addListener(SWT.Selection, this);

	    Composite g = new Composite(this, SWT.NONE);
	    GridLayout glayout = new GridLayout(3, false);
		g.setLayout(glayout);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.END;
	    gridData.horizontalSpan = numColumns-1;
	    g.setLayoutData(gridData);
	    
	    Button delButton = new Button(g, SWT.PUSH);
	    delButton.setText("Delete");
	    delButton.setData("delete");
	    delButton.addListener(SWT.Selection, CommentController.getInstance());
	    
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
	protected boolean saveChanges() {
		if(attributesChanged()) {
			editedObject.setLastModified(Calendar.getInstance());
			return true;
		} else {
			return false;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.view.detail.AbstractDetail#fillContents(java.lang.Object)
	 */
	public void fillContents(Comment comment) {
		if(comment != null) {
			this.backupObject = (Comment)comment.copy();
			this.editedObject = comment;
			authorInstance.setText(comment.getAuthor());

		   //  if (Boolean.valueOf(PropertiesManager.getInstance().getExternalPreference(PropertiesManager.EXTERNAL_KEYS.SUGGESTIONS_ENABLED)) && comment.getRecipient().equals("")) {
			 if (PropertiesManager.getPreferences().getBoolean(PropertiesManager.EXTERNAL_KEYS.SUGGESTIONS_ENABLED) && comment.getRecipient().isEmpty()) {
		    	recipientText.setText(PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.LAST_RECIPIENT));
		    } else {
		    	recipientText.setText(comment.getRecipient());
		    }

			if(comment.getText() != null) {
				this.txt.setText(comment.getText());
			} else {
				this.txt.setText("");
			}
			
			Reply[] replys = comment.getReplies().getReplyArray();
			this.replys.setText("");
			for(int i = 0; i < replys.length; i++) {
				addReply(replys[i].getAuthor(), replys[i].newCursor().getTextValue().trim());
			}

//			if (Boolean.valueOf(PropertiesManager.getInstance().getExternalPreference(PropertiesManager.EXTERNAL_KEYS.SUGGESTIONS_ENABLED))) {
//				priorityDropDown.select(Integer.parseInt(PropertiesManager.getInstance().getExternalPreference(PropertiesManager.EXTERNAL_KEYS.LAST_PRIORITY)));
//			}
			priorityDropDown.select(comment.getPriority());
			statusDropDown.select(comment.getStatus());
			
			// Give the focus to the comment text field
			// TODO: Will later be done only, if we know that it is an inital comment
			this.txt.setFocus();
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
	 * adds a reply to the reply list shown in the view
	 * @param author of the reply
	 * @param text of the reply
	 */
	private void addReply(String author, String text) {
		String replyText = this.replys.getText();
		DateFormat df = new SimpleDateFormat("dd.M.yyyy', 'HH:mm:ss");
		replyText += (replyText.equals("") ? "" : "\n\n") + "----- "+author+":"
			+df.format(Calendar.getInstance().getTime())+" -----\n";
		replyText += text;
		this.replys.setText(replyText);
	}
	
	/**
	 * checks whether changes on the current Comment are made and saves this changes
	 * @return true, if changes have been made<br> false, if no changes have been made
	 */
	private boolean attributesChanged() {
		boolean result = false;
		
		//save replies before
		Pattern p = Pattern.compile("-----([^:]*):([^\\n]*)-----\\n(.*)");
		Matcher m = p.matcher(this.replys.getText());
		ArrayList<String[]> shownReplies = new ArrayList<String[]>();
		while(m.find()) {
			//trim() to delete non used whitespace
			shownReplies.add(new String[]{m.group(1).trim(),m.group(2).trim(),m.group(3).trim()});
		}
		//XXX should be changed if someone can edit saved replies:
		//delete and edit of replies not considered in this implementation
		int savedReplies = this.editedObject.getReplies().getReplyArray().length;
		if(savedReplies != shownReplies.size()) {
			result = true;
			for(int i = savedReplies; i < shownReplies.size(); i++) {
				Reply newReply = this.editedObject.getReplies().addNewReply();
				newReply.setAuthor(shownReplies.get(i)[0]);
				newReply.setCreationDate(Calendar.getInstance());
				newReply.newCursor().setTextValue(shownReplies.get(i)[2]);
			}
		}
		
		if(editedObject.getPriority() != this.priorityDropDown.getSelectionIndex()) {
			editedObject.setPriority(this.priorityDropDown.getSelectionIndex());
			PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.LAST_PRIORITY, String.valueOf(this.priorityDropDown.getSelectionIndex()));
			result = true;
		} else if(editedObject.getStatus() != this.statusDropDown.getSelectionIndex()) {
			editedObject.setStatus(this.statusDropDown.getSelectionIndex());
			result = true;
		} else if(!editedObject.getRecipient().equals(this.recipientText.getText().trim())) {
			editedObject.setRecipient(this.recipientText.getText().trim());
			PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.LAST_RECIPIENT, recipientText.getText().trim());
			result = true;
		} else if(!editedObject.getText().equals(this.txt.getText().trim())) {
			editedObject.setText(this.txt.getText().trim());
			result = true;
		}

		return result;
	}
	
	/**
	 * Sets the levels for the status and priority configuration of a comment.
	 */
	private void setPropertyConfigurations() {
		PropertiesManager pm = PropertiesManager.getInstance();				
		String value = pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.COMMENT_STATUS);
		String[] levels = value.split(",");
		statusDropDown.removeAll();
		for(int i = 0; i < levels.length; i++) {
			statusDropDown.add(levels[i]);
		}
		
		value = pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.COMMENT_PRIORITIES);
		levels = value.split(",");
		priorityDropDown.removeAll();
		for(int i = 0; i < levels.length; i++) {
			priorityDropDown.add(levels[i]);
		}
	}

	/**
	 * handles actions of "revert" and "new reply" button
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		super.handleEvent(event);
		if(event.widget.getData().equals("reply")) {
			Shell shell = new Shell(this.getShell());
			ReplyDialog dialog = new ReplyDialog(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.SHELL_TRIM);
			dialog.setSize(250, 150);
		    shell.pack();
		    shell.open();
			while (!shell.isDisposed()) {
				if (!Display.getCurrent().readAndDispatch()) Display.getCurrent().sleep();
		    }
			
			if(dialog.getSaved()) {
				saveChanges();
				addReply(dialog.getReplyAuthor(), dialog.getReplyText());
			}
		}
	}
}
