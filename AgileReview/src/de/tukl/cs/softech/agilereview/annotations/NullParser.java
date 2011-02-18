package de.tukl.cs.softech.agilereview.annotations;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;

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
	public void filter(ArrayList<Comment> comments) {}

	@Override
	public void addTagsInDocument(Comment comment) throws BadLocationException, CoreException {}

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

}
