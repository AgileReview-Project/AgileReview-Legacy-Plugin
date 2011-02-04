package de.tukl.cs.softech.agilereview.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class AgileReviewPreferencePage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage{

	/**
	 * Creates the PreferencePage. 
	 */
	public AgileReviewPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("General AgileReview setting:");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		//different fields on the page
		addField(new StringFieldEditor(PropertiesManager.EXTERNAL_KEYS.AUTHOR_NAME, "author:", 
				getFieldEditorParent()));
		
//		addField(new DirectoryFieldEditor(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, 
//				"agileReviews-folder:", getFieldEditorParent()));
//		addField(new StringFieldEditor(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, 
//				"agileReviews-folder:", getFieldEditorParent()));
		
		//colorfieldeditor with listener
		addField(new ColorFieldEditor (PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR, "commentcolor:",
	            getFieldEditorParent()));
		
		addField(new BooleanFieldEditor(PropertiesManager.EXTERNAL_KEYS.SUGGESTIONS_ENABLED, "use smart suggestion",
				getFieldEditorParent()));
	}
	
	@Override
	public void performApply(){
		super.performApply();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		new InstanceScope().getNode("org.eclipse.ui.editors").put("Comment_Annotation", store.getString(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR)); // XXX: This does not work proberly yet 
	}
	
	@Override
	public boolean performOk(){
		boolean result = super.performOk();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		new InstanceScope().getNode("org.eclipse.ui.editors").put("Comment_Annotation", store.getString(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR)); // XXX: This does not work proberly yet
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}	
}