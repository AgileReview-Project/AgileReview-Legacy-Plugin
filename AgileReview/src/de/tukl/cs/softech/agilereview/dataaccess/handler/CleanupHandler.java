package de.tukl.cs.softech.agilereview.dataaccess.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

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
		String message = "Really do cleanup? " + options + "\nCheck the preferences to adjust the behavior of the Project Cleanup Action.";
		messageDialog.setMessage(message);
		int result = messageDialog.open();

		if (result == SWT.CANCEL) {
			// cancel selected -> quit method
			return null;
		}
		
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		
		try {
			Object firstElement = selection.getFirstElement();
			// cleanup for reviews
			if (firstElement instanceof MultipleReviewWrapper) {
	    		MultipleReviewWrapper reviewWrapper = ((MultipleReviewWrapper) firstElement);
	            cleanupReview(event, reviewWrapper, deleteComments, ignoreOpenComments);
	        } else 
			// cleanup for projects
			if (firstElement instanceof IAdaptable && ((IAdaptable) firstElement).getAdapter(IProject.class) != null) {
				List<IProject> selProjects = new ArrayList<IProject>();
				for (Iterator<?> it = selection.iterator(); it.hasNext();) {
					Object currentElement = it.next();
					if (currentElement instanceof IAdaptable) {
						IProject selProject = (IProject) ((IAdaptable) currentElement).getAdapter(IProject.class);
						if (selProject != null) {
							selProjects.add(selProject);	
						}
					}
				}
	
				cleanupProjects(selProjects, event, deleteComments, ignoreOpenComments);	
			}
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
		
		ViewControl.refreshViews(ViewControl.COMMMENT_TABLE_VIEW | ViewControl.REVIEW_EXPLORER, true);
		if (ViewControl.isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().reparseAllEditors();
		}

		return null;
	}

	/**
	 * Perform cleanup on selected Projects
	 * @param selProjects
	 * @param event 
	 * @param deleteComments
	 * @param ignoreOpenComments
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */
	private void cleanupProjects(List<IProject> selProjects, final ExecutionEvent event,
			boolean deleteComments, boolean ignoreOpenComments) throws InvocationTargetException, InterruptedException {
		ProgressMonitorDialog pmd = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event));
		pmd.open();
		pmd.run(true, false, new CleanupProjectsProcess(selProjects, deleteComments, ignoreOpenComments));
		pmd.close();
	}
	
	/**
	 * Perform cleanup on selected review
	 * @param event
	 * @param reviewWrapper
	 * @param deleteComments 
	 * @param ignoreOpenComments 
	 * @throws ExecutionException
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */
	private void cleanupReview(ExecutionEvent event, MultipleReviewWrapper reviewWrapper, boolean deleteComments, boolean ignoreOpenComments)
			throws ExecutionException, InvocationTargetException, InterruptedException {
		
		if (!checkReviewOpen(event, reviewWrapper)) { return; }
		
	    ProgressMonitorDialog pmd = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event));
	    pmd.open();
	    pmd.run(true, false, new CleanupReviewProcess(reviewWrapper.getWrappedReview(), deleteComments, ignoreOpenComments));
	    pmd.close();
	}
    
    /**
     * Checks whether the selected Review is open. If it is not, it will be opened automatically.
     * @param event original handler event
     * @param reviewWrapper wrapper class of the selected review
     * @throws ExecutionException thrown when the execution of the open/close command for reviews fails
     * @return true, if the review is open or was opened successfully<br>false, otherwise
     * @author Malte Brunnlieb (25.05.2012)
     */
    private boolean checkReviewOpen(ExecutionEvent event, MultipleReviewWrapper reviewWrapper) throws ExecutionException {
        if (!reviewWrapper.isOpen()) {
            ICommandService cmdService = (ICommandService) HandlerUtil.getActiveSite(event).getService(ICommandService.class);
            if (cmdService != null) {
                Command cmd = cmdService.getCommand("de.tukl.cs.softech.agilereview.views.reviewexplorer.openClose");
                if (cmd != null) {
                    try {
                        cmd.executeWithChecks(event);
                    } catch (NotDefinedException e) {
                        MessageDialog.openError(HandlerUtil.getActiveShell(event), "Open Review",
                                "An error occurred while opening the selected Review (1)\nPlease do it by yourself.");
                        return false;
                    } catch (NotEnabledException e) {
                        MessageDialog.openError(HandlerUtil.getActiveShell(event), "Open Review",
                                "An error occurred while opening the selected Review (2)\nPlease do it by yourself.");
                        return false;
                    } catch (NotHandledException e) {
                        MessageDialog.openError(HandlerUtil.getActiveShell(event), "Open Review",
                                "An error occurred while opening the selected Review (3)\nPlease do it by yourself.");
                        return false;
                    }
                } else {
                    MessageDialog.openError(HandlerUtil.getActiveShell(event), "An error occured",
                            "The open/close command for Reviews could not be found. Please open the review by yourself before "
                                    + "performing another cleanup.");
                    return false;
                }
            } else {
                MessageDialog.openError(HandlerUtil.getActiveShell(event), "An error occured",
                        "The eclipse command service could not be found. Please open the review by yourself before performing another cleanup.");
                return false;
            }
        }
        return true;
    }

}
