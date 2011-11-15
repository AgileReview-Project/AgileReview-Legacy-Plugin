package de.tukl.cs.softech.agilereview.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.annotations.ColorManager;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;

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

public class AgileReviewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * Textfield for author
	 */
	private StringFieldEditor strAuthorField;
	/**
	 * Combobox for selecting the AgileReview source folder which should be used
	 */
	private ComboFieldEditor comboReviewProjectField;
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
		setDescription("General AgileReview settings");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
	
		// Field for author
		strAuthorField = new StringFieldEditor(PropertiesManager.EXTERNAL_KEYS.AUTHOR_NAME, "author:", getFieldEditorParent()) {		
			@Override
			protected boolean doCheckState() {
				String isValidReply = PropertiesManager.getInstance().isValid(this.getStringValue());
				this.setErrorMessage(isValidReply);
				return (isValidReply == null);
			}		
		};
		strAuthorField.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
		addField(strAuthorField);
		
		// Field for AgileReview-folder
		List<String> list = new ArrayList<String>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projArr = workspaceRoot.getProjects();
		for (IProject currProj : projArr) {
			try {
				if (currProj.hasNature(PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.AGILEREVIEW_NATURE))) {
					list.add(currProj.getName());
				}
			} catch (CoreException e) {
				// Is thrown, if currProj is closed or  does not exist -> ignore it
			}
		}
		
		String[][] vals = new String[list.size()][2];
		for (int i=0;i<list.size();i++){
			vals[i][0] = list.get(i);
			vals[i][1] = list.get(i);
		}
		comboReviewProjectField = new ComboFieldEditor(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, "review source project:", vals,getFieldEditorParent());
		addField(comboReviewProjectField);
		
		// Checkbox for using Smart Suggestion
		booleanSmartSuggestionsField = new BooleanFieldEditor(PropertiesManager.EXTERNAL_KEYS.SUGGESTIONS_ENABLED, "use smart suggestion", getFieldEditorParent());
		addField(booleanSmartSuggestionsField);
		
		// Directory Browser for export folder
		directoryExportField = new DirectoryFieldEditor(PropertiesManager.EXTERNAL_KEYS.EXPORT_PATH, "Default XLS export location:", getFieldEditorParent());
		addField(directoryExportField);
		
		// export template file
		fileExportTemplateField = new FileFieldEditor(PropertiesManager.EXTERNAL_KEYS.TEMPLATE_PATH, "Default template for XLS export:", getFieldEditorParent());
		fileExportTemplateField.setFileExtensions(new String[]{"*.xls*"});
		addField(fileExportTemplateField);
		
		// link text
		addField(new LinkField(getFieldEditorParent()));
	}

	
	// performApply() simply calls performOk (by default). As we need no additional behavior, we don't have to override it
	
	@Override
	public boolean performOk(){
		boolean result = super.performOk();
		
		//change IDE user for color management/*?|r59|Malte|c4|*/
		ColorManager.changeIDEUser(strAuthorField.getStringValue());/*|r59|Malte|c4|?*/
		
		//refresh views
		if (ReviewAccess.getInstance().updateReviewSourceProject()) {
			ViewControl.refreshViews(ViewControl.ALL_VIEWS, true);
		} else {
			ViewControl.refreshViews(ViewControl.DETAIL_VIEW);
		}
		
		return result;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}	
}