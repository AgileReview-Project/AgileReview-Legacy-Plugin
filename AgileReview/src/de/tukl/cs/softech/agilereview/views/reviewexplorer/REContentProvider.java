package de.tukl.cs.softech.agilereview.views.reviewexplorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import agileReview.softech.tukl.de.FileDocument.File;
import agileReview.softech.tukl.de.FolderDocument.Folder;
import agileReview.softech.tukl.de.ProjectDocument.Project;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.AbstractMultipleWrapper;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleFileWrapper;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleFolderWrapper;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleProjectWrapper;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * The ReviewExplorerContentProvider provides the content for the tree viewer of the {@link ReviewExplorer} 
 */
class REContentProvider implements ITreeContentProvider {
	
//	/**
//	 * The tree viewer to whom the content is provided
//	 */
//	private TreeViewer viewer;
	
	
	/**
	 * Wraps the objects given in <i>input</i> 
	 * @param input Array of {@link Project}, {@link Folder} and {@link File}
	 * @param reviewId The review id of the corresponding review
	 * @return Array of {@link MultipleProjectWrapper}, {@link MultipleFolderWrapper} and {@link MultipleFileWrapper}
	 */
	private Object[] wrapObjects(Object[] input, String reviewId)
	{
		ArrayList<Object> listResult = new ArrayList<Object>();
		HashMap<String, MultipleProjectWrapper> projectListResult = new HashMap<String, MultipleProjectWrapper>();
		HashMap<String, MultipleFolderWrapper> folderListResult = new HashMap<String, MultipleFolderWrapper>();
		HashMap<String, MultipleFileWrapper> fileListResult = new HashMap<String, MultipleFileWrapper>();
		
		for (Object o :input)
		{
			if (o instanceof Project)
			{
				Project currProject = (Project)o;
				// Try if a project-wrapper for this folder already exists
				MultipleProjectWrapper pWrap = new MultipleProjectWrapper(currProject, reviewId);
				if (projectListResult.containsValue(pWrap))
				{
					projectListResult.get(pWrap.getName()).add(currProject);
				}
				else
				{
					projectListResult.put(pWrap.getName(), pWrap);
				}
			}
			else if (o instanceof Folder)
			{
				Folder currFolder = (Folder)o;
				// Try if a folder-wrapper for this folder already exists
				MultipleFolderWrapper fWrap = new MultipleFolderWrapper(currFolder, reviewId);
				if (folderListResult.containsValue(fWrap))
				{
					folderListResult.get(fWrap.getName()).add(currFolder);
				}
				else
				{
					folderListResult.put(fWrap.getName(), fWrap);
				}
			}
			else if (o instanceof File)
			{
				File currFile = (File)o;
				// Try if a file-wrapper for this folder already exists
				MultipleFileWrapper fWrap = new MultipleFileWrapper(currFile, reviewId);
				if (fileListResult.containsValue(fWrap))
				{
					fileListResult.get(fWrap.getName()).add(currFile);
				}
				else
				{
					fileListResult.put(fWrap.getName(), fWrap);
				}
			}
		}
		
		// Add all found items in following order : projects, folder, file
		listResult.addAll(projectListResult.values());
		listResult.addAll(folderListResult.values());
		listResult.addAll(fileListResult.values());
		
		return listResult.toArray();
	}
	
	@Override
	public void dispose() {/* do nothing */	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// this.viewer = (TreeViewer)viewer;
	}
	
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) 
	{
		Object[] result = new Object[0];
		// Special case: the root node
		if(parentElement instanceof RERootNode) 
		{
			TreeSet<MultipleReviewWrapper> wrapSet = ((RERootNode)parentElement).getReviews();
			result = wrapSet.toArray();
		} 
		// else wrap the children
		else if (parentElement instanceof AbstractMultipleWrapper)
		{
			Object[] children = ((AbstractMultipleWrapper)parentElement).getChildren();
			result = wrapObjects(children, ((AbstractMultipleWrapper)parentElement).getReviewId());
		}
		return result;
	}
		
	@Override
	public Object getParent(Object element) {
		// not implemented
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return (getChildren(element).length > 0 ? true : false);
	}
}
