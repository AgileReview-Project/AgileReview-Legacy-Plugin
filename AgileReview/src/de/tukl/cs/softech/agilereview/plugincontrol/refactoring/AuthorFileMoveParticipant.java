package de.tukl.cs.softech.agilereview.plugincontrol.refactoring;

import java.io.IOException;
import java.util.Collection;
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

/**
 * Refactoring participant for move issues. This participant assures the synchronous refactoring of the comment storage
 */
public class AuthorFileMoveParticipant extends MoveParticipant implements ISharableParticipant {
	
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
		} catch (XmlException e) {
			errorWhileInitialization = 1;
			return true;
		} catch (IOException e) {
			errorWhileInitialization = 2;
			return true;
		}
		
		addRefactoringIssue(element, getArguments());
		
		if(errorWhileInitialization != 0) {
			//participate and display the error as otherwise the agile review files will be corrupted
			return true;
		} else {
			affectedFiles = ra.getAffectedFiles(this.element, type);
			prevDocs = ra.getPrevDocuments();
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
		//as sub packages will be moved also by this MoveRefactoring we only consider the first refactoring issue
		if(affectedFiles != null && affectedFiles.isEmpty()) {
			return;
		}
		
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
			errorWhileInitialization = 7;
			return;
		}
		
		if(element instanceof IPackageFragment) {
			try {
				this.element = ((IPackageFragment)element).getCorrespondingResource();
				oldPath = this.element.getFullPath().toOSString()+System.getProperty("file.separator")+this.element.getName();
				newPath = dest.getFullPath().toOSString();
				type = IResource.FOLDER;
			} catch (JavaModelException e) {
				errorWhileInitialization = 3;
				return;
			}
		} else if(element instanceof IResource) {
			this.element = (IResource) element;
			oldPath = this.element.getFullPath().toOSString();
			newPath = dest.getFullPath().toOSString()+System.getProperty("file.separator")+this.element.getName();
			
			if(this.element instanceof IProject) {
				type = IResource.PROJECT;
			} else if(this.element instanceof IFolder) {
				type = IResource.FOLDER;
			} else if(this.element instanceof IFile) {
				type = IResource.FILE;
			} else {
				errorWhileInitialization = 5;
				return;
			}
		} else if(element instanceof IPackageFragmentRoot) {
			try {
				this.element = ((IPackageFragmentRoot)element).getCorrespondingResource();
				oldPath = this.element.getFullPath().toOSString();
				newPath = dest.getFullPath().toOSString()+System.getProperty("file.separator")+this.element.getName();
				type = IResource.FOLDER;
			} catch (JavaModelException e) {
				errorWhileInitialization = 4;
				return;
			}
		}
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
			return RefactoringStatus.create(new Status(Status.ERROR, Activator.PLUGIN_ID, "An error occurred while accessing AgileReview files. ("+errorWhileInitialization+")"));
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
							return RefactoringStatus.create(new Status(Status.ERROR, Activator.PLUGIN_ID, f.getLocation()+" is not accessible."));
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
			postDocs = ra.getPostDocumentsOfRefactoring(oldPath, newPath, type, true);
		} catch (IOException e) {
			return RefactoringStatus.create(new Status(Status.ERROR, Activator.PLUGIN_ID, "An error occured while accessing AgileReview data in order to simulate refactoring changes. (1)"));
		} catch (XmlException e) {
			return RefactoringStatus.create(new Status(Status.ERROR, Activator.PLUGIN_ID, "An error occured while accessing AgileReview data in order to simulate refactoring changes. (2)"));
		}
		
		return RefactoringStatus.create(new Status(Status.OK, Activator.PLUGIN_ID, "AgileReview refactoring conditions valid."));
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {

		CompositeChange result = new CompositeChange("Refactoring of all affected comment paths");
	
		ComputeDiff diffProcessor = new ComputeDiff();
		for(IFile f : affectedFiles) {
			
			TextFileChange change = (TextFileChange) getTextChange(f);
			
			if(change != null) {
				//if there are already changes in this file, do not touch it
				return null;
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
			
			result.add(change);
		}
		return result;
	}
}
