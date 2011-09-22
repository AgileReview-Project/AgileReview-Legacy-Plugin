package de.tukl.cs.softech.agilereview.preferences.lang;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * The {@link FileendingContentProvider} manages the contents for the configuration table in the preferences dialog
 */
public class FileendingContentProvider implements IStructuredContentProvider {

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
	 * not yet used
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
	public void dispose() {
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if(newInput instanceof SupportedLanguageEntity[]) {
			data = Arrays.asList(((SupportedLanguageEntity[])newInput));
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