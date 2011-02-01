package de.tukl.cs.softech.agilereview.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IInputValidator;
import org.osgi.service.prefs.Preferences;


import de.tukl.cs.softech.agilereview.views.detail.DetailView;

/**
 * The PropertiesManager manages the internal configurations (in file: "config/project.properties")
 * as well as the external (workspace-specific) configurations (called preferences)
 */
public class PropertiesManager implements IInputValidator{
	
	/**
	 * Static class for accessing keys of the internal properties
	 * Private keys should not be accessed directly but via methods specified in {@link PropertiesManager}
	 */
	public static class INTERNAL_KEYS
	{
		/**
		 * The plugin id
		 */
		public static String PLUGIN_ID = "plugin_id";
		/**
		 * Log level for logging for this session
		 */
		public static String LOG_LEVEL = "log_level";
		/**
		 * Boolean whether logs should also occur in System.out or not
		 */
		public static String LOG_SYSOUT = "log_sysout";
		/**
		 * The source folder where the reviews are stored (path based from Workspace-root)
		 */
		public static String SOURCE_FOLDER = "source_folder";
		/**
		 * The separator character to combine reviewId, author and commentId
		 */
		public static String KEY_SEPARATOR = "key_separator";
		/**
		 * Characters which are not allowed in ReviewIds and author names
		 */
		private static String FORBIDDEN_CHARS = "forbidden_chars";
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
	 * These preferences are stored in the users workspace and are therefore workspace-specific.
	 * Private keys should not be accessed directly but via methods specified in {@link PropertiesManager}
	 */
	public static class EXTERNAL_KEYS
	{
		/**
		 * List of all open review
		 */
		private static String OPEN_REVIEWS = "openReviews";
		/**
		 * The currently active review
		 */
		public static String ACTIVE_REVIEW = "activeReview";
		/**
		 * The currently active review
		 */
		private static String AUTHOR_NAME = "authorName";
		/**
		 * Indicates if explorer and table are linked
		 */
		public static String LINK_EXPLORER = "linkExplorer";
		
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
	 * Pattern for detecting forbidden characters
	 */
	private Pattern forbiddenCharPattern;
	
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
		
		// Set inputValidator regex
		String forbiddenChars = String.valueOf(this.getForbiddenChars());
		String regex = "[^"+Pattern.quote(forbiddenChars)+"]*";
		this.forbiddenCharPattern = Pattern.compile(regex);	
	}
	
	/**
	 * Returns the property according to the given key
	 * @param key for the requested value
	 * @return the requested value or null if no mapping exists
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
	{
		String strKey = PropertiesManager.EXTERNAL_KEYS.OPEN_REVIEWS;
		String oldValue = getExternalPreference(strKey);
		
		if (oldValue.isEmpty())
		{
			// This mapping does not exist
			setExternalPreference(strKey, reviewId);
		}
		else
		{
			// Mapping is existent
			String newValue = oldValue + this.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR) + reviewId;
			setExternalPreference(strKey, newValue);
		}
	}
	
	/**
	 * Removes the given review from the list of open reviews in the workspace-specific preferences of this plugin
	 * @param reviewId Id of the review to be removed
	 */
	public void removeFromOpenReviews(String reviewId)
	{
		String keySeparator = this.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
		String oldValue = getExternalPreference(PropertiesManager.EXTERNAL_KEYS.OPEN_REVIEWS);
		String[] group = oldValue.split(Pattern.quote(keySeparator));
		
		String newValue = "";
		for (String currID :group)
		{
			if (!reviewId.equals(currID))
			{
				if (newValue.isEmpty())
				{
					newValue = currID;
				}
				else
				{
					newValue = newValue + keySeparator + currID;
				}
			}
		}
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
			result = val.split(Pattern.quote(this.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR)));
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
	 * Returns the author name as specified in the preferences or (if no name is specified in the preferences) the System's user name,
	 * @return author name or null, if user name consists forbidden characters
	 */
	public String getAuthor()
	{
		String result = null;
		String authorName = this.getExternalPreference(PropertiesManager.EXTERNAL_KEYS.AUTHOR_NAME);
		String sysName = System.getProperty("user.name"); // TODO: auto-write sysname later to preferences

		if (!authorName.isEmpty() && this.isValid(authorName) == null)
		{
			result = authorName;
		}
		else if (!sysName.isEmpty() && this.isValid(sysName) == null)
		{
			result = sysName;
		}
		
		return result;
	}

	/**
	 * Returns the characters which should not be in all keys (reviewId, author, commentId)
	 * @return all characters which are forbidden
	 */
	public char[] getForbiddenChars()
	{
		String forbiddenChars = this.getInternalProperty(PropertiesManager.INTERNAL_KEYS.FORBIDDEN_CHARS);
		String keySeparator = this.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
		String sequence = forbiddenChars + keySeparator;
		
		return sequence.toCharArray();
	}

	/**
	 *  @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
	 */
	@Override
	public String isValid(String newText) 
	{
		String result = null;
		if (!this.forbiddenCharPattern.matcher(newText).matches())
		{
			result = "Please don't use any of the following characters: "+String.valueOf(this.getForbiddenChars());
		}
		return result;
	}
	
	/**
	 * Returns the log level as an integer
	 * @return the current log level
	 */
	public int getLogLevel() {
		return Integer.parseInt(getInternalProperty(INTERNAL_KEYS.LOG_LEVEL));
	}
	
	/**
	 * Returns the boolean whether logs should also occur in System.out or not
	 * @return true, if the logs should also occur in System.out<br>false, otherwise
	 */
	public boolean getLogSysout() {
		return Boolean.parseBoolean(getInternalProperty(INTERNAL_KEYS.LOG_SYSOUT));
	}
}
