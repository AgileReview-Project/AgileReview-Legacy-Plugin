package de.tukl.cs.softech.agilereview.dataaccess.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.annotations.TagCleaner;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.plugincontrol.ExceptionHandler;
import de.tukl.cs.softech.agilereview.plugincontrol.exceptions.NoReviewSourceFolderException;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * Class that performs the cleanup process
 */
public class CleanupProcess implements IRunnableWithProgress {
    
    /**
     * Supported files mapping to the corresponding comment tags
     */
    private static final HashMap<String, String[]> supportedFiles = PropertiesManager.getParserFileendingsMappingTags();
    /**
     * Instance of ReviewAccess
     */
    private static ReviewAccess ra = ReviewAccess.getInstance();
    /**
     * the project to clean
     */
    private final IProject selProject;
    /**
     * delete (true) or keep (false) comments
     */
    private final boolean deleteComments;
    
    /**
     * Constructor of the Cleanup process
     * @param selProject the project to clean (remove tags)
     * @param deleteComments indicates whether to delete (true) or keep (false) comments
     */
    public CleanupProcess(IProject selProject, boolean deleteComments) {
        this.selProject = selProject;
        this.deleteComments = deleteComments;
    }
    
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("Performing cleanup: ", IProgressMonitor.UNKNOWN);
        monitor.worked(0);
        
        // get all reviews (not only open ones)
        ArrayList<Review> reviews = ra.getAllReviews();
        
        // some helper variables
        ArrayList<Comment> comments = new ArrayList<Comment>();
        HashSet<String> paths = new HashSet<String>();
        String selProjectPath = selProject.getFullPath().toOSString().replaceAll(Pattern.quote(System.getProperty("file.separator")), "");
        
        monitor.worked(10);
        monitor.subTask("Loading all reviews...");
        // load all comments for all reviews
        try {
            ra.fillDatabaseCompletely();
            
            // save all comments for the given project
            for (Review r : reviews) {
                comments.addAll(ra.getComments(r.getId(), selProjectPath));
            }
            
            monitor.worked(20);
            monitor.subTask("Searching for project files...");
            // save the paths of all files of the project
            paths.addAll(getFilesOfProject(selProject));
            
            monitor.worked(30);
            monitor.subTask("Removing tags...");
            // remove tags from files
            PluginLogger.log(this.getClass().toString(), "execute", "Removing comments from " + paths.toString());
            for (String path : paths) {
                IPath actPath = new Path(path);
                if (!TagCleaner.removeAllTags(actPath)) { throw new InterruptedException("Tags of file " + actPath.toOSString()
                        + " not successfully removed!"); }
            }
            
            monitor.worked(60);
            // delete comments based on users decision
            if (deleteComments) {
                monitor.subTask("Deleting comments...");
                PluginLogger.log(this.getClass().toString(), "execute", "Removing comments from XML");
                ra.deleteComments(comments);
            }
            
            monitor.worked(90);
            // unload closed reviews again
            monitor.subTask("Unloading closed reviews...");
            List<String> openReviews = Arrays.asList(PropertiesManager.getInstance().getOpenReviews());
            for (Review r : reviews) {
                if (!openReviews.contains(r.getId())) {
                    ra.unloadReviewComments(r.getId());
                }
            }
        } catch (NoReviewSourceFolderException e) {
            ExceptionHandler.handleNoReviewSourceFolderException();
        }
        monitor.worked(100);
        monitor.done();
    }
    
    /**
     * Get all files of the given project
     * @param project the project
     * @return list of paths of files relatively to the workspace
     */
    private HashSet<String> getFilesOfProject(final IProject project) {
        HashSet<String> paths = new HashSet<String>();
        try {
            for (IResource r : project.members()) {
                if (r instanceof IFolder) {
                    paths.addAll(getFilesOfFolderRecursively((IFolder) r));
                } else if (r instanceof IFile) {
                    if (supportedFiles.containsKey(((IFile) r).getFileExtension())) {
                        paths.add(r.getFullPath().toOSString());
                    }
                }
            }
        } catch (CoreException e) {
            Display.getDefault().asyncExec(new Runnable() {
                
                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), "CoreException",
                            "An eclipse internal error occured when performing cleanup!\n" + "The resource " + project.getName()
                                    + " does not exist or is closed.");
                }
                
            });
            PluginLogger.logError(this.getClass().toString(), "getFilesOfProject", "CoreException while trying to fetch files of project "
                    + project.getName() + ".", e);
        }
        return paths;
    }
    
    /**
     * Recursively get all files of the given folder
     * @param folder the folder
     * @return set of paths of files relatively to the workspace
     * @throws CoreException
     */
    private HashSet<String> getFilesOfFolderRecursively(IFolder folder) throws CoreException {
        HashSet<String> paths = new HashSet<String>();
        for (IResource r : folder.members()) {
            if (r instanceof IFolder) {
                paths.addAll(getFilesOfFolderRecursively((IFolder) r));
            } else if (r instanceof IFile) {
                if (supportedFiles.containsKey(((IFile) r).getFileExtension())) {
                    paths.add(r.getFullPath().toOSString());
                }
            }
        }
        return paths;
    }
    
}
