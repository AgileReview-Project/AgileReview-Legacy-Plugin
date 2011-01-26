package de.tukl.cs.softech.agilereview.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	private HashMap<String, Annotation> annotationMap = new HashMap<String, Annotation>();
	private HashMap<String, Position> positionMap = new HashMap<String, Position>();
	
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
	public void displayAnnotations(Map<Position, String> keyPositionMap) {
		//add annotations that are not already displayed
		for (Position position : keyPositionMap.keySet()) {
			if (!this.positionMap.values().contains(position)) {
				addAnnotation(keyPositionMap.get(position), position);
			}
		}
		//remove annotations that are not to be displayed
		for (String key : this.positionMap.keySet()) {
			if (!keyPositionMap.containsKey(this.positionMap.get(key))) {
				deleteAnnotation(key);
			}
		}
	}
	
	/**
	 * Adds a new annotation at a given position p.
	 * @param p The position to add the annotation on.
	 */
	public void addAnnotation(String commentKey, Position p) {
		//TODO debug
		System.out.println("add Annotation: "+p);
		Annotation annotation = new Annotation("AgileReview.comment.annotation", true, "AgileReview Annotation");
		this.annotationMap.put(commentKey, annotation);
		this.annotationModel.addAnnotation(annotation, p);
		this.positionMap.put(commentKey, p);
	}
	
	/**
	 * Deletes an existing annotation at a given position p.
	 * @param p The position where the annotation will be deleted.
	 */
	public void deleteAnnotation(String commentKey) {
		//TODO debug
		System.out.println("delete Annotation: "+commentKey);
		// Delete from local savings
		Annotation annotation = this.annotationMap.get(commentKey);
		Position position = this.annotationModel.getPosition(annotation);
		// Delete from AnnotationModel
		this.annotationMap.remove(commentKey);
		annotation.markDeleted(true);
		position.delete();
		this.annotationModel.removeAnnotation(annotation);
		this.positionMap.remove(commentKey);
	}
	
	/**
	 * Removes all annotations from the editor's annotation model.
	 */
	public void deleteAllAnnoations() {
		if (!this.positionMap.isEmpty()) {
			for(String p : positionMap.keySet()) {
				deleteAnnotation(p);
			}
			annotationMap.clear();
			positionMap.clear();
		}
	}
}
