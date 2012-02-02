package de.tukl.cs.softech.agilereview.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.preferences.fieldEditors.AuthorColorFieldEditor;
import de.tukl.cs.softech.agilereview.preferences.fieldEditors.EnableContainerFieldEditor;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;

/**
 * Preference page for controlling the color management of comments
 */
public class CommentColorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {/*?|r110|Malte|c8|?*/

	/**
	 * Creates the preferences page for the comment color settings.
	 */
	public CommentColorPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("AgileReview comment color settings");
	}
	
	@Override
	public void init(IWorkbench workbench) {}

	@Override
	protected void createFieldEditors() {
		ColorFieldEditor colorAnnotationField = new ColorFieldEditor(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR, "Default Comment Color:", getFieldEditorParent());
		addField(colorAnnotationField);
		
		EnableContainerFieldEditor containerField = new EnableContainerFieldEditor(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR_ENABLED, "Enable multi-color comments", "Color Settings", getFieldEditorParent());
		// colorfieldeditor for annotation color of IDE user
		AuthorColorFieldEditor authorColorAnnotationField = new AuthorColorFieldEditor(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLORS_AUTHOR[0], "Comment color (IDE User):", 0, containerField.getContainer());
		containerField.addField(authorColorAnnotationField);
		
		// colorfieldeditors for other customizable annotations-colors
		for (int i = 1; i < PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLORS_AUTHOR.length; i++) {
			authorColorAnnotationField = new AuthorColorFieldEditor(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLORS_AUTHOR[i], "Comment color (Author "+(i+1)+"):", i, containerField.getContainer());
			containerField.addField(authorColorAnnotationField);
		}
		
		addField(containerField);
	}
	
	
	@Override
	public boolean performOk(){
		boolean result = super.performOk();
		//set changed colors for annotations
		
		// TODO: Use of deprecated method here, as it seems that the new "correct" way is only available since 3.7 (indigo). Will be checked...
		new InstanceScope().getNode("org.eclipse.ui.editors").put("Comment_Annotation", PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR));
		for (int i=0; i<PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLORS_AUTHOR.length; i++) {
			new InstanceScope().getNode("org.eclipse.ui.editors").put("Comment_Annotation_Author"+i, PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLORS_AUTHOR[i]));			
		}
		
		//refresh views
		if (ReviewAccess.getInstance().updateReviewSourceProject()) {
			ViewControl.refreshViews(ViewControl.ALL_VIEWS, true);
		} else {
			ViewControl.refreshViews(ViewControl.DETAIL_VIEW);
		}
		
		return result;
	}

}