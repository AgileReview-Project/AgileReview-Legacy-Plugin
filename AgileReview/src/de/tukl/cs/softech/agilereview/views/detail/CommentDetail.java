package de.tukl.cs.softech.agilereview.views.detail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlCursor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReplyDocument.Reply;
import de.tukl.cs.softech.agilereview.annotations.ColorManager;
import de.tukl.cs.softech.agilereview.plugincontrol.SourceProvider;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * The CommentDetail class describes one detail representation of a Comment Object
 */
public class CommentDetail extends AbstractDetail<Comment> {
	
	/**
	 * Text to show the comment tag of the shown Comment
	 */
	private Text tagInstance;
	/**
	 * Text to show the author of the shown Comment
	 */
	private Text authorInstance;
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
	 * Composite to wrap all reply components
	 */
	private Composite replies;
	/**
	 * ScrolledComposite to wrap the replies wrapper component with a scroll bar
	 */
	private ScrolledComposite replyScrolledWrapper;

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

		Label tagID = new Label(this, SWT.NONE);
		tagID.setText("Tag-ID: ");
		super.bgComponents.add(tagID);
		
		tagInstance = new Text(this, SWT.WRAP);
		GridData gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    tagInstance.setEditable(false);
	    tagInstance.setLayoutData(gridData);
	    super.bgComponents.add(tagInstance);
		
	    Label author = new Label(this, SWT.PUSH);
	    author.setText("Author: ");
	    super.bgComponents.add(author);
	    
	    authorInstance = new Text(this, SWT.WRAP);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    authorInstance.setEditable(false);
	    authorInstance.setLayoutData(gridData);
	    super.bgComponents.add(authorInstance);
	    
	    Label status = new Label(this, SWT.PUSH);
	    status.setText("Status: ");
	    super.bgComponents.add(status);
	    
	    statusDropDown = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.PUSH);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    statusDropDown.setLayoutData(gridData);
	    statusDropDown.addFocusListener(this);
	    statusDropDown.addModifyListener(this);
	    super.bgComponents.add(statusDropDown);
	    
	    Label priority = new Label(this, SWT.PUSH);
	    priority.setText("Priority: ");
	    super.bgComponents.add(priority);
	    
	    priorityDropDown = new Combo(this, SWT.DROP_DOWN | SWT.BORDER | SWT.PUSH);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    priorityDropDown.setLayoutData(gridData);
	    priorityDropDown.addFocusListener(this);
	    priorityDropDown.addModifyListener(this);
	    super.bgComponents.add(priorityDropDown);
	    	
	    Label recipient = new Label(this, SWT.PUSH);
	    recipient.setText("Recipient: ");
	    super.bgComponents.add(recipient);
	    
	    recipientText = new Text(this, SWT.BORDER | SWT.SINGLE | SWT.WRAP);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns-1;
	    recipientText.setLayoutData(gridData);
	    recipientText.addFocusListener(this);
	    recipientText.addModifyListener(this);
	    
	    Sash sash = new Sash(this, SWT.PUSH);
	    sash.setVisible(false);
	    
	    Label caption = new Label(this, SWT.PUSH);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.horizontalSpan = numColumns;
	    caption.setLayoutData(gridData);
	    caption.setText("Description / Replys:");
	    super.bgComponents.add(caption);
	    
	    SashForm sashArea = new SashForm(this, SWT.VERTICAL);
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.verticalAlignment = GridData.FILL;
	    gridData.verticalSpan = 5;
	    gridData.horizontalSpan = numColumns;
	    gridData.grabExcessVerticalSpace = true;
	    gridData.grabExcessHorizontalSpace = true;
	    sashArea.setLayoutData(gridData);
	    super.bgComponents.add(sashArea);
	    
	    txt = new StyledText(sashArea, SWT.V_SCROLL | SWT.BORDER);
	    txt.setVisible(true);
		txt.setWordWrap(true);
		txt.setEditable(true);
		txt.setEnabled(true);
	    txt.addFocusListener(this);
	    txt.addModifyListener(this);
	    
	    replyScrolledWrapper = new ScrolledComposite(sashArea, SWT.V_SCROLL);
	    replyScrolledWrapper.setExpandHorizontal(true);
	    replyScrolledWrapper.setExpandVertical(true);
	    replyScrolledWrapper.setLayout(new GridLayout(1, true));
	    replyScrolledWrapper.addControlListener(new ControlListener() {
			@Override
			public void controlResized(ControlEvent e) {
				refreshReplies();
			}
	    
			@Override
			public void controlMoved(ControlEvent e) {
				refreshReplies();
			}
		});
	    gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.verticalAlignment = GridData.FILL;
	    gridData.grabExcessHorizontalSpace = true;
	    gridData.grabExcessVerticalSpace = true;
	    replyScrolledWrapper.setLayoutData(gridData);
	    
	    replies = new Composite(replyScrolledWrapper, SWT.NONE);
	    replyScrolledWrapper.setContent(replies);
	    
	    setPropertyConfigurations();
	}
	
	/**
	 * Recreates the replies component
	 */
	private void resetReplies() {
		replies.dispose();
	    replies = new Composite(replyScrolledWrapper, SWT.NONE);
	    GridLayout replyLayout = new GridLayout();
	    replyLayout.numColumns = 1;
	    replies.setLayout(replyLayout);
	    replies.addFocusListener(this);
		replies.setVisible(true);
	}
	
	/**
	 * Refreshes the reply components and recalculates the scroll bars
	 */
	private void refreshReplies() {
		replies.layout();
	    replyScrolledWrapper.setContent(replies);
	    replyScrolledWrapper.setMinSize(replies.computeSize(replyScrolledWrapper.getSize().x-15, SWT.DEFAULT));
	    replyScrolledWrapper.layout();
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
			if(editedObject.getLastModified().equals(editedObject.getCreationDate())) {
				editedObject.setLastModified(Calendar.getInstance());
				return true;
			} else {
				return false;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.view.detail.AbstractDetail#fillContents(java.lang.Object)
	 */
	public void fillContents(Comment comment) {
		// XXX @May-Bee: can we do this here? -> need to save one next comment shortcut
		if (editedObject!=null && backupObject!=null) {
			saveChanges();
		}
			
		if(comment != null) {
			this.backupObject = (Comment)comment.copy();
			this.editedObject = comment;
			tagInstance.setText(generateCommentKey(comment));
			tagInstance.setToolTipText(generateCommentKey(comment));
			authorInstance.setText(comment.getAuthor());
			authorInstance.setToolTipText(comment.getAuthor());

			// Proof if the comment is loaded for the first time
			recipientText.setText(comment.getRecipient());
			if (comment.getLastModified().equals(comment.getCreationDate())) {
				if (PropertiesManager.getPreferences().getBoolean(PropertiesManager.EXTERNAL_KEYS.SUGGESTIONS_ENABLED)) {
			    	recipientText.setText(PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.LAST_RECIPIENT));
				}
				// Give the focus to the comment text field
				this.txt.setFocus();
			}
			
			if(comment.getText() != null) {
				this.txt.setText(comment.getText());
			} else {
				this.txt.setText("");
			}
			
			resetReplies();
			
			Reply[] replies = comment.getReplies().getReplyArray();		    
			for(int i = 0; i < replies.length; i++) {
				XmlCursor cursor = replies[i].newCursor();
				addReply(replies[i].getAuthor(), cursor.getTextValue().trim(), replies[i].getCreationDate());
				cursor.dispose();
			}
			
		    refreshReplies();

			priorityDropDown.select(comment.getPriority());
			statusDropDown.select(comment.getStatus());
		}
		//set revertable to false because it was set from the ModificationListener while inserting inital content
		ISourceProviderService isps = (ISourceProviderService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISourceProviderService.class);
		SourceProvider sp = (SourceProvider) isps.getSourceProvider(SourceProvider.REVERTABLE);
		sp.setVariable(SourceProvider.REVERTABLE, false);
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
	 * @param creationDate of the reply
	 */
	void addReply(String author, String text, Calendar creationDate) {
		StyledText newReply = new StyledText(this.replies, SWT.WRAP | SWT.BORDER);
		GridData gridData = new GridData();
	    gridData.horizontalAlignment = GridData.FILL;
	    gridData.verticalAlignment = GridData.BEGINNING;
	    gridData.grabExcessHorizontalSpace = true;
	    newReply.setLayoutData(gridData);
	    newReply.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	    
		DateFormat df = new SimpleDateFormat("dd.M.yyyy', 'HH:mm:ss");
		String header = author+" ("+df.format(creationDate.getTime())+"):\n";
		newReply.setText(header+text);
		newReply.addFocusListener(this);
		newReply.setVisible(true);
		
		StyleRange style = new StyleRange();
	    style.start = 0;
	    style.length = header.length();
	    style.fontStyle = SWT.BOLD;
	    newReply.setStyleRange(style);
	    
	    refreshReplies();
	}
	
	/**
	 * checks whether changes on the current Comment are made and saves this changes
	 * @return true, if changes have been made<br> false, if no changes have been made
	 */
	private boolean attributesChanged() {
		boolean result = false;
		
		//extract replies beforehand
		Pattern p = Pattern.compile("([^\\)]*)\\(([^\\)]*)\\):\\n(.*)", Pattern.DOTALL);
		Matcher m;
		ArrayList<String[]> shownReplies = new ArrayList<String[]>();
		Control[] replys = this.replies.getChildren();
		for(Control l : replys) {
			if(l instanceof StyledText) {
				m = p.matcher(((StyledText)l).getText());
				if(m.find()) {
					shownReplies.add(new String[]{m.group(1).trim(), m.group(2).trim(), super.convertLineBreaks(m.group(3).trim())});
				}
			}
		}
		
		String newStr = "";
		//XXX should be changed if someone can delete saved replies:
		//delete and edit of replies not considered in this implementation
		int savedRepliesSize = editedObject.getReplies().getReplyArray().length;
		if(savedRepliesSize != shownReplies.size()) {
			result = true;
			for(int i = savedRepliesSize; i < shownReplies.size(); i++) {
				Reply newReply = editedObject.getReplies().addNewReply();
				newReply.setAuthor(shownReplies.get(i)[0]);
				newReply.setCreationDate(Calendar.getInstance());
				
				XmlCursor cursor = newReply.newCursor();
				cursor.setTextValue(shownReplies.get(i)[2]);
				cursor.dispose();
			}
		}
		
		if(editedObject.getPriority() != this.priorityDropDown.getSelectionIndex()) {
			editedObject.setPriority(this.priorityDropDown.getSelectionIndex());
			PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.LAST_PRIORITY, String.valueOf(this.priorityDropDown.getSelectionIndex()));
			result = true;
		}
		if(editedObject.getStatus() != this.statusDropDown.getSelectionIndex()) {
			editedObject.setStatus(this.statusDropDown.getSelectionIndex());
			result = true;
		} 
		if(!(newStr = this.recipientText.getText().trim()).equals(editedObject.getRecipient())) {
			editedObject.setRecipient(this.recipientText.getText().trim());
			PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.LAST_RECIPIENT, recipientText.getText().trim());
			result = true;
		} 
		if(!(newStr = super.convertLineBreaks(this.txt.getText().trim())).equals(editedObject.getText())) {
			editedObject.setText(newStr);
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
	 * Generates the comment key for the given comment in the following scheme: reviewID|author|commendID
	 * @param comment which comment key should be generated
	 * @return comment key
	 */
	private String generateCommentKey(Comment comment) {
		String keySeparator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
		String commentTag = comment.getReviewID()+keySeparator+comment.getAuthor()+keySeparator+comment.getId();
		return commentTag;
	}

	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.views.detail.AbstractDetail#determineBackgroundColor()
	 */
	@Override
	protected Color determineBackgroundColor() {
		//get the backupObject as changes should only have impact on the background when they are saved
		return ColorManager.getColor(this.backupObject.getAuthor());
	}

}