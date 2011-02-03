package de.tukl.cs.softech.agilereview.wizard;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import agileReview.softech.tukl.de.ReviewDocument.Review;

public class ExportTreeViewContentProvider implements ITreeContentProvider {

	@Override
	public Object[] getChildren(Object parentElement) {
		return null;
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		return false;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		ArrayList<Review> reviews = (ArrayList<Review>) inputElement;
		ArrayList<String> reviewIDs = new ArrayList<String>(); 
		for (Review review : reviews) {
			reviewIDs.add(review.getId());
		}
		return reviewIDs.toArray();
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// TODO Auto-generated method stub

	}

}
