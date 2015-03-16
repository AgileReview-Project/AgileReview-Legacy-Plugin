package de.tukl.cs.softech.agilereview.dataaccess.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * Handler for the cleanup process
 */
public class CleanupHandler extends AbstractHandler {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands
	 * .ExecutionEvent)
	 */
	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "Cleanup triggered in Package-Explorer");

		boolean deleteComments = PropertiesManager.getPreferences().getBoolean(PropertiesManager.EXTERNAL_KEYS.CLEANUP_DELETE_COMMENTS);
		boolean ignoreOpenComments = PropertiesManager.getPreferences().getBoolean(PropertiesManager.EXTERNAL_KEYS.CLEANUP_IGNORE_OPEN_COMMENTS);

		MessageBox messageDialog = new MessageBox(HandlerUtil.getActiveShell(event), SWT.ICON_QUESTION | SWT.OK | SWT.CANCEL);
		messageDialog.setText("AgileReview Cleanup");
		String options = (deleteComments ? (ignoreOpenComments ? "All comments that are not in state 'open' will be deleted." : "All Comments will be deleted.")
				: "References to code passages will be deleted. All comments will be kept.");
		String message = "Really do cleanup? " + options
				+ " Check the preferences to adjust whether no comments or only closed and fixed comments should be deleted.";
		messageDialog.setMessage(message);
		int result = messageDialog.open();

		if (result == SWT.CANCEL) {
			// cancel selected -> quit method
			return null;
		}

		// get the elements selected in the packageexplorer
		List<IProject> selProjects = new ArrayList<IProject>();
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		for (Iterator<?> it = selection.iterator(); it.hasNext();) {
			Object currentElement = it.next();
			if (currentElement instanceof IAdaptable) {
				// get selected project
				IProject selProject = (IProject) ((IAdaptable) currentElement).getAdapter(IProject.class);
				selProjects.add(selProject);
			}
		}

		try {
			ProgressMonitorDialog pmd = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event));
			pmd.open();
			pmd.run(true, false, new CleanupProcess(selProjects, deleteComments, ignoreOpenComments));
			pmd.close();
		} catch (InvocationTargetException e) {
			PluginLogger.logError(this.getClass().toString(), "execute", "InvocationTargetException", e);
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error while performing cleanup",
							"An Eclipse internal error occured!\nRetry and please report the bug to the AgileReview team when it occurs again.\nCode:1");
				}
			});
		} catch (InterruptedException e) {
			PluginLogger.logError(this.getClass().toString(), "execute", "InterruptedException", e);
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error while performing cleanup",
							"An Eclipse internal error occured!\nRetry and please report the bug to the AgileReview team when it occurs again.\nCode:2");
				}
			});
		}

		if (ViewControl.isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().reparseAllEditors();
		}
		ViewControl.refreshViews(ViewControl.COMMMENT_TABLE_VIEW | ViewControl.REVIEW_EXPLORER, true);

		return null;
	}

}
