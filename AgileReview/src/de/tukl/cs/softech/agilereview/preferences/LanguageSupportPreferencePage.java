package de.tukl.cs.softech.agilereview.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.preferences.lang.TableFieldEditor;

/**
 * This Class represents the preferences page for the language support settings
 */
public class LanguageSupportPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * Creates the preferences page for the language support settings.
	 */
	public LanguageSupportPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("AgileReview language support settings");
	}
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	protected void createFieldEditors() {
		addField(new TableFieldEditor(getFieldEditorParent()));
	}
}
