package de.tukl.cs.softech.agilereview.plugincontrol.refactoring;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.mapping.IResourceChangeDescriptionFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.IConditionChecker;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.ResourceChangeChecker;
import org.eclipse.text.edits.DeleteEdit;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.plugincontrol.refactoring.ComputeDiff.Diff;

/**
 * Static class of reusable refactoring functionalities usable by different refactoring participants
 * @author Malte Brunnlieb - AgileReview
 */
public class RefactoringKit {
    
    /**
     * Adds an {@link IConditionChecker} to every file given in the collection of affected files. If there already is one added to a file, the
     * CoreException will be suppressed and no new condition checker will be added to this file.
     * @param affectedFiles files to which an {@link IConditionChecker} will be added
     * @param context where the condition checker will be registered
     */
    static void addConditionChecker(Collection<IFile> affectedFiles, CheckConditionsContext context) {
        
        ResourceChangeChecker checker = (ResourceChangeChecker) context.getChecker(ResourceChangeChecker.class);
        IResourceChangeDescriptionFactory deltaFactory = checker.getDeltaFactory();
        
        for (final IFile f : affectedFiles) {
            try {
                context.add(new IConditionChecker() {
                    @Override
                    public RefactoringStatus check(IProgressMonitor monitor) throws CoreException {
                        if (!f.isReadOnly() && f.isAccessible()) {
                            return RefactoringStatus.create(new Status(Status.OK, Activator.PLUGIN_ID, f.getLocation() + " ready to be changed."));
                        } else {
                            return RefactoringStatus.create(new Status(Status.WARNING, Activator.PLUGIN_ID, f.getLocation()
                                    + " is not accessible. Continuing will corrupt AgileReview Comments!"));
                        }
                    }
                });
            } catch (CoreException e) {
                //do not inform the user as this condition checker can be added twice by different AgileReview Refactoring participants:
                //can be called twice (e.g. when renaming a single package which is represented by an IResource AND an IPackageFragment) 
            }
            deltaFactory.change(f);
        }
    }
    
    /**
     * Creates all changes for the given files on the basis of their contents before and after the refactoring simulation.
     * @param affectedFiles all files which changes should be determined in eclipse representation
     * @param prevDocs contents of the files before the refactoring simulation
     * @param postDocs contents of the files after the refactoring simulation
     * @param participant the reference of the refactoring participant which calls this function in order to get possible changes for each file which
     *            could be done by other participants beforehand
     * @return a {@link CompositeChange} which comprises all changes
     */
    static Change createChange(Collection<IFile> affectedFiles, Map<IFile, String> prevDocs, Map<IFile, String> postDocs,
            RefactoringParticipant participant) {
        
        CompositeChange result = new CompositeChange("Refactoring of all affected comment paths");
        ComputeDiff diffProcessor = new ComputeDiff();
        
        for (IFile f : affectedFiles) {
            
            TextFileChange change = (TextFileChange) participant.getTextChange(f);
            
            if (change == null) {
                //only touch this file if there are no changes done so far
                change = new TextFileChange(f.getName(), f);
                change.setEdit(new MultiTextEdit());
                
                //current index of the previous (original) document
                int oldIndex = 0;
                //should be != null if a delete edit occurs before a insert edit
                DeleteEdit dEdit = null;
                for (Diff d : diffProcessor.diff_main(prevDocs.get(f), postDocs.get(f), false)) {
                    switch (d.operation) {
                    case EQUAL:
                        if (dEdit != null) {
                            try {
                                change.addEdit(new DeleteEdit(oldIndex, dEdit.getLength()));
                            } catch (MalformedTreeException e) {
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
                        if (dEdit != null) {
                            try {
                                change.addEdit(new ReplaceEdit(oldIndex, dEdit.getLength(), d.text));
                            } catch (MalformedTreeException e) {
                                //only catch this as it is possible to have duplicated edits (e.g. IResource && IPackageFragment)
                            }
                            oldIndex += dEdit.getLength();
                            dEdit = null;
                        } else {
                            try {
                                change.addEdit(new InsertEdit(oldIndex, d.text));
                            } catch (MalformedTreeException e) {
                                //only catch this as it is possible to have duplicated edits (e.g. IResource && IPackageFragment)
                            }
                        }
                        break;
                    }
                }
                //only add the change if there are edits to be performed
                if (((MultiTextEdit) change.getEdit()).getChildren().length != 0) {
                    result.add(change);
                }
            }
        }
        
        /* XXX there is the case that we participate on refactoring without changing anything:
         * A package is moved containing no commented files in the next level. However this package contains
         * sub folder which contain commented files. Then the affected author files will be listed so far.
         * So we have to cope with this by monitoring the changes on a file and remove one if no changes were detected.
         * --> In oder to correct this in a more efficient way, rewrite the getAffectedFiles method of RefactoringAccess
         */
        if (result.getChildren().length != 0) {
            return result;
        } else {
            return null;
        }
    }
}