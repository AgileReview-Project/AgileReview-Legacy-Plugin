package de.tukl.cs.softech.agilereview.preferences.lang;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

/**
 * Editing support the begin tag column
 */
public class BeginTagEditingSupport extends EditingSupport {

	/**
	 * TableViewer on which this editing support is applied
	 */
	private TableViewer viewer;

	/**
	 * Creates a FileendingEditingSupport with the viewer on which it is applied
	 * @param viewer
	 */
	BeginTagEditingSupport(TableViewer viewer) {
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
			return ((SupportedLanguageEntity)element).getBeginTag();
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if(element instanceof SupportedLanguageEntity && value instanceof String) {
			((SupportedLanguageEntity)element).setBeginTag((String)value);
			viewer.refresh();
		}
	}
}
