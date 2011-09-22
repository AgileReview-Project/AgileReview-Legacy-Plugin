package de.tukl.cs.softech.agilereview.views.detail;

import java.io.IOException;
import java.util.Calendar;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * The relocate dialog is displayed on the detail view when the user want to relocate a comment
 */
public class RelocateDialog extends Composite implements Listener {
	
	/**
	 * Comment which will be moved
	 */
	private Comment oldComment;
	
	/**
	 * Creates a RelocateDialog on the given parent with a given style.
	 * Also the comment which should be moved have to be passed.
	 * @param parent of the RelocateDialog, see {@link Composite}
	 * @param style for the RelocateDialog, see {@link Composite}
	 * @param comment which should be moved
	 */
	RelocateDialog(Composite parent, int style, Comment comment) {
		super(parent, style);
		oldComment = comment;
		initUI();
	}
	
	/**
	 * Initiates the UI components of the dialog
	 */
	private void initUI() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		this.setLayout(gridLayout);
		
		StyledText label = new StyledText(this, SWT.WRAP);
		label.setText("Select the new location the comment should be moved to and confirm the selection here using Relocate.");
		label.setBackground(this.getParent().getBackground());
		label.setWordWrap(true);
		label.setEditable(false);
		label.setAlignment(SWT.CENTER);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		label.setLayoutData(data);
		
		Button confirm = new Button(this, SWT.PUSH);
		confirm.setText("Relocate");
		confirm.setData("confirm");
		confirm.addListener(SWT.Selection, this);
		confirm.setSize(80, 5);
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.CENTER;
		confirm.setLayoutData(data);
		
		Button abort = new Button(this, SWT.PUSH);
		abort.setText("Cancel");
		abort.setData("abort");
		abort.addListener(SWT.Selection, this);
		abort.setSize(80, 5);
		data = new GridData();
		data.grabExcessHorizontalSpace = true;
		data.horizontalAlignment = GridData.CENTER;
		abort.setLayoutData(data);
		
		this.getParent().layout(true, true);
	}
	
	/**
	 * Performs the relocation of the current shown comment
	 */
	void performCommentRelocation() {
		
		if (ViewControl.isOpen(CommentTableView.class)) {
				CommentTableView ctv = CommentTableView.getInstance();
				
				try {
					IEditorPart editor;
					if((editor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor()) != null) {
						
						if(ctv.openEditorContains(oldComment)) {
							//comment stays in same file
							ctv.relocateComment(oldComment);
							//show old comment
							ctv.selectComment(oldComment);
						} else {
							//comment has to move to new file
							IEditorInput input = editor.getEditorInput();
							if (input != null && input instanceof FileEditorInput) {
								ReviewAccess ra = ReviewAccess.getInstance();
								
								//create new comment in new file
								String pathToNewFile = ((FileEditorInput)input).getFile().getFullPath().toOSString().replaceFirst(Pattern.quote(System.getProperty("file.separator")), "");
								Comment newComment = ra.createNewComment(oldComment.getReviewID() , oldComment.getAuthor(), pathToNewFile);
								
								//fill contents
								newComment.setCreationDate(oldComment.getCreationDate());
								newComment.setLastModified(Calendar.getInstance());
								newComment.setPriority(oldComment.getPriority());
								newComment.setRecipient(oldComment.getRecipient());
								newComment.setStatus(oldComment.getStatus());
								newComment.setRevision(oldComment.getRevision());
								newComment.setReplies(oldComment.getReplies());
								newComment.setText(oldComment.getText());
								ctv.addComment(newComment);
								
								//delete old comment
								ctv.deleteComment(oldComment);
								ra.deleteComment(oldComment);
								
								//refresh views
								ViewControl.refreshViews(ViewControl.REVIEW_EXPLORER | ViewControl.COMMMENT_TABLE_VIEW, true);
								ctv.selectComment(newComment);
							} else {
								MessageDialog.openError(this.getShell(), "Comment Detail - Repositioning", "You cannot relocate this comment as the currently opened editor is not yet supported!");
							}
						}
						
					} else {
						MessageDialog.openError(this.getShell(), "Comment Detail - Repositioning", "You cannot relocate this comment as there is no editor opened at the moment!");
					}
					
				} catch (IOException e) {
					PluginLogger.log(this.getClass().toString(), "execute", "IOException when trying to create the new moved comment or deleting the old one", e);
				}
				
		} else {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Comment Detail - Repositioning", "In oder to move a comment, the DetailView and the Comment Summary has to be opened!");
		}
	}

	@Override
	public void handleEvent(Event event) {
		if(event.widget.getData().equals("confirm")) {
			performCommentRelocation();
		} else if(event.widget.getData().equals("abort")) {
			if(ViewControl.isOpen(CommentTableView.class)) {
				CommentTableView.getInstance().selectComment(oldComment);
			}
		}
	}
}