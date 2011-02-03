package de.tukl.cs.softech.agilereview.export;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import agileReview.softech.tukl.de.FileDocument.File;

public class FileExportWrapper {
	
	private String filename = "";
	private String review = "";
	private String project = "";
	private String path = "";

	protected FileExportWrapper(java.io.File file, String project) {
		this.filename = file.getName();
		this.project = project;
		this.path = file.getPath();
	}
	
	protected FileExportWrapper(File file, String review, String project) {
		this.filename = file.getName();
		this.review = review;
		this.project = project;
		this.path = ReviewAccess.computePath(file);
	}
	
	public String getFilename() {
		return filename;
	}

	public String getReview() {
		return review;
	}

	public String getProject() {
		return project;
	}

	public String getPath() {
		return path;
	}
}
