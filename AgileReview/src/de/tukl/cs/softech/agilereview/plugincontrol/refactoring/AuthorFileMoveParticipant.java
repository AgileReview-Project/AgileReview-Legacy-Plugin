package de.tukl.cs.softech.agilereview.plugincontrol.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;

import de.tukl.cs.softech.agilereview.dataaccess.RefactoringAccess;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;

/**
 * Refactoring participant for move issues. This participant assures the synchronous refactoring of the comment storage
 * @author Malte Brunnlieb - AgileReview
 */
public class AuthorFileMoveParticipant extends MoveParticipant implements ISharableParticipant {
	
	/**
	 * Instance of the RefactoringAccess managing the potential model and their refactoring
	 */
	private RefactoringAccess ra;
	/**
	 * A set of affected files for the given refactoring issue
	 */
	private Collection<IFile> affectedFiles = new HashSet<IFile>();
	/**
	 * This integer will be set != 0 when an error occurred before checkConditions in order to report them to the user
	 */
	private int errorWhileInitialization = 0;
	
	/**
	 * The old path of the element to be refactored
	 */
	private ArrayList<String> oldPath = new ArrayList<String>();
	/**
	 * The new path the element should be refactored to
	 */
	private ArrayList<String> newPath = new ArrayList<String>();
	/**
	 * The type of the element to be refactored corresponding to the static fields of @link{IResource}
	 */
	private ArrayList<Integer> type = new ArrayList<Integer>();
	/**
	 * This list of booleans declares whether the subfolders should be moved also
	 */
	private ArrayList<Boolean> moveSubfolders = new ArrayList<Boolean>();
	
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

		if(!ra.getFailedFiles().isEmpty()) {
			//participate and display the error as otherwise the agile review files will be corrupted
			return true;
		}
		
		if(affectedFiles.isEmpty()) {
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
		MoveArguments mArguments;
		if(arguments instanceof MoveArguments) {
			mArguments = (MoveArguments)arguments;
		} else {
			errorWhileInitialization = 1;
			PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IM1: Unknown refactoring Arguments: "+arguments.getClass());
			return;
		}
		
		//get destination for move refactoring
		IResource dest;
		if(mArguments.getDestination() instanceof IJavaElement) {
			try {
				dest = ((IJavaElement) mArguments.getDestination()).getCorrespondingResource();
			} catch (JavaModelException e) {
				errorWhileInitialization = 8;
				PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IM8: JavaModelException while accessing: "+((IPackageFragmentRoot)element).getElementName(), e);
				return;
			}
		} else if(mArguments.getDestination() instanceof IResource) {
			dest = (IResource) mArguments.getDestination();
		} else {
			errorWhileInitialization = 7;
			PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IM7: Unknown destination type: "+mArguments.getDestination().getClass());
			return;
		}
		
		String fSep = System.getProperty("file.separator");
		IResource resource;
		
		//determine information about the element to be moved, such as oldPath, newPath, type(of IResource) and whether the sub folders should also be moved
		if(element instanceof IPackageFragment) {
			try {
				resource = ((IPackageFragment)element).getCorrespondingResource();
			} catch (JavaModelException e) {
				errorWhileInitialization = 3;
				PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IM3: JavaModelException while accessing: "+((IPackageFragmentRoot)element).getElementName(), e);
				return;
			}
			
			oldPath.add(resource.getFullPath().toOSString());
			newPath.add(dest.getFullPath().toOSString()+fSep+((IPackageFragment)element).getElementName().replaceAll("\\.", "\\"+fSep));
			type.add(IResource.FOLDER);
			moveSubfolders.add(false);
			
		} else if(element instanceof IResource) {
			resource = (IResource) element;
			String oldPathTmp = resource.getFullPath().toOSString();
			
			//optimizations to prevent from unnecessary refactoring steps
			if(oldPath.contains(oldPathTmp)) {
				//this element was added beforehand (by an IPackageFragment) -> set moveSubfolders to true
				int i = oldPath.indexOf(oldPathTmp);
				moveSubfolders.add(i, true);
				moveSubfolders.remove(i+1);
				return;
			} else {
				//check whether a file of a already captured folder has to be refactored -> ignore it, as it will be moved always
				for(int i = 0; i < oldPath.size(); i++) {
					if(oldPathTmp.startsWith(oldPath.get(i))) {
						return;
					}
				}
			}

			oldPath.add(oldPathTmp);
			newPath.add(dest.getFullPath().toOSString()+fSep+resource.getName());
			
			if(resource instanceof IProject) {
				type.add(IResource.PROJECT);
			} else if(resource instanceof IFolder) {
				type.add(IResource.FOLDER);
			} else if(resource instanceof IFile) {
				type.add(IResource.FILE);
			} else {
				errorWhileInitialization = 5;
				PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IM5: Unknown IResource subtype: "+resource.getClass());
				return;
			}
			moveSubfolders.add(true);
			
		} else if(element instanceof IPackageFragmentRoot) {
			try {
				resource = ((IPackageFragmentRoot)element).getCorrespondingResource();
			} catch (JavaModelException e) {
				errorWhileInitialization = 4;
				PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IM4: JavaModelException while accessing: "+((IPackageFragmentRoot)element).getElementName(), e);
				return;
			}
			
			oldPath.add(resource.getFullPath().toOSString());
			newPath.add(dest.getFullPath().toOSString()+System.getProperty("file.separator")+resource.getName());
			type.add(IResource.FOLDER);
			moveSubfolders.add(true);
		} else {
			errorWhileInitialization = 9;
			PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Error code IM9: Unknown type of the element which should be refactored: "+element.getClass());
			return;
		}
		
		//compute newly affectedFiles and add them to the overall affectedFiles list and get the new previous documents
		affectedFiles.addAll(ra.getAffectedFiles(resource, type.get(type.size()-1)));
		prevDocs = ra.getPrevDocuments();
	}
	
	@Override
	public void addElement(Object element, RefactoringArguments arguments) {
		addRefactoringIssue(element, arguments);
	}

	@Override
	public String getName() {
		return "AgileReview Move Processor";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		
		RefactoringStatus resultStatus = new RefactoringStatus();
		
		//when an error occurred during the initialization, abort the refactoring process
		if(errorWhileInitialization != 0) {
			PluginLogger.logWarning(getClass().toString(), "checkConditions", "An error occured during initialization (Code: IM"+errorWhileInitialization+").");
			resultStatus.addWarning("An error occurred while accessing AgileReview files (Code: IM"+errorWhileInitialization+"). Continuing could corrupt AgileReview Comments!");
			return resultStatus;
		}
		
		//check if all files could be read by the RefactoringAccess, otherwise report files which are faulty
		HashMap<IFile, Exception> errorFiles = ra.getFailedFiles();
		if (!errorFiles.isEmpty()) {
			PluginLogger.logError(this.getClass().toString(), "checkConditions", "Loading of files for refactoring lead to failures:\n");
			for (Entry<IFile, Exception> entry : errorFiles.entrySet()) {
				String location = entry.getKey().getLocation().toOSString();
				resultStatus.addWarning("Could not load file "+location+"for Refactoring. Continuing could corrupt AgileReview Comments!");
				PluginLogger.logError(this.getClass().toString(), "addRefactoringIssue", "Could not load file "+location+"for Refactoring", entry.getValue());
			}
		}
		
		//add context checker which are only there for assuring accessibility for the files to be refactored
		RefactoringKit.addConditionChecker(affectedFiles, context);
		
		//simulate changes
		try {
			for(int i = 0; i < oldPath.size(); i++) {
				postDocs = ra.getPostDocumentsOfRefactoring(oldPath.get(i), newPath.get(i), type.get(i), moveSubfolders.get(i));
			}
		} catch (IOException e) {
			resultStatus.addWarning("An error occured while accessing AgileReview data in order to simulate refactoring changes. (Code IM10) Continuing will corrupt AgileReview Comments!");
			return resultStatus;
		} catch (XmlException e) {
			resultStatus.addWarning("An error occured while accessing AgileReview data in order to simulate refactoring changes. (Code IM11) Continuing will corrupt AgileReview Comments!");
			return resultStatus;
		}
		
		//when no warnings are captured -> add info "everything ok"
		if(resultStatus.getEntries().length == 0) {
			resultStatus.addInfo("AgileReview refactoring conditions valid.");
		}
		return resultStatus;
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws OperationCanceledException {
		
		//no changes to be done if there was an error during initialization
		if(errorWhileInitialization != 0) {
			return null;
		}

		return RefactoringKit.createChange(affectedFiles, prevDocs, postDocs, this);
	}
}