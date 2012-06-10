package de.tukl.cs.softech.agilereview.wizards.export;

import org.eclipse.jface.viewers.LabelProvider;

import agileReview.softech.tukl.de.ReviewDocument.Review;

/**
 * Label Provider for the Export TreeViewer
 */
public class ExportTreeViewLabelProvider extends LabelProvider {
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText(Object element) {
        String result = "";
        if (element instanceof Review) {
            result = ((Review) element).getId();
        }
        return result;
    }
}
