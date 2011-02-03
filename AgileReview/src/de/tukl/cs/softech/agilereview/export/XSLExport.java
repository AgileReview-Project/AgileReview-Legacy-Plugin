package de.tukl.cs.softech.agilereview.export;

import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import net.sf.jxls.exception.ParsePropertyException;
import net.sf.jxls.transformer.Configuration;
import net.sf.jxls.transformer.XLSTransformer;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.eclipse.core.resources.IProject;
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
		ArrayList<File> reviewFiles = new ArrayList<File>();
		ArrayList<java.io.File> projects = new ArrayList<java.io.File>();
		for(Review r : reviews) {
			for(Project p : ra.getProjects(r.getId())) {

				if(workspaceRoot.getProject(p.getName()).exists()) {
					projects.add(workspaceRoot.getProject().getLocation().toFile());
				}
				
				reviewFiles.addAll(Arrays.asList(p.getFileArray()));
				for(Folder f : p.getFolderArray()) {
					reviewFiles.addAll(Arrays.asList(f.getFileArray()));
				}
			}
		}
		beans.put("reviewFiles", reviewFiles);
		
		//collect all files which are in a project which has been reviewed partially
		ArrayList<java.io.File> projectFiles = new ArrayList<java.io.File>();
		for(java.io.File f : projects) {
			projectFiles.addAll(getAllFiles(f));
		}
		beans.put("projectFiles", projectFiles);
		beans.put("reviews", reviews);
		
        Configuration config = new Configuration();
        XLSTransformer transformer = new XLSTransformer( config );
		transformer.transformXLS(templatePath, beans, outputPath);
	}
	
	private static ArrayList<java.io.File> getAllFiles(java.io.File file) {
		ArrayList<java.io.File> files = new ArrayList<java.io.File>();
		
		java.io.File[] fs = file.listFiles();
		for(java.io.File f : fs) {
			if(f.isFile()) {
				files.add(f);
			} else if(f.isDirectory()) {
				getAllFiles(f);
			}
		}
		
		return files;
	}
}
