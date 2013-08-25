package de.tukl.cs.softech.agilereview.dataaccess;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.CommentsDocument;
import agileReview.softech.tukl.de.CommentsDocument.Comments;
import agileReview.softech.tukl.de.FileDocument.File;
import agileReview.softech.tukl.de.FilesDocument.Files;
import agileReview.softech.tukl.de.FolderDocument.Folder;
import agileReview.softech.tukl.de.PersonInChargeDocument.PersonInCharge;
import agileReview.softech.tukl.de.ProjectDocument.Project;
import agileReview.softech.tukl.de.ReviewDocument;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.plugincontrol.ExceptionHandler;
import de.tukl.cs.softech.agilereview.plugincontrol.exceptions.NoReviewSourceFolderException;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * Class for accessing the review and comment data (xml and internal model).
 */
public class ReviewAccess {
    
    // //////////////
    // attributes //
    // //////////////
    /**
     * Instance of PropertiesManager
     */
    private static PropertiesManager pm = PropertiesManager.getInstance();
    /**
     * Private instance for Singleton-Pattern
     */
    private static ReviewAccess RA;
    
    /**
     * Reference to the folder where the review and comments xml files are located. This must never be null (after creation of ReviewAccess)
     */
    private static volatile IProject REVIEW_REPO_FOLDER = null;
    
    private static final Object syncObj = new Object();
    
    /**
     * Instance of the comment model
     */
    private final ReviewModel rModel = new ReviewModel();
    
    /**
     * Instance of the review file model
     */
    private final ReviewFileModel rFileModel = new ReviewFileModel();
    
    // //////////////////
    // static methods //
    // //////////////////
    /**
     * Creates a File object which represents the file for storing comments based on the given reviewId/author pair
     * @param reviewId
     * @param author
     * @return File for the given parameter pair
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    private static IFile createCommentFile(String reviewId, String author) throws NoReviewSourceFolderException {
        IFile file = ReviewAccess.createReviewFolder(reviewId).getFile("author_" + author + ".xml");
        if (!file.exists()) {
            try {
                file.create(new ByteArrayInputStream("".getBytes()), IResource.NONE, null);
                while (!file.exists()) {
                };
            } catch (final CoreException e) {
                PluginLogger.logError(ReviewAccess.class.toString(), "createCommentFile", "CoreException while creating comment file", e);
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not create comment file", e
                                .getLocalizedMessage());
                    }
                });
            }
        }
        return file;
    }
    
    /**
     * Creates a File object which represents the folder of the given review
     * @param reviewId
     * @return Folder for this review
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    private static IFolder createReviewFolder(String reviewId) throws NoReviewSourceFolderException {
        if (REVIEW_REPO_FOLDER == null) throw new NoReviewSourceFolderException();
        IFolder folder = REVIEW_REPO_FOLDER.getFolder("review." + reviewId);
        if (!folder.exists()) {
            try {
                folder.create(IResource.NONE, true, null);
                while (!folder.exists()) {
                }
            } catch (final CoreException e) {
                PluginLogger.logError(ReviewAccess.class.toString(), "createReviewFolder", "CoreException while creating review folder", e);
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not create review folder", e
                                .getLocalizedMessage());
                    }
                });
            }
        }
        return folder;
    }
    
    /**
     * Creates a File object which represents the the review-file of the given review
     * @param reviewId
     * @return review file for this review
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    private static IFile createReviewFile(String reviewId) throws NoReviewSourceFolderException {
        IFile file = ReviewAccess.createReviewFolder(reviewId).getFile("review.xml");
        if (!file.exists()) {
            try {
                file.create(new ByteArrayInputStream("".getBytes()), IResource.NONE, null);
                while (!file.exists()) {
                }
            } catch (final CoreException e) {
                PluginLogger.logError(ReviewAccess.class.toString(), "createReviewFile", "CoreException while creating review file", e);
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not create review file", e
                                .getLocalizedMessage());
                    }
                });
            }
        }
        return file;
    }
    
    /**
     * Creates a Project as child of the given XmlObject (if possible)
     * @param parent parent XmlObject (should be Files)
     * @param name name of the file
     * @return The newly created project or null if the given parent does not support project children
     */
    private static Project createXmlProject(XmlObject parent, String name) {
        Project p = null;
        if (parent instanceof Files) {
            p = ((Files) parent).addNewProject();
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
            f = ((Project) parent).addNewFolder();
            f.setName(name);
        } else {
            f = ((Folder) parent).addNewFolder();
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
            f = ((Project) parent).addNewFile();
            f.setName(name);
        } else {
            f = ((Folder) parent).addNewFile();
            f.setName(name);
        }
        return f;
    }
    
    /**
     * Computes the path for a given comment, file or folder
     * @param item comment for which the path should be returned
     * @return path of the item (excluding the item itself)
     */
    public static String computePath(XmlObject item) {
        XmlCursor c = item.newCursor();
        c.toParent();
        String path = c.getAttributeText(new QName("name"));
        
        while (c.toParent() && !(c.getObject() instanceof Files)) {
            path = c.getAttributeText(new QName("name")) + System.getProperty("file.separator") + path;
        }
        c.dispose();
        
        return path;
    }
    
    /**
     * Creates and opens the given AgileReview Source Project (if not existent or closed)
     * @param projectName project name
     * @return <i>true</i> if everything worked,<i>false</i> if something went wrong
     */
    public static boolean createAndOpenReviewProject(String projectName) {
        boolean result = true;
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject p = workspaceRoot.getProject(projectName);
        
        try {
            // Create a new Project, if necessary
            if (!p.exists()) {
                p.create(null);// TODO: Use ProgressMonitor
                while (!p.exists()) {
                }
            }
            
            // Open the Project, if necessary
            if (!p.isOpen()) {
                p.open(null);// TODO: Use ProgressMonitor
            }
            while (!p.isOpen()) {
            }
            
            // Set project description
            setProjectNatures(p, new String[] { PropertiesManager.getInstance().getInternalProperty(
                    PropertiesManager.INTERNAL_KEYS.AGILEREVIEW_NATURE) });
        } catch (final CoreException e) {
            PluginLogger.logError(ReviewAccess.class.toString(), "createReviewProject", "CoreException in ReviewAccess constructor", e);
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(),
                            "AgileReview: Could not create or open AgileReview source project", e.getLocalizedMessage());
                }
            });
            result = false;
        }
        return result;
    }
    
    /**
     * Sets the given project natures to those specified by those in "natures".
     * @param p The project.
     * @param natures The natures.
     * @return <code>true</code> if everything worked, <code>false</code> otherwise
     */
    static boolean setProjectNatures(IProject p, String[] natures) {
        try {
            IProjectDescription projectDesc = p.getDescription();
            projectDesc.setNatureIds(natures);
            p.setDescription(projectDesc, null);// TODO: Use ProgressMonitor
            return true;
        } catch (final CoreException e) {
            PluginLogger.logError(ReviewAccess.class.toString(), "createReviewProject", "CoreException while setting project natures", e);
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not set project natures", e
                            .getLocalizedMessage());
                }
            });
            return false;
        }
    }
    
    /**
     * Singleton Pattern
     * @return Instance of this class
     */
    public static ReviewAccess getInstance() {
        synchronized (syncObj) {
            if (RA == null) {
                RA = new ReviewAccess();
            }
            return RA;
        }
    }
    
    // //////////////////////////////
    // default non-static methods //
    // //////////////////////////////
    
    /**
     * Returns the current active source folder
     * @return the current active source folder
     */
    IProject getCurrentSourceFolder() {
        return REVIEW_REPO_FOLDER;
    }
    
    // //////////////////////////////
    // private non-static methods //
    // //////////////////////////////
    /**
     * Constructor: Sets the directory where to look for the xml-Files
     */
    private ReviewAccess() {
        PluginLogger.log(this.getClass().toString(), "constructor", "ReviewAccess created");
        // Set the directory where the comments are located
        String projectName = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER);
        if (!loadReviewSourceProject(projectName)) {
            // Shell currShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
            // String msg = "AgileReview is either started for the first time or you deleted your 'AgileReview Source Folder'.\n" +
            // "Please set an 'AgileReview Source Folder' for AgileReview to work properly.";
            // MessageDialog.openInformation(currShell, "AgileReview Initialization", msg);
            ExceptionHandler.handleNoReviewSourceFolderExceptionOnStartUp();
        }
    }
    
    /**
     * Clears all used models
     */
    private void clearAllModels() {
        this.rFileModel.clearModel();
        this.rModel.clearModel();
        System.gc();
    }
    
    /**
     * Removes the "active" nature from the current AgileReview Source Project
     * @return the old project, which was unloaded
     */
    IProject unloadCurrentReviewSourceProject() {
        IProject oldProject = REVIEW_REPO_FOLDER;
        if (REVIEW_REPO_FOLDER != null) {
            // if(REVIEW_REPO_FOLDER.exists() && REVIEW_REPO_FOLDER.isOpen()) {
            // setProjectNatures(REVIEW_REPO_FOLDER, new String[]
            // {PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.AGILEREVIEW_NATURE)});
            // // update decorator
            // Display.getDefault().asyncExec(new Runnable() {
            // @Override
            // public void run() {
            // while(PlatformUI.getWorkbench() == null) {}
            // PlatformUI.getWorkbench().getDecoratorManager().update("de.tukl.cs.softech.agilereview.active_decorator");
            // }
            // });
            REVIEW_REPO_FOLDER = null;
            // }
        }
        return oldProject;
    }
    
    /**
     * Loads the given project as AgileReview source project
     * @param projectName project name
     * @return <i>true</i> if everything works, <i>false</i> otherwise (e.g. when the project does not exist)
     */
    public boolean loadReviewSourceProject(String projectName) {
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject p = workspaceRoot.getProject(projectName);
        if (!p.exists() || !p.isOpen()) {
            return false;
        } else {
            // remove active nature from old project
            unloadCurrentReviewSourceProject();
            
            // set new project
            REVIEW_REPO_FOLDER = p;
            PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, p.getName());
            // add active nature to new project
            setProjectNatures(p, new String[] {
                    PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.AGILEREVIEW_NATURE),
                    PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.ACTIVE_AGILEREVIEW_NATURE) });
            // update decorator
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    while (PlatformUI.getWorkbench() == null) {
                    }
                    PlatformUI.getWorkbench().getDecoratorManager().update("de.tukl.cs.softech.agilereview.active_decorator");
                }
            });
            
            // Load open reviews initially
            try {
                fillDatabaseForOpenReviews();
            } catch (NoReviewSourceFolderException e) {
                // does not occur as REVIEW_REPO_FOLDER is set beforehand
            }
            
            return true;
        }
    }
    
    /**
     * Fills the comment model
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    private void loadAllComment() throws NoReviewSourceFolderException {
        if (REVIEW_REPO_FOLDER == null) throw new NoReviewSourceFolderException();
        PluginLogger.log(this.getClass().toString(), "loadAllComments",
                "All comment files will be loaded from file (including closed reviews). Exception thrown when parsing xml-file");
        // Get all relevant folders in the review repository
        try {
            IResource[] allFolders = REVIEW_REPO_FOLDER.members();
            // Iterate all folders
            LinkedList<IResource> errorFiles = new LinkedList<IResource>();
            for (IResource currFolder : allFolders) {
                if (currFolder instanceof IFolder) {
                    IResource[] allFiles = ((IFolder) currFolder).members();
                    // Iterate all files in the current folder
                    for (IResource currFile : allFiles) {
                        if (currFile instanceof IFile) {
                            // Open file and read basic information
                            if (!((IFile) currFile).getName().equals("review.xml")) {
                                try {
                                    InputStream stream = ((IFile) currFile).getContents();
                                    CommentsDocument doc = CommentsDocument.Factory.parse(stream);
                                    stream.close();
                                    this.rFileModel.addXmlDocument(doc, (IFile) currFile);
                                    readCommentsDocument(doc);
                                } catch (final CoreException e) {
                                    PluginLogger.logError(ReviewAccess.class.toString(), "loadAllComment", "CoreException while loading comments", e);
                                    Display.getDefault().syncExec(new Runnable() {
                                        @Override
                                        public void run() {
                                            MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not open files", e
                                                    .getLocalizedMessage());
                                        }
                                    });
                                } catch (Exception e) {
                                    PluginLogger.logError(ReviewAccess.class.toString(), "loadAllComment", "Could not load file " + currFile, e);
                                    errorFiles.add(currFile);
                                }
                            }
                        }
                    }
                }
            }
            // Show errors to user
            if (!errorFiles.isEmpty()) {
                String message = "AgileReview could not load the following files:\n\n";
                for (IResource file : errorFiles) {
                    message += file.getLocation().toOSString() + "\n";
                }
                message += "\nThese files may be corrupted (i.e. empty). Please check them.";
                final String finalMessage = message;
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not load files", finalMessage);
                    }
                });
            }
        } catch (final CoreException e) {
            PluginLogger.logError(ReviewAccess.class.toString(), "loadAllComment", "CoreException while filling comment model", e);
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not open files", e.getLocalizedMessage());
                }
            });
        }
    }
    
    /**
     * Fills the review model
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    private void loadAllReviews() throws NoReviewSourceFolderException {
        if (REVIEW_REPO_FOLDER == null) throw new NoReviewSourceFolderException();
        PluginLogger.log(this.getClass().toString(), "loadAllReviews", "All reviews will be loaded from files");
        // Get all relevant folders in the review repository
        try {
            IResource[] allFolders = REVIEW_REPO_FOLDER.members();
            // Iterate all folders
            LinkedList<IResource> errorFiles = new LinkedList<IResource>();
            for (IResource currFolder : allFolders) {
                if (currFolder instanceof IFolder) {
                    // Get all relevant files in the review repository (only review files(no "." in author names allowed))
                    IResource[] allFiles = ((IFolder) currFolder).members();
                    // Fill internal database
                    // Iterate all review-files in directory (should only be one) // TODO: performance enhancement: take review file directly
                    for (int i = 0; i < allFiles.length; i++) {
                        if (allFiles[i] instanceof IFile && ((IFile) allFiles[i]).getName().equals("review.xml")) {
                            try {
                                // Open file and store review
                                InputStream stream = ((IFile) allFiles[i]).getContents();
                                ReviewDocument doc = ReviewDocument.Factory.parse(stream);
                                stream.close();
                                this.rFileModel.addXmlDocument(doc, (IFile) allFiles[i]);
                                rModel.addReview(doc.getReview());
                            } catch (final CoreException e) {
                                PluginLogger.logError(ReviewAccess.class.toString(), "loadAllReviews", "CoreException while filling review model", e);
                                Display.getDefault().syncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not open files", e
                                                .getLocalizedMessage());
                                    }
                                });
                            } catch (Exception e) {
                                PluginLogger.logError(ReviewAccess.class.toString(), "loadAllReviews", "Could not load file " + allFiles[i], e);
                                errorFiles.add(allFiles[i]);
                            }
                        }
                    }
                }
            }
            // Show errors to user
            if (!errorFiles.isEmpty()) {
                String message = "AgileReview could not load the following review files:\n\n";
                for (IResource file : errorFiles) {
                    message += file.getLocation().toOSString() + "\n";
                }
                message += "\nThese files may be corrupted (i.e. empty). Please check them.\nComments of a review cannot be loaded without working review file.";
                MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not load files", message);
            }
        } catch (final CoreException e) {
            PluginLogger.logError(ReviewAccess.class.toString(), "loadAllReviews", "CoreException while filling review model", e);
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not open files", e.getLocalizedMessage());
                }
            });
        }
    }
    
    /**
     * Loads all comments of the given document into the database
     * @param doc document to read
     */
    private void readCommentsDocument(CommentsDocument doc) {
        Comments currComments = doc.getComments();
        
        // Find all comments in this file and store them
        XmlObject[] xPathResult = currComments.selectPath("declare namespace s='http://de.tukl.softech.agileReview'; $this//s:comment");
        for (int j = 0; j < xPathResult.length; j++) {
            Comment c = (Comment) xPathResult[j];
            this.rModel.addComment(c);
        }
    }
    
    /**
     * Finds the given path in the given CommentsDocument, whereas the last element is of the given type. In case of createPath being true, the path
     * is created if not there.
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
            String xPathQuery = "declare namespace s='http://de.tukl.softech.agileReview'; $this/s:*[@name=\"" + pathArray[i] + "\"]";
            XmlObject[] xPathResult = currObject.selectPath(xPathQuery);
            // if no result is found, the corresponding object has to be created
            if (xPathResult.length == 0) {
                if (!createPath) {
                    return null;
                } else if (i == 0) {
                    // First element: Create a project
                    currObject = createXmlProject(currObject, pathArray[i]);
                } else if (i == pathArray.length - 1) {
                    // Last element: Create the given type
                    switch (type) {
                    case IResource.PROJECT:
                        currObject = createXmlProject(currObject, pathArray[pathArray.length - 1]);
                        break;
                    case IResource.FOLDER:
                        currObject = createXmlFolder(currObject, pathArray[pathArray.length - 1]);
                        break;
                    case IResource.FILE:
                        currObject = createXmlFile(currObject, pathArray[pathArray.length - 1]);
                        break;
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
    
    // /////////////////////////////
    // public non-static methods //
    // /////////////////////////////
    
    // /////////////////////////
    // comment functionality //
    // /////////////////////////
    
    /**
     * States, whether the ReviewAccess has a valid Source Project at the moment
     * @return <code>true</code> if the current Source Folder is valid, <code>false</code> otherwise
     */
    public boolean isCurrentSourceValid() {
        boolean result = REVIEW_REPO_FOLDER != null;
        if (result) {
            result &= REVIEW_REPO_FOLDER.exists() && REVIEW_REPO_FOLDER.isOpen();
        }
        return result;
    }
    
    /**
     * Creates a new empty Comment at the right position in the xml, which is returned
     * @param reviewId Review in which the comment is placed
     * @param author author of the comment
     * @param path file path of the commented file
     * @return empty comment
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    public Comment createNewComment(String reviewId, String author, String path) throws NoReviewSourceFolderException {
        PluginLogger.log(this.getClass().toString(), "createNewComment", "Comment created for:\n reviewId: " + reviewId + " \n author: " + author
                + " \n path: " + path);
        // Check if file for this author in this review does already exist (assumption: database and file system are synch)
        IFile commentFile = ReviewAccess.createCommentFile(reviewId, author);
        // Check if file for this author does already exist
        if (!this.rFileModel.containsFile(commentFile)) {
            // No file exists. So we have to create one
            CommentsDocument commentsDoc = CommentsDocument.Factory.newInstance();
            Comments comments = commentsDoc.addNewComments();
            comments.addNewAuthor().setName(author);
            comments.addNewFiles();
            // And put it into the database (for being saved to file later)
            this.rFileModel.addXmlDocument(commentsDoc, commentFile);
        }
        
        // Now get the right document and find the right place in it
        CommentsDocument currCommentsDoc = this.rFileModel.getCommentsDoc(commentFile);
        File currFile = (File) findXmlPath(currCommentsDoc, path, IResource.FILE, true);
        
        // Prepare new Comment
        Comment result = currFile.addNewComment();
        // Find the next id
        Integer newKey = this.rModel.getNextCommentIdFor(reviewId, author);
        result.setId("c" + newKey);
        
        // Fill attributes
        result.setAuthor(author);
        result.setReviewID(reviewId);
        Calendar currCal = Calendar.getInstance();
        result.setCreationDate(currCal);
        result.setLastModified(currCal);
        result.setPriority(0);
        result.setRecipient("");
        result.setStatus(0);
        result.setRevision(0);
        
        // Fill children
        result.setText("");
        // Reference will be set when saved
        result.addNewReplies();
        
        // Store comment in database
        this.rModel.addComment(result);
        
        // Return the new empty comment
        return result;
    }
    
    /**
     * Deletes the specified comment of the specified author in the specified review.
     * @param reviewId review id of the comment to be deleted
     * @param author author of the comment to be deleted
     * @param commentId comment id of the comment to be deleted
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    public void deleteComment(String reviewId, String author, String commentId) throws NoReviewSourceFolderException {
        PluginLogger.log(this.getClass().toString(), "deleteComment", "Following comment deleted:\n reviewId: " + reviewId + " \n author: " + author
                + " \n commentId: " + commentId);
        // Find comment in database
        Comment delCom = this.rModel.getComment(reviewId, author, commentId);
        
        // Remove xml nodes
        cleanXmlPath(delCom);
        IFile changedFile = ReviewAccess.createCommentFile(reviewId, author);
        
        // Remove from database and eventually from file system
        if (this.rModel.removeComment(reviewId, author, commentId)) {
            // Last comment of this author in this review has been deleted
            // -> Remove from file system
            this.rFileModel.removeXmlDocument(changedFile);
        } else {
            // There are still comments in this file --> save the changes
            try {
                this.rFileModel.save(changedFile);
            } catch (IOException e) {
                PluginLogger.logError(this.getClass().toString(), "deleteComment", "IOException occured while deleting comment: " + reviewId + "|"
                        + author + "|" + commentId, e);
            }
        }
    }
    
    /**
     * @see ReviewAccess#deleteComment(String, String, String)
     * @param comment
     * @throws IOException
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    public void deleteComment(Comment comment) throws IOException, NoReviewSourceFolderException {
        deleteComment(comment.getReviewID(), comment.getAuthor(), comment.getId());
    }
    
    /**
     * @see ReviewAccess#deleteComment(String, String, String)
     * @param comments
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    public void deleteComments(Collection<Comment> comments) throws NoReviewSourceFolderException {
        for (Comment c : comments) {
            deleteComment(c.getReviewID(), c.getAuthor(), c.getId());
        }
    }
    
    /**
     * Returns all Comments, or an empty List if no comments are found
     * @return all Comments
     */
    public ArrayList<Comment> getAllComments() {
        return this.rModel.getAllComments();
    }
    
    /**
     * Returns all comments of the given review
     * @param reviewId
     * @return all comments of given review
     */
    public ArrayList<Comment> getComments(String reviewId) {
        return this.rModel.getComments(reviewId);
    }
    
    /**
     * Return all comments of this review, which are associated with a file in the given folder or one of its sub-folders
     * @param reviewId
     * @param path
     * @return Comments associated with a file in the given folder or one of its sub-folders
     */
    public ArrayList<Comment> getComments(String reviewId, String path) {
        return this.rModel.getComments(reviewId, path);
    }
    
    /**
     * Returns the comment specified by the given tupel
     * @param reviewId
     * @param author
     * @param commentId
     * @return comment specified by given tupel
     */
    public Comment getComment(String reviewId, String author, String commentId) {
        return this.rModel.getComment(reviewId, author, commentId);
    }
    
    // ////////////////////////
    // review functionality //
    // ////////////////////////
    
    /**
     * States whether the given reviewId belongs to a loaded review in the model
     * @param reviewId review id in question
     * @return <i>true</i> if review is in model, <i>false</i> otherwise
     */
    public boolean isReviewLoaded(String reviewId) {
        return this.rModel.containsReview(reviewId, true);
    }
    
    /**
     * Checks whether the given reviewId already exists
     * @param reviewId
     * @return true, if reviewId already exists, false otherwise
     */
    public boolean reviewExists(String reviewId) {
        return this.rModel.containsReview(reviewId, false);
    }
    
    /**
     * Returns all reviews
     * @return all reviews
     */
    public ArrayList<Review> getAllReviews() {
        return rModel.getAllReviews();
    }
    
    /**
     * Return all project elements which are commented in the given review. A project can be returned multiple times, as there might be many project
     * elements for the same project (one per author).
     * @param reviewId
     * @return List of all projects of the given review
     */
    public HashSet<Project> getProjects(String reviewId) {
        PluginLogger.log(this.getClass().toString(), "getProjects", "all projects of review \"" + reviewId + "\" requested");
        HashSet<Project> result = new HashSet<Project>();
        
        // Iterate all comments of this review
        for (Comment c : rModel.getComments(reviewId)) {
            // Get the corresponding <project> node
            XmlObject[] xPathResult = c.selectPath("declare namespace s='http://de.tukl.softech.agileReview'; $this/ancestor::s:project");
            
            // Every comment has exactly one ancestor of type project
            Project currProject = (Project) xPathResult[0];
            result.add(currProject);
        }
        
        return result;
    }
    
    /**
     * Creates a new Review with the given Id and returns it to be filled
     * @param reviewId
     * @return empty Review (except for review id) or null if reviewid is already in use
     * @throws IOException
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    public Review createNewReview(String reviewId) throws IOException, NoReviewSourceFolderException {
        PluginLogger.log(this.getClass().toString(), "createNewReview", "Create new review: " + reviewId);
        
        // Create the folder and file for this review
        IFile revFile = ReviewAccess.createReviewFile(reviewId);
        
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
        if (!rModel.addReview(result)) {
            return null;
        }
        this.rModel.createModelEntry(reviewId);
        
        // save new review file
        this.rFileModel.addXmlDocument(revDoc, revFile);
        this.rFileModel.save(revFile);
        
        return result;
    }
    
    /**
     * Deletes a review and all comments specified by this review
     * @param reviewId
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    public void deleteReview(String reviewId) throws NoReviewSourceFolderException {
        PluginLogger.log(this.getClass().toString(), "deleteReview", "Delete review: " + reviewId);
        IFile delFile = ReviewAccess.createReviewFile(reviewId);
        
        // Delete review from Model
        this.rModel.removeReview(reviewId, true);
        this.rFileModel.removeXmlDocument(delFile);
    }
    
    // /////////////////////
    // I/O functionality //
    // /////////////////////
    /**
     * Load all comments of the given review into the database
     * @param reviewId
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    public void loadReviewComments(String reviewId) throws NoReviewSourceFolderException {
        PluginLogger.log(this.getClass().toString(), "loadReviewComments", "Load comments of review: " + reviewId);
        IFolder currFolder = ReviewAccess.createReviewFolder(reviewId);
        
        try {
            IResource[] allFiles = currFolder.members();
            
            this.rModel.createModelEntry(reviewId);
            LinkedList<IResource> errorFiles = new LinkedList<IResource>();
            // Iterate all files in the current folder
            for (IResource currFile : allFiles) {
                if (currFile instanceof IFile) {
                    if (!((IFile) currFile).getName().equals("review.xml")) {
                        // Open file and read basic information
                        try {
                            InputStream stream = ((IFile) currFile).getContents();
                            CommentsDocument doc = CommentsDocument.Factory.parse(stream);
                            stream.close();
                            this.rFileModel.addXmlDocument(doc, (IFile) currFile);
                            readCommentsDocument(doc);
                        } catch (final CoreException e) {
                            PluginLogger.logError(ReviewAccess.class.toString(), "loadReviewComments",
                                    "CoreException while loading comments from file " + currFile, e);
                            Display.getDefault().syncExec(new Runnable() {
                                @Override
                                public void run() {
                                    MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not open files", e
                                            .getLocalizedMessage());
                                }
                            });
                        } catch (Exception e) {
                            PluginLogger.logError(ReviewAccess.class.toString(), "loadReviewComments", "Could not load file " + currFile, e);
                            errorFiles.add(currFile);
                        }
                    }
                }
            }
            // Show errors to user
            if (!errorFiles.isEmpty()) {
                String message = "AgileReview could not load the following files:\n\n";
                for (IResource file : errorFiles) {
                    message += file.getLocation().toOSString() + "\n";
                }
                message += "\nThese files may be corrupted (i.e. empty). Please check them.";
                MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not load files", message);
            }
        } catch (final CoreException e) {
            PluginLogger.logError(ReviewAccess.class.toString(), "loadReviewComments", "CoreException while loading comments of review " + reviewId
                    + " into database", e);
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not open files", e.getLocalizedMessage());
                }
            });
        }
    }
    
    /**
     * Removes the comments of the given review from the database
     * @param reviewId
     */
    public void unloadReviewComments(String reviewId) {
        PluginLogger.log(this.getClass().toString(), "unloadReviewComments", "Unload comments of review: " + reviewId);
        // Remove the given review from the models
        this.rModel.removeReview(reviewId, false);
        // TODO: Erstmal nicht aus dem anderen Model rauslöschen. Dazu muss es cleverer werden
    }
    
    /**
     * Fills the CommentModel with all found files
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    public void fillDatabaseCompletely() throws NoReviewSourceFolderException {
        PluginLogger.log(this.getClass().toString(), "fillDatabaseCompletely",
                "Clear all models and reload everything from file (including closed reviews)");
        // Clear old values
        this.clearAllModels();
        
        // Fill in new values
        loadAllReviews();
        loadAllComment();
        
    }
    
    /**
     * Fills the comment database for all open reviews. Therefore all review data are read and based on the workspace-specific preferences the open
     * reviews are loaded.
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    public void fillDatabaseForOpenReviews() throws NoReviewSourceFolderException {
        // Clear old models
        this.clearAllModels();
        
        // Load all reviews
        loadAllReviews();
        
        // Load all comments from open reviews
        boolean activeReviewFound = false;
        String activeReview = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
        for (String currReview : pm.getOpenReviews()) {
            if (rModel.containsReview(currReview, false)) {
                this.loadReviewComments(currReview);
                // Test for active review
                activeReviewFound = activeReviewFound || currReview.equals(activeReview);
            } else {
                // Just remove open, but not existent reviews
                pm.removeFromOpenReviews(currReview);
            }
        }
        if (!activeReviewFound) {
            PropertiesManager.getPreferences().setToDefault(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
        }
    }
    
    /**
     * Tells the ReviewAccess to get the current ReviewSourceProject (which should have changed) and reload the comments
     * @return true, if something has changed, false otherwise
     */
    public boolean updateReviewSourceProject() {
        boolean result = false;
        String strPropManReviewSourceName = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER);
        if (REVIEW_REPO_FOLDER == null || !REVIEW_REPO_FOLDER.getName().equals(strPropManReviewSourceName)) {
            loadReviewSourceProject(strPropManReviewSourceName);
            result = true;
        }
        return result;
    }
    
    /**
     * Saves the current xmlBeans objects to files (all in model)
     * @param obj The object which changed (to determine which file has to be saved). Has to be a comment or a review
     * @throws NoReviewSourceFolderException will be thrown if no review source folder had been defined beforehand
     */
    public void save(XmlObject obj) throws NoReviewSourceFolderException {
        // Determine the file of this comment
        IFile file2save = null;
        if (obj instanceof Comment) {
            file2save = createCommentFile(((Comment) obj).getReviewID(), ((Comment) obj).getAuthor());
        } else if (obj instanceof Review) {
            file2save = createReviewFile(((Review) obj).getId());
        }
        try {
            if (file2save != null) {
                PluginLogger.log(this.getClass().toString(), "save", "Save file '" + file2save.getName() + "' in order to save comment " + obj);
                rFileModel.save(file2save);
            } else {
                PluginLogger.logError(this.getClass().toString(), "save", obj + " could not be saved, as it is neither a comment nor a review");
            }
        } catch (IOException e) {
            PluginLogger.logError(this.getClass().toString(), "save", "IOException occured while trying to save to file " + file2save, e);
        }
        
    }
    
}
