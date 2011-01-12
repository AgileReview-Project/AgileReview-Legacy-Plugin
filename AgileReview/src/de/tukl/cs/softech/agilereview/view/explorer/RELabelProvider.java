package de.tukl.cs.softech.agilereview.view.explorer;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.model.wrapper.AbstractMultipleWrapper;
import de.tukl.cs.softech.agilereview.model.wrapper.MultipleFolderWrapper;
import de.tukl.cs.softech.agilereview.model.wrapper.MultipleProjectWrapper;
import de.tukl.cs.softech.agilereview.model.wrapper.MultipleReviewWrapper;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * The LabelProvider specifies how the nodes of the tree viewer should be displayed
 */
public class RELabelProvider implements ILabelProvider {
	
	@Override
	public void addListener(ILabelProviderListener listener) {/* not implemented */}

	@Override
	public void dispose() {/* not implemented */}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		/* not implemented */
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {/* not implemented */}

	@Override
	public Image getImage(Object element) {
		Image result = PlatformUI.getWorkbench().getEditorRegistry().getImageDescriptor(getText(element)).createImage();
		if ((element instanceof MultipleReviewWrapper))
		{
			MultipleReviewWrapper reviewElement = (MultipleReviewWrapper)element;
			String elementId = (reviewElement).getReviewId();
			
			// A closed review cannot be active
			if (!reviewElement.isOpen())
			{
				result = PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT_CLOSED);
			}
			else if (elementId.equals(PropertiesManager.getInstance().getActiveReview()))
			{
				result = AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.ICONS.REVIEW_OK)).createImage();
			}
		}
		else if(element instanceof MultipleProjectWrapper) {
			result = PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT);
		} else if(element instanceof MultipleFolderWrapper) {
			result = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		} 
		
		return result;
	}

	@Override
	public String getText(Object element)
	{
		String result = "unknown object";
		if(element instanceof MultipleReviewWrapper) {
			MultipleReviewWrapper wrap = (MultipleReviewWrapper)element;
			result = wrap.getReviewId();
			if (wrap.getReviewId().equals(PropertiesManager.getInstance().getActiveReview()))
			{
				result = result + " (active)";
			}
		} else if(element instanceof AbstractMultipleWrapper) {
			result = ((AbstractMultipleWrapper)element).getName();
		}
		return result;
	}

}
