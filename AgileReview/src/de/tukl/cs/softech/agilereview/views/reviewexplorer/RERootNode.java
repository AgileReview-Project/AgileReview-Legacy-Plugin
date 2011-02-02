package de.tukl.cs.softech.agilereview.views.reviewexplorer;

import agileReview.softech.tukl.de.ReviewDocument.Review;

import java.util.Collection;
import java.util.HashSet;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * Wrapper class which acts as root node for the tree viewer of the {@link ReviewExplorer}
 */
class RERootNode {
	/**
	 * Wrapped {@link Review}s as {@link MultipleReviewWrapper}
	 */
	private HashSet<MultipleReviewWrapper> reviews = new HashSet<MultipleReviewWrapper>();
	
	/**
	 * Constructor doing nothing
	 */
	protected RERootNode()
	{
		super();
	}
	
	/**
	 * Constructs an ReviewExplorerRootNode
	 * @param reviews wrapped reviews
	 */
	protected RERootNode(Collection<MultipleReviewWrapper> reviews) {
		this();
		this.reviews.addAll(reviews);
	}
	
	/**
	 * Adds a review to the node
	 * @param review Review to be added
	 */
	protected void addReview(MultipleReviewWrapper review) {
		this.reviews.add(review);
	}
	
	/**
	 * Deletes a review from the node
	 * @param review Review to be deleted
	 */
	protected void deleteReview(MultipleReviewWrapper review)
	{
		this.reviews.remove(review);
	}
	
	/**
	 * Returns the wrapped reviews
	 * @return Wrapped reviews
	 */
	protected HashSet<MultipleReviewWrapper> getReviews() {
		return this.reviews;
	}
	
	/**
	 *  Removes all objects from the root node
	 */
	protected void clear()
	{
		PluginLogger.log(this.getClass().toString(), "clear", "ReviewExplorer root node cleared");
		this.reviews.clear();
	}
}
