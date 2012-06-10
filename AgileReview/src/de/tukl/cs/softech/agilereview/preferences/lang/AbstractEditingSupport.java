package de.tukl.cs.softech.agilereview.preferences.lang;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;

/**
 * This {@link AbstractEditingSupport} should specify some common behavior of all {@link EditingSupport}s of the language configuration table in the
 * preferences dialog
 */
public abstract class AbstractEditingSupport extends EditingSupport {
    
    /**
     * TableViewer on which this editing support is applied
     */
    TableViewer viewer;
    /**
     * TableFieldEditor this editing support regards to
     */
    TableFieldEditor tableFieldEditor;
    
    /**
     * Creates a AbstractEditingSupport with the viewer on which it is applied
     * @param viewer
     * @param tableFieldEditor
     */
    AbstractEditingSupport(TableViewer viewer, TableFieldEditor tableFieldEditor) {
        super(viewer);
        this.viewer = viewer;
        this.tableFieldEditor = tableFieldEditor;
    }
    
    @Override
    protected CellEditor getCellEditor(Object element) {
        return new TextCellEditor(viewer.getTable());
    }
    
    @Override
    protected boolean canEdit(Object element) {
        return true;
    }
    
}