package de.tukl.cs.softech.agilereview.model;


import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Model which holds all comments and provides some query functions
 */
public class ReviewModel {

	/**
	 * Database object which stores the comments in the following way: ReviewID.author -> CommentID (without leading "c" for correct ordering) (sorted) -> Comment
	 */
	private HashMap<String, TreeMap<Integer,Comment>> commentDB = new HashMap<String,TreeMap<Integer, Comment>>(); 
	
	/**
	 * Review model. Contains all reviews (ReviewId -> Review)
	 */
	private HashMap<String, Review> rModel = new HashMap<String, Review>();
	
	//////////////////
	// Constructors //
	//////////////////

	
	/////////////
	// private //
	/////////////
	/**
	 * Returns the Id of a Comment as an Integer representation
	 * @param c Comment id as string
	 * @return Id without leading character (="c")
	 */
	private Integer parseCommentId(String c)
	{
		String strIntegerId = c.substring(1);
		return new Integer(strIntegerId);
	}
	
	////////////
	// Setter //
	////////////

	/**
	 * Adds a comment to the database
	 * @param comment Comment to be added
	 */
	public void addComment(Comment comment)
	{
		String key = comment.getReviewID()+"."+comment.getAuthor();
		// If entry for specified author and review already exists, put the new comment there
		if (commentDB.containsKey(key))
		{
			commentDB.get(key).put(parseCommentId(comment.getId()), comment);
		}
		else // create a new entry in the database
		{
			TreeMap<Integer,Comment> tmpMap = new TreeMap<Integer,Comment>();
			tmpMap.put(parseCommentId(comment.getId()), comment);
			commentDB.put(key, tmpMap);
		}
	}
	 
	/**
	 * Removes a comment from the database. Returns true, 
	 * if it was the last comment for this review and author.
	 * @param reviewId
	 * @param author
	 * @param commentId
	 * @return <i>true</i> if the deleted comment was the last comment for this review and author, <i>false</i> otherwise
	 */
	public boolean removeComment(String reviewId, String author, String commentId)
	{
		boolean result = false;
		String key = reviewId+"."+author;
		TreeMap<Integer,Comment> tmpMap = commentDB.get(key);
		
		// Remove comment
		tmpMap.remove(parseCommentId(commentId));
		
		// If Map is empty, remove the whole review/author combination from database
		if (tmpMap.isEmpty())
		{
			commentDB.remove(key);
			result = true;
		}
		return result;
	}
	
	/**
	 * Adds a review to this model
	 * @param r
	 * @return <i>false</i> if a review with this name does already exist (review will not be added then). <i>true</i> otherwise.
	 */
	public boolean addReview(Review r)
	{
		boolean result = false;
		if (!rModel.containsKey(r.getId()))
		{
			this.rModel.put(r.getId(), r);
			result = true;
		}
		return result;
	}
	
	/**
	 * Removes the given review from the model
	 * @param reviewId
	 */
	public void removeReview(String reviewId)
	{
		// Remove the review itself
		this.rModel.remove(reviewId);
		
		// Remove the comment associated with this review
		for (Comment c : this.getComments(reviewId))
		{
			this.removeComment(reviewId, c.getAuthor(), c.getId());
		}
	}
	
	/**
	 * Clears the model
	 */
	public void clearModel()
	{
		commentDB.clear();
		rModel.clear();
	}
	
	////////////
	// Getter //
	////////////
	/**
	 * Returns the next free Id for the given reviewId/author pair.
	 * @param reviewId 
	 * @param author
	 * @return next free id
	 */
	public Integer getNextCommentIdFor(String reviewId, String author)
	{
		String key = reviewId+"."+author;
		Integer result = 0;
		// If an entry does already exist, give back the highest key+1
		if (commentDB.containsKey(key))
		{
			result = commentDB.get(key).lastKey()+1;
		}
		// else return 0 (=do nothing)
		
		return result;
	}
	
	/**
	 * Returns the comment specified by the given combination
	 * @param reviewId
	 * @param author
	 * @param commentId
	 * @return Comment specified by the given combination
	 */
	public Comment getComment(String reviewId, String author, String commentId)
	{
		return commentDB.get(reviewId+"."+author).get(parseCommentId(commentId));
	}
	
	/**
	 * Returns all Comments for the given reviewId/path combination. 
	 * If path is a folder, all comments in all sub-folders are returned.
	 * @param reviewId
	 * @param path 
	 * @return All Comments as Collection or an empty Collection, if no Comments exist
	 */
	public ArrayList<Comment> getComments(String reviewId, String path)
	{
		ArrayList<Comment> result = new ArrayList<Comment>();
		
		ArrayList<Comment> allForReviewId = getComments(reviewId);
		for (Comment c : allForReviewId)
		{
			if (path.startsWith(c.getPath()))
			{
				result.add(c);
			}
		}	
		return result;
	}

	
	/**
	 * Returns all Comments of the given review
	 * @param reviewId
	 * @return Returns all Comments as Collection or an empty Collection, if no Comments exist
	 */
	public ArrayList<Comment> getComments(String reviewId)
	{
		ArrayList<Comment> result = new ArrayList<Comment>();
		Pattern p = Pattern.compile(Pattern.quote(reviewId)+"\\..+");
		for(String key : commentDB.keySet()) 
		{
			if (p.matcher(key).matches())
			{
				result.addAll(commentDB.get(key).values());
			}
		}	
		return result;
	}
	
	/**
	 * Returns all comments, or an empty Collection if no comments exist
	 * @return All comments or an empty Collection if no comments exist
	 */
	public ArrayList<Comment> getAllComments()
	{
		ArrayList<Comment> result = new ArrayList<Comment>();
		
		Iterator<TreeMap<Integer,Comment>> it = commentDB.values().iterator();
		while (it.hasNext())
		{
			result.addAll(it.next().values());
		}
		
		return result;
	}
	
	/**
	 * Checks whether the given reviewId is stored in the model
	 * @param reviewId
	 * @return <i>true</i> if the model contains such a reviewId, <i>false</i> otherwise.
	 */
	public boolean containsReview(String reviewId)
	{
		return this.rModel.containsKey(reviewId);
	}
	
	/**
	 * Returns all reviews being stored in the model
	 * @return All Reviews stored in this model
	 */
	public ArrayList<Review> getAllReviews()
	{
		ArrayList<Review> result = new ArrayList<Review>();
		result.addAll(rModel.values());
		return result;
	}
}
