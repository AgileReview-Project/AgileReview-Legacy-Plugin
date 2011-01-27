package de.tukl.cs.softech.agilereview.dataaccess;


import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;


/**
 * Model which holds all comments and provides some query functions
 */
public class ReviewModel {

	/**
	 * Database object which stores the comments in the following way: ReviewID.author -> CommentID (without leading "c" for correct ordering) (sorted) -> Comment
	 */
	private HashMap<String, HashMap<String, TreeMap<Integer,Comment>>> commentDB = new HashMap<String,HashMap<String, TreeMap<Integer, Comment>>>(); 
	
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
		String key1 = comment.getReviewID();
		String key2 = comment.getAuthor();
		int key3 = this.parseCommentId(comment.getId());
		
		if (commentDB.containsKey(key1)) // review already exists
		{
			HashMap<String, TreeMap<Integer,Comment>> authorMap = commentDB.get(key1);
			if (authorMap.containsKey(key2)) // author already exists
			{
				authorMap.get(key2).put(key3, comment);
			}
			else // author does not exist
			{
				TreeMap<Integer,Comment> tmpTreeMap = new TreeMap<Integer,Comment>();
				tmpTreeMap.put(key3, comment);
					
				authorMap.put(key2, tmpTreeMap);
			}
		}
		else // review did not exist
		{
			TreeMap<Integer,Comment> tmpTreeMap = new TreeMap<Integer,Comment>();
			tmpTreeMap.put(key3, comment);
			
			HashMap<String, TreeMap<Integer,Comment>> tmpAuthorMap = new HashMap<String, TreeMap<Integer,Comment>>();
			tmpAuthorMap.put(key2, tmpTreeMap);
			
			commentDB.put(key1, tmpAuthorMap);
		}
		
//		String key = comment.getReviewID()+"."+comment.getAuthor();
//		// If entry for specified author and review already exists, put the new comment there
//		if (commentDB.containsKey(key))
//		{
//			commentDB.get(key).put(parseCommentId(comment.getId()), comment);
//		}
//		else // create a new entry in the database
//		{
//			TreeMap<Integer,Comment> tmpMap = new TreeMap<Integer,Comment>();
//			tmpMap.put(parseCommentId(comment.getId()), comment);
//			commentDB.put(key, tmpMap);
//		}
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
		TreeMap<Integer,Comment> tmpMap = commentDB.get(reviewId).get(author);
		
		// Remove comment
		tmpMap.remove(parseCommentId(commentId));
		
		// If Map is empty, remove the author-Map
		if (tmpMap.isEmpty())
		{
			commentDB.get(reviewId).remove(author);
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
		Integer result = 0;
		// If an entry does already exist, give back the highest key+1
		if (commentDB.containsKey(reviewId))
		{
			if (commentDB.get(reviewId).containsKey(author))
			{
				result = commentDB.get(reviewId).get(author).lastKey()+1;
			}
			
		}	
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
		return commentDB.get(reviewId).get(author).get(parseCommentId(commentId));
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
			if (ReviewAccess.computePath(c).startsWith(path))
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
		
		HashMap<String, TreeMap<Integer,Comment>> authorMap = commentDB.get(reviewId);
		if (authorMap!=null)
		{
			for (TreeMap<Integer, Comment> x : authorMap.values())
			{
				result.addAll(x.values());
			}
		}
	
//		Pattern p = Pattern.compile(Pattern.quote(reviewId)+"\\..+");
//		for(String key : commentDB.keySet()) 
//		{
//			if (p.matcher(key).matches())
//			{
//				result.addAll(commentDB.get(key).values());
//			}
//		}	
		return result;
	}
	
	/**
	 * Returns all comments, or an empty Collection if no comments exist
	 * @return All comments or an empty Collection if no comments exist
	 */
	public ArrayList<Comment> getAllComments()
	{
		ArrayList<Comment> result = new ArrayList<Comment>();
		
		for (String currReviewId : commentDB.keySet())
		{
			result.addAll(this.getComments(currReviewId));
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
