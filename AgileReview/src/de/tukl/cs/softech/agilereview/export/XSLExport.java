package de.tukl.cs.softech.agilereview.export;

import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.Configuration;
import net.sf.jxls.transformer.XLSTransformer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.PlatformUI;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.FileDocument.File;
import agileReview.softech.tukl.de.FolderDocument.Folder;
import agileReview.softech.tukl.de.ProjectDocument.Project;
import agileReview.softech.tukl.de.ReplyDocument.Reply;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * This class represents the interface to jxls for exporting reviews and comments to xls, xlsx sheets
 */
public class XSLExport implements IRunnableWithProgress {
    
    /**
     * Instance of ReviewAccess
     */
    private final ReviewAccess ra = ReviewAccess.getInstance();
    /**
     * Instance of PropertiesManager
     */
    private static PropertiesManager pm = PropertiesManager.getInstance();
    
    /**
     * Reviews to be exported
     */
    private final List<Review> reviews;
    /**
     * Template path where the xls template can be found
     */
    private final String templatePath;
    /**
     * Output path where the new instance of the exported template should be stored
     */
    private String outputPath;
    
    /**
     * Creates a new Instance of XSLExport for a list of reviews, the templatePath where the export class can find the xls template to be used and the
     * outputPath where the new instance of the exported template should be stored
     * @param reviews which should be exported
     * @param templatePath path to the xls/xlsx template
     * @param outputPath directory to which the data should exported
     */
    public XSLExport(List<Review> reviews, String templatePath, String outputPath) {
        this.reviews = reviews;
        this.templatePath = templatePath;
        this.outputPath = outputPath;
    }
    
    /**
     * Starts the export process for the attributes with which this class was instantiated
     * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
        try {
            exportReviews(monitor);
        } catch (ParsePropertyException e) {
            PluginLogger.logError(this.getClass().toString(), "run", "ParsePropertyException", e);
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error while exporting Reviews",
                    "The formulas in the selected template file cannot be evaluated correctly");
        } catch (InvalidFormatException e) {
            PluginLogger.logError(this.getClass().toString(), "run", "InvalidFormatException", e);
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error while exporting Reviews",
                    "An error occured while exporting the selected Reviews!");
        } catch (IOException e) {
            PluginLogger.logError(this.getClass().toString(), "run", "IOException", e);
            MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error while exporting Reviews",
                    "One of the selected files could not be read or written!");
        }
    }
    
    /**
     * This function provides the functionality for exporting the given Reviews to the outputPath by using the xls/xlsx template specified in the
     * templatePath
     * @param monitor
     * @throws ParsePropertyException occurs during the transfomation process of jxls
     * @throws InvalidFormatException occurs during the transfomation process of jxls
     * @throws IOException occurs during the transfomation process of jxls
     */
    private void exportReviews(IProgressMonitor monitor) throws ParsePropertyException, InvalidFormatException, IOException {
        monitor.beginTask("Performing export: ", IProgressMonitor.UNKNOWN);
        monitor.subTask("Collecting review data...");
        Map<String, Object> beans = new HashMap<String, Object>();
        
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        
        //collect all files which have been reviewed
        ArrayList<FileExportWrapper> reviewFiles = new ArrayList<FileExportWrapper>();
        ArrayList<CommentWrapper> comments = new ArrayList<CommentWrapper>();
        HashSet<java.io.File> projects = new HashSet<java.io.File>();
        for (Review r : reviews) {
            for (Project p : ra.getProjects(r.getId())) {
                
                if (workspaceRoot.getProject(p.getName()).exists()) {
                    projects.add(workspaceRoot.getProject(p.getName()).getLocation().toFile());
                }
                
                reviewFiles.addAll(convertFilesToWrappedFiles(Arrays.asList(p.getFileArray()), r.getId(), p.getName()));
                for (File f : p.getFileArray()) {
                    collectComments(comments, r.getId(), p.getName(), f);
                }
                for (Folder f : p.getFolderArray()) {
                    reviewFiles.addAll(getAllWrappedFiles(f, r.getId(), p.getName(), comments));
                }
            }
        }
        beans.put("reviewFiles", reviewFiles);
        beans.put("comments", comments);
        
        // Collect all replies
        ArrayList<ReplyWrapper> replies = new ArrayList<ReplyWrapper>();
        for (CommentWrapper c : comments) {
            for (Reply r : c.getReplies()) {
                replies.add(new ReplyWrapper(r, c));
            }
        }
        beans.put("replies", replies);
        
        //collect all files which are in a project which has been reviewed partially
        ArrayList<FileExportWrapper> projectFiles = new ArrayList<FileExportWrapper>();
        for (java.io.File f : projects) {
            projectFiles.addAll(getAllWrappedFiles(f, f.getName()));
        }
        beans.put("projectFiles", projectFiles);
        beans.put("reviews", reviews);
        
        Configuration config = new Configuration();
        XLSTransformer transformer = new XLSTransformer(config);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String filetype = templatePath.substring(templatePath.lastIndexOf("."));
        
        if (!outputPath.endsWith(System.getProperty("file.separator"))) {
            outputPath += System.getProperty("file.separator");
        }
        
        monitor.subTask("Creating export sheet...");
        transformer.transformXLS(templatePath, beans, outputPath + "agilereview_export_" + df.format(Calendar.getInstance().getTime()) + filetype);
        monitor.done();
    }
    
    /**
     * @param comments
     * @param r
     * @param p
     * @param f
     * @author Malte Brunnlieb (06.11.2013)
     */
    private static void collectComments(ArrayList<CommentWrapper> comments, String reviewId, String projectName, File f) {
        for (Comment c : f.getCommentArray()) {
            comments.add(new CommentWrapper(c, new FileExportWrapper(f, reviewId, projectName)));
        }
    }
    
    /**
     * Searches recursively for all files under the given folder and wraps them into a {@link FileExportWrapper} object
     * @param folder root node of the search process
     * @param review to which these files correlate
     * @param project to which these files correlate
     * @return a list of all found and wrapped {@link FileExportWrapper} objects
     */
    private static ArrayList<FileExportWrapper> getAllWrappedFiles(Folder folder, String review, String project, ArrayList<CommentWrapper> comments) {
        ArrayList<FileExportWrapper> files = new ArrayList<FileExportWrapper>();
        
        TreeSet<String> omittings = new TreeSet<String>(Arrays.asList(pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.EXPORT_OMITTINGS).split(
                ",")));
        LinkedList<File> tmpFiles = new LinkedList<File>();
        for (File f : Arrays.asList(folder.getFileArray())) {
            if (!omittings.contains(f.getName())) {
                tmpFiles.add(f);
                collectComments(comments, review, project, f);
            }
        }
        
        files.addAll(convertFilesToWrappedFiles(tmpFiles, review, project));
        for (Folder f : folder.getFolderArray()) {
            files.addAll(getAllWrappedFiles(f, review, project, comments));
        }
        
        return files;
    }
    
    /**
     * Searches recursively for all files under the given file and wraps them into a {@link FileExportWrapper} object
     * @param file root node of the search process
     * @param project to which these files correlate
     * @return a list of all found and wrapped {@link FileExportWrapper} objects
     */
    private static ArrayList<FileExportWrapper> getAllWrappedFiles(java.io.File file, String project) {
        ArrayList<FileExportWrapper> files = new ArrayList<FileExportWrapper>();
        
        final TreeSet<String> omittings = new TreeSet<String>(Arrays.asList(pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.EXPORT_OMITTINGS)
                .split(",")));
        java.io.File[] fs = file.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(java.io.File arg0, String arg1) {
                if (omittings.contains(arg1)) {
                    return false;
                } else {
                    return true;
                }
            }
        });
        
        for (java.io.File f : fs) {
            if (f.isFile()) {
                files.add(new FileExportWrapper(f, project));
            } else if (f.isDirectory()) {
                files.addAll(getAllWrappedFiles(f, project));
            }
        }
        
        return files;
    }
    
    /**
     * Converts a given list of files in a given review and project to the respective {@link FileExportWrapper} representation
     * @param input files which should be wrapped
     * @param review in which the given files are in
     * @param project under which the given files are lying
     * @return a list of all wrapped {@link FileExportWrapper}
     */
    private static ArrayList<FileExportWrapper> convertFilesToWrappedFiles(List<File> input, String review, String project) {
        ArrayList<FileExportWrapper> files = new ArrayList<FileExportWrapper>();
        
        for (File f : input) {
            files.add(new FileExportWrapper(f, review, project));
        }
        
        return files;
    }
}
