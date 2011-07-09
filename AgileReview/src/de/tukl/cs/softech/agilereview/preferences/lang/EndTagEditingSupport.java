package de.tukl.cs.softech.agilereview.preferences.lang;/*?|3309698-preferenceslang|reuter|c13|*/

import org.eclipse.jface.viewers.TableViewer;

/**
 * Editing support the end tag column
 */
public class EndTagEditingSupport extends AbstractEditingSupport {
	
	/**
	 * Creates a EndTagEditingSupport with the viewer on which it is applied
	 * @param viewer
	 * @param tableFieldEditor 
	 */

	EndTagEditingSupport(TableViewer viewer, TableFieldEditor tableFieldEditor) {
		super(viewer, tableFieldEditor);
	}

	@Override
	protected Object getValue(Object element) {
		if(element instanceof SupportedLanguageEntity) {
			System.err.println(((SupportedLanguageEntity) element).getFileendingsAsString());
			return ((SupportedLanguageEntity)element).getEndTag();
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if(element instanceof SupportedLanguageEntity && value instanceof String) {
			((SupportedLanguageEntity)element).setEndTag((String)value);
			viewer.refresh();
			tableFieldEditor.checkValidity();
		}
	}
}/*|3309698-preferenceslang|reuter|c13|?*/
