package de.tukl.cs.softech.agilereview.control;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.CommentsDocument;
import agileReview.softech.tukl.de.CommentsDocument.Comments;
import agileReview.softech.tukl.de.FilesDocument.Files;
import agileReview.softech.tukl.de.FolderDocument.Folder;
import agileReview.softech.tukl.de.PersonInChargeDocument.PersonInCharge;
import agileReview.softech.tukl.de.ProjectDocument.Project;
import agileReview.softech.tukl.de.ReviewDocument;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.model.ReviewFileModel;
import de.tukl.cs.softech.agilereview.model.ReviewModel;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * Class for accessing the review and comment data (xml and internal model).  
 */
public class ReviewAccess {
	
	////////////////
	// attributes //
	////////////////
	/**
	 * Private instance for Singleton-Pattern
	 */
	private static ReviewAccess RA = new ReviewAccess();
	
	/**
	 * Reference to the folder where the review and comments xml files are located
	 */
	private static File REVIEW_REPO_FOLDER;
	
	/**
	 * Instance of the comment model
	 */
	private ReviewModel rModel = new ReviewModel();
	
	/**
	 * Instance of the review file model
	 */
	private ReviewFileModel rFileModel = new ReviewFileModel();
	
	
	
	
	////////////////////
	// static methods //
	////////////////////
	/**
	 * Creates a File object which represents the file for storing comments based on the given reviewId/author pair 
	 * @param reviewId 
	 * @param author
	 * @return File for the given parameter pair
	 */
	private static File createCommentFile(String reviewId, String author)
	{
		return new File(ReviewAccess.createReviewFolder(reviewId)+System.getProperty("file.separator")+"author_"+author+".xml");
	}
	
	/**
	 * Creates a File object which represents the folder of the given review
	 * @param reviewId
	 * @return Folder for this review
	 */
	private static File createReviewFolder(String reviewId)
	{
		return new File(REVIEW_REPO_FOLDER+System.getProperty("file.separator")+"review."+reviewId);
	}
	
	/**
	 * Creates a File object which represents the the review-file of the given review
	 * @param reviewId
	 * @return review file for this review
	 */
	private static File createReviewFile(String reviewId)
	{
		return new File(ReviewAccess.createReviewFolder(reviewId)+System.getProperty("file.separator")+"review.xml");
	}
	
	/**
	 * Creates a Project as child of the given XmlObject (if possible)
	 * @param parent parent XmlObject (should be Files)
	 * @param name name of the file
	 * @return The newly created project or null if the given parent does not support project children
	 */
	private static Project createProject (XmlObject parent, String name)
	{
		Project p = null;
		if (parent instanceof Files)
		{
			p = ((Files)parent).addNewProject();
			p.setName(name);
		}
	
		return p;
	}
	
	/**
	 * Creates a Folder as child of the given XmlObject (if possible)
	 * @param parent parent XmlObject (should be Folder or Project)
	 * @param name name of the folder
	 * @return The newly created folder or null if the given parent does not support folder children
	 */
	private static Folder createFolder(XmlObject parent, String name)
	{
		Folder f = null;
		if (parent instanceof Project)
		{
			f = ((Project)parent).addNewFolder();
			f.setName(name);
		}
		else
		{
			f = ((Folder)parent).addNewFolder();
			f.setName(name);
		}
		return f;
	}
	
	/**
	 * Creates a File as child of the given XmlObject (if possible)
	 * @param parent parent XmlObject (should be Folder or Project)
	 * @param name name of the file
	 * @return The newly created file or null if the given parent does not support file children
	 */
	private static agileReview.softech.tukl.de.FileDocument.File createFile(XmlObject parent, String name)
	{
		agileReview.softech.tukl.de.FileDocument.File f = null;
		if (parent instanceof Project)
		{
			f = ((Project)parent).addNewFile();
			f.setName(name);
		}
		else
		{
			f = ((Folder)parent).addNewFile();
			f.setName(name);
		}
		return f;
	}
	
	/**
	 * Computes the path for a given comment
	 * @param comment comment for which the path should be returned
	 * @return path of the file, to which this comment belongs
	 */
	public static String computePath(Comment comment)
	{  
		XmlCursor c = comment.newCursor();
		c.toParent();
		String path = c.getAttributeText(new QName("name"));
		
		while(c.toParent() && !(c.getObject() instanceof Files))
		{
			path = c.getAttributeText(new QName("name"))+System.getProperty("file.separator")+path;
		}
		c.dispose();
		
		return path;
	}
	
	/**
	 * Singleton Pattern
	 * @return Instance of this class
	 */
	public static ReviewAccess getInstance()
	{
		return RA;
	}
	
	
	
	////////////////////////////////
	// private non-static methods //
	////////////////////////////////
	/**
	 * Constructor: Sets the directory where to look for the xml-Files 
	 */
	private ReviewAccess()
	{
		// Set the directory where the comments are located
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject p = workspaceRoot.getProject(PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.SOURCE_FOLDER));
		try
		{
			// Create a new Project, if necessary
			if (!p.exists())
			{
				p.create(null);
			}
			// Open the Project, if necessary
			if (!p.isOpen())
			{
				p.open(null);
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
			// TODO: Auto-generated
		}
		REVIEW_REPO_FOLDER = p.getLocation().toFile();
	}

	/**
	 * Clears all used models
	 */
	private void clearAllModels()
	{
		this.rFileModel.clearModel();
		this.rModel.clearModel();
	}
	
	/**
	 * Fills the comment model
	 * @throws XmlException
	 * @throws IOException
	 */
	private void loadAllComment() throws XmlException, IOException
	{
		// Get all relevant folders in the review repository
		FileFilter folderFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		};
		File[] allFolders = REVIEW_REPO_FOLDER.listFiles(folderFilter);
		
		// Iterate all folders
		for (File currFolder : allFolders)
		{
			FilenameFilter fileFilter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.startsWith("author_");
				}
			};
			File[] allFiles = currFolder.listFiles(fileFilter);
			
			// Iterate all files in the current folder
			for (File currFile : allFiles)
			{
				// Open file and read basic information
				CommentsDocument doc = CommentsDocument.Factory.parse(currFile);
				this.rFileModel.addXmlDocument(doc, currFile);
				Comments currComments = doc.getComments();
				
				// Find all comments in this file and store them
				XmlObject[] xPathResult = currComments.selectPath("declare namespace s='http://de.tukl.softech.agileReview'; $this//s:comment");
				for (int j=0;j<xPathResult.length;j++)
				{
					Comment c = (Comment)xPathResult[j];
					this.rModel.addComment(c);
				}
			}
		}
	}
	
	/**
	 * Fills the review model
	 * @throws XmlException
	 * @throws IOException
	 */
	private void loadAllReviews() throws XmlException, IOException
	{
		// Get all relevant folders in the review repository
		FileFilter folderFilter = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		};
		File[] allFolders = REVIEW_REPO_FOLDER.listFiles(folderFilter);
		
		// Iterate all folders
		for (File currFolder : allFolders)
		{
			// Get all relevant files in the review repository (only review files(no "." in author names allowed))
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File dir, String name) {
					return name.equals("review.xml");		
				}
			};
			File[] allFiles = currFolder.listFiles(filter);
			
			// Fill internal database
			// Iterate all review-files in directory (should only be one)
			for (int i=0;i<allFiles.length;i++)
			{
				// Open file and store review
				ReviewDocument doc = ReviewDocument.Factory.parse(allFiles[i]);
				this.rFileModel.addXmlDocument(doc, allFiles[i]);
				Review currReview = doc.getReview();
				rModel.addReview(currReview);		
			}
		}
	}

	
	/**
	 * Finds the given path in the given CommentsDocument, whereas the last element is of the given type.
	 * In case of createPath being true, the path is created if not there.
	 * @param currCommentsDoc CommentsDocument where the path should be created
	 * @param path path that should be found/created
	 * @param type type of the last element of the path
	 * @param createPath if true, path is created (if necessary)
	 * @return element specified by path or null if the path was not found and createPath is false
	 */
	private XmlObject findXmlPath(CommentsDocument currCommentsDoc, String path, int type, boolean createPath)
	{
		Comments currComments = currCommentsDoc.getComments();
		XmlObject currObject = currComments.getFiles();
		
		if (path.startsWith(System.getProperty("file.separator")))
		{
			path = path.replaceFirst(Pattern.quote(System.getProperty("file.separator")), "");
		}
		
		// Iterate path and stop one position before end
		String[] pathArray = path.split(Pattern.quote(System.getProperty("file.separator")));
		for (int i=0;i<pathArray.length;i++)
		{
			String xPathQuery = "declare namespace s='http://de.tukl.softech.agileReview'; $this/s:*[@name=\""+pathArray[i]+"\"]";
			XmlObject[] xPathResult = currObject.selectPath(xPathQuery);
			// if no result is found, the corresponding object has to be created
			if (xPathResult.length == 0)
			{
				if (!createPath)
				{
					return null;
				}
				else if (i==0)
				{
					// First element: Create a project
					currObject = createProject(currObject, pathArray[i]);
				}
				else if (i==pathArray.length-1)
				{
					// Last element: Create the given type
					switch (type) 
					{
						case IResource.PROJECT:  currObject = createProject(currObject, pathArray[pathArray.length-1]); break;
						case IResource.FOLDER:	currObject = createFolder(currObject, pathArray[pathArray.length-1]); break;
						case IResource.FILE:	currObject = createFile(currObject,  pathArray[pathArray.length-1]); break;
					}
				}
				else
				{
					// As we do neither consider the last element nor the first, we always create a folder
					currObject = createFolder(currObject, pathArray[i]);
				}

			}
			else
			{
				// Only one result should be found then
				currObject = xPathResult[0];
			}
		}

		return currObject;
	}
	
	/**
	 * Deletes the given XmlObject and the part of it's path which is empty afterwards
	 * @param pathObject Object to delete
	 */
	private void cleanXmlPath(XmlObject pathObject) {
		// Remove xml
		XmlCursor c = pathObject.newCursor();
		// Remove <comment> node
		c.removeXml();
		// Recursively check parents
		while (c.toParent() && !c.toFirstChild() && !(c.getObject() instanceof Files))
		{
			c.removeXml();
		}	
		c.dispose();
	}
	
	
	
	///////////////////////////////
	// public non-static methods //
	///////////////////////////////
	
		///////////////////////////
		// comment functionality // 
		///////////////////////////
	
	/**
	 *  Creates a new empty Comment at the right position in the xml, which is returned
	 * @param reviewId Review in which the comment is placed
	 * @param author author of the comment
	 * @param path file path of the commented file
	 * @return empty comment
	 * @throws IOException 
	 */
	public Comment createNewComment(String reviewId, String author, String path) throws IOException 
	{		
		// Check if file for this author in this review does already exist (assumption: database and file system are synch)
		File commentFile = ReviewAccess.createCommentFile(reviewId, author);
		// Check if file for this author does already exist
		if (!this.rFileModel.containsFile(commentFile))
		{
			// No file exists. So we have to create one
			CommentsDocument commentsDoc = CommentsDocument.Factory.newInstance();
			Comments comments = commentsDoc.addNewComments();
			comments.addNewAuthor().setName(author);
			comments.addNewFiles();
			// And put it into the database (for being saved to file later)
			this.rFileModel.addXmlDocument(commentsDoc, commentFile);
		}
		
		// Now get the right document and find the right place in it
		CommentsDocument currCommentsDoc =  this.rFileModel.getCommentsDoc(commentFile);
		agileReview.softech.tukl.de.FileDocument.File currFile = (agileReview.softech.tukl.de.FileDocument.File)findXmlPath(currCommentsDoc, path, IResource.FILE, true);
		
		// Prepare new Comment
		Comment result = currFile.addNewComment();
		// Find the next id
		Integer newKey = this.rModel.getNextCommentIdFor(reviewId, author);
		result.setId("c"+newKey);
		
		// Fill attributes
		result.setAuthor(author);
		result.setReviewID(reviewId);
		result.setCreationDate(Calendar.getInstance());
		// last-modified will be set when saved
		result.setPriority(0);
		result.setRecipient("");
		result.setStatus(0);
		result.setRevision(0); 
		result.setCreationDate(Calendar.getInstance());
		result.setLastModified(Calendar.getInstance());
		
		// Fill children
		result.setText("");
		// Reference will be set when saved
		result.addNewReplies();
		
		// Store comment in database
		this.rModel.addComment(result);
		
		// XXX: hier das neu erzeugte direkt speichern? (wäre konsistent zum Review)
		//      wo wird eigentlich gespeichert?
		
		// Return the new empty comment 
		return result;
	}
	
	/**
	 * Deletes the specified comment of the specified author in the specified review.
	 * @param reviewId review id of the comment to be deleted
	 * @param author author of the comment to be deleted
	 * @param commentId comment id of the comment to be deleted
	 * @throws IOException 
	 */
	public void deleteComment (String reviewId, String author, String commentId) throws IOException
	{
		// Find comment in database
		Comment delCom = this.rModel.getComment(reviewId, author, commentId);
		
		// Remove xml nodes
		cleanXmlPath(delCom);
		
		// Remove from database and eventually from file system
		if (this.rModel.removeComment(reviewId, author, commentId))
		{
			// Last comment of this author in this review has been deleted
			// -> Remove from file system
			File fileToDelete = ReviewAccess.createCommentFile(reviewId, author);
			this.rFileModel.removeXmlDocument(fileToDelete);
		}
	}
	
	/**
	 * @see ReviewAccess#deleteComment(String, String, String)
	 * @param comment
	 * @throws IOException
	 */
	public void deleteComment (Comment comment) throws IOException
	{
		deleteComment(comment.getReviewID(), comment.getAuthor(), comment.getId());
	}
	
	/**
	 * @see ReviewAccess#deleteComment(String, String, String)
	 * @param comments
	 * @throws IOException
	 */
	public void deleteComments (Collection<Comment> comments) throws IOException {
		for(Comment c : comments) {
			deleteComment(c.getReviewID(), c.getAuthor(), c.getId());
		}
	}
	
	/**
	 * Returns all Comments, or an empty List if no comments are found
	 * @return all Comments
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public ArrayList<Comment> getAllComments() throws XmlException, IOException
	{			
		return this.rModel.getAllComments();
	}
	
	/**
	 * Returns all comments of the given review
	 * @param reviewId
	 * @return all comments of given review
	 */
	public ArrayList<Comment> getComments(String reviewId)
	{
		return this.rModel.getComments(reviewId);
	}
	
	/**
	 * Return all comments of this review, which are associated with a file 
	 * in the given folder or one of its sub-folders
	 * @param reviewId
	 * @param path
	 * @return Comments associated with a file in the given folder or one of its sub-folders
	 */
	public ArrayList<Comment> getComments(String reviewId, String path)
	{
		return this.rModel.getComments(reviewId, path);
	}
	
	
		//////////////////////////
		// review functionality //
		//////////////////////////

	/**
	 * States whether the given reviewId belongs to a loaded review in the model
	 * @param reviewId review id in question
	 * @return <i>true</i> if review is in model, <i>false</i> otherwise
	 */
	public boolean isReviewLoaded(String reviewId)
	{
		boolean contains = this.rModel.containsReview(reviewId);
		boolean loaded = !this.rModel.getComments(reviewId).isEmpty();
		return contains && loaded; 
	}
	
	/**
	 * Returns all reviews
	 * @return all reviews
	 */
	public ArrayList<Review> getAllReviews()
	{
		return rModel.getAllReviews();
	}
	
	/**
	 * Return all project elements which are commented in the given review.
	 * A project can be returned multiple times, 
	 * as there might be many project elements for the same project (one per author). 
	 * @param reviewId
	 * @return List of all projects of the given review 
	 */
	public HashSet<Project> getProjects(String reviewId)
	{
		HashSet<Project> result = new HashSet<Project>();
			
		// Iterate all comments of this review
		for (Comment c : rModel.getComments(reviewId))
		{
			// Get the corresponding <project> node
			XmlObject[] xPathResult = c.selectPath("declare namespace s='http://de.tukl.softech.agileReview'; $this/ancestor::s:project");
			
			// Every comment has exactly one ancestor of type project
			Project currProject = (Project)xPathResult[0];
			result.add(currProject);
		}
		
		return result;
	}
	
	/**
	 * Creates a new Review with the given Id and returns it to be filled
	 * @param reviewId
	 * @return empty Review (except for review id) or null if reviewid is already in use
	 * @throws IOException 
	 */
	public Review createNewReview(String reviewId) throws IOException
	{
		// Create the new review
		ReviewDocument revDoc = ReviewDocument.Factory.newInstance();
		Review result = revDoc.addNewReview();
		result.setId(reviewId);
		result.setStatus(0);
		result.setReferenceId("");
		PersonInCharge p = result.addNewPersonInCharge();
		p.setName("");
		p.setMailaddress("");
		result.setDescription("");
		
		// Add review to model,
		// return null in case of the reviewId being already in use
		if (!rModel.addReview(result))
		{
			return null;
		}
		
		// Create the folder for this review
		File commentFolder = ReviewAccess.createReviewFolder(reviewId);
		if (!commentFolder.exists())
		{
			commentFolder.mkdir();
		}
		
		// Create the file
		File revFile = ReviewAccess.createReviewFile(reviewId);
		
		// save new review file
		this.rFileModel.addXmlDocument(revDoc, revFile);
		this.rFileModel.save(revFile);
		
		return result;
	}
	
	/**
	 * Deletes a review and all comments specified by this review
	 * @param reviewId
	 */
	public void deleteReview(String reviewId) 
	{	
		File delFile = ReviewAccess.createReviewFile(reviewId);
		
		// Delete review from Model
		this.rModel.removeReview(reviewId);
		this.rFileModel.removeXmlDocument(delFile);
	}
	
		///////////////////////
		// I/O functionality //
		///////////////////////
	/**
	 * Load all comments of the given review into the database
	 * @param reviewId
	 * @throws XmlException
	 * @throws IOException
	 */
	public void loadReviewComments(String reviewId) throws XmlException, IOException
	{
		File currFolder = ReviewAccess.createReviewFolder(reviewId);
		
		FilenameFilter fileFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("author_");
			}
		};
		File[] allFiles = currFolder.listFiles(fileFilter);
		
		// Iterate all files in the current folder
		for (File currFile : allFiles)
		{
			// Open file and read basic information
			CommentsDocument doc = CommentsDocument.Factory.parse(currFile);
			this.rFileModel.addXmlDocument(doc, currFile);
			Comments currComments = doc.getComments();
	
			// Find all comments in this file and store them
			XmlObject[] xPathResult = currComments.selectPath("declare namespace s='http://de.tukl.softech.agileReview'; $this//s:comment");
			for (int j=0;j<xPathResult.length;j++)
			{
				Comment c = (Comment)xPathResult[j];
				this.rModel.addComment(c);
			}
		}
	}
	
	/**
	 * Removes the comments of the given review from the database
	 * @param reviewId
	 */
	public void unloadReviewComments(String reviewId)
	{
		// Remove the given review from the models
		for (Comment c: this.rModel.getComments(reviewId))
		{
			this.rModel.removeComment(c.getReviewID(), c.getAuthor(), c.getId());
		}
		// TODO: Erstmal nicht aus dem anderen Model rauslöschen. Dazu muss es cleverer werden
	}
	
	/**
	 * Fills the CommentModel with all found files
	 * @throws XmlException
	 * @throws IOException
	 */
	public void fillDatabaseCompletely() throws XmlException, IOException
	{
		// Clear old values
		this.clearAllModels();
		
		// Fill in new values
		loadAllComment();
		loadAllReviews();
	}
	
	/**
	 * Fills the comment database for all open reviews. 
	 * Therefore all review data are read and based 
	 * on the workspace-specific preferences the open reviews are loaded.
	 * @throws XmlException
	 * @throws IOException
	 */
	public void fillDatabaseForOpenReviews() throws XmlException, IOException
	{
		// Clear old models
		this.clearAllModels();
		
		// Load all reviews
		loadAllReviews();
		
		// Load all comments from open reviews
		for (String currReview: PropertiesManager.getInstance().getOpenReviews())
		{
			if (rModel.containsReview(currReview))
			{
				this.loadReviewComments(currReview);
			}
			else
			{
				// A not existent review is marked as open
				// Ask the user, if he/she wants to delete it	
				if (MessageDialog.openQuestion(null, "Missing Review found", "Review \""+currReview+"\" cannot be found, but is marked as \"open\". Do you want to remove it from the list of open reviews?"))
				{
					// Yes
					PropertiesManager.getInstance().removeFromOpenReviews(currReview);
				}
				// Else do nothing
			}
		}
	}
	
	
	/**
	 * Saves the current xmlBeans objects to files (all in model) TODO: Save on demand
	 * @throws IOException 
	 */
	public void save() throws IOException
	{
		rFileModel.saveAll();
	}
	
	
	/**
	 * Refactors the whole database according to the given parameters and saves it automatically
	 * @param oldPath old path of the refactored item
	 * @param newPath new path of the refactored item
	 * @param type type of the refactored item (see static fields PROJECT, FOLDER, FILE in {@link IResource})
	 * @throws IOException 
	 */
	public void refactorPath(String oldPath, String newPath, int type) throws IOException
	{
		for (CommentsDocument cDoc : this.rFileModel.getAllCommentsDocument())
		{
			// Find old path
			XmlObject oldObject = findXmlPath(cDoc, oldPath, type, false);
			// If not found in document, then no refactoring has to be done
			if (oldObject !=null)
			{
				// create new path
				XmlObject newObject = findXmlPath(cDoc, newPath, type, true);
				// Move children
				XmlCursor c = newObject.newCursor();
				c.toFirstContentToken();
				oldObject.newCursor().moveXmlContents(c);
				// Delete old path
				cleanXmlPath(oldObject);
			}
		}
		this.save();
	}
	
}
