package de.tukl.cs.softech.agilereview.annotations;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;

import agileReview.softech.tukl.de.CommentDocument.Comment;

/**
 * Interface for all annotation parsers
 */
public interface IAnnotationParser {

	/**
	 * Filter annotations and display only the given comments
	 * @param comments which should be displayed
	 */
	public abstract void filter(HashSet<Comment> comments);
	
	/**
	 * Adds the Comment tags for the given comment in the currently opened file at the currently selected place
	 * @param comment Comment for which the tags should be inserted
	 * @param display if true, the new comment will instantly displayed<br>false, otherwise
//	 * @return Position of the added {@link Comment} or null if the selection is no instance of {@link ITextSelection}
	 * @throws BadLocationException Thrown if the selected location is not in the document (Should theoretically never happen)
	 * @throws CoreException 
	 */
	public abstract void addTagsInDocument(Comment comment, boolean display) throws BadLocationException, CoreException;
	
	/**
	 * Removes the tags for one comment. Attention: if you want to delete more then one {@link Comment} in a row
	 * use the {@code removeCommentsTags(Set<Comment> comments} function, because after every deletion the document
	 * will be reparsed
	 * @param comment which should be deleted
	 * @throws BadLocationException if the {@link Position} is corrupted (the document should be reparsed then)
	 * @throws CoreException  if document can not be saved
	 */
	public abstract void removeCommentTags(Comment comment) throws BadLocationException, CoreException;
	
	/**
	 * Removes all tags of the given comments. After this is done the document will be reparsed
	 * @param comments which should be deleted
	 * @throws BadLocationException if the {@link Position} is corrupted (the document should be reparsed then)
	 * @throws CoreException if document can not be saved
	 */
	public abstract void removeCommentsTags(Set<Comment> comments) throws BadLocationException, CoreException;
	
	/**
	 * Jumps to the first line of the given comment
	 * @param commentID of the displayed comment
	 * @throws BadLocationException if no tags for this commentID exists
	 */
	public abstract void revealCommentLocation(String commentID) throws BadLocationException;
	
	/**
	 * deletes all current displayed Annotations
	 */
	public abstract void clearAnnotations();
	
	/**
	 * Parses the document another time
	 */
	public abstract void reload();
	
	/**
	 * Returns all comments which are overlapping with the given {@link Position}
	 * @param p position
	 * @return all comments which are overlapping with the given {@link Position}
	 */
	public String[] getCommentsByPosition(Position p);
}
