package de.tukl.cs.softech.agilereview.preferences.lang;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The {@link FileendingContentProvider} manages the contents for the configuration table in the preferences dialog
 */
public class FileendingContentProvider implements IContentProvider, IStructuredContentProvider {

	/**
	 * {@link List} of {@link SupportedLanguageEntity}s which lists the contents for the table
	 */
	List<SupportedLanguageEntity> data = new LinkedList<SupportedLanguageEntity>();
	
	/**
	 * Creates a new instance of the {@link FileendingContentProvider}
	 */
	FileendingContentProvider() {
	}
	
	/**
	 * Clears the hold data of the {@link FileendingContentProvider}
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		data.clear();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput instanceof Map<?,?>) {
			Map<?,?> rawData = (Map<?,?>) newInput;
			//convert data to sort by same comment tags
			for(Object rawKey : rawData.keySet()) {
				//proof for right format
				if(!(rawKey instanceof String) && !(rawData.get(rawKey) instanceof String[])) {
					//not the right format
					return;
				}
				
				//create loop variables
				boolean contained = false;
				String key = (String)rawKey;
				String[] tags = (String[]) rawData.get(key);
				SupportedLanguageEntity toAddTo = null;
				//search for already found comment tags
				for(SupportedLanguageEntity sl : data) {
					if(tags[0].equals(sl.getBeginTag()) && tags[1].equals(sl.getEndTag())) {
						contained = true;
						toAddTo = sl;
						break;
					}
				}
				//apply search result
				if(!contained) {
					data.add(new SupportedLanguageEntity(key, tags[0], tags[1]));
				} else if(toAddTo != null) {
					toAddTo.addFileending(key);
				}
			}
		}
		if(newInput instanceof List<?>) {
			List<?> list = ((List<?>)newInput);
			LinkedList<SupportedLanguageEntity> tmp = new LinkedList<SupportedLanguageEntity>();
			for(Object o : list) {
				if(o instanceof SupportedLanguageEntity) {
					tmp.add((SupportedLanguageEntity)o);
				}
			}
			data = tmp;
		}
		viewer.refresh();
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return data.toArray();
	}

}
