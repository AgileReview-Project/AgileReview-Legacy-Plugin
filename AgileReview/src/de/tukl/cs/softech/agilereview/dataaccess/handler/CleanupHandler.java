package de.tukl.cs.softech.agilereview.dataaccess.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.handlers.HandlerUtil;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;

/**
 * 
 */
public class CleanupHandler extends AbstractHandler {

	/**
	 * Key separator for tag creation
	 */
	private static String keySeparator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
	/**
	 * Core Regular Expression to find the core tag structure
	 */
	private static String rawTagRegex = "\\s*(\\??)"+Pattern.quote(keySeparator)+"\\s*([^"+Pattern.quote(keySeparator)+"]+"+Pattern.quote(keySeparator)+"[^"+Pattern.quote(keySeparator)+"]+"+Pattern.quote(keySeparator)+"[^\\?"+Pattern.quote(keySeparator)+"]*)\\s*"+Pattern.quote(keySeparator)+"(\\??)\\s*";
	/**
	 * Regular Expression to find each comment tag in java files
	 */	
	private static final String javaTagRegex = "/\\*\\s*"+rawTagRegex+"\\s*\\*/";
	/**
	 * Regular Expression to find each comment tag in XML files
	 */
	private static final String xmlTagRegex = "<!--\\s*"+rawTagRegex+"\\s*-->";
	
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
			messageDialog.setMessage("Delete comments when removing tags?");
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
			ArrayList<Review> reviews = ReviewAccess.getInstance().getAllReviews();
			
			// some helper variables
			ArrayList<Comment> comments = new ArrayList<Comment>();
			HashSet<String> paths = new HashSet<String>();
			String selProjectPath = selProject.getFullPath().toOSString().replaceAll(Pattern.quote(System.getProperty("file.separator")), "");
			
			// load all comments for all reviews
			try {
				ReviewAccess.getInstance().fillDatabaseCompletely();
			} catch (XmlException e1) {
				success = false;
				PluginLogger.logError(this.getClass().toString(), "execute", "XMLException while trying to fill database.", e1);
			} catch (IOException e1) {
				success = false;
				PluginLogger.logError(this.getClass().toString(), "execute", "IOException while trying to fill database.", e1);
			}
			
			// save all comments for the given project
			for (Review r : reviews) {
				comments.addAll(ReviewAccess.getInstance().getComments(r.getId(), selProjectPath));
			}
			
			// save the paths of all files that are being reviewed (ergo that comments exist for)
			for (Comment c : comments) {
				paths.add(ReviewAccess.computePath(c));
			}
			
			// remove tags from files
			PluginLogger.log(this.getClass().toString(), "execute", "Removing comments from "+paths.toString());
			for (String path : paths) {
				if (!success) {
					removeTagsFromFile(path);
				} else {
					success = removeTagsFromFile(path);
				}				
			}
			
			// delete comments based on users decision
			if (deleteComments) {
				try {
					PluginLogger.log(this.getClass().toString(), "execute", "Removing comments from XML");
					ReviewAccess.getInstance().deleteComments(comments);
					if (ViewControl.isOpen(CommentTableView.class)) {
						CommentTableView.getInstance().resetComments();	
					}
					if (ViewControl.isOpen(ReviewExplorer.class)) {
						ReviewExplorer.getInstance().refreshInput();
					}
				} catch (IOException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "IOException while trying to delete comments.", e);
					success = false;
				}
			}
			
			// unload closed reviews again
			List<String> openReviews = Arrays.asList(PropertiesManager.getInstance().getOpenReviews());
			for (Review r : reviews) {				
				if (!openReviews.contains(r.getId())) {
					ReviewAccess.getInstance().unloadReviewComments(r.getId());
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
	
	
	/**
	 * Removes the comment tags from the file given by the path
	 * @param path the path relative to the workspaceroot
	 * @return true if tags were removed successfully, else false
	 */
	private boolean removeTagsFromFile(String path) {

		boolean success = true;
		// get workspaceroot to make filepath absolute
		String workspaceRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		if (!workspaceRoot.endsWith(System.getProperty("file.separator"))) {
			workspaceRoot += System.getProperty("file.separator");
		}
		
		File file = new File(workspaceRoot + path);
		// supported files are *.java and *.xml -> if file is of type and exists proceed
		if (file.exists()) {
			if (path.endsWith("java") || path.endsWith("xml")) {
				try {
					// read file and remove tags
					Scanner input = new Scanner(file);
					String modifiedContent = "";
					while (input.hasNext()) {
						String actLine = input.nextLine();
						if (path.endsWith("java")) {
							modifiedContent += actLine.replaceAll(javaTagRegex, "");	
						} else if (path.endsWith("xml")) {
							modifiedContent += actLine.replaceAll(xmlTagRegex, "");
						} 
						modifiedContent += input.hasNext() ? System.getProperty("line.separator") : "";
					}
					input.close();

					// save modified content to file
					FileWriter output = new FileWriter(file);
					output.write(modifiedContent);
					output.close();


				} catch (FileNotFoundException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "FileNotFoundException while trying to remove tags.", e);
					success = false;
				} catch (IOException e) {
					PluginLogger.logError(this.getClass().toString(), "execute", "IOException while trying to remove tags.", e);
					success = false;
				}	
			}
		} else {
			success = false;
		}
		return success;
	}

}
