package de.tukl.cs.softech.agilereview.view.explorer;

import org.eclipse.jface.viewers.ViewerComparator;

import de.tukl.cs.softech.agilereview.model.wrapper.MultipleReviewWrapper;

/**
 * Comparator which determines the ordering of elements in the Review Explorer.
 * Reviews are firstly ordered by category (category=1 if review is closed, category=0 otherwise) 
 * and then based on their names, all other elements are directly ordered by their names
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
}
