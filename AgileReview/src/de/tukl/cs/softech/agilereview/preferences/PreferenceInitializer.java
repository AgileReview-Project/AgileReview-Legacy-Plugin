package de.tukl.cs.softech.agilereview.preferences;


import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		//Initilization of the preferences values
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.AUTHOR_NAME,
				System.getProperty("user.name"));
		
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER,
				"AgileReviews"); 		
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.SUGGESTIONS_ENABLED, true);
		
		//initial color value and transferation of the color to the other preference
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR,
				PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.DEFAULT_ANNOTATION_COLOR));
		
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.TEMPLATE_PATH, "");
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.EXPORT_PATH, "");
		
		// active review
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW, "");
	}

}
