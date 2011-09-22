package de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper;

import agileReview.softech.tukl.de.FilesDocument.Files;
import agileReview.softech.tukl.de.FolderDocument.Folder;
import agileReview.softech.tukl.de.ProjectDocument.Project;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * Special implementation of {@link AbstractMultipleWrapper} for <folder> nodes
 */
public class MultipleFolderWrapper extends AbstractMultipleWrapper
{
	/**
	 * Constructor, creating a new MultipleFolderWrapper based on the given parameters
	 * @param initalElement a <folder> node this wrapper should represent
	 * @param reviewId Review this wrapper belongs to
	 */
	public MultipleFolderWrapper(Folder initalElement, String reviewId) {
		super(initalElement, reviewId);
	}
	
	@Override
	public Object[] getChildren()
	{
		ArrayList<Object> result = new ArrayList<Object>();
		for (XmlObject o: super.internalList)
		{
			Folder f = (Folder)o;
			
			result.addAll(Arrays.asList(f.getFolderArray()));
			result.addAll(Arrays.asList(f.getFileArray()));
		}
		
		return result.toArray();
	}
	
//	public boolean belongsHere(Folder f)
//	{
//		return this.path.equals(createPath(f));
//	}
	
	@Override
	protected String createPath(XmlObject obj)
	{
		Folder f = (Folder)obj;
		String path = f.getName();
		XmlCursor c = f.newCursor();
		while(c.toParent() && !(c.getObject() instanceof Files))
		{
			XmlObject o = c.getObject(); 
			if (o instanceof Folder)
			{
				path = ((Folder)o).getName()+System.getProperty("file.separator")+path;
			} 
			else if (o instanceof Project)
			{
				path = ((Project)o).getName()+System.getProperty("file.separator")+path;
			}
		}
		c.dispose();
		
		return path;
	}

}