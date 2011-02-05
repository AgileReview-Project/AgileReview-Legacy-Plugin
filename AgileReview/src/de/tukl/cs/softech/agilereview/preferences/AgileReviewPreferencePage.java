package de.tukl.cs.softech.agilereview.preferences;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

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
	 * Textfield for author
	 */
	private StringFieldEditor strAuthorField;
	/**
	 * ColorChooser for annotations' color
	 */
	private ColorFieldEditor colorAnnotationField;
	/**
	 * Checkbox for using Smart Suggestions
	 */
	private BooleanFieldEditor booleanSmartSuggestionsField;
	/**
	 * Textfield for xls-Template file (including change button)
	 */
	private FileFieldEditor fileExportTemplateField;
	/**
	 * Textfield for xls-export folder (including change button)
	 */
	private DirectoryFieldEditor directoryExportField;

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
		strAuthorField = new StringFieldEditor(PropertiesManager.EXTERNAL_KEYS.AUTHOR_NAME, "author:", 
				getFieldEditorParent())
		{		
			@Override
			protected boolean doCheckState()
			{
				String isValidReply = PropertiesManager.getInstance().isValid(this.getStringValue());
				this.setErrorMessage(isValidReply);
				return (isValidReply == null);
			}		
		};
		strAuthorField.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		addField(strAuthorField);
		
//		addField(new DirectoryFieldEditor(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, 
//				"agileReviews-folder:", getFieldEditorParent()));
//		addField(new StringFieldEditor(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, 
//				"agileReviews-folder:", getFieldEditorParent()));
		
		// colorfieldeditor for annotations-color
		colorAnnotationField = new ColorFieldEditor (PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR, "commentcolor:",
	            getFieldEditorParent());
		addField(colorAnnotationField);
		
		// Checkbox for using Smart Suggestion
		booleanSmartSuggestionsField = new BooleanFieldEditor(PropertiesManager.EXTERNAL_KEYS.SUGGESTIONS_ENABLED, "use smart suggestion",
				getFieldEditorParent());
		addField(booleanSmartSuggestionsField);
		
		// export template file
		fileExportTemplateField = new FileFieldEditor(PropertiesManager.EXTERNAL_KEYS.TEMPLATE_PATH, 
				"Default template for XLS export:", getFieldEditorParent());
		fileExportTemplateField.setFileExtensions(new String[]{"*.xls*"});
		addField(fileExportTemplateField);
		
		// Directory Browser for export folder
		directoryExportField = new DirectoryFieldEditor(PropertiesManager.EXTERNAL_KEYS.EXPORT_PATH, 
				"Default XLS export location:", getFieldEditorParent());
		addField(directoryExportField);
	}

	@Override
	public void performApply(){
		super.performApply();
		new InstanceScope().getNode("org.eclipse.ui.editors").put("Comment_Annotation", PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR));
	}
	
	@Override
	public boolean performOk(){
		boolean result = super.performOk();
		new InstanceScope().getNode("org.eclipse.ui.editors").put("Comment_Annotation", PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR));
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}	
}