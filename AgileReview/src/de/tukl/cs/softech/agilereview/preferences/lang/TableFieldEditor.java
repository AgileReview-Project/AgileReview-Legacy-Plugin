package de.tukl.cs.softech.agilereview.preferences.lang;

import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * The TableFieldEditor represents the configurable table of languages supported by AgileReview
 */
public class TableFieldEditor extends FieldEditor implements Listener {
	
	/**
	 * Instance of PropertiesManager
	 */
	private static PropertiesManager pm = PropertiesManager.getInstance();
	/**
	 * Content provider for the TableViewer
	 */
	private FileendingContentProvider cp;
	/**
	 * TableViewer managing the table
	 */
	private TableViewer table;
	/**
	 * Parent composite of this field editor
	 */
	private Composite parent;
	
	/**
	 * Creates a new TableFieldEditor
	 * @param parent
	 */
	public TableFieldEditor(Composite parent) {
		super("", "", parent);
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
		this.parent = parent;
		
		Label label = new Label(parent, SWT.WRAP);
		label.setText("More than one fileendings in one cell should be managed by comma separation." +
				"The begin and end tag should specify the tags of a multiline comment in the corresponding programming " +
				"language.");
		GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        gd.horizontalAlignment = GridData.FILL;
        gd.widthHint = parent.getSize().x;
        label.setLayoutData(gd);
		
		table = new TableViewer(parent, SWT.H_SCROLL | SWT.FULL_SELECTION | SWT.V_SCROLL | SWT.BORDER);
		createColumn("fileendings",200,0);
        createColumn("begin tag",100,1);
        createColumn("end tag",100,2);
        table.setColumnProperties(new String[]{"fileendings","begin tag", "end tag"});
        table.getTable().setLinesVisible(true);
        table.getTable().setHeaderVisible(true);
        cp = new FileendingContentProvider();
        table.setContentProvider(cp);
        
        gd = new GridData();
        gd.horizontalSpan = numColumns-1;
        gd.verticalSpan = 2;
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        table.getTable().setLayoutData(gd);
        
        Button but = new Button(parent, SWT.PUSH);
        but.setText("+");
        but.setData("+");
        but.addListener(SWT.Selection, this);
        gd = new GridData();
        gd.widthHint = 40;
        gd.horizontalSpan = 1;
        gd.horizontalAlignment = SWT.END;
        gd.verticalAlignment = SWT.TOP;
        but.setLayoutData(gd);
        
        but = new Button(parent, SWT.PUSH);
        but.setText("-");
        but.setData("-");
        but.addListener(SWT.Selection, this);
        gd = new GridData();
        gd.widthHint = 40;
        gd.horizontalSpan = 1;
        gd.horizontalAlignment = SWT.END;
        gd.verticalAlignment = SWT.TOP;
        but.setLayoutData(gd);
        
        checkValidity();
	}

	@Override
	protected void doLoad() {
		table.setInput(pm.getParserFileendingsAndTagsAsEntity());
	}

	@Override
	protected void doLoadDefault() {
		table.setInput(pm.getParserFileendingsAndTagsAsEntity());
	}

	@Override
	protected void doStore() {
		if(!checkValidity()) {
			return;
		}
		//delete empty entities for saving issues
		Iterator<SupportedLanguageEntity> it = cp.data.iterator();
		while(it.hasNext()) {
			SupportedLanguageEntity lang = it.next();
			if(lang.isEmpty()) {
				it.remove();
			}
		}
		//save
		pm.setParserFileendingsAndTags(cp.data.toArray(new SupportedLanguageEntity[0]));
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}

	/**
	 * Creates a single column of the viewer with given parameters 
	 * @param title The title to be set
	 * @param bound The width of the column
	 * @param colNumber The columns number
	 */
	private void createColumn(String title, int bound, final int colNumber) {
		TableViewerColumn viewerColumn = new TableViewerColumn(table, SWT.NONE);
		
		switch(colNumber) {
		case 0:
			viewerColumn.setEditingSupport(new FileendingEditingSupport(table, this));
			viewerColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					String result = "";
					if(element instanceof SupportedLanguageEntity) {
						result = ((SupportedLanguageEntity)element).getFileendingsAsString();
					}
					return result;
				}
			});
			break;
		case 1:
			viewerColumn.setEditingSupport(new BeginTagEditingSupport(table, this));
			viewerColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					String result = "";
					if(element instanceof SupportedLanguageEntity) {
						result = ((SupportedLanguageEntity)element).getBeginTag();
					}
					return result;
				}
			});
			break;
		case 2:
			viewerColumn.setEditingSupport(new EndTagEditingSupport(table, this));
			viewerColumn.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					String result = "";
					if(element instanceof SupportedLanguageEntity) {
						result = ((SupportedLanguageEntity)element).getEndTag();
					}
					return result;
				}
			});
			break;
		
		}
		
		TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
	}

	@Override
	public void handleEvent(Event event) {
		if(event.widget.getData().equals("+")) {
			LinkedList<SupportedLanguageEntity> workset = new LinkedList<SupportedLanguageEntity>(cp.data);
			workset.add(new SupportedLanguageEntity());
			cp.inputChanged(table, null, workset);
		} else if(event.widget.getData().equals("-")) {
			for(TableItem i : table.getTable().getSelection()) {
				if(i.getData() instanceof SupportedLanguageEntity) {
					cp.data.remove((SupportedLanguageEntity)i.getData());
				}
			}
			cp.inputChanged(table, null, cp.data);
			checkValidity();
		}
		parent.layout();
	}
	
	/**
	 * Checks the validity of user inputs
	 * @return true, if all entries are valid<br>false, otherwise
	 */
	boolean checkValidity() {
		for(SupportedLanguageEntity e : cp.data) {
			if(!e.isValid()) {
				if(getPage() != null && getPage() instanceof PreferencePage) {
					((PreferencePage) getPage()).setValid(false);
					((PreferencePage) getPage()).setErrorMessage("One or more lines are not specified correctly!");
				}
				return false;
			}
		}
		if(getPage() != null && getPage() instanceof PreferencePage) {
			((PreferencePage) getPage()).setValid(true);
			((PreferencePage) getPage()).setErrorMessage(null);
		}
		return true;
	}
}
