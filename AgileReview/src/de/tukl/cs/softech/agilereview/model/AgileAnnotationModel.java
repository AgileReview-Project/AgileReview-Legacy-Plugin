package de.tukl.cs.softech.agilereview.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
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
	private IAnnotationModelExtension annotationModel;
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
		this.annotationModel = (IAnnotationModelExtension) ((ITextEditor)editor).getDocumentProvider().getAnnotationModel(input);
	}
	
	/**
	 * Displays the given positions as annotations in the provided editor. Therefore annotations which should
	 * not be displayed any more will be removed and not yet drawn annotations will be added to the annotation model.
	 * @param keyPositionMap a map of Positions which should be annotated and the comment keys correlated to the positions
	 */
	public void displayAnnotations(Map<String, Position> keyPositionMap) {
		//add annotations that are not already displayed
		Map<Annotation, Position> annotationsToAdd = new HashMap<Annotation, Position>();
		for (String s : keyPositionMap.keySet()) {
			if (!annotationMap.containsKey(s)) {
				createNewAnnotation(s);
				annotationsToAdd.put(annotationMap.get(s), keyPositionMap.get(s));
			}
		}
		//remove annotations that should not be displayed
		ArrayList<Annotation> annotationsToRemove = new ArrayList<Annotation>();
		ArrayList<String> keysToDelete = new ArrayList<String>();
		for (String key : annotationMap.keySet()) {
			if (!keyPositionMap.containsKey(key)) {
				annotationsToRemove.add(annotationMap.get(key));
				keysToDelete.add(key);
			}
		}
		annotationModel.replaceAnnotations(annotationsToRemove.toArray(new Annotation[0]), annotationsToAdd);
		annotationMap.keySet().removeAll(keysToDelete);
	}
	
	/**
	 * Adds a new annotation at a given position p.
	 * @param commentKey The tag key of the comment for which this annotation holds
	 * @param p The position to add the annotation on.
	 */
	public void addAnnotation(String commentKey, Position p) {
		//TODO debug
		((IAnnotationModel) this.annotationModel).addAnnotation(createNewAnnotation(commentKey), p);
	}
	
	/**
	 * Deletes all annotations correlating to the given comment keys
	 * @param commentKeys unique tag keys of the comment annotations which should
	 * be deleted
	 */
	public void deleteAnnotations(List<String> commentKeys) {
		Annotation[] annotationsToRemove = new Annotation[commentKeys.size()];
		for(int i = 0; i < commentKeys.size(); i++) {
			annotationsToRemove[i] = annotationMap.get(commentKeys.get(i));
			annotationMap.remove(commentKeys.get(i));
			annotationsToRemove[i].markDeleted(true);
			//TODO debug
		}
		annotationModel.replaceAnnotations(annotationsToRemove, null);
	}
	
	/**
	 * Creates a new annotation for a given comment key
	 * @param commentKey for which an annotation will be created
	 * @return created annotation
	 */
	private Annotation createNewAnnotation(String commentKey) {
		Annotation annotation = new Annotation("AgileReview.comment.annotation", true, "AgileReview Annotation");
		this.annotationMap.put(commentKey, annotation);
		return annotation;
	}
}
