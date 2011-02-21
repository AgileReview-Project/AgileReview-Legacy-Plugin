package de.tukl.cs.softech.agilereview.annotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * This class is used to draw and manage annotations for a given text editor
 */
public class AgileAnnotationController {

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
	protected AgileAnnotationController(IEditorPart editor) {
		IEditorInput input = editor.getEditorInput();
		this.annotationModel = (IAnnotationModelExtension) ((ITextEditor)editor).getDocumentProvider().getAnnotationModel(input);
	}
	
	/**
	 * Displays the given positions as annotations in the provided editor. Therefore annotations which should
	 * not be displayed any more will be removed and not yet drawn annotations will be added to the annotation model.
	 * @param keyPositionMap a map of Positions which should be annotated and the comment keys correlated to the positions
	 */
	protected void displayAnnotations(Map<String, Position> keyPositionMap) {
		PluginLogger.log(this.getClass().toString(), "displayAnnotations", "display: "+keyPositionMap.keySet().toString());
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
	protected void addAnnotation(String commentKey, Position p) {
		((IAnnotationModel) this.annotationModel).addAnnotation(createNewAnnotation(commentKey), p);
	}
	
	/**
	 * Deletes all annotations correlating to the given comment keys
	 * @param commentKeys unique tag keys of the comment annotations which should
	 * be deleted
	 */
	protected void deleteAnnotations(Set<String> commentKeys) {
		HashSet<Annotation> annotationsToRemove = new HashSet<Annotation>();
		Annotation a;
		for(String key : commentKeys) {
			a = annotationMap.remove(key);
			if(a != null) {
				a.markDeleted(true);
				annotationsToRemove.add(a);
			}
		}
		annotationModel.replaceAnnotations(annotationsToRemove.toArray(new Annotation[0]), null);
	}
	
	/**
	 * Creates a new annotation for a given comment key
	 * @param commentKey for which an annotation will be created
	 * @return created annotation
	 */
	private Annotation createNewAnnotation(String commentKey) {
		String[] commentData = commentKey.split(Pattern.quote(PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR)));
		Annotation annotation = new Annotation("AgileReview.comment.annotation", true, "Review: "+commentData[0]+", Author: "+commentData[1]+", Comment-ID: "+commentData[2]);
		this.annotationMap.put(commentKey, annotation);
		return annotation;
	}
}
