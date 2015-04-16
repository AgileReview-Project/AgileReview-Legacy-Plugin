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
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		// Initilization of the preferences values
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.AUTHOR_NAME, System.getProperty("user.name"));

		store.setDefault(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, "AgileReviews");
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.SUGGESTIONS_ENABLED, true);
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.CLEANUP_DELETE_COMMENTS, true);
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.CLEANUP_IGNORE_OPEN_COMMENTS, false);

		// initial color value and transferation of the color to the other
		// preference
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR_ENABLED,
				PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.ANNOTATION_COLOR_ENABLED));

		store.setDefault(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR,
				PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.DEFAULT_ANNOTATION_COLOR));

		for (int i = 0; i < PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLORS_AUTHOR.length; i++) {
			store.setDefault(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLORS_AUTHOR[i],
					PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.DEFAULT_ANNOTATION_COLORS_AUTHOR[i]));
		}

		store.setDefault(PropertiesManager.EXTERNAL_KEYS.TEMPLATE_PATH, "");
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.EXPORT_PATH, "");

		// active review
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW, "");

		// language support defaults
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.PARSER_COMMENT_BEGIN_TAG,
				PropertiesManager.getInstance().getInternalProperty(PropertiesManager.EXTERNAL_KEYS.PARSER_COMMENT_BEGIN_TAG));
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.PARSER_COMMENT_END_TAG,
				PropertiesManager.getInstance().getInternalProperty(PropertiesManager.EXTERNAL_KEYS.PARSER_COMMENT_END_TAG));
		store.setDefault(PropertiesManager.EXTERNAL_KEYS.PARSER_FILEENDINGS,
				PropertiesManager.getInstance().getInternalProperty(PropertiesManager.EXTERNAL_KEYS.PARSER_FILEENDINGS));
	}

}