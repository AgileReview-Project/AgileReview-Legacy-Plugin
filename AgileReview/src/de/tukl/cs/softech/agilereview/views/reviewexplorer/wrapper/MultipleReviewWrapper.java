package de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper;

import org.apache.xmlbeans.XmlObject;

import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;

/**
 * Special implementation of {@link AbstractMultipleWrapper} for <review> nodes
 * A MultipleReviewWrapper can only have one <review> node!
 */
public class MultipleReviewWrapper extends AbstractMultipleWrapper {

	/**
	 * States whether this review is open (comments are loaded)
	 */
	private boolean open;
	
	/**
	 * Constructor, creating a new MultipleReviewWrapper based on the given parameters
	 * @param initalElement a <review> node this wrapper should represent
	 * @param reviewId Review this wrapper belongs to
	 */
	public MultipleReviewWrapper(Review initalElement, String reviewId) {
		super(initalElement, reviewId);
		this.open = false;
	}
	
	/**
	 * States if this review is open
	 * @return <i>true</i> if this review is open (comments are loaded), <i>false</i> otherwise
	 */
	public boolean isOpen()
	{
		return this.open;
	}
	
	/**
	 * Can set the "open" state of this review (comments loaded)
	 * @param newState new state for the "open" attribute
	 */
	public void setOpen (boolean newState)
	{
		this.open = newState;
	}

	/**
	 * Returns the wrapped review
	 * @return wrapped review
	 */
	public Review getWrappedReview()
	{
		return (Review)this.internalList.get(0);
	}

	@Override
	public Object[] getChildren() 
	{
		return ReviewAccess.getInstance().getProjects(reviewId).toArray();
	}

	@Override
	protected String createPath(XmlObject obj) {
		return "";
	}

}
