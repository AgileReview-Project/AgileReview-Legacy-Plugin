package de.tukl.cs.softech.agilereview.export;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
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

public class XSLExport {

	public static void exportReviews(List<Review> reviews, String templatePath, String outputPath) throws ParsePropertyException, InvalidFormatException, IOException {
		
		ReviewAccess ra = ReviewAccess.getInstance();
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
		ArrayList<java.io.File> projects = new ArrayList<java.io.File>();
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
        DateFormat df = new SimpleDateFormat( "yy-mm-dd_HH-mm-ss" );
        String filetype = templatePath.substring(templatePath.lastIndexOf("."));
		transformer.transformXLS(templatePath, beans, outputPath+"agilereview_export_"+df.format(Calendar.getInstance().getTime())+"."+filetype);
	}
	
	private static ArrayList<FileExportWrapper> getAllWrappedFiles(Folder folder, String review, String project) {
		ArrayList<FileExportWrapper> files = new ArrayList<FileExportWrapper>();
		
		files.addAll(convertFilesToWrappedFiles(Arrays.asList(folder.getFileArray()), review, project));
		for(Folder f : folder.getFolderArray()) {
			files.addAll(getAllWrappedFiles(f, review, project));
		}
		
		return files;
	}
	
	private static ArrayList<FileExportWrapper> getAllWrappedFiles(java.io.File file, String project) {
		ArrayList<FileExportWrapper> files = new ArrayList<FileExportWrapper>();
		
		java.io.File[] fs = file.listFiles();
		for(java.io.File f : fs) {
			if(f.isFile()) {
				files.add(new FileExportWrapper(f, project));
			} else if(f.isDirectory()) {
				files.addAll(getAllWrappedFiles(f, project));
			}
		}
		
		return files;
	}
	
	private static ArrayList<FileExportWrapper> convertFilesToWrappedFiles(List<File> input, String review, String project) {
		ArrayList<FileExportWrapper> files = new ArrayList<FileExportWrapper>();
		
		for(File f : input) {
			files.add(new FileExportWrapper(f, review, project));
		}
		
		return files;
	}
}
