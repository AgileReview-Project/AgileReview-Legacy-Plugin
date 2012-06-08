package de.tukl.cs.softech.agilereview.plugincontrol;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;

/**
 * SourceProvider which provides several states of the DetailView as workbench variables
 */
public class SourceProvider extends AbstractSourceProvider {
	
	/**
	 * Variable for the state "revertable"
	 */
	public static final String REVERTABLE = "de.tukl.cs.softech.agilereview.views.detail.variables.revertable";
	/**
	 * States whether a comment is shown in the detail view
	 */
	public static final String COMMENT_SHOWN = "de.tukl.cs.softech.agilereview.views.detail.variables.commentShown";
	/**
	 * Variable for the state "content available"
	 */
	public static final String CONTENT_AVAILABLE = "de.tukl.cs.softech.agilereview.views.detail.variables.contentAvailable";
	/**
	 * Variable for the state "contains closed review"
	 */
	public static final String CONTAINS_CLOSED_REVIEW = "de.tukl.cs.softech.agilereview.views.export.variables.containsClosedReview";
	/**
	 * Variable for the state "is review active"
	 */
	public static final String IS_ACTIVE_REVIEW = "de.tukl.cs.softech.agilereview.views.reviewexplorer.variables.isActiveReview";
	/**
	 * Map of variable value mappings
	 */
	private HashMap<String, Boolean> map = new HashMap<String, Boolean>();
	
	/**
	 * Creates the SourceProvider and initiates all states with false
	 */
	public SourceProvider() {
		map.put(REVERTABLE, false);
		map.put(COMMENT_SHOWN, false);
		map.put(CONTENT_AVAILABLE, false);
		map.put(CONTAINS_CLOSED_REVIEW, false);
		map.put(IS_ACTIVE_REVIEW, false);
	}

	@Override
	public void dispose() {
		map.clear();
	}

	@Override
	public Map<String, Boolean> getCurrentState() {
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return map.keySet().toArray(new String[0]);
	}
	
	/**
	 * Sets the given variable (use one of the static fields of this class)
	 * @param variable to be set
	 * @param b new value
	 */
	public void setVariable(String variable, boolean b) {
		if(map.get(variable) != b) {
			map.put(variable, b);
			this.fireSourceChanged(0, map);
		}
	}
}