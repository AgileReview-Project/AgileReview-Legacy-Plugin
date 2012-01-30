package de.tukl.cs.softech.agilereview.dataaccess;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;
import org.apache.xmlbeans.XmlTokenSource;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.swt.widgets.Display;

import agileReview.softech.tukl.de.CommentsDocument;
import agileReview.softech.tukl.de.CommentsDocument.Comments;
import agileReview.softech.tukl.de.FileDocument.File;
import agileReview.softech.tukl.de.FilesDocument.Files;
import agileReview.softech.tukl.de.FolderDocument.Folder;
import agileReview.softech.tukl.de.ProjectDocument.Project;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * Class for accessing the review and comment data (xml and internal model).  
 */
public class RefactoringAccess {
	
	/**
	 * Local instance of the ReviewAccess. 
	 */
	private ReviewAccess ra = ReviewAccess.getInstance();
	/**
	 * Instance of the review file model
	 */
	private ReviewFileModel rFileModel = new ReviewFileModel();
	/**
	 * String representations of documents before they are refactored. 
	 */
	private HashMap<IFile, String> prevDocuments = new HashMap<IFile, String>();
	/**
	 * String representations of documents after they were refactored.
	 */
	private HashMap<IFile, String> postDocuments = new HashMap<IFile, String>();
	/**
	 * List of files that are affected by the refactoring. 
	 */
	private Collection<IFile> affectedFilesBuffer = new HashSet<IFile>();
	/**
	 * List of files that could not be parsed 
	 */
	private HashMap<IFile, Exception> failedFiles = new HashMap<IFile, Exception>();
	
	/**
	 * Constructor of the RefactoringAccess. Initially loads all comments from the database.
	 */
	public RefactoringAccess() {
		loadAllComments();
	}
	
	/**
	 * Returns a list of files that have to be refactored.
	 * @param refactoringTarget the resource that is to be refactored
	 * @param type type of the refactored item (see static fields PROJECT, FOLDER, FILE in {@link IResource})
	 * @return a list of files that have to be refactored.
	 */
	public Collection<IFile> getAffectedFiles(IResource refactoringTarget, int type) {
		
		Collection<IFile> affectedFiles = new HashSet<IFile>();
		for (IFile f : rFileModel.getAllCommentFiles()) {
			if(findXmlPath(rFileModel.getCommentsDoc(f), refactoringTarget.getFullPath().toOSString(), type, false) != null && !affectedFiles.contains(f)) {
				affectedFiles.add(f);
			}
		}
		
		affectedFilesBuffer.addAll(affectedFiles);
		return affectedFilesBuffer;
	}
	
	/**
	 * Returns a String representation of documents before they were refactored.
	 * @return String representation of documents before they were refactored.
	 */
	public HashMap<IFile, String> getPrevDocuments() {
		return prevDocuments;
	}

	/**
	 * Returns a string representation of all documents after they were refactored
	 * @param oldPath old path of the refactored item
	 * @param newPath new path of the refactored item
	 * @param type type of the refactored item (see static fields PROJECT, FOLDER, FILE in {@link IResource})
	 * @param moveAllChilds indicates whether all children of oldPath should be moved to newPath
	 * @return String representation of documents after they were refactored
	 * @throws IOException
	 * @throws XmlException
	 */
	public HashMap<IFile, String> getPostDocumentsOfRefactoring(String oldPath, String newPath, int type, boolean moveAllChilds) throws IOException, XmlException {
		simulateRefactoring(oldPath, newPath, type, moveAllChilds);		
		return postDocuments;
	}

	/**
	 * Refactors the whole database according to the given parameters and saves it automatically
	 * @param oldPath old path of the refactored item
	 * @param newPath new path of the refactored item
	 * @param type type of the refactored item (see static fields PROJECT, FOLDER, FILE in {@link IResource})
	 * @param moveAllChilds indicates whether all children of oldPath should be moved to newPath
	 * @throws IOException 
	 */
	private void simulateRefactoring(String oldPath, String newPath, int type, boolean moveAllChilds) throws IOException {
		
		// do the refactoring on the internal structure
		//XXX assumption: getAffectedFiles was called beforehand
		for(IFile f : affectedFilesBuffer) {
			// Find old path
			XmlObject oldObject = findXmlPath(rFileModel.getCommentsDoc(f), oldPath, type, false);
			// If not found in document, then no refactoring has to be done
			if (oldObject != null) {
				// Select all items to move
				String xPath = "declare namespace s='http://de.tukl.softech.agileReview'; ";
				if (moveAllChilds) {
					xPath += "$this/*";
				} else {
					xPath += "$this/s:comment | $this/s:file";
				}
				
				XmlObject[] xPathResultCopy = oldObject.copy().selectPath(xPath);
				// now that we have a copy, remove the old originals directly
				XmlObject[] xPathResult = oldObject.selectPath(xPath);
				for (int i=0; i<xPathResult.length;i++) {
					cleanXmlPath(xPathResult[i]);
				}
				
				// create new path
				XmlObject newObject = findXmlPath(rFileModel.getCommentsDoc(f), newPath, type, true);
				// create cursor and point to the place where content will be placed (inside of new node)
				XmlCursor newC = newObject.newCursor();
				if (!newC.toFirstChild()) {
					newC.toEndToken();
				}
				
				boolean newIsEmpty = true;
				
				// move all children of old node to new node
				for (int i=0; i<xPathResultCopy.length;i++) {
					// copy object to new location
					XmlCursor x = xPathResultCopy[i].newCursor();
					x.copyXml(newC);
					x.dispose();
					// new node is no longer empty
					newIsEmpty = false;
				}
				
				// Clean up
				newC.dispose();
				if (newIsEmpty) {
					cleanXmlPath(newObject);
				}
				 
			}
			saveToString(rFileModel.getCommentsDoc(f), f, false);
		}
	}
	
	/**
	 * Saving method for a given XML document / File pair 
	 * @param document
	 * @param file
	 * @param pre should be set to true, if the file document is the original before the refactoring step
	 * @throws IOException
	 */
	private void saveToString(XmlTokenSource document, IFile file, boolean pre) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		document.save(baos, new XmlOptions().setSavePrettyPrint());
		if(pre) {
			prevDocuments.put(file, baos.toString().replaceAll("\r\n|\r|\n", System.getProperty("line.separator")));
		} else {
			postDocuments.put(file, baos.toString().replaceAll("\r\n|\r|\n", System.getProperty("line.separator")));
		}
	}
	
	///////////////////////////////////////////
	///// Original RA functionality ///////////
	///////////////////////////////////////////
	
	/**
	 * Finds the given path in the given CommentsDocument, whereas the last element is of the given type.
	 * In case of createPath being true, the path is created if not there.
	 * @param currCommentsDoc CommentsDocument where the path should be created
	 * @param path path that should be found/created
	 * @param type type of the last element of the path
	 * @param createPath if true, path is created (if necessary)
	 * @return element specified by path or null if the path was not found and createPath is false
	 */
	private XmlObject findXmlPath(CommentsDocument currCommentsDoc, String path, int type, boolean createPath) {
		Comments currComments = currCommentsDoc.getComments();
		XmlObject currObject = currComments.getFiles();
		
		if (path.startsWith(System.getProperty("file.separator"))) {
			path = path.replaceFirst(Pattern.quote(System.getProperty("file.separator")), "");
		}
		
		// Iterate path and stop one position before end
		String[] pathArray = path.split(Pattern.quote(System.getProperty("file.separator")));
		for (int i = 0; i < pathArray.length; i++) {
			String xPathQuery = "declare namespace s='http://de.tukl.softech.agileReview'; $this/s:*[@name=\""+pathArray[i]+"\"]";
			XmlObject[] xPathResult = currObject.selectPath(xPathQuery);
			
			// if no result is found, the corresponding object has to be created
			if (xPathResult.length == 0) {
				
				if (!createPath) {
					return null;
				} else if (i == 0) {
					// First element: Create a project
					currObject = createXmlProject(currObject, pathArray[i]);
				} else if (i == pathArray.length-1) {
					// Last element: Create the given type
					switch (type) {
						case IResource.PROJECT: currObject = createXmlProject(currObject, pathArray[i]); break;
						case IResource.FOLDER:	currObject = createXmlFolder(currObject, pathArray[i]); break;
						case IResource.FILE:	currObject = createXmlFile(currObject,  pathArray[i]); break;
					}
				} else {
					// As we do neither consider the last element nor the first, we always create a folder
					currObject = createXmlFolder(currObject, pathArray[i]);
				}
			} else {
				// Only one result should be found then
				currObject = xPathResult[0];
			}
		}
		return currObject;
	}
	
	/**
	 * Fills the comment model
	 */
	private void loadAllComments() {
		// Get all relevant folders in the review repository
		try {
			if (!PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ASK_FOR_REVIEW_FOLDER).equals(MessageDialogWithToggle.ALWAYS)) {/*?|r108|Peter Reuter|c1|?*/
				IResource[] allFolders = ra.getCurrentSourceFolder().members();
				// Iterate all folders
				for (IResource currFolder : allFolders) {
					if (currFolder instanceof IFolder) {
						IResource[] allFiles = ((IFolder)currFolder).members();
						// Iterate all files in the current folder
						for (IResource currFile : allFiles) {
							if (currFile instanceof IFile) {
								// Open file and read basic information
								if (!((IFile)currFile).getName().equals("review.xml")) {
									try {
										CommentsDocument doc = CommentsDocument.Factory.parse(((IFile)currFile).getContents());
										rFileModel.addXmlDocument(doc, (IFile)currFile);
										saveToString(doc, (IFile)currFile, true);
									} catch (Exception e) {
										// catch all exceptions as they might influence the refactoring process
										failedFiles.put((IFile) currFile, e);
									} 
								}
							}
						}
					}
				}	
			}
		} catch (CoreException e) {
			PluginLogger.logError(ReviewAccess.class.toString(), "loadAllComment", "CoreException while filling comment model", e);
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(Display.getDefault().getActiveShell(), "CoreException", "An error occured while reading the files of the AgileReview Source Folder in order to do the refactoring!");
				}
				
			});
		}
	}
	
	/**
	 * Creates a Project as child of the given XmlObject (if possible)
	 * @param parent parent XmlObject (should be Files)
	 * @param name name of the file
	 * @return The newly created project or null if the given parent does not support project children
	 */
	private static Project createXmlProject (XmlObject parent, String name) {
		Project p = null;
		if (parent instanceof Files) {
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
	private static Folder createXmlFolder(XmlObject parent, String name) {
		Folder f = null;
		if (parent instanceof Project) {
			f = ((Project)parent).addNewFolder();
			f.setName(name);
		} else {
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
	private static File createXmlFile(XmlObject parent, String name) {
		agileReview.softech.tukl.de.FileDocument.File f = null;
		if (parent instanceof Project) {
			f = ((Project)parent).addNewFile();
			f.setName(name);
		} else {
			f = ((Folder)parent).addNewFile();
			f.setName(name);
		}
		return f;
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
		while (c.toParent() && !c.toFirstChild() && !(c.getObject() instanceof Files)) {
			c.removeXml();
		}	
		c.dispose();
	}
	
	/**
	 * Returns all files that could not be parsed
	 * @return collection of files that could not be parsed
	 */
	public HashMap<IFile, Exception> getFailedFiles() {
		return this.failedFiles;
	}
}