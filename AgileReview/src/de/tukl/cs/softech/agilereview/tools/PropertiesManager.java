package de.tukl.cs.softech.agilereview.tools;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.preference.IPreferenceStore;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.preferences.lang.SupportedLanguageEntity;

/**
 * The PropertiesManager manages the internal configurations (in file: "OSGI-INF/l10n/bundle.properties")
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
		public static String PLUGIN_ID = "plugin.id";
		/**
		 * Log level for logging for this session
		 */
		public static String LOG_LEVEL = "log_level";
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
		 * Default color of standard AgileReview annotations
		 */
		public static String DEFAULT_ANNOTATION_COLOR = "annotations.default.color";
		/**
		 * Default color of AgileReview annotations for author 1
		 */
		public static String[] DEFAULT_ANNOTATION_COLORS_AUTHOR = new String[]{"annotations.default.color.author0",
																			   "annotations.default.color.author1",
																			   "annotations.default.color.author2",
																			   "annotations.default.color.author3",
																			   "annotations.default.color.author4",
																			   "annotations.default.color.author5",
																			   "annotations.default.color.author6",
																			   "annotations.default.color.author7",
																			   "annotations.default.color.author8",
																			   "annotations.default.color.author9"};
		/**
		 * Default color of comments, either if colors are disabled or if too many authors are there
		 */
		public static String DEFAULT_REVIEW_COLOR = "review.default.color";
		/**
		 * Message the display when saving a reply on a comment, without all fields filled out
		 */
		public static String COMMENT_EMPTY_REPLY_MESSAGE = "reply_inf_completeness";
		/**
		 * Filenames which should be omitted during export
		 */
		public static String EXPORT_OMITTINGS = "export_omittings";
		/**
		 * Nature id of the AgileReview source folder
		 */
		public static String AGILEREVIEW_NATURE = "agileReview_nature.id";
		/**
		 * Nature id of the active AgileReview source folder 
		 */
		public static String ACTIVE_AGILEREVIEW_NATURE = "agileReview_active_nature.id";
		/**
		 * The URL directing to the export templates
		 */
		public static String URL_EXAMPLE_EXPORT_TEMPLATES = "url_example_export_templates";
		
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
			/**
			 * Icon for "repositioning" buttons
			 */
			public static String COMMENT_REPOS = "icon_comment_repos";
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
		public static String AUTHOR_NAME = "author";
		/**
		 * The source folder where the reviews are stored (path based from Workspace-root)
		 */
		public static String SOURCE_FOLDER = "source_folder";
		/**
		 * The recipient that was entered the last time
		 */
		public static String LAST_RECIPIENT = "lastRecipient";
		/**
		 * The priority that was entered in the DetailView the last time
		 */
		public static String LAST_PRIORITY = "lastPriority";
		/**
		 * Indicates whether the last recipient and priority are to be preset for new comments
		 */
		public static String SUGGESTIONS_ENABLED = "enableSuggestions";
		/**
		 * The color of the annotations
		 */
		public static String ANNOTATION_COLOR_ENABLED = "enableAnnotationColor";
		/**
		 * The color of the annotations
		 */
		public static String ANNOTATION_COLOR = "annotationColor";
		/**
		 * Default color of AgileReview annotations for author 1
		 */
		public static String[] ANNOTATION_COLORS_AUTHOR = new String[]{"annotationColorAuthor0",
																	   "annotationColorAuthor1",
																	   "annotationColorAuthor2",
																	   "annotationColorAuthor3",
																	   "annotationColorAuthor4",
																	   "annotationColorAuthor5",
																	   "annotationColorAuthor6",
																	   "annotationColorAuthor7",
																	   "annotationColorAuthor8",
																	   "annotationColorAuthor9"};
		/**
		 * The path for the export template
		 */
		public static String TEMPLATE_PATH = "templatePath";
		/**
		 * The default export location
		 */
		public static String EXPORT_PATH = "exportPath";
		/**
		 * States whether the AgileReview Perspective should automatically be opened on certain events
		 */
		public static String AUTO_OPEN_PERSPECTIVE = "autoOpenPerspective";
		/**
		 * File endings supported by the parser
		 */
		public static String PARSER_FILEENDINGS = "parser_fileendings";
		/**
		 * Correlated comment begin tags for every set of file endings representing the same language
		 */
		public static String PARSER_COMMENT_BEGIN_TAG ="parser_comment_begin_tag";
		/**
		 * Correlated comment end tags for every set of file endings representing the same language
		 */
		public static String PARSER_COMMENT_END_TAG = "parser_comment_end_tag";
		
	}
	
	/**
	 * unique instance of the PropertiesManager
	 */
	private static final PropertiesManager instance = new PropertiesManager();
	/**
	 * loaded internal properties
	 */
	private Properties internalProperties;
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
		// Internal properties
		internalProperties = new Properties();
		// TODO: Is there a better way to access the bundle properties?
		String path = "OSGI-INF/l10n/bundle.properties";
		InputStream stream = PropertiesManager.class.getClassLoader().getResourceAsStream(path);
		// InputStream stream = new FileInputStream("OSGI-INF"+System.getProperty("file.separator")+"l10n"+System.getProperty("file.separator")+"bundle.properties");
		// InputStream stream = new FileInputStream(internalPropertyFile);
		if (stream != null)
		{
			try {
				internalProperties.load(stream);
				stream.close();
				String value = internalProperties.getProperty(PropertiesManager.INTERNAL_KEYS.COMMENT_STATUS);
				commentStates = value.split(",");
				value = internalProperties.getProperty(PropertiesManager.INTERNAL_KEYS.COMMENT_PRIORITIES);
				commentPriorities = value.split(",");				
			} catch (IOException e) {
				PluginLogger.logError(this.getClass().toString(), "constructor", "IOException occurs while loading internal properties from file", e);
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
	 * Returns the PreferencesStore to store/retrieve the workspace-specific preferences of this plugin
	 * @return PreferencesStore of this plugin
	 */
	public static IPreferenceStore getPreferences() {
		return Activator.getDefault().getPreferenceStore();
	}
	
	/**
	 * Adds the given review to the list of open reviews in the workspace-specific preferences of this plugin
	 * @param reviewId Id of the review to be added
	 */
	public void addToOpenReviews(String reviewId)
	{
		String strKey = PropertiesManager.EXTERNAL_KEYS.OPEN_REVIEWS;
		String oldValue = getPreferences().getString(strKey);
		
		if (oldValue.isEmpty())
		{
			// This mapping does not exist
			getPreferences().setValue(strKey, reviewId);
		}
		else
		{
			// Mapping is existent
			String newValue = oldValue + this.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR) + reviewId;
			getPreferences().setValue(strKey, newValue);
		}
	}
	
	/**
	 * Removes the given review from the list of open reviews in the workspace-specific preferences of this plugin
	 * @param reviewId Id of the review to be removed
	 */
	public void removeFromOpenReviews(String reviewId)
	{
		String keySeparator = this.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
		String oldValue = getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.OPEN_REVIEWS);
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
		getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.OPEN_REVIEWS, newValue);
	}
	
	/**
	 * Returns all reviews which are declared to be "open" in the workspace-specific preferences of this plugin
	 * @return Array of IDs of all review which are "open"
	 */
	public String[] getOpenReviews()
	{
		String[] result;
		String val = getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.OPEN_REVIEWS);
		
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
		if (newText.isEmpty()){
			result = "Value must not be empty";
		}
		if (!this.forbiddenCharPattern.matcher(newText).matches())
		{
			result = "Value must not contain any of the following characters: "+String.valueOf(this.getForbiddenChars());
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
	 * Returns a map of all supported file endings with the correlated comment tags (first begin, then end tag)
	 * @return map of all supported file endings with the correlated comment tags
	 */
	public HashMap<String, String[]> getParserFileendingsMappingTags() {
		HashMap<String, String[]> result = new HashMap<String, String[]>();
		String[] languages = getInternalProperty(EXTERNAL_KEYS.PARSER_FILEENDINGS).split(",");
		String[] beginTags = getInternalProperty(EXTERNAL_KEYS.PARSER_COMMENT_BEGIN_TAG).split(",");
		String[] endTags = getInternalProperty(EXTERNAL_KEYS.PARSER_COMMENT_END_TAG).split(",");
		
		for(int i = 0; i < languages.length; i++) {
		 	String[] endings = languages[i].split("\\s");
		 	for(String e : endings) {
		 		result.put(e, new String[]{beginTags[i], endTags[i]});
		 	}
		}
		return result;
	}
	
	/**
	 * Returns an array of entities containing file endings, begin and end tag
	 * @return an array of entities containing file endings, begin and end tag
	 */
	public SupportedLanguageEntity[] getParserFileendingsAndTagsAsEntity() {
		String[] languages = getPreferences().getString(EXTERNAL_KEYS.PARSER_FILEENDINGS).split(",");
		String[] beginTags = getPreferences().getString(EXTERNAL_KEYS.PARSER_COMMENT_BEGIN_TAG).split(",");
		String[] endTags = getPreferences().getString(EXTERNAL_KEYS.PARSER_COMMENT_END_TAG).split(",");
		
		//get max length for result array
		int[] lengths = new int[]{languages.length, beginTags.length, endTags.length};
		Arrays.sort(lengths);
		SupportedLanguageEntity[] result = new SupportedLanguageEntity[lengths[0]];
		
		//create result
		for(int i = 0; i < lengths[0]; i++) {
			boolean b = false, e = false;
			if(i < beginTags.length) b = true;
			if(i < endTags.length) e = true;
			
			if(i < languages.length) {
				String[] endings = languages[i].split("\\s+");
				
				
				result[i] = new SupportedLanguageEntity(endings, b ? beginTags[i] : "", e ? endTags[i] : "");
			} else {
				result[i] = new SupportedLanguageEntity(new String[0], b ? beginTags[i] : "", e ? endTags[i] : "");
			}
		}
		
		return result;
	}
	
	/**
	 * Saves the new configuration for supported languages in the preferences.
	 * Beforehand all double entities will be removed.
	 * @param newConfig
	 */
	public void setParserFileendingsAndTags(SupportedLanguageEntity[] newConfig) {
		//clean up input
		//search for duplicated entities
		LinkedList<SupportedLanguageEntity> cleaned = new LinkedList<SupportedLanguageEntity>();
		for(SupportedLanguageEntity e1 : newConfig) {
			boolean contained = false;
			SupportedLanguageEntity toAddTo = null;
			//search for entries having the same begin and end tag
			for(SupportedLanguageEntity e2 : cleaned) {
				if(e2.getBeginTag().equals(e1.getBeginTag()) && e2.getEndTag().equals(e1.getEndTag())) {
					contained = true;
					toAddTo = e2;
					break;
				}
			}
			//apply search result
			if(!contained) {
				cleaned.add(e1);
			} else if(toAddTo != null) {
				toAddTo.addFileendings(e1.getFileendings());
			}
		}
		
		//create storable strings
		String fileendings = "";
		String beginTags = "";
		String endTags = "";
		boolean first = true;
		
		for(SupportedLanguageEntity e : cleaned) {
			if(first) {
				beginTags = e.getBeginTag();
				endTags = e.getEndTag();
				first = false; 
			} else {
				fileendings += ",";
				beginTags += "," + e.getBeginTag();
				endTags += "," + e.getEndTag();
			}
			boolean firstSpace = true;
			for(String s : e.getFileendings()) {
				if(firstSpace) {
					fileendings += s;
					firstSpace = false;
				} else {
					fileendings += " " + s;
				}
			}
		}
		
		getPreferences().setValue(EXTERNAL_KEYS.PARSER_FILEENDINGS, fileendings);
		getPreferences().setValue(EXTERNAL_KEYS.PARSER_COMMENT_BEGIN_TAG, beginTags);
		getPreferences().setValue(EXTERNAL_KEYS.PARSER_COMMENT_END_TAG, endTags);
	}
	
	/**
	 * Sets the template and export path for the export dialog
	 * @param template
	 * @param output
	 */
	public void setDefaultExportPaths(String template, String output) {
		getPreferences().setValue(EXTERNAL_KEYS.EXPORT_PATH, output);
		getPreferences().setValue(EXTERNAL_KEYS.TEMPLATE_PATH, template);
	}
}