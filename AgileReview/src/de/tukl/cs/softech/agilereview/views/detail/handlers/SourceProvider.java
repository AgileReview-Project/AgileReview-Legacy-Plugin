package de.tukl.cs.softech.agilereview.views.detail.handlers;

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
	 * Variable for the state "reply possible"
	 */
	public static final String REPLY_POSSIBLE = "de.tukl.cs.softech.agilereview.views.detail.variables.replyPossible";
	/**
	 * Variable for the state "content available"
	 */
	public static final String CONTENT_AVAILABLE = "de.tukl.cs.softech.agilereview.views.detail.variables.contentAvailable";
	/**
	 * Map of variable value mappings
	 */
	private HashMap<String, Boolean> map = new HashMap<String, Boolean>();
	
	/**
	 * Creates the SourceProvider and initiates all states with false
	 */
	public SourceProvider() {
		map.put(REVERTABLE, false);
		map.put(REPLY_POSSIBLE, false);
		map.put(CONTENT_AVAILABLE, false);
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
	 * Sets the current variable for the state "revertable" of the view
	 * @param b new value
	 */
	public void setRevertable(boolean b) {
		if(map.get(REVERTABLE) != b) {
			map.put(REVERTABLE, b);
			this.fireSourceChanged(0, map);
		}
	}
	
	/**
	 * Sets the current variable for the state "reply possible" of the view
	 * @param b new value
	 */
	public void setReplyPossible(boolean b) {
		if(map.get(REPLY_POSSIBLE) != b) {
			map.put(REPLY_POSSIBLE, b);
			this.fireSourceChanged(0, map);
		}
	}
	
	/**
	 * Sets the current variable for the state "content available" of the view
	 * @param b new value
	 */
	public void setContentAvailable(boolean b) {
		if(map.get(CONTENT_AVAILABLE) != b) {
			map.put(CONTENT_AVAILABLE, b);
			this.fireSourceChanged(0, map);
		}
	}
}
