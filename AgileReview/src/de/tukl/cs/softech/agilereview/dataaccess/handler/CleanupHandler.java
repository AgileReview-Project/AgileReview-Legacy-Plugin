package de.tukl.cs.softech.agilereview.dataaccess.handler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Resource;
import javax.swing.ProgressMonitor;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.annotations.TagCleaner;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.export.XSLExport;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * 
 */
public class CleanupHandler extends AbstractHandler {
	

	

	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// get the element selected in the packageexplorer
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		Object firstElement = selection.getFirstElement();
		
		if (firstElement instanceof IAdaptable) {
			
			

			// ask user whether to delete comments and tags or only tags
			boolean deleteComments = true;
			MessageBox messageDialog = new MessageBox(HandlerUtil.getActiveShell(event), SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
			messageDialog.setText("AgileReview Cleanup");
			messageDialog.setMessage("Delete comments? Otherwise they will be converted to global comments!");
			int result = messageDialog.open();

			if (result==SWT.CANCEL) {
				// cancel selected -> quit method
				return null;
			} else if (result==SWT.NO) {
				deleteComments = false;
			}
			
			// get selected project
			IProject selProject = (IProject)((IAdaptable)firstElement).getAdapter(IProject.class);
			try {
				ProgressMonitorDialog pmd = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event));
				pmd.open();			
				pmd.run(true, false, new CleanupProcess(selProject, deleteComments));
				pmd.close();
			} catch (InvocationTargetException e) {
				PluginLogger.logError(this.getClass().toString(),"execute", "InvocationTargetException", e);
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error while performing cleanup", "An Eclipse internal error occured!\nRetry and please report the bug to the AgileReview team when it occurs again.\nCode:1");
			} catch (InterruptedException e) {
				PluginLogger.logError(this.getClass().toString(),"execute", "InterruptedException", e);
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error while performing cleanup", "An Eclipse internal error occured!\nRetry and please report the bug to the AgileReview team when it occurs again.\nCode:2");
			}			
		}
		
		if (ViewControl.isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().reparseAllEditors();
		}
		ViewControl.refreshViews(ViewControl.COMMMENT_TABLE_VIEW | ViewControl.REVIEW_EXPLORER, true);

		return null;
	}

}
