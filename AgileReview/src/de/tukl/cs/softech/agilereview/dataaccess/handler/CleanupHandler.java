package de.tukl.cs.softech.agilereview.dataaccess.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.annotations.TagCleaner;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * 
 */
public class CleanupHandler extends AbstractHandler {
	
	/**
	 * Instance of ReviewAccess
	 */
	private static ReviewAccess ra = ReviewAccess.getInstance();
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		// get the element selected in the packageexplorer
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		Object firstElement = selection.getFirstElement();
		
		if (firstElement instanceof IAdaptable) {
			
			boolean success = true;

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
					
			// get all reviews (not only open ones)
			ArrayList<Review> reviews = ra.getAllReviews();
			
			// some helper variables
			ArrayList<Comment> comments = new ArrayList<Comment>();
			HashSet<String> paths = new HashSet<String>();
			String selProjectPath = selProject.getFullPath().toOSString().replaceAll(Pattern.quote(System.getProperty("file.separator")), "");
			
			// load all comments for all reviews
			try {
				ra.fillDatabaseCompletely();
			} catch (XmlException e1) {
				success = false;
				PluginLogger.logError(this.getClass().toString(), "execute", "XMLException while trying to fill database.", e1);
			} catch (IOException e1) {
				success = false;
				PluginLogger.logError(this.getClass().toString(), "execute", "IOException while trying to fill database.", e1);
			}
			
			// save all comments for the given project
			for (Review r : reviews) {
				comments.addAll(ra.getComments(r.getId(), selProjectPath));
			}
			
			// save the paths of all files that are being reviewed (ergo that comments exist for)
			for (Comment c : comments) {
				paths.add(ReviewAccess.computePath(c));
			}
			
			// remove tags from files
			PluginLogger.log(this.getClass().toString(), "execute", "Removing comments from "+paths.toString());
			for (String path : paths) {
				if (!success) {
					IPath actPath = new Path(path);
					TagCleaner.removeAllTags(actPath);
				} else {
					IPath actPath = new Path(path);
					success = TagCleaner.removeAllTags(actPath);
				}				
			}
			
			if (ViewControl.isOpen(CommentTableView.class)) {
				CommentTableView.getInstance().reparseAllEditors();
			}
			
			// delete comments based on users decision
			if (deleteComments) {
				try {
					PluginLogger.log(this.getClass().toString(), "execute", "Removing comments from XML");
					ra.deleteComments(comments);
					ra.save();
					ViewControl.refreshViews(ViewControl.COMMMENT_TABLE_VIEW | ViewControl.REVIEW_EXPLORER, true);
				} catch (IOException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "IOException while trying to delete comments.", e);
					success = false;
				}
			}
			
			// unload closed reviews again
			List<String> openReviews = Arrays.asList(PropertiesManager.getInstance().getOpenReviews());
			for (Review r : reviews) {				
				if (!openReviews.contains(r.getId())) {
					ra.unloadReviewComments(r.getId());
				}
			}
		
			// Inform user
			if (success) {
				MessageDialog.openInformation(HandlerUtil.getActiveShell(event), "AgileReview Cleanup", "The project " + ((IProject)selProject).getName() + " was successfully cleaned.");
			} else {
				MessageDialog.openWarning(HandlerUtil.getActiveShell(event), "AgileReview Cleanup", "The project " + ((IProject)selProject).getName() + " could not be cleaned.");
			}
			
		}
		
		return null;
	}

}
