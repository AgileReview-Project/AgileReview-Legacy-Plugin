package de.tukl.cs.softech.agilereview.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension2;
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
	private IAnnotationModelExtension2 annotationModel;
	/**
	 * The annotations added by AgileReview to the editor's annotation model 
	 */
	private HashMap<String, Annotation> annotationMap = new HashMap<String, Annotation>();
	
	/**
	 * Creates a new AgileAnnotationModel
	 * @param editor The text editor in which the annotations will be displayed
	 */
	public AgileAnnotationModel(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		this.annotationModel = (IAnnotationModelExtension2) ((ITextEditor)editor).getDocumentProvider().getAnnotationModel(input);
	}
	
	/**
	 * Displays the given positions as annotations in the provided editor. Therefore annotations which should
	 * not be displayed any more will be removed and not yet drawn annotations will be added to the annotation model.
	 * @param keyPositionMap a map of Positions which should be annotated and the comment keys correlated to the positions
	 */
	public void displayAnnotations(Map<String, Position> keyPositionMap) {
		//add annotations that are not already displayed
		for (String s : keyPositionMap.keySet()) {
			if (!annotationMap.containsKey(s)) {
				addAnnotation(s, keyPositionMap.get(s));
			}
		}
		//remove annotations that should not be displayed
		for (String key : annotationMap.keySet()) {
			if (!keyPositionMap.containsKey(key)) {
				deleteAnnotation(key);
			}
		}
		
	}
	
	/**
	 * Adds a new annotation at a given position p.
	 * @param commentKey The tag key of the comment for which this annotation holds
	 * @param p The position to add the annotation on.
	 */
	public void addAnnotation(String commentKey, Position p) {
		//TODO debug
		System.out.println("add Annotation: "+commentKey);
		Annotation annotation = new Annotation("AgileReview.comment.annotation", true, "AgileReview Annotation");
		this.annotationMap.put(commentKey, annotation);
		((IAnnotationModel) this.annotationModel).addAnnotation(annotation, p);
	}
	
	/**
	 * Deletes an existing annotation at a given position p.
	 * @param commentKey unique tag key of the comment
	 */
	public void deleteAnnotation(String commentKey) {
		//TODO debug
		System.out.println("delete Annotation: "+commentKey);
		// Delete from local savings
		Annotation annotation = this.annotationMap.get(commentKey);
		// Delete from AnnotationModel
		this.annotationMap.remove(commentKey);
		annotation.markDeleted(true);
		//TODO removeAnnotation performance hint!
		((IAnnotationModel) this.annotationModel).removeAnnotation(annotation);
	}
	
	/**
	 * Removes all annotations from the editor's annotation model.
	 */
	public void deleteAllAnnoations() {
		if (!this.annotationMap.isEmpty()) {
			for(String p : annotationMap.keySet()) {
				deleteAnnotation(p);
			}
			annotationMap.clear();
		}
	}
}
