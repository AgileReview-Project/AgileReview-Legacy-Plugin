package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;

/**
 * Class that performs the cleanup process
 */
public class CleanupProcess implements IRunnableWithProgress {
    
    /**
     * Instance of ReviewAccess
     */
    private static ReviewAccess ra = ReviewAccess.getInstance();
    /**
     * the review to clean
     */
    private Review review;
    /**
     * delete (true) or keep (false) comments
     */
    private boolean deleteComments;
    
    /**
     * Constructor of the Cleanup process
     * @param review the review to clean (remove tags)
     * @param deleteComments indicates whether to delete (true) or keep (false) comments
     */
    public CleanupProcess(Review review, boolean deleteComments) {
        this.review = review;
        this.deleteComments = deleteComments;
    }
    
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        monitor.beginTask("Performing cleanup: ", IProgressMonitor.UNKNOWN);
        monitor.worked(0);
        
        monitor.subTask("Loading related comments...");
        List<Comment> comments = ra.getComments(review.getId());
        monitor.worked(10);
        
        //        try {
        //            ra.fillDatabaseCompletely();
        //            
        //            // save all comments for the given project
        //            for (Review r : reviews) {
        //                comments.addAll(ra.getComments(r.getId(), selProjectPath));
        //            }
        //            
        //            monitor.worked(20);
        //            monitor.subTask("Searching for project files...");
        //            // save the paths of all files of the project
        //            paths.addAll(getFilesOfProject(selProject));
        //            
        //            monitor.worked(30);
        //            monitor.subTask("Removing tags...");
        //            // remove tags from files
        //            PluginLogger.log(this.getClass().toString(), "execute", "Removing comments from " + paths.toString());
        //            for (String path : paths) {
        //                IPath actPath = new Path(path);
        //                if (!TagCleaner.removeAllTags(actPath)) { throw new InterruptedException("Tags of file " + actPath.toOSString()
        //                        + " not successfully removed!"); }
        //            }
        //            
        //            monitor.worked(60);
        //            // delete comments based on users decision
        //            if (deleteComments) {
        //                monitor.subTask("Deleting comments...");
        //                PluginLogger.log(this.getClass().toString(), "execute", "Removing comments from XML");
        //                ra.deleteComments(comments);
        //            }
        //            
        //            monitor.worked(90);
        //            // unload closed reviews again
        //            monitor.subTask("Unloading closed reviews...");
        //            List<String> openReviews = Arrays.asList(PropertiesManager.getInstance().getOpenReviews());
        //            for (Review r : reviews) {
        //                if (!openReviews.contains(r.getId())) {
        //                    ra.unloadReviewComments(r.getId());
        //                }
        //            }
        //        } catch (NoReviewSourceFolderException e) {
        //            ExceptionHandler.handleNoReviewSourceFolderException();
        //        }
        monitor.worked(100);
        monitor.done();
    }
}