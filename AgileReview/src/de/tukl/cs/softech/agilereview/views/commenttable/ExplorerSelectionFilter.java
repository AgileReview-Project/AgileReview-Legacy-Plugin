package de.tukl.cs.softech.agilereview.views.commenttable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;

/**
 * Filters the viewer's model by the selections of the review explorer
 */
public class ExplorerSelectionFilter extends ViewerFilter {
    
    /**
     * ID's of the selected reviews
     */
    private final ArrayList<String> reviewIDs;
    
    /**
     * paths of the selected projects/folders/files
     */
    private final HashMap<String, HashSet<String>> paths;
    
    /**
     * @param reviewIDs the reviews
     * @param paths the paths
     */
    public ExplorerSelectionFilter(ArrayList<String> reviewIDs, HashMap<String, HashSet<String>> paths) {
        this.reviewIDs = reviewIDs;
        this.paths = paths;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        boolean matches = false;
        if (reviewIDs == null || paths == null || (reviewIDs.isEmpty() && paths.isEmpty())) {
            matches = true;
        } else {
            Comment comment = (Comment) element;
            
            if (reviewIDs.contains(comment.getReviewID())) {
                matches = true;
            }
            
            HashSet<String> containedPaths = paths.get(comment.getReviewID());
            if (!(containedPaths == null)) {
                for (String path : containedPaths) {
                    String pathMatcher = ".*" + Pattern.quote(path) + ".*";
                    if (ReviewAccess.computePath(comment).matches(pathMatcher)) {
                        matches = true;
                    }
                }
            }
        }
        return matches;
    }
    
}