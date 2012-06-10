package de.tukl.cs.softech.agilereview.plugincontrol.refactoring;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.xmlbeans.SystemProperties;
import org.apache.xmlbeans.XmlException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

import de.tukl.cs.softech.agilereview.dataaccess.RefactoringAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;

/**
 * Refactoring participant for rename issues. This participant assures the synchronous refactoring of the comment storage
 * @author Malte Brunnlieb - AgileReview
 */
public class AuthorFileRenameParticipant extends RenameParticipant implements ISharableParticipant {
    
    /**
     * Instance of the RefactoringAccess managing the potential model and their refactoring
     */
    private RefactoringAccess ra;
    /**
     * Element which will be refactored
     */
    private IResource element;
    /**
     * A set of affected files for the given refactoring issue
     */
    private Collection<IFile> affectedFiles;
    /**
     * This integer will be set != 0 when an error occurred before checkConditions in order to report them to the user
     */
    private int errorWhileInitialization = 0;
    
    /**
     * The old path of the element to be refactored
     */
    private String oldPath;
    /**
     * The new path the element should be refactored to
     */
    private String newPath;
    /**
     * The type of the element to be refactored corresponding to the static fields of @link{IResource}
     */
    private int type;
    /**
     * This flag will be set, when the sub packages should also be renamed
     */
    private boolean renameSubpackages = false;
    
    /**
     * A map representing the printed document of each file to be changed before the refactoring
     */
    private Map<IFile, String> prevDocs;
    /**
     * A map representing the printed document of each file to be changed after the refactoring
     */
    private Map<IFile, String> postDocs;
    
    @Override
    protected boolean initialize(Object element) {
        ra = new RefactoringAccess();
        addRefactoringIssue(element, getArguments());
        
        if (!ra.getFailedFiles().isEmpty()) {
            //participate and display the error as otherwise the agile review files will be corrupted
            return true;
        } else {
            affectedFiles = ra.getAffectedFiles(this.element, type);
            prevDocs = ra.getPrevDocuments();
        }
        
        if (affectedFiles.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
    
    /**
     * Adds a refactoring issue as this is a sharable participant it will be called for each renaming done within one user triggered refactoring
     * @param element to be refactored
     * @param arguments passed by the refactoring event
     */
    private synchronized void addRefactoringIssue(Object element, RefactoringArguments arguments) {
        
        //check whether the given arguments are the right ones, otherwise leave this function
        RenameArguments rArguments;
        if (arguments instanceof RenameArguments) {
            rArguments = (RenameArguments) arguments;
        } else {
            errorWhileInitialization = 1;
            PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IR7: Unknown refactoring Arguments: "
                    + arguments.getClass());
            return;
        }
        
        //determine information about the element to be renamed, such as oldPath, newPath, type(of IResource) and whether the sub folders should also be renamed
        if (element instanceof IPackageFragment && !renameSubpackages) {
            try {
                this.element = ((IPackageFragment) element).getCorrespondingResource();
                String convOldPath = ((IPackageFragment) element).getElementName().replaceAll("\\.", "/");
                String convNewPath = rArguments.getNewName().replaceAll("\\.", "/");
                String finalPath = this.element.getFullPath().toString();
                int i = finalPath.lastIndexOf(convOldPath);
                finalPath = finalPath.replace(finalPath.subSequence(i, finalPath.length()), convNewPath);
                String oldTmp = this.element.getFullPath().toOSString();
                String newTmp = new Path(finalPath).toOSString();
                
                // if the next refactoring issue is a refinement of the previous one, rename subpackages also
                if (oldPath != null && newPath != null) {
                    if (oldTmp.startsWith(oldPath) && newTmp.startsWith(newPath)) {
                        renameSubpackages = true;
                    } else {
                        //else should not occur (assumption on observation)
                        errorWhileInitialization = 6;
                        PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue",
                                "Error code IR6: Not managed situation (excluded by observation):" + "\noldPath=" + oldPath + "\noldTmp=" + oldTmp
                                        + "\nnewPath=" + newPath + "\nnewTmp=" + newTmp);
                        return;
                    }
                } else {
                    oldPath = oldTmp;
                    newPath = newTmp;
                }
                type = IResource.FOLDER;
            } catch (JavaModelException e) {
                errorWhileInitialization = 3;
                PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IR3: JavaModelException while accessing: "
                        + ((IPackageFragmentRoot) element).getElementName(), e);
                return;
            }
        } else if (element instanceof IResource) {
            this.element = (IResource) element;
            
            //When an IResource occurs, sub packages will be also refactored
            oldPath = this.element.getFullPath().toOSString();
            int i = oldPath.lastIndexOf(SystemProperties.getProperty("file.separator"));
            newPath = new String(oldPath);
            //subSequence i+1 because index is selected on last file.separator
            newPath = newPath.replace(newPath.subSequence(i + 1, newPath.length()), rArguments.getNewName());
            renameSubpackages = true;
            
            if (this.element instanceof IProject) {
                type = IResource.PROJECT;
            } else if (this.element instanceof IFolder) {
                type = IResource.FOLDER;
            } else if (this.element instanceof IFile) {
                type = IResource.FILE;
            } else {
                errorWhileInitialization = 5;
                PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IR5: Unknown IResource subtype: "
                        + element.getClass());
                return;
            }
        } else if (element instanceof IPackageFragmentRoot && !renameSubpackages) {
            try {
                this.element = ((IPackageFragmentRoot) element).getCorrespondingResource();
                oldPath = this.element.getFullPath().toOSString();
                int i = oldPath.lastIndexOf(SystemProperties.getProperty("file.separator"));
                newPath = new String(oldPath);
                newPath = newPath.replace(newPath.subSequence(i + 1, newPath.length()), rArguments.getNewName());
                type = IResource.FOLDER;
                renameSubpackages = true;
            } catch (JavaModelException e) {
                errorWhileInitialization = 4;
                PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IR4: JavaModelException while accessing: "
                        + ((IPackageFragmentRoot) element).getElementName(), e);
                return;
            }
        } else {
            errorWhileInitialization = 9;
            PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue",
                    "Error code IR9: Unknown type of the element which should be refactored: " + element.getClass());
            return;
        }
    }
    
    @Override
    public void addElement(Object element, RefactoringArguments arguments) {
        addRefactoringIssue(element, arguments);
    }
    
    @Override
    public String getName() {
        return "AgileReview Rename Processor";
    }
    
    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
        
        RefactoringStatus resultStatus = new RefactoringStatus();
        
        //when an error occurred during the initialization, abort the refactoring process
        if (errorWhileInitialization != 0) {
            PluginLogger.logWarning(getClass().toString(), "checkConditions", "An error occured during initialization (Code: IR"
                    + errorWhileInitialization + ").");
            resultStatus.addWarning("An error occurred while accessing AgileReview files. (Code: IR" + errorWhileInitialization
                    + ") Continuing could corrupt AgileReview Comments!");
            return resultStatus;
        }
        
        //check if all files could be read by the RefactoringAccess, otherwise report files which are faulty
        HashMap<IFile, Exception> errorFiles = ra.getFailedFiles();
        if (!errorFiles.isEmpty()) {
            PluginLogger.logError(this.getClass().toString(), "checkConditions", "Loading of files for refactoring lead to failures:\n");
            for (Entry<IFile, Exception> entry : errorFiles.entrySet()) {
                String location = entry.getKey().getLocation().toOSString();
                resultStatus.addWarning("Could not load file " + location + "for Refactoring. Continuing could corrupt AgileReview Comments!");
                PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Could not load file " + location + "for Refactoring", entry
                        .getValue());
            }
        }
        
        //add condition checker which are only there for assuring accessibility for the files to be refactored
        RefactoringKit.addConditionChecker(affectedFiles, context);
        
        //simulate changes
        try {
            postDocs = ra.getPostDocumentsOfRefactoring(oldPath, newPath, type, renameSubpackages);
        } catch (IOException e) {
            resultStatus
                    .addWarning("An error occured while accessing AgileReview data in order to simulate refactoring changes. (Code IR10) Continuing will corrupt AgileReview Comments!");
            return resultStatus;
        } catch (XmlException e) {
            resultStatus
                    .addWarning("An error occured while accessing AgileReview data in order to simulate refactoring changes. (Code IR11) Continuing will corrupt AgileReview Comments!");
            return resultStatus;
        } catch (CoreException e) {
            resultStatus
                    .addWarning("An error occured while accessing AgileReview data in order to simulate refactoring changes. (Code IR12) Continuing will corrupt AgileReview Comments!");
            return resultStatus;
        }
        
        //when no warnings are captured -> add info "everything ok"
        if (resultStatus.getEntries().length == 0) {
            resultStatus.addInfo("AgileReview refactoring conditions valid.");
        }
        return resultStatus;
    }
    
    @Override
    public Change createChange(IProgressMonitor pm) throws OperationCanceledException {
        
        //no changes to be done if there was an error during initialization
        if (errorWhileInitialization != 0) { return null; }
        
        return RefactoringKit.createChange(affectedFiles, prevDocs, postDocs, this);
    }
}