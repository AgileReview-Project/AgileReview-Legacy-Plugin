package de.tukl.cs.softech.agilereview.views.detail;

import java.io.IOException;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

public class RelocateDialog extends Composite implements Listener {
	
	private Comment oldComment;
	
	RelocateDialog(Composite parent, int style, Comment comment) {
		super(parent, style);
		oldComment = comment;
		initUI();
	}
	
	private void initUI() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		this.setLayout(gridLayout);
		
		StyledText label = new StyledText(this, SWT.WRAP);
		label.setText("Select the new location the comment should be moved to and confirm here or run the relocate command again");
		label.setBackground(this.getParent().getBackground());
		label.setWordWrap(true);
		label.setEditable(false);
		GridData data = new GridData();
		data.horizontalSpan = 2;
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		label.setLayoutData(data);
		
		Button confirm = new Button(this, SWT.PUSH);
		confirm.setText("relocate");
		confirm.setData("confirm");
		confirm.addListener(SWT.Selection, this);
		Button abort = new Button(this, SWT.PUSH);
		abort.setText("cancel");
		abort.setData("abort");
		abort.addListener(SWT.Selection, this);
		
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
								ctv.addComment(newComment);
								//delete old comment
								ra.deleteComment(oldComment);
								ctv.deleteComment(oldComment);
								//refresh views
								ViewControl.refreshViews(ViewControl.REVIEW_EXPLORER);
								ctv.selectComment(newComment);
							} else {
								MessageDialog.openError(null, "Comment Detail - Repositioning", "You cannot relocate this comment as the currently opened editor is not yet supported!");
							}
						}
						
					} else {
						MessageDialog.openError(null, "Comment Detail - Repositioning", "You cannot relocate this comment as there is no editor opened at the moment!");
					}
					
				} catch (IOException e) {
					PluginLogger.log(this.getClass().toString(), "execute", "IOException when trying to create the new moved comment or deleting the old one", e);
				}
				
		} else {
			MessageDialog.openError(null, "Comment Detail - Repositioning", "In oder to move a comment, the DetailView and the Comment Summary has to be opened!");
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
