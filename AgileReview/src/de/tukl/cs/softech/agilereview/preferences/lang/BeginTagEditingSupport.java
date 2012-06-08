package de.tukl.cs.softech.agilereview.preferences.lang;

import org.eclipse.jface.viewers.TableViewer;

/**
 * Editing support the begin tag column
 */
public class BeginTagEditingSupport extends AbstractEditingSupport {

	/**
	 * Creates a BeginTagEditingSupport with the viewer on which it is applied
	 * @param viewer
	 * @param tableFieldEditor 
	 */
	BeginTagEditingSupport(TableViewer viewer, TableFieldEditor tableFieldEditor) {
		super(viewer, tableFieldEditor);
	}

	@Override
	protected Object getValue(Object element) {
		if(element instanceof SupportedLanguageEntity) {
			return ((SupportedLanguageEntity)element).getBeginTag();
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if(element instanceof SupportedLanguageEntity && value instanceof String) {
			((SupportedLanguageEntity)element).setBeginTag((String)value);
			viewer.refresh();
			tableFieldEditor.checkValidity();
		}
	}
}