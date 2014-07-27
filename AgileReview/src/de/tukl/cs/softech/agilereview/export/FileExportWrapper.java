package de.tukl.cs.softech.agilereview.export;

import agileReview.softech.tukl.de.FileDocument.File;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;

/**
 * File wrapper in order to get a standardized object with the fitting output informations for an export
 */
public class FileExportWrapper {
    
    /**
     * Filename of the represented file
     */
    private String filename = "";
    /**
     * Review of the represented file
     */
    private String review = "";
    /**
     * Project of the represented file
     */
    private String project = "";
    /**
     * Path of the represented file
     */
    private String path = "";
    /**
     * Full Path
     */
    private String fullPath = "";
    
    /**
     * Creates a new FileExportWrapper instance with a given {@link java.io.File} and the correlated project
     * @param file which should be represented by this object
     * @param project correlating to the given file
     */
    protected FileExportWrapper(java.io.File file, String project) {
        this.filename = file.getName();
        this.project = project;
        String p = file.getPath();
        this.path = p.substring(p.indexOf(project) + project.length(), p.indexOf(filename) - 1);
    }
    
    /**
     * Creates a new FileExportWrapper instance with a given {@link File} and the correlated project
     * @param file which should be represented by this object
     * @param review in which the given file is in
     * @param project correlating to the given file
     */
    protected FileExportWrapper(File file, String review, String project) {
        this.filename = file.getName();
        this.review = review;
        this.project = project;
        String p = ReviewAccess.computePath(file);
        this.path = p.substring(p.indexOf(project) + project.length());
        this.fullPath = p;
    }
    
    /**
     * Returns the filename of this instance
     * @return the filename of this instance
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * Returns the review of this instance
     * @return the review of this instance
     */
    public String getReview() {
        return review;
    }
    
    /**
     * Returns the project of this instance
     * @return the project of this instance
     */
    public String getProject() {
        return project;
    }
    
    /**
     * Returns the path of this instance
     * @return the path of this instance
     */
    public String getPath() {
        return path;
    }
    
    /**
     * @return the fullPath
     * @author Malte Brunnlieb (06.11.2013)
     */
    public String getFullPath() {
        return fullPath;
    }
}
