package de.tukl.cs.softech.agilereview.annotations;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.text.BadLocationException;
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
	public void filter(HashSet<Comment> comments);
	
	/**
	 * Adds the Comment tags for the given comment in the currently opened file at the currently selected place
	 * @param comment Comment for which the tags should be inserted
	 * @param display if true, the new comment will instantly displayed<br>false, otherwise
	 * @throws BadLocationException Thrown if the selected location is not in the document (Should theoretically never happen)
	 */
	public void addTagsInDocument(Comment comment, boolean display) throws BadLocationException;
	
	/**
	 * Removes the tags for one comment. Attention: if you want to delete more then one {@link Comment} in a row
	 * use the {@code removeCommentsTags(Set<Comment> comments} function, because after every deletion the document
	 * will be reparsed
	 * @param comment which should be deleted
	 * @throws BadLocationException if the {@link Position} is corrupted (the document should be reparsed then)
	 */
	public void removeCommentTags(Comment comment) throws BadLocationException;
	
	/**
	 * Removes all tags of the given comments. After this is done the document will be reparsed
	 * @param comments which should be deleted
	 * @throws BadLocationException if the {@link Position} is corrupted (the document should be reparsed then)
	 */
	public void removeCommentsTags(Set<Comment> comments) throws BadLocationException;
	
	/**
	 * Jumps to the first line of the given comment
	 * @param commentID of the displayed comment
	 * @throws BadLocationException if no tags for this commentID exists
	 */
	public void revealCommentLocation(String commentID) throws BadLocationException;
	
	/**
	 * deletes all current displayed Annotations
	 */
	public void clearAnnotations();
	
	/**
	 * Parses the document another time
	 */
	public void reload();
	
	/**
	 * Returns all comments which are overlapping with the given {@link Position}
	 * @param p position
	 * @return all comments which are overlapping with the given {@link Position}
	 */
	public String[] getCommentsByPosition(Position p);
	
	/**
	 * Relocates the comment passed to the current selection within the same file
	 * @param comment comment which should be relocated
	 * @param display defines whether the comment should currently be displayed or not
	 * @throws BadLocationException if no tags for the given comment exists
	 */
	public void relocateComment(Comment comment, boolean display) throws BadLocationException;
}