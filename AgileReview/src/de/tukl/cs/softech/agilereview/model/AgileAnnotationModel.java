package de.tukl.cs.softech.agilereview.model;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map.Entry;

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
	private HashMap<Position, ArrayDeque<Annotation>> annotationMap = new HashMap<Position, ArrayDeque<Annotation>>();
	
	/**
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
		
		// there's already an annotation at this position
		if (this.annotationMap.containsKey(p))
		{
			this.annotationMap.get(p).add(annotation);
		}
		// there's no annotation at this position
		else
		{
			ArrayDeque<Annotation> tmpQueue = new ArrayDeque<Annotation>();
			tmpQueue.add(annotation);
			this.annotationMap.put(p, tmpQueue);
		}
		
		this.annotationModel.addAnnotation(annotation, p);
		
	}
	
	/**
	 * Deletes an existing annotation at a given position p.
	 * @param p The position where the annotation will be deleted.
	 */
	public void deleteAnnotation(Position p) {
		
		// Delete from local savings
		ArrayDeque<Annotation> currQue = this.annotationMap.get(p);
		Annotation delAnnotation = currQue.remove();
		if (currQue.isEmpty())
		{
			this.annotationMap.remove(p);
		}
			
		// Delete from AnnotationModel
		Position delPosition = this.annotationModel.getPosition(delAnnotation);
		delPosition.delete();
		delAnnotation.markDeleted(true);
		this.annotationModel.removeAnnotation(delAnnotation);
		
	}
	
	/**
	 * Removes all annotations from the editor's annotation model.
	 */
	public void deleteAnnoations() {
		if (!this.annotationMap.isEmpty()) {
			for (Entry<Position, ArrayDeque<Annotation>> annotationsAtPosition : annotationMap.entrySet()) {
				for (@SuppressWarnings("unused") Annotation annotation : annotationsAtPosition.getValue()) {
					deleteAnnotation(annotationsAtPosition.getKey());
				}
			}
		}
	}
	
}
