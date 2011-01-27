package de.tukl.cs.softech.agilereview.views.explorer.wrapper;


import java.util.ArrayList;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;

/**
 * Abstract implementation of a Wrapper which can wrap multiple xml-nodes 
 * which represent the same file path. 
 */
public abstract class AbstractMultipleWrapper
{
	/**
	 * Name of the wrapper (representing the wrapped objects)
	 */
	protected String name;
	/**
	 * Path of the wrapper (representing the wrapped objects)
	 */
	protected String path;
	/**
	 * ReviewId the wrapper belongs to (representing the wrapped objects)
	 */
	protected String reviewId;
	/**
	 * List of all wrapped objects
	 */
	protected ArrayList<XmlObject> internalList = new ArrayList<XmlObject>();
	
	/**
	 * Constructor which creates the wrapper and fills the attributes based on the inital element given
	 * @param initalElement element which should be wrapped
	 * @param reviewId Review this wrapper belongs to
	 */
	public AbstractMultipleWrapper(XmlObject initalElement, String reviewId)
	{
		this.name = initalElement.newCursor().getAttributeText(new QName("name"));
		add(initalElement);
		// Create path
		this.path = createPath(initalElement);
		this.reviewId = reviewId;
	}
	
	/**
	 * Returns the name of the wrapper
	 * @return name of the wrapper
	 */
	public String getName()
	{
		return this.name;
	}
	
	/**
	 * Returns the review id this wrapper belongs to
	 * @return review id this wrapper belongs to
	 */
	public String getReviewId()
	{
		return this.reviewId;
	}
	
	/**
	 * Returns the path of the wrapper (relative to the workspace)
	 * @return path of the wrapper (relative to the workspace)
	 */
	public String getPath()
	{
		return this.path;
	}
		
	/**
	 * Adds an element to this wrapper
	 * @param f element
	 */
	public void add(XmlObject f)
	{
		this.internalList.add(f);
	}
	
	/**
	 * Removes an element from this wrapper
	 * @param f element
	 */
	public void remove(XmlObject f)
	{
		this.internalList.remove(f);
	}
	
	/**
	 * Abstract method returning the children of this wrapper. 
	 * Each subclass of this abstract class has to implement this routine itself.
	 * @return array of all children of this wrapper
	 */
	public abstract Object[] getChildren();
	
	/**
	 * Abstract method for creating the path representing this wrapper.
	 * Each subclass of this abstract class has to implement this routine itself
	 * @param obj object from which the path is derived
	 * @return derived path
	 */
	protected abstract String createPath(XmlObject obj);	
	
	@Override
	public boolean equals(Object o)
	{
		boolean result = false;
		if (o instanceof AbstractMultipleWrapper)
		{
			AbstractMultipleWrapper wrap = (AbstractMultipleWrapper)o;
			boolean bReviewId = this.reviewId.equals(wrap.getReviewId());
			boolean bPath = this.path.equals(wrap.getPath());
			result = bReviewId && bPath;
		}
		return result;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}
}
