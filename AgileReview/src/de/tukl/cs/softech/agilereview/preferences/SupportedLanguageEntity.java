package de.tukl.cs.softech.agilereview.preferences;

import java.util.LinkedList;
import java.util.List;

public class SupportedLanguageEntity {
	
	private List<String> fileendings = new LinkedList<String>();
	private String beginTag;
	private String endTag;
	
	SupportedLanguageEntity(String fileendings, String beginTag, String endTag) {
		this.fileendings.add(fileendings);
		this.beginTag = beginTag;
		this.endTag = endTag;
	}
	
	public void addFileending(String fileending) {
		fileendings.add(fileending);
	}

	List<String> getFileendings() {
		return fileendings;
	}

	String getBeginTag() {
		return beginTag;
	}

	String getEndTag() {
		return endTag;
	}
}
