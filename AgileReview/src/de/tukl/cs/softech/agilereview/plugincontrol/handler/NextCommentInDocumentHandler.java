package de.tukl.cs.softech.agilereview.plugincontrol.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.ITextEditor;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * Compute and reveal the next visible comment in the document
 */
public class NextCommentInDocumentHandler extends AbstractHandler {/*?|r69|Peter Reuter|c1|?*/

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "\"Next Comment In Document\" triggered");
		IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
		if (editorPart instanceof ITextEditor) {
			ISelection sel = ((ITextEditor)editorPart).getSelectionProvider().getSelection();
			if (sel instanceof ITextSelection) {
				ITextSelection textSel = (ITextSelection)sel;
				Position p = new Position(textSel.getOffset(), textSel.getLength());
				
				Position nextComment = null;
				if (ViewControl.isOpen(CommentTableView.class)) {
					nextComment = CommentTableView.getInstance().getNextCommentPosition(p);
				}
				
				if (nextComment!=null) {
					((ITextEditor) editorPart).selectAndReveal(nextComment.getOffset(), 0);
				}
				
				String command = "de.tukl.cs.softech.agilereview.showComment";
				IHandlerService handlerService = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
				try {
					// the "showComment" can be executed, as we are required to be in the handler
					handlerService.executeCommand(command, null);
				} catch (ExecutionException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "Problems occured executing command \""+command+"\"", e);
					// if "showComment" throws an ExecutionException it will be forwarded
					throw e;
				} catch (NotDefinedException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "Command \""+command+"\" is not defined", e);
				} catch (NotEnabledException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "Command \""+command+"\" is not enabled", e);
				} catch (NotHandledException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "Command \""+command+"\" is not handled", e);
				}
			}
		}

		return null;
	}

}
