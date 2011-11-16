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
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.IConditionChecker;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.swt.widgets.Display;
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
 * Refactoring participant for rename issues. This participant assures the synchronous refactoring of the comment storage
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
		ra = new RefactoringAccess();/*?|r68|Peter Reuter|c2|*/
		HashMap<IFile, Exception> errorFiles = ra.getFailedFiles();
		if (!errorFiles.isEmpty()) {
			String message = "AgileReview could not refactor the following files:\n\n";
			for (Entry<IFile, Exception> entry : errorFiles.entrySet()) {
				String location = entry.getKey().getLocation().toOSString();
				message += location+"\n";
				PluginLogger.logError(this.getClass().toString(), "initialize", "Could not refactor file "+location, entry.getValue());
			}
			message += "\nThese files may be corrupted (i.e. empty). Please check them.";
			MessageDialog.openError(Display.getDefault().getActiveShell(), "AgileReview: Could not refactor files", message);
		}
		
		addRefactoringIssue(element, getArguments());

		// TODO: adapt error handling!!/*|r68|Peter Reuter|c2|?*/
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
		RenameArguments rArguments;
		if(arguments instanceof RenameArguments) {
			rArguments = (RenameArguments)arguments;
		} else {
			errorWhileInitialization = 7;
			return;
		}
		
		if(element instanceof IPackageFragment && !renameSubpackages) {
			try {
				this.element = ((IPackageFragment)element).getCorrespondingResource();
				String convOldPath = ((IPackageFragment)element).getElementName().replaceAll("\\.", "/");
				String convNewPath = rArguments.getNewName().replaceAll("\\.", "/");
				String finalPath = this.element.getFullPath().toString();
				int i = finalPath.lastIndexOf(convOldPath);
				finalPath = finalPath.replace(finalPath.subSequence(i, finalPath.length()), convNewPath);
				String oldTmp = this.element.getFullPath().toOSString();
				String newTmp = new Path(finalPath).toOSString();
				
				// if the next refactoring issue is a refinement of the previous one, rename subpackages also
				if(oldPath != null && newPath != null) {
					if(oldTmp.startsWith(oldPath) && newTmp.startsWith(newPath)) {
						renameSubpackages = true;
					} else {
						//else should not occur (assumption on observation)
						errorWhileInitialization = 6;
						return;
					}
				} else {
					oldPath = oldTmp;
					newPath = newTmp;
				}
				type = IResource.FOLDER;
			} catch (JavaModelException e) {
				errorWhileInitialization = 3;
				return;
			}
		} else if(element instanceof IResource) {
			this.element = (IResource) element;
			
			//When an IResource occurs, sub packages will be also refactored
			oldPath = this.element.getFullPath().toOSString();
			int i = oldPath.lastIndexOf(SystemProperties.getProperty("file.separator"));
			newPath = new String(oldPath);
			//subSequence i+1 because index is selected on last file.separator
			newPath = newPath.replace(newPath.subSequence(i+1, newPath.length()), rArguments.getNewName());
			renameSubpackages = true;
			
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
		} else if(element instanceof IPackageFragmentRoot && !renameSubpackages) {
			try {
				this.element = ((IPackageFragmentRoot)element).getCorrespondingResource();
				oldPath = this.element.getFullPath().toOSString();
				int i = oldPath.lastIndexOf(SystemProperties.getProperty("file.separator"));
				newPath = new String(oldPath);
				newPath = newPath.replace(newPath.subSequence(i+1, newPath.length()), rArguments.getNewName());
				type = IResource.FOLDER;
				renameSubpackages = true;
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
		return "AgileReview Rename Processor";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException {
		//when an error occurred during the initialization, abort the refactoring process
		if(errorWhileInitialization != 0) {
			PluginLogger.logWarning(getClass().toString(), "checkConditions", "An error occured during initialization");
			return RefactoringStatus.create(new Status(Status.WARNING, Activator.PLUGIN_ID, "An error occurred while accessing AgileReview files. ("+errorWhileInitialization+") Continuing will corrupt AgileReview Comments!"));
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
							return RefactoringStatus.create(new Status(Status.WARNING, Activator.PLUGIN_ID, f.getLocation()+" is not accessible. Continuing will corrupt AgileReview Comments!"));
						}
					}
				});
			} catch (CoreException e) {/*?|r81|Malte|c1|?*/
				//do not inform the user as this condition checker can be added twice by different AgileReview Refactoring participants:
				//can be called twice (e.g. when renaming a single package which is represented by an IResource AND an IPackageFragment) 
			}
			deltaFactory.change(f);
		}
		
		//simulate changes
		try {
			//do refactoring
			postDocs = ra.getPostDocumentsOfRefactoring(oldPath, newPath, type, renameSubpackages);
		} catch (IOException e) {
			return RefactoringStatus.create(new Status(Status.WARNING, Activator.PLUGIN_ID, "An error occured while accessing AgileReview data in order to simulate refactoring changes. (XI) Continuing will corrupt AgileReview Comments!"));
		} catch (XmlException e) {
			return RefactoringStatus.create(new Status(Status.WARNING, Activator.PLUGIN_ID, "An error occured while accessing AgileReview data in order to simulate refactoring changes. (XII) Continuing will corrupt AgileReview Comments!"));
		}
		
		return RefactoringStatus.create(new Status(Status.OK, Activator.PLUGIN_ID, "AgileReview refactoring conditions valid."));
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws OperationCanceledException {
		
		if(errorWhileInitialization != 0) {
			return null;
		}
		
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