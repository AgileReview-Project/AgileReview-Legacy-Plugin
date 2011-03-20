package de.tukl.cs.softech.agilereview.export;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.Configuration;
import net.sf.jxls.transformer.XLSTransformer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.FileDocument.File;
import agileReview.softech.tukl.de.FolderDocument.Folder;
import agileReview.softech.tukl.de.ProjectDocument.Project;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;

/**
 * This class represents the interface to jxls for exporting reviews and comments to xls, xlsx sheets
 */
public class XSLExport {

	/**
	 * Instance of ReviewAccess
	 */
	private static ReviewAccess ra = ReviewAccess.getInstance();
	
	/**
	 * This function provides the functionality for exporting the given Reviews to the outputPath by using the xls/xlsx template
	 * specified in the templatePath
	 * @param reviews which should be exported
	 * @param templatePath path to the xls/xlsx template
	 * @param outputPath directory to which the data should exported
	 * @throws ParsePropertyException occurs during the transfomation process of jxls
	 * @throws InvalidFormatException occurs during the transfomation process of jxls
	 * @throws IOException occurs during the transfomation process of jxls
	 */
	public static void exportReviews(List<Review> reviews, String templatePath, String outputPath) throws ParsePropertyException, InvalidFormatException, IOException {
		
		Map<String, Object> beans = new HashMap<String, Object>();
		
		//collect all comments
		ArrayList<Comment> comments = new ArrayList<Comment>();
		for(Review r : reviews) {
			comments.addAll(ra.getComments(r.getId()));
		}
		beans.put("comments", comments);
		
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		
		//collect all files which have been reviewed
		ArrayList<FileExportWrapper> reviewFiles = new ArrayList<FileExportWrapper>();
		HashSet<java.io.File> projects = new HashSet<java.io.File>();
		for(Review r : reviews) {
			for(Project p : ra.getProjects(r.getId())) {

				if(workspaceRoot.getProject(p.getName()).exists()) {
					projects.add(workspaceRoot.getProject(p.getName()).getLocation().toFile());
				}
				
				reviewFiles.addAll(convertFilesToWrappedFiles(Arrays.asList(p.getFileArray()), r.getId(), p.getName()));
				for(Folder f : p.getFolderArray()) {
					reviewFiles.addAll(getAllWrappedFiles(f, r.getId(), p.getName()));
				}
			}
		}
		beans.put("reviewFiles", reviewFiles);
		
		//collect all files which are in a project which has been reviewed partially
		ArrayList<FileExportWrapper> projectFiles = new ArrayList<FileExportWrapper>();
		for(java.io.File f : projects) {
			projectFiles.addAll(getAllWrappedFiles(f, f.getName()));
		}
		beans.put("projectFiles", projectFiles);
		beans.put("reviews", reviews);
		
        Configuration config = new Configuration();
        XLSTransformer transformer = new XLSTransformer( config );
        DateFormat df = new SimpleDateFormat( "yyyy-MM-dd_HH-mm-ss" );
        String filetype = templatePath.substring(templatePath.lastIndexOf("."));
        
        if(!outputPath.endsWith(System.getProperty("file.separator"))) {
        	outputPath += System.getProperty("file.separator");
        }
        
		transformer.transformXLS(templatePath, beans, outputPath+"agilereview_export_"+df.format(Calendar.getInstance().getTime())+filetype);
	}
	
	/**
	 * Searches recursively for all files under the given folder and wraps them into a {@link FileExportWrapper} object
	 * @param folder root node of the search process
	 * @param review to which these files correlate
	 * @param project to which these files correlate
	 * @return a list of all found and wrapped {@link FileExportWrapper} objects
	 */
	private static ArrayList<FileExportWrapper> getAllWrappedFiles(Folder folder, String review, String project) {
		ArrayList<FileExportWrapper> files = new ArrayList<FileExportWrapper>();
		
		files.addAll(convertFilesToWrappedFiles(Arrays.asList(folder.getFileArray()), review, project));
		for(Folder f : folder.getFolderArray()) {
			files.addAll(getAllWrappedFiles(f, review, project));
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
		
		//TODO exclude via preferences
		java.io.File[] fs = file.listFiles();
		for(java.io.File f : fs) {
			if(f.isFile() && !f.getName().equals(".project") && !f.getName().equals(".classpath")) {
				files.add(new FileExportWrapper(f, project));
			} else if(f.isDirectory() && !f.getName().equals("bin") && !f.getName().equals(".settings")) {
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
		
		for(File f : input) {
			files.add(new FileExportWrapper(f, review, project));
		}
		
		return files;
	}
}
