package de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.xmlbeans.XmlObject;

import agileReview.softech.tukl.de.ProjectDocument.Project;

/**
 * Special implementation of {@link AbstractMultipleWrapper} for <project> nodes
 */
public class MultipleProjectWrapper extends AbstractMultipleWrapper {
    /**
     * Constructor, creating a new MultipleProjectWrapper based on the given parameters
     * @param initalElement a <project> node this wrapper should represent
     * @param reviewId Review this wrapper belongs to
     */
    public MultipleProjectWrapper(Project initalElement, String reviewId) {
        super(initalElement, reviewId);
    }
    
    @Override
    public Object[] getChildren() {
        ArrayList<Object> result = new ArrayList<Object>();
        for (XmlObject o : internalList) {
            Project p = (Project) o;
            result.addAll(Arrays.asList(p.getFolderArray()));
            result.addAll(Arrays.asList(p.getFileArray()));
        }
        
        return result.toArray();
    }
    
    @Override
    protected String createPath(XmlObject obj) {
        return ((Project) obj).getName();
    }
}