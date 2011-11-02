package de.tukl.cs.softech.agilereview.plugincontrol.refactoring;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.IConditionChecker;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.MoveArguments;
import org.eclipse.ltk.core.refactoring.participants.MoveParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.dataaccess.RefactoringAccess;
import de.tukl.cs.softech.agilereview.plugincontrol.refactoring.ComputeDiff.Diff;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;

/**
 * Refactoring participant for move issues. This participant assures the synchronous refactoring of the comment storage
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
		try {
			ra = new RefactoringAccess();
			addRefactoringIssue(element, getArguments());
		} catch (XmlException e) {/*?|r68|Peter Reuter|c0|*/
			errorWhileInitialization = 1;
			PluginLogger.logError(getClass().toString(), "initialize", e.getLocalizedMessage(), e);
			return true;
		} catch (IOException e) {
			PluginLogger.logError(getClass().toString(), "initialize", e.getLocalizedMessage(), e);
			errorWhileInitialization = 2;
			return true;
		}/*|r68|Peter Reuter|c0|?*/
		
		if(errorWhileInitialization != 0) {
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
		
		MoveArguments mArguments;
		if(arguments instanceof MoveArguments) {
			mArguments = (MoveArguments)arguments;
		} else {
			return;
		}
		
		IResource dest;
		if(mArguments.getDestination() instanceof IJavaElement) {
			try {
				dest = ((IJavaElement) mArguments.getDestination()).getCorrespondingResource();
			} catch (JavaModelException e) {
				errorWhileInitialization = 8;
				return;
			}
		} else if(mArguments.getDestination() instanceof IResource) {
			dest = (IResource) mArguments.getDestination();
		} else {
			errorWhileInitialization = 7;/*?|r68|Peter Reuter|c2|?*/
			return;
		}
		
		String fSep = System.getProperty("file.separator");
		IResource resource;
		
		if(element instanceof IPackageFragment) {
			try {
				resource = ((IPackageFragment)element).getCorrespondingResource();
			} catch (JavaModelException e) {
				errorWhileInitialization = 3;
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
				return;
			}
			moveSubfolders.add(true);
			
		} else if(element instanceof IPackageFragmentRoot) {
			try {
				resource = ((IPackageFragmentRoot)element).getCorrespondingResource();
			} catch (JavaModelException e) {
				errorWhileInitialization = 4;
				return;
			}
			
			oldPath.add(resource.getFullPath().toOSString());
			newPath.add(dest.getFullPath().toOSString()+System.getProperty("file.separator")+resource.getName());
			type.add(IResource.FOLDER);
			moveSubfolders.add(true);
		} else {
			errorWhileInitialization = 9;
			return;
		}
		
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
		//when an error occurred during the initialization, abort the refactoring process
		if(errorWhileInitialization != 0) {
			PluginLogger.logWarning(getClass().toString(), "checkConditions", "An error occured during initialization");
			return RefactoringStatus.create(new Status(Status.WARNING, Activator.PLUGIN_ID, "An error occurred while accessing AgileReview files. ("+errorWhileInitialization+") Continuing will corrupt AgileReview Comments!"));/*?|r68|Peter Reuter|c3|?*/
		}
		
		ResourceChangeChecker checker = (ResourceChangeChecker) context.getChecker(ResourceChangeChecker.class);
		IResourceChangeDescriptionFactory deltaFactory = checker.getDeltaFactory();
	
		for(final IFile f : affectedFiles) {
			try {
				context.add(new IConditionChecker() {
					@Override
					public RefactoringStatus check(IProgressMonitor monitor) throws CoreException {
						if(!f.isReadOnly() && f.isAccessible()) {
							return RefactoringStatus.create(new Status(Status.OK, Activator.PLUGIN_ID, f.getLocation()+" ready to be changed."));
						} else {
							return RefactoringStatus.create(new Status(Status.WARNING, Activator.PLUGIN_ID, f.getLocation()+" is not accessible. Continuing will corrupt AgileReview Comments!"));/*?|r68|Peter Reuter|c4|?*/
						}
					}
				});
			} catch (CoreException e) {
				//can be called twice (e.g. when renaming a single package which is represented by an IResource AND an IPackageFragment) 
			}
			deltaFactory.change(f);
		}
		
		//simulate changes
		try {
			//do refactoring
			for(int i = 0; i < oldPath.size(); i++) {
				postDocs = ra.getPostDocumentsOfRefactoring(oldPath.get(i), newPath.get(i), type.get(i), moveSubfolders.get(i));
			}
		} catch (IOException e) {
			return RefactoringStatus.create(new Status(Status.WARNING, Activator.PLUGIN_ID, "An error occured while accessing AgileReview data in order to simulate refactoring changes. (XI) Continuing will corrupt AgileReview Comments!"));
		} catch (XmlException e) {
			return RefactoringStatus.create(new Status(Status.WARNING, Activator.PLUGIN_ID, "An error occured while accessing AgileReview data in order to simulate refactoring changes. (XII) Continuing will corrupt AgileReview Comments!"));
		}
		
		return RefactoringStatus.create(new Status(Status.OK, Activator.PLUGIN_ID, "AgileReview refactoring conditions valid."));
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		
		if(errorWhileInitialization != 0) {
			return null;
		}

		CompositeChange result = new CompositeChange("Refactoring of all affected comment paths");
	
		ComputeDiff diffProcessor = new ComputeDiff();
		
		for(IFile f : affectedFiles) {
			
			TextFileChange change = (TextFileChange) getTextChange(f);
			
			if(change != null) {
				//if there are already changes in this file, do not touch it
				return null;/*?|r68|Peter Reuter|c5|?*/
			}
			
			change = new TextFileChange(f.getName(), f);
			change.setEdit(new MultiTextEdit());
			
			//current index of the previous (original) document
			int oldIndex = 0;
			//should be != null if a delete edit occurs before a insert edit
			DeleteEdit dEdit = null;
			for(Diff d : diffProcessor.diff_main(prevDocs.get(f), postDocs.get(f), false)) {
				switch(d.operation) {
				case EQUAL: 
					if(dEdit != null) {
						try {
							change.addEdit(new DeleteEdit(oldIndex, dEdit.getLength()));
						} catch(MalformedTreeException e) {
							//only catch this as it is possible to have duplicated edits (e.g. IResource && IPackageFragment)
						}
						oldIndex += dEdit.getLength();
						dEdit = null;
					}
					oldIndex += d.text.length();
					break;
				case DELETE:
					dEdit = new DeleteEdit(oldIndex, d.text.length());
					break;
				case INSERT:
					if(dEdit != null) {
						try {
							change.addEdit(new ReplaceEdit(oldIndex, dEdit.getLength(), d.text));
						} catch(MalformedTreeException e) {
							//only catch this as it is possible to have duplicated edits (e.g. IResource && IPackageFragment)
						}
						oldIndex += dEdit.getLength();
						dEdit = null;
					} else {
						try {
							change.addEdit(new InsertEdit(oldIndex, d.text));
						} catch(MalformedTreeException e) {
							//only catch this as it is possible to have duplicated edits (e.g. IResource && IPackageFragment)
						}
					}
					break;
				}
			}
			
			//only add the change if there are edits to be performed
			if(((MultiTextEdit)change.getEdit()).getChildren().length != 0) {
				result.add(change);
			}
		}
		
		/* XXX there is the case that we participate on refactoring without changing anything:
		 * A package is moved containing no commented files in the next level. However this package contains
		 * sub folder which contain commented files. Then the affected author files will be listed so far.
		 * So we have to cope with this by monitoring the changes on a file and remove one if no changes were detected.
		 * --> In oder to correct this in a more efficient way, rewrite the getAffectedFiles method of RefactoringAccess
		 */
		if(result.getChildren().length != 0) {
			return result;
		} else {
			return null;
		}
	}
}