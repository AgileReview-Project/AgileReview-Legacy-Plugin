package de.tukl.cs.softech.agilereview.plugincontrol;

import java.util.regex.Pattern;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * Handler for showing the comment, which is currently selected in the editor, in the DetailView
 */
public class ShowCommentHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "\"Show Comment\" triggered");
		
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		if (editorPart instanceof ITextEditor) {
			ISelection sel = ((ITextEditor)editorPart).getSelectionProvider().getSelection();
			if (sel instanceof ITextSelection) {
				ITextSelection textSel = (ITextSelection)sel;
				Position p = new Position(textSel.getOffset(), textSel.getLength());
				
				String[] tagTupel = new String[0];
				if (ViewControl.isOpen(CommentTableView.class)) {
					tagTupel = CommentTableView.getInstance().getCommentsByPositionOfActiveEditor(p);
				}
				
				if (tagTupel.length>0) {
					String reviewId = "";
					String author = "";
					String commentId = "";
					String tag = "";
					
					if (tagTupel.length == 1) {
						tag = tagTupel[0];
					} else {
						// More than one possible comment -> let the user choose
						Shell shell = new Shell(HandlerUtil.getActiveShell(event));
						shell.setText("Select a Comment");
						CommentChooserDialog dialog = new CommentChooserDialog(shell, SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.SHELL_TRIM, tagTupel);
						dialog.setSize(200,100);
					    shell.pack();
					    shell.open();
						while (!shell.isDisposed()) {
							if (!Display.getCurrent().readAndDispatch()) Display.getCurrent().sleep();
					    }
						
						if(dialog.getSaved()) {
							tag = dialog.getReplyText();
						} else {
							return null;
						}
					}
					String[] tagPart = tag.split(Pattern.quote(PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR)));
					reviewId = tagPart[0];
					author = tagPart[1];
					commentId = tagPart[2];
					
					// Get the right comment static PropertiesManager
					Comment c = ReviewAccess.getInstance().getComment(reviewId, author, commentId);
					
					// select it
					if (ViewControl.isOpen(CommentTableView.class)) {
						CommentTableView.getInstance().selectComment(c);
					}
				}
			}
		}

		return null;
	}

}
