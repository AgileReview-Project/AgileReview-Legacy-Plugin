package de.tukl.cs.softech.agilereview.preferences.lang;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A {@link SupportedLanguageEntity} represents a package of file endings which all correspond to the same comment begin and end tag
 */
public class SupportedLanguageEntity {
    
    /**
     * List of file endings
     */
    private final TreeSet<String> fileendings = new TreeSet<String>();
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
     * @param fileendings
     * @param beginTag
     * @param endTag
     */
    public SupportedLanguageEntity(String[] fileendings, String beginTag, String endTag) {
        for (String s : fileendings) {
            addFileending(s);
        }
        this.beginTag = beginTag.trim();
        this.endTag = endTag.trim();
    }
    
    /**
     * Adds a new file ending to this Entity
     * @param fileending
     */
    void addFileending(String fileending) {
        if (!fileending.trim().isEmpty()) {
            fileendings.add(fileending.trim());
        }
    }
    
    /**
     * Adds all passed file endings to this Entity
     * @param fileendings
     */
    public void addFileendings(Set<String> fileendings) {
        for (String s : fileendings) {
            String tmp = s.trim();
            if (!tmp.isEmpty()) {
                this.fileendings.add(tmp);
            }
        }
    }
    
    /**
     * Returns all file endings in a {@link List}
     * @return a {@link List} of all file endings of this Entity
     */
    public Set<String> getFileendings() {
        return fileendings;
    }
    
    /**
     * Returns the file endings converted to a comma separated list as a {@link String}
     * @return the file endings converted to a comma separated list as a {@link String}
     */
    String getFileendingsAsString() {
        String result = "";
        boolean first = true;
        for (String s : fileendings) {
            String tmp = s.trim();
            if (!tmp.isEmpty()) {
                if (first) {
                    result += tmp;
                    first = false;
                } else {
                    result += ", " + tmp;
                }
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
        if (!in.isEmpty()) {
            fileendings.clear();
            String[] endings = in.split(",");
            for (String s : endings) {
                s = s.trim();
                if (!s.isEmpty()) {
                    fileendings.add(s);
                }
            }
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
        this.beginTag = beginTag.trim();
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
        this.endTag = endTag.trim();
    }
    
    /**
     * Returns whether this representation is empty
     * @return true, if this representation is empty<br>false, otherwise
     */
    boolean isEmpty() {
        return fileendings.isEmpty() && beginTag.isEmpty() && endTag.isEmpty();
    }
    
    /**
     * Return whether this Entity is defined valid
     * @return true, if each begin and end tag is set with arbitrary an arbitrary string not equals empty<br>false, otherwise
     */
    boolean isValid() {
        return (!beginTag.isEmpty() && !endTag.isEmpty()) || (beginTag.isEmpty() && endTag.isEmpty() && fileendings.isEmpty());
    }
}