package de.tukl.cs.softech.agilereview.preferences;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**
 * Advanced Field Editor capable of displaying multiple field editors in a title-bordered container,
 * with a check-box above disabling and enabling the whole container. 
 * In order to add field editors to this component, create the field editors using the result of <code>getContainer()</code> as parent 
 * and then add the field editor using <code>addField(FieldEditor fieldEditor)</code>.
 * Currently there is no support for validity checking and advanced layout inside the container.
 */
public class EnableContainerFieldEditor extends FieldEditor {

	/**
	 * Check-Box for enabling the container (also represents a preference value)
	 */
	private Button checkboxEnable;
	/**
	 * Container for the field editors
	 */
	private Group groupPluginEditors;
	/**
	 * List of field editors inside the container
	 */
	private List<FieldEditor> pluginFieldEditorList;
	
	/**
	 * Text of the check-box, as the build-in label support is used for the container title
	 */
	private String strCheckboxText;
	
	/**
	 * Creates a new field editor with a check-box and a title-bordered container, to which other field editors can be added
	 * @param checkboxName preference value represented by the check-box
	 * @param checkboxText Text displayed by the check-box
	 * @param title Text displayed as title of the container
	 * @param parent the parent control
	 */
	public EnableContainerFieldEditor (String checkboxName, String checkboxText, String title, Composite parent) {
		strCheckboxText = checkboxText;
		pluginFieldEditorList = new LinkedList<FieldEditor>();
		
		init(checkboxName, title);
		createControl(parent);
	}
	
	
	@Override
	protected void adjustForNumColumns(int numColumns) {
		((GridData)checkboxEnable.getLayoutData()).horizontalSpan = numColumns;
		((GridData)groupPluginEditors.getLayoutData()).horizontalSpan = numColumns;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		checkboxEnable = new Button(parent, SWT.CHECK);
		checkboxEnable.setText(strCheckboxText);

		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		gd.grabExcessHorizontalSpace = true;
		checkboxEnable.setLayoutData(gd);
		
		groupPluginEditors = new Group(parent, SWT.NONE);
		groupPluginEditors.setText(getLabelText());
		gd = new GridData();
		gd.horizontalSpan = numColumns;
		gd.grabExcessHorizontalSpace = true;
		groupPluginEditors.setLayoutData(gd);
		
		checkboxEnable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleCheckEvent();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				handleCheckEvent();
			}
		});
	}
	
	/**
	 * What should happen when the check-box is clicked
	 */
	private void handleCheckEvent() {
		setPresentsDefaultValue(false);/*?|r73+r87|Malte|c0|?*/
		setGroupDisabledState();
	}
	
	/**
	 * Disable the container and all its inhabitants
	 */
	private void setGroupDisabledState() {/*?|r73+r87|Malte|c1|?*/
		groupPluginEditors.setEnabled(checkboxEnable.getSelection());
		for (FieldEditor fieldEdit: pluginFieldEditorList) {
			fieldEdit.setEnabled(checkboxEnable.getSelection(), getContainer());
		}	
	}

	@Override
	protected void doLoad() {
		// Load checkbox/*?|r73+r87|Malte|c2|*/
		checkboxEnable.setSelection(getPreferenceStore().getBoolean(getPreferenceName()));
		groupPluginEditors.setEnabled(checkboxEnable.getSelection());
		// Load the Field-Editors
		for (FieldEditor fieldEdit: pluginFieldEditorList) {
			fieldEdit.load();
		}
		setGroupDisabledState();/*|r73+r87|Malte|c2|?*/
	}

	@Override
	protected void doLoadDefault() {
		// Load default checkbox
		checkboxEnable.setSelection(getPreferenceStore().getDefaultBoolean(getPreferenceName()));
		groupPluginEditors.setEnabled(checkboxEnable.getSelection());
		// Load the Field-Editors (default)
		for (FieldEditor fieldEdit: pluginFieldEditorList) {
			fieldEdit.loadDefault();
		}
		setGroupDisabledState();
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
		// Load default checkbox
		getPreferenceStore().setValue(getPreferenceName(), checkboxEnable.getSelection());
		// Load the Field-Editors (default)
		for (FieldEditor fieldEdit: pluginFieldEditorList) {
			fieldEdit.store();
		}
	}
	
	@Override
	public int getNumberOfControls() {
		return 2;
	}
	
	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
		
		for (FieldEditor fieldEdit: pluginFieldEditorList) {
			fieldEdit.setPreferenceStore(store);
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
