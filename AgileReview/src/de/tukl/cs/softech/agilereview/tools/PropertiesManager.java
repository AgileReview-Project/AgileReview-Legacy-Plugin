package de.tukl.cs.softech.agilereview.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.Preferences;


import de.tukl.cs.softech.agilereview.view.DetailView;

/**
 * The PropertiesManager manages the internal configurations (in file: "config/project.properties")
 * as well as the external (workspace-specific) configurations (called preferences)
 */
public class PropertiesManager {
	
	/**
	 * Static class for accessing keys of the internal properties
	 */
	public static class INTERNAL_KEYS
	{
		/**
		 * The plugin id
		 */
		public static String PLUGIN_ID = "plugin_id";
		/**
		 * The source folder where the reviews are stored (path based from Workspace-root)
		 */
		public static String SOURCE_FOLDER = "source_folder";
		/**
		 * Possible values for a comment's priority
		 */
		public static String COMMENT_PRIORITIES = "comment_priority";
		/**
		 * Possible values for a comment's status
		 */
		public static String COMMENT_STATUS = "comment_status";
		/**
		 * Possible values for a review's status
		 */
		public static String REVIEW_STATUS = "review_status";
		/**
		 * Message the display when saving a reply on a comment, without all fields filled out
		 */
		public static String COMMENT_EMPTY_REPLY_MESSAGE = "reply_inf_completeness";
		
		/**
		 * Static subclass: clustering of icon keys
		 */
		public static class ICONS
		{
			/**
			 * Icon for adding a review
			 */
			public static String REVIEW_ADD = "icon_review_add";
			/**
			 * Icon for deleting a review
			 */
			public static String REVIEW_DELETE = "icon_review_delete";
			/**
			 * Icon for activating a review
			 */
			public static String REVIEW_OK = "icon_review_ok";
			/**
			 * Icon for adding a comment
			 */
			public static String COMMENT_ADD = "icon_comment_add";
			/**
			 * Icon for deleting a comment
			 */
			public static String COMMENT_DELETE = "icon_comment_delete";
			/**
			 * Icon "ok" for comments
			 */
			public static String COMMENT_OK = "icon_comment_ok";
			/**
			 * Icon for "syncronize" buttons
			 */
			public static String SYNCED = "icon_synced";
		}
	}
	
	/**
	 * Static class for accessing keys of the external properties (=preferences).
	 * These preferences are stored in the users workspace and are therefore workspace-specific
	 */
	public static class EXTERNAL_KEYS
	{
		/**
		 * List of all open review
		 */
		public static String OPEN_REVIEWS = "openReviews";
		/**
		 * The currently active review
		 */
		public static String ACTIVE_REVIEW = "activeReview";
	}
	
	/**
	 * unique instance of the PropertiesManager
	 */
	private static final PropertiesManager instance = new PropertiesManager();
	/**
	 * path to the internal properties file
	 */
	private static final String internalPropertyFile = "config/project.properties";
	/**
	 * loaded internal properties
	 */
	private Properties internalProperties;
	/**
	 * loaded external properties
	 */
	private Preferences externalPreferences;
	/**
	 * Strings representing the configured status of Comments
	 */
	private String[] commentStates;
	/**
	 * Strings representing the configured priorities of Comments
	 */
	private String[] commentPriorities;
	
	/**
	 * Returns the unique instance of PropertiesManager
	 * @return the unique instance of PropertiesManager
	 */
	public static PropertiesManager getInstance() {
		return instance;
	}
	
	/**
	 * Creates a new instance of PropertiesManager
	 */
	private PropertiesManager() {		
		
		// External preferences
		externalPreferences =  new InstanceScope().getNode("de.tukl.cs.softech.agilereview");		
		// Internal properties
		internalProperties = new Properties();
		InputStream stream = DetailView.class.getClassLoader().getResourceAsStream(internalPropertyFile);
		if(stream != null) {
			try {
				internalProperties.load(stream);
				stream.close();
				String value = internalProperties.getProperty(PropertiesManager.INTERNAL_KEYS.COMMENT_STATUS);
				commentStates = value.split(",");
				value = internalProperties.getProperty(PropertiesManager.INTERNAL_KEYS.COMMENT_PRIORITIES);
				commentPriorities = value.split(",");				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the property according to the given key
	 * @param key for the requested value
	 * @return the requested value //TODO or null??
	 */
	public String getInternalProperty(String key) {
		return internalProperties.getProperty(key);
	}
	
	/**
	 * Returns the value mapped to this key in the workspace-specific preferences of this plugin
	 * @param key key which is mapped to the given value
	 * @return value which is mapped to the given key or an empty String if no mapping is found
	 */
	public String getExternalPreference(String key)
	{
		return externalPreferences.get(key, "");
	}
	/**
	 * Set this key/value pair in the workspace-specific preferences of this plugin
	 * @param key
	 * @param value
	 */
	public void setExternalPreference(String key, String value)
	{
		externalPreferences.put(key, value);
	}
	/**
	 * Adds the given review to the list of open reviews in the workspace-specific preferences of this plugin
	 * @param reviewId Id of the review to be added
	 */
	public void addToOpenReviews(String reviewId)
	{// TODO: using "." as separator is bad. Think of something better
		String strKey = PropertiesManager.EXTERNAL_KEYS.OPEN_REVIEWS;
		String oldValue = getExternalPreference(strKey);
		
		if (oldValue.equals(""))
		{
			// This mapping does not exist
			setExternalPreference(strKey, reviewId);
		}
		else
		{
			// Mapping is existent
			String newValue = oldValue + "." + reviewId;
			setExternalPreference(strKey, newValue);
		}
	}
	
	/**
	 * Removes the given review from the list of open reviews in the workspace-specific preferences of this plugin
	 * @param reviewId Id of the review to be removed
	 */
	public void removeFromOpenReviews(String reviewId)
	{
		String oldValue = getExternalPreference(PropertiesManager.EXTERNAL_KEYS.OPEN_REVIEWS);
		String newValue = oldValue.replaceAll("\\.?"+Pattern.quote(reviewId)+"\\.?", "");
		newValue = newValue.replaceAll("\\.\\.", "\\.");
		setExternalPreference(PropertiesManager.EXTERNAL_KEYS.OPEN_REVIEWS, newValue);
	}
	/**
	 * Returns all reviews which are declared to be "open" in the workspace-specific preferences of this plugin
	 * @return Array of IDs of all review which are "open"
	 */
	public String[] getOpenReviews()
	{
		String[] result;
		String val = getExternalPreference(PropertiesManager.EXTERNAL_KEYS.OPEN_REVIEWS);
		
		if (val.isEmpty())
		{
			result = new String[0];
		}
		else
		{
			result = val.split("\\.");
		}
	
		return result;
	}
	
	/**
	 * Checks if the given review is declared as "open" in the workspace-specific preferences of this plugin
	 * @param reviewId
	 * @return <i>true</i> if review is open, <i>false</i> otherwise.
	 */
	public boolean isReviewOpen(String reviewId)
	{
		String[] arr = this.getOpenReviews();
		return Arrays.asList(arr).contains(reviewId);
	}
	
	/**
	 * Returns a Comment status value defined in the properties
	 * according to its id
	 * @param ID for the requested Comment status
	 * @return the String according to the given ID
	 */
	public String getCommentStatusByID(int ID) {
		String status = "Status not found!";
		if (ID<0 || ID>=this.commentStates.length) {
			throw new RuntimeException(ID+" no valid StatusID!");
		} else {
			status = this.commentStates[ID];
		}
		return status; 
	}

	/**
	 * Returns a Comment priority value defined in the properties
	 * according to its id
	 * @param ID for the requested Comment priority
	 * @return the String according to the given ID
	 */
	public String getCommentPriorityByID(int ID) {
		String prio = "Priority not found!";
		if (ID<0 || ID>=this.commentPriorities.length) {
			throw new RuntimeException(ID+" no valid PrioritiesID!");
		} else {
			prio = this.commentPriorities[ID];
		}
		return prio; 
	}
	
	/**
	 * Saves the given review as "active" review to the workspace-specific preferences
	 * @param reviewId
	 */
	public void setActiveReview(String reviewId)
	{
		this.setExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW, reviewId);
	}
	
	/**
	 * Returns the "active" review from the workspace-specific preferences
	 * @return Id of the currently active review
	 */
	public String getActiveReview()
	{
		return this.getExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
	}
	
}
