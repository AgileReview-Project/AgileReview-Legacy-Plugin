package de.tukl.cs.softech.agilereview.views.explorer;

import agileReview.softech.tukl.de.ReviewDocument.Review;

import java.util.Collection;
import java.util.HashSet;

import de.tukl.cs.softech.agilereview.views.ReviewExplorer;
import de.tukl.cs.softech.agilereview.views.explorer.wrapper.MultipleReviewWrapper;

/**
 * Wrapper class which acts as root node for the tree viewer of the {@link ReviewExplorer}
 */
public class RERootNode {
	/**
	 * Wrapped {@link Review}s as {@link MultipleReviewWrapper}
	 */
	private HashSet<MultipleReviewWrapper> reviews = new HashSet<MultipleReviewWrapper>();
	
	/**
	 * Constructor doing nothing
	 */
	public RERootNode()
	{
		super();
	}
	
	/**
	 * Constructs an ReviewExplorerRootNode
	 * @param reviews wrapped reviews
	 */
	public RERootNode(Collection<MultipleReviewWrapper> reviews) {
		this();
		this.reviews.addAll(reviews);
	}
	
	/**
	 * Adds a review to the node
	 * @param review Review to be added
	 */
	public void addReview(MultipleReviewWrapper review) {
		this.reviews.add(review);
	}
	
	/**
	 * Deletes a review from the node
	 * @param review Review to be deleted
	 */
	public void deleteReview(MultipleReviewWrapper review)
	{
		this.reviews.remove(review);
	}
	
	/**
	 * Returns the wrapped reviews
	 * @return Wrapped reviews
	 */
	public HashSet<MultipleReviewWrapper> getReviews() {
		return this.reviews;
	}
	
	/**
	 *  Removes all objects from the root node
	 */
	public void clear()
	{
		this.reviews.clear();
	}
}
