package de.tukl.cs.softech.agilereview.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This class is used to store annotations for a given text editor
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
	 * Adds a new annotation at a given position p.
	 * @param p The position to add the annotation on.
	 */
	public void addAnnotation(Position p) {
		
		Annotation annotation = new Annotation("AgileReview.comment.annotation", true, "AgileReview Annotation");
		this.annotationModel.addAnnotation(annotation, p);
		
	}
	
	/**
	 * Adds all annotations of the given collection
	 * @param ps collection of positions to be added
	 */
	public void addAnnotations(Collection<Position> ps) {
		for(Position p : ps) {
			addAnnotation(p);
		}
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
	
	/**
	 * Removes all annotations from the editor's annotation model.
	 */
	public void deleteAllAnnoations() {
		if (!this.annotationMap.isEmpty()) {
			HashSet<Position> delPos = new HashSet<Position>();
			delPos.addAll(annotationMap.keySet());
			for (Position position : delPos) {
				deleteAnnotation(position);
			}
		}
	}
	
	/**
	 * Hides an annotation at a given position instead of removing it completely
	 * @param position the position
	 */
	public void hideAnnotation(Position position) {
		Annotation annotation = this.annotationMap.get(position);
		if (annotation!=null) {
			annotation.markDeleted(true);
			this.annotationModel.removeAnnotation(annotation);
		}
	}
	
	/**
	 * Hides all annotation at the given positions instead of removing them completely
	 * @param positions the positions
	 */
	public void hideAllAnnotations(Collection<Position> positions) {
		for (Position p : positions) {
			hideAnnotation(p);
		}
	}
	
	/**
	 * Displays an annotation at a given position if it was hidden before
	 * @param position the position
	 */
	public void showAnnotation(Position position) {
		Annotation annotation = this.annotationMap.get(position);
		if (annotation!=null & annotation.isMarkedDeleted()) {
			annotation.markDeleted(false);
			annotationModel.addAnnotation(annotation, position);
		}
	}
	
	public void showAllAnnoations() {
		for (Position position : annotationMap.keySet()) {
			Annotation annotation = this.annotationMap.get(position); 
			if (annotation.isMarkedDeleted()) {
				this.annotationModel.addAnnotation(annotation, position);
			}
		}
	}
	
}
