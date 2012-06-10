package de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

import agileReview.softech.tukl.de.FileDocument.File;
import agileReview.softech.tukl.de.FilesDocument.Files;
import agileReview.softech.tukl.de.FolderDocument.Folder;
import agileReview.softech.tukl.de.ProjectDocument.Project;

/**
 * Special implementation of {@link AbstractMultipleWrapper} for <file> nodes
 */
public class MultipleFileWrapper extends AbstractMultipleWrapper {
    /**
     * Constructor, creating a new MultipleFileWrapper based on the given parameters
     * @param initalElement a <file> node this wrapper should represent
     * @param reviewId Review this wrapper belongs to
     */
    public MultipleFileWrapper(File initalElement, String reviewId) {
        super(initalElement, reviewId);
    }
    
    @Override
    public Object[] getChildren() {
        return new Object[0];
    }
    
    @Override
    protected String createPath(XmlObject obj) {
        File f = (File) obj;
        String path = f.getName();
        XmlCursor c = f.newCursor();
        while (c.toParent() && !(c.getObject() instanceof Files)) {
            XmlObject o = c.getObject();
            if (o instanceof Folder) {
                path = ((Folder) o).getName() + System.getProperty("file.separator") + path;
            } else if (o instanceof Project) {
                path = ((Project) o).getName() + System.getProperty("file.separator") + path;
            }
        }
        c.dispose();
        
        return path;
    }
    
}