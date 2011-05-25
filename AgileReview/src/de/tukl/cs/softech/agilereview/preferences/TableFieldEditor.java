package de.tukl.cs.softech.agilereview.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

public class TableFieldEditor extends FieldEditor {
	
	private static PropertiesManager pm = PropertiesManager.getInstance();
	private TableViewer table;
	
	TableFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}

	/**
	 * not yet used
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		getLabelControl(parent);
		
		Label label = new Label(parent, SWT.None);
		label.setText("More than one fileendings in one cell should be managed by comma separation." +
				"The begin and end tag should specify the tags of a multiline comment in the corresponding programming" +
				"language.");
		
		table = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		table.getTable().setLinesVisible(true);
        table.setColumnProperties(new String[]{"fileendings","begin tag", "end tag"});
        table.getTable().setHeaderVisible(true);
        table.setContentProvider(new FileendingContentProvider());
        table.setLabelProvider(new ColumnLabelProvider());
        table.setInput(pm.getParserFileendingsAndTags());
//        table.setInput(new String[]{"est","werg","234"});
        
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        table.getTable().setLayoutData(gd);
	}

	@Override
	protected void doLoad() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doLoadDefault() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doStore() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getNumberOfControls() {
		// TODO Auto-generated method stub
		return 1;
	}

}
