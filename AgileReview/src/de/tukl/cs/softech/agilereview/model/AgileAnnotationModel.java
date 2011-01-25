package de.tukl.cs.softech.agilereview.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class is used to draw and manage annotations for a given text editor
 */
public class AgileAnnotationModel {

	/**
	 * The texteditor's annotation model
	 */
	private IAnnotationModel annotationModel;
	/**
	 * The annotations added by AgileReview to the editor's annotation model 
	 */
	private HashMap<Position, Annotation> annotationMap = new HashMap<Position, Annotation>();
	
	/**
	 * Creates a new AgileAnnotationModel
	 * @param editor The text editor in which the annotations will be displayed
	 */
	public AgileAnnotationModel(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		this.annotationModel = ((ITextEditor)editor).getDocumentProvider().getAnnotationModel(input);
	}
	
	/**
	 * Displays the given positions as annotations in the provided editor. Therefore annotations which should
	 * not be displayed any more will be removed and not yet drawn annotations will be added to the annotation model.
	 * @param positions to be marked as annotations in the editor
	 */
	public void displayAnnotations(Collection<Position> positions) {
		//remove annotations which should not be displayed
		Set<Position> toDelete = this.annotationMap.keySet();
		toDelete.removeAll(positions);
		for(Position p : toDelete) {
			deleteAnnotation(p);
		}
		
		//determine all annotations which should be displayed and have not been displayed yet
		Set<Position> toDraw = new HashSet<Position>(positions);
		toDraw.removeAll(this.annotationMap.keySet());
		for(Position p : toDraw) {
			addAnnotation(p);
		}
	}
	
	/**
	 * Adds a new annotation at a given position p.
	 * @param p The position to add the annotation on.
	 */
	public void addAnnotation(Position p) {
		Annotation annotation = new Annotation("AgileReview.comment.annotation", true, "AgileReview Annotation");
		this.annotationModel.addAnnotation(annotation, p);
	}
	
	/**
	 * Deletes an existing annotation at a given position p.
	 * @param p The position where the annotation will be deleted.
	 */
	public void deleteAnnotation(Position p) {
		// Delete from local savings
		Annotation annotation = this.annotationMap.get(p);
		Position position = this.annotationModel.getPosition(annotation);
		// Delete from AnnotationModel
		this.annotationMap.remove(position);
		annotation.markDeleted(true);
		position.delete();
		this.annotationModel.removeAnnotation(annotation);
	}
}
