package de.tukl.cs.softech.agilereview.preferences.lang;/*?|3309698-preferenceslang|reuter|c10|*/

import org.eclipse.jface.viewers.TableViewer;

/**
 * Editing support the file endings column
 */
public class FileendingEditingSupport extends AbstractEditingSupport {
	
	/**
	 * Creates a FileendingEditingSupport with the viewer on which it is applied
	 * @param viewer
	 * @param tableFieldEditor 
	 */
	FileendingEditingSupport(TableViewer viewer, TableFieldEditor tableFieldEditor) {
		super(viewer, tableFieldEditor);
	}

	@Override
	protected Object getValue(Object element) {
		if(element instanceof SupportedLanguageEntity) {
			return ((SupportedLanguageEntity)element).getFileendingsAsString();
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if(element instanceof SupportedLanguageEntity && value instanceof String) {
			((SupportedLanguageEntity)element).setFileendingsAsString((String)value);
			viewer.refresh();
			tableFieldEditor.checkValidity();
		}
	}
}/*|3309698-preferenceslang|reuter|c10|?*/
