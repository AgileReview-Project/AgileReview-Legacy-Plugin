package de.tukl.cs.softech.agilereview.preferences.lang;

import java.util.Arrays;
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

	public List<String> getFileendings() {
		return fileendings;
	}
	
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
	
	void setFileendings(String in) {
		in = in.trim();
		if(!in.isEmpty()) {
			String[] endings = in.split(",");
			for(String s : endings) {
				s = s.trim();
			}
			fileendings.clear();
			fileendings.addAll(Arrays.asList(endings));
		}
	}

	public String getBeginTag() {
		return beginTag;
	}
	
	void setBeginTag(String beginTag) {
		this.beginTag = beginTag;
	}

	public String getEndTag() {
		return endTag;
	}
	
	void setEndTag(String endTag) {
		this.endTag = endTag;
	}
}
