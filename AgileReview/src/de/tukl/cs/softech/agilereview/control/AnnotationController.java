package de.tukl.cs.softech.agilereview.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.model.AgileAnnotationModel;

/**
 * @author reuter
 * Class to control the table view
 *
 */
public class AnnotationController {
	
	/**
	 * The single instance of this class
	 */
	private static AnnotationController instance = new AnnotationController();
	/**
	 * A map of the paths of the editors which are currently opened and their annotation model.
	 */
	private HashMap<String, AgileAnnotationModel> annotationModelMap = new HashMap<String, AgileAnnotationModel>();
	
	/**
	 * @return The single instance of this class
	 */
	public static AnnotationController getInstance() {
		return instance;
	}
	
	/**
	 * @param editor The editor for which to return the relative file path
	 * @return the relative file path
	 */
	private String getEditorPath(IEditorPart editor) {
		IEditorPart currentEditor = editor;
		FileEditorInput input = (FileEditorInput) currentEditor.getEditorInput(); 
		String editorPath = ((FileEditorInput)input).getFile().getLocation().toOSString();
		return editorPath;
	}
	
	/**
	 * @param comment The comment for which the position will be returned.
	 * @return The position of the comment
	 */
	private Position getPosition(Comment comment) {
		return new Position(comment.getReference().getOffset(), comment.getReference().getLength());
	}

	/**
	 * Adds a new {@link Annotation} for each given {@link Comment} to the {@link ITextEditor} if it belongs to the file opened in the given {@link ITextEditor}
	 * @param editor the {@link ITextEditor} for which to add {@link Annotation}s
	 * @param comments the {@link Comment}s for which to show the new {@link Annotation}s
	 */
	public void addAnnotations(ITextEditor editor, ArrayList<Comment> comments) {
		// add annotations for given comments
		for (Comment comment : comments) {
			addAnnotation(editor, comment);
		}
	}
	
	/**
	 * Adds a new {@link Annotation} for the given {@link Comment} to the {@link ITextEditor} if it belongs to the file opened in the given {@link ITextEditor} 
	 * @param editor the {@link ITextEditor} for which to add an {@link Annotation}
	 * @param comment the {@link Comment} for which to show the new {@link Annotation}
	 */
	public void addAnnotation(ITextEditor editor, Comment comment) {
		// add annotation, check if comment really belongs to the given editor
		/*//TODO BUG!!!! for example: the relative paths /src/main/blubb.java and
		 *  /main/blubb.java will be the same!!! */
		if (getEditorPath(editor).matches(".*"+Pattern.quote(comment.getPath()))) {
			// set annotation model; if there's none, create one
			if (!this.annotationModelMap.containsKey(comment.getPath())) {
				this.annotationModelMap.put(comment.getPath(), new AgileAnnotationModel(editor));
			}
			AgileAnnotationModel annotationModel = this.annotationModelMap.get(comment.getPath());
			// add annotation
			annotationModel.addAnnotation(getPosition(comment));
		}
	}
	
	
	/**
	 * @param comment comment the {@link Comment} for which to remove the {@link Annotation}
	 */
	public void removeAnnotation(Comment comment) {
		if (this.annotationModelMap.containsKey(comment.getPath())) {
			AgileAnnotationModel annotationModel = this.annotationModelMap.get(comment.getPath());
			annotationModel.deleteAnnotation(getPosition(comment));
		}
	}
	
	/**
	 * @param editor the current editor who's annotations will be hidden
	 */
	public void removeAnnotations(ITextEditor editor) {
		AgileAnnotationModel annotationModel = null;
		for (String key : annotationModelMap.keySet()) {
			if (getEditorPath(editor).matches(".*"+Pattern.quote(key))) {
				annotationModel = annotationModelMap.get(key);
				break;
			}
		}
		if (annotationModel != null) {
			annotationModel.deleteAnnoations();
		}
	}
	
	/**
	 * @param editor the editor who's annotations are to be refreshed
	 * @param comments the comments who's annotations are to be refreshed
	 */
	public void refreshAnnotations(ITextEditor editor, ArrayList<Comment> comments) {
		removeAnnotations(editor);
		addAnnotations(editor, comments);
	}

}
