package de.tukl.cs.softech.agilereview.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * Programmatic decorator, to be able to decorate based on a preference (not possible with declarative)
 * @author Thilo Rauch
 */
public class AgileReviewActiveSourceDecorator extends
		LabelProvider implements ILightweightLabelDecorator {

	/**
	 * Green check mark for indicating the active review source folder
	 */
	private static final ImageDescriptor ACTIVE_SOURCE_ICON;
	
	static {  
		ACTIVE_SOURCE_ICON = AbstractUIPlugin.imageDescriptorFromPlugin("de.tukl.cs.softech.agilereview", "icons/agile_active_icon.png");   
		}  
	
	@Override
	public void decorate(Object element, IDecoration decoration) {
		if (element instanceof IProject) {
			String projectName = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER);
			if (projectName.equals(((IProject) element).getName())) {
				// decoration.addSuffix(" [activated]");
				decoration.addOverlay(ACTIVE_SOURCE_ICON, IDecoration.BOTTOM_RIGHT);  
			}
		}
	}
}
