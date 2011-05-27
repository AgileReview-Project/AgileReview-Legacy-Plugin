package de.tukl.cs.softech.agilereview.preferences.lang;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link SupportedLanguageEntity} represents a package of file endings which all correspond to the same
 * comment begin and end tag
 */
public class SupportedLanguageEntity {
	
	/**
	 * List of file endings
	 */
	private List<String> fileendings = new LinkedList<String>();
	/**
	 * Comment begin tag
	 */
	private String beginTag;
	/**
	 * Comment end tag
	 */
	private String endTag;
	
	/**
	 * Creates a new {@link SupportedLanguageEntity} while setting begin and end tag to ""
	 */
	SupportedLanguageEntity() {
		this.beginTag = "";
		this.endTag = "";
	}
	
	/**
	 * Creates a new {@link SupportedLanguageEntity} with the given parameter
	 * @param fileending
	 * @param beginTag
	 * @param endTag
	 */
	SupportedLanguageEntity(String fileending, String beginTag, String endTag) {
		this.fileendings.add(fileending);
		this.beginTag = beginTag;
		this.endTag = endTag;
	}
	
	/**
	 * Adds a new file ending to this Entity
	 * @param fileending
	 */
	public void addFileending(String fileending) {
		if(!fileending.isEmpty()) {
			fileendings.add(fileending);
		}
	}

	/**
	 * Returns all file endings in a {@link List}
	 * @return a {@link List} of all file endings of this Entity
	 */
	public List<String> getFileendings() {
		return fileendings;
	}
	
	/**
	 * Returns the file endings converted to a comma separated list as a {@link String}
	 * @return the file endings converted to a comma separated list as a {@link String}
	 */
	String getFileendingsAsString() {
		String result = "";
		boolean first = true;
		for(String s : fileendings) {
			if(first) {
				result += s;
				first = false;
			} else {
				result += ", " + s;
			}
		}
		return result;
	}
	
	/**
	 * Sets the file endings as a comma separated list
	 * @param in a comma separated list of file endings
	 */
	void setFileendingsAsString(String in) {
		in = in.trim();
		if(!in.isEmpty()) {
			String[] endings = in.split(",");
			for(String s : endings) {
				s = s.trim();
			}
			fileendings.clear();
			fileendings.addAll(Arrays.asList(endings));
		} else {
			fileendings.clear();
		}
	}

	/**
	 * Returns the begin tag
	 * @return the begin tag
	 */
	public String getBeginTag() {
		return beginTag;
	}
	
	/**
	 * Sets the begin tag
	 * @param beginTag
	 */
	void setBeginTag(String beginTag) {
		this.beginTag = beginTag;
	}

	/**
	 * Returns the end tag
	 * @return the end tag
	 */
	public String getEndTag() {
		return endTag;
	}
	
	/**
	 * Sets the end tag
	 * @param endTag
	 */
	void setEndTag(String endTag) {
		this.endTag = endTag;
	}
	
	/**
	 * Returns whether this representation is empty
	 * @return true, if this representation is empty<br>false, otherwise
	 */
	boolean isEmpty() {
		if(fileendings.isEmpty() && beginTag.isEmpty() && endTag.isEmpty()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Return whether this Entity is defined valid
	 * @return true, if each begin and end tag is set with arbitrary an arbitrary string not equals empty<br>false, otherwise
	 */
	boolean isValid() {
		if((beginTag.isEmpty() && !endTag.isEmpty()) || (!beginTag.isEmpty() && endTag.isEmpty())) {
			return false;
		}
		return true;
	}
}
