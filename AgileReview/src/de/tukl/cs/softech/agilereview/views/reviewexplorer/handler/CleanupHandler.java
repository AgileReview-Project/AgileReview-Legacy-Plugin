/**
 * Copyright (c) 2011, 2012 AgileReview Development Team and others.
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License - v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Malte Brunnlieb, Philipp Diebold, Peter Reuter, Thilo Rauch
 */
package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * Cleanup handler for Review specific Cleanups
 * @author Malte Brunnlieb (25.05.2012)
 */
public class CleanupHandler extends AbstractHandler {
    
    /* (non-Javadoc)
     * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     * @author Malte Brunnlieb (25.05.2012)
     */
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        
        IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
        Object firstElement = selection.getFirstElement();
        
        if (firstElement instanceof MultipleReviewWrapper) {
            MultipleReviewWrapper reviewWrapper = ((MultipleReviewWrapper) firstElement);
            
            if (!checkReviewOpen(event, reviewWrapper)) { return null; }
            
            // ask user whether to delete comments and tags or only tags
            boolean deleteComments = true;
            MessageBox messageDialog = new MessageBox(HandlerUtil.getActiveShell(event), SWT.ICON_QUESTION | SWT.YES | SWT.NO | SWT.CANCEL);
            messageDialog.setText("AgileReview Cleanup");
            messageDialog.setMessage("Delete comments? Otherwise they will be converted to global comments!");
            int result = messageDialog.open();
            
            if (result == SWT.CANCEL) {
                // cancel selected -> quit method
                return null;
            } else if (result == SWT.NO) {
                deleteComments = false;
            }
            
            // get selected project
            try {
                ProgressMonitorDialog pmd = new ProgressMonitorDialog(HandlerUtil.getActiveShell(event));
                pmd.open();
                pmd.run(true, false, new CleanupProcess(reviewWrapper.getWrappedReview(), deleteComments));
                pmd.close();
            } catch (InvocationTargetException e) {
                PluginLogger.logError(this.getClass().toString(), "execute", "InvocationTargetException", e);
                MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error while performing cleanup",
                        "An Eclipse internal error occured!\nRetry and please report the bug to the AgileReview team when it occurs again.\nCode:1");
            } catch (InterruptedException e) {
                PluginLogger.logError(this.getClass().toString(), "execute", "InterruptedException", e);
                MessageDialog.openError(HandlerUtil.getActiveShell(event), "Error while performing cleanup",
                        "An Eclipse internal error occured!\nRetry and please report the bug to the AgileReview team when it occurs again.\nCode:2");
            }
        }
        
        if (ViewControl.isOpen(CommentTableView.class)) {
            CommentTableView.getInstance().reparseAllEditors();
        }
        ViewControl.refreshViews(ViewControl.COMMMENT_TABLE_VIEW | ViewControl.REVIEW_EXPLORER, true);
        
        return null;
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
