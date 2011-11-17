package de.tukl.cs.softech.agilereview.annotations;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Position;

import agileReview.softech.tukl.de.CommentDocument.Comment;

/**
 * Parser, which does nothing and returns null whenever a value is requested
 */
public class NullParser implements IAnnotationParser {
	
	/**
	 * Protected constructor
	 */
	protected NullParser() {
		
	}

	@Override
	public void filter(HashSet<Comment> comments) {}

	@Override
	public void addTagsInDocument(Comment comment, boolean display) throws BadLocationException, CoreException {}

	@Override
	public void removeCommentTags(Comment comment) throws BadLocationException,	CoreException {}

	@Override
	public void removeCommentsTags(Set<Comment> comments) throws BadLocationException, CoreException {}

	@Override
	public void revealCommentLocation(String commentID)	throws BadLocationException {}

	@Override
	public void clearAnnotations() {}

	@Override
	public void reload() {}

	/**
	 * Returns always an empty String array
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#getCommentsByPosition(org.eclipse.jface.text.Position)
	 */
	@Override
	public String[] getCommentsByPosition(Position p) {
		return new String[]{};
	}

	@Override
	public Position getNextCommentsPosition(Position current) {
		return null;
	}
}