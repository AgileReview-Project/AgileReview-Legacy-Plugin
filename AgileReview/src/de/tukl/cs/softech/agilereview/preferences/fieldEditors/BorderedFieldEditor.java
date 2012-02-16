package de.tukl.cs.softech.agilereview.preferences.fieldEditors;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * The BorderedFieldEditor is a wrapper field editor which manages a list of field editors and
 * displays a border around them as a group
 * @author Malte Brunnlieb
 */
public class BorderedFieldEditor extends FieldEditor {/*?|r110|Malte|c6|?*/

	/**
	 * Group title
	 */
	private String title;
	/**
	 * Container for the field editors
	 */
	protected Group groupPluginEditors;
	/**
	 * List of field editors inside the container
	 */
	protected List<FieldEditor> pluginFieldEditorList;
	
	/**
	 * Creates a new field editor with a check-box and a title-bordered container, to which other field editors can be added
	 * @param parent the parent control
	 * @param title which will be displayed above the group
	 */
	public BorderedFieldEditor(Composite parent, String title) {
		pluginFieldEditorList = new LinkedList<FieldEditor>();
		this.title = title;
		createControl(parent);
	}
	
	@Override
	protected void adjustForNumColumns(int numColumns) {
		GridData gd = ((GridData)groupPluginEditors.getLayoutData());
		gd.horizontalSpan = numColumns;
		groupPluginEditors.setLayoutData(gd);
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		groupPluginEditors = new Group(parent, SWT.NONE);
		groupPluginEditors.setText(title);
		groupPluginEditors.setLayout(new GridLayout());
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL_HORIZONTAL;
		gd.verticalIndent = 5;
		groupPluginEditors.setLayoutData(gd);
	}

	@Override
	protected void doLoad() {
		// Load the Field-Editors
		for (FieldEditor fieldEdit: pluginFieldEditorList) {
			fieldEdit.load();
		}
	}

	@Override
	protected void doLoadDefault() {
		// Load the Field-Editors (default)
		for (FieldEditor fieldEdit: pluginFieldEditorList) {
			fieldEdit.loadDefault();
		}
	}

  
	/**
	 * Has to be overridden for ensuring that the field editors inside the container get stored.
	 * Otherwise <code>doStore()</code> will not be called if "Restore Default" was pressed before.
	 */
	@Override
    public void store() {
		super.store();
		// If checkbox was defaulted 
		if (presentsDefaultValue()) {
			for (FieldEditor fieldEdit: pluginFieldEditorList) {
				fieldEdit.store();
			}
		}
    }
	
	@Override
	protected void doStore() {
		// Load the Field-Editors (default)
		for (FieldEditor fieldEdit: pluginFieldEditorList) {
			fieldEdit.store();
		}
	}
	
	@Override
	public int getNumberOfControls() {
		return 3;
	}
	
	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		
		for (FieldEditor fieldEdit: pluginFieldEditorList) {
			fieldEdit.setPreferenceStore(store);/*?|r110|Peter Reuter|c0|?*/
		}
	}
	
	/**
	 * Returns the container, so other field editors can set it as their parent
	 * @return the container
	 */
	public Composite getContainer() {
		return groupPluginEditors;
	}
	
	/**
	 * Adds a field editor, so it will be handled as inside the container. 
	 * Please ensure that the parent of the field editor was set using the <code>getContainer()</code> method. 
	 * Otherwise the field editor will not really be inside the container.
	 * @param fieldEditor the field editor which should be handled by this container 
	 */
	public void addField(FieldEditor fieldEditor) {
		pluginFieldEditorList.add(fieldEditor);
	}
}
