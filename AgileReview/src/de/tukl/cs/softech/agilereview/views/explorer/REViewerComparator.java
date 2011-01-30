package de.tukl.cs.softech.agilereview.views.explorer;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.explorer.wrapper.MultipleReviewWrapper;

/**
 * Comparator which determines the ordering of elements in the Review Explorer.
 * Reviews are firstly ordered by category (category=1 if review is closed, category=0 otherwise) 
 * and then based on their names (except for the active review, which is always on top), 
 * all other elements are directly ordered by their names
 */
public class REViewerComparator extends ViewerComparator 
{
	@Override
	public int category(Object element)
	{
		int result = 0;
		// Set category only if element is a ReviewWrapper
		if (element instanceof MultipleReviewWrapper)
		{
			MultipleReviewWrapper reviewWrap = (MultipleReviewWrapper)element;
			// Closed reviews are category 1 -> sorted below open reviews
			if (!reviewWrap.isOpen())
			{
				result = 1;
			}
		}

		return result;
	}
	
	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		int result = super.compare(viewer, e1, e2);
		if (e1 instanceof MultipleReviewWrapper && e2 instanceof MultipleReviewWrapper)
		{
			String activeReview = PropertiesManager.getInstance().getExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
			if (activeReview.equals(((MultipleReviewWrapper)e1).getReviewId()))
			{
				result = -1;
			}
			else if (activeReview.equals(((MultipleReviewWrapper)e2).getReviewId()))
			{
				result = 1;
			}
		}
		
		return result;
	}
}
