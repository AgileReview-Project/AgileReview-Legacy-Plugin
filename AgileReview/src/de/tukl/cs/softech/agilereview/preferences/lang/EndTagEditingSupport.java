package de.tukl.cs.softech.agilereview.preferences.lang;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

/**
 * Editing support the end tag column
 */
public class EndTagEditingSupport extends EditingSupport {

	/**
	 * TableViewer on which this editing support is applied
	 */
	private TableViewer viewer;

	/**
	 * Creates a FileendingEditingSupport with the viewer on which it is applied
	 * @param viewer
	 */
	EndTagEditingSupport(TableViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return new TextCellEditor(viewer.getTable());
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
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
		}
	}
}
