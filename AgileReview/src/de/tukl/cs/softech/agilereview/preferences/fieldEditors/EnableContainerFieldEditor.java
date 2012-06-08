package de.tukl.cs.softech.agilereview.preferences.fieldEditors;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Advanced Field Editor capable of displaying multiple field editors in a title-bordered container,
 * with a check-box above disabling and enabling the whole container. 
 * In order to add field editors to this component, create the field editors using the result of <code>getContainer()</code> as parent 
 * and then add the field editor using <code>addField(FieldEditor fieldEditor)</code>.
 * Currently there is no support for validity checking and advanced layout inside the container.
 */
public class EnableContainerFieldEditor extends BorderedFieldEditor {

	/**
	 * Check-Box for enabling the container (also represents a preference value)
	 */
	private Button checkboxEnable;	
	/**
	 * Text of the check-box, as the build-in label support is used for the container title
	 */
	private String strCheckboxText;
	
	/**
	 * Creates a new field editor with a check-box and a title-bordered container, to which other field editors can be added
	 * @param property preference value represented by the check-box
	 * @param checkboxText Text displayed by the check-box
	 * @param title for the group
	 * @param parent the parent control
	 */
	public EnableContainerFieldEditor(String property, String checkboxText, String title, Composite parent) {
		super(parent, title);
		strCheckboxText = checkboxText;
		init(property, title);
		createControl(parent);
	}
	
	
	@Override
	protected void adjustForNumColumns(int numColumns) {
		super.adjustForNumColumns(numColumns);
		((GridData)checkboxEnable.getLayoutData()).horizontalSpan = numColumns;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		if(strCheckboxText != null) { // this is the case when the constructor calls super(parent) and strCheckboxText has not been set yet
			checkboxEnable = new Button(parent, SWT.CHECK);
			checkboxEnable.setText(strCheckboxText);

			GridData gd = new GridData();
			gd.horizontalSpan = numColumns;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = GridData.FILL_HORIZONTAL;
			checkboxEnable.setLayoutData(gd);

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
			
			super.doFillIntoGrid(parent, numColumns);
		}
	}
	
	/**
	 * What should happen when the check-box is clicked
	 */
	private void handleCheckEvent() {
		setPresentsDefaultValue(false);
		updateGroupDisabledState();
	}
	
	/**
	 * Disable the container and all its inhabitants
	 */
	private void updateGroupDisabledState() {
		groupPluginEditors.setEnabled(checkboxEnable.getSelection());
		for (FieldEditor fieldEdit: pluginFieldEditorList) {
			fieldEdit.setEnabled(checkboxEnable.getSelection(), getContainer());
		}	
	}

	@Override
	protected void doLoad() {
		// Load checkbox
		checkboxEnable.setSelection(getPreferenceStore().getBoolean(getPreferenceName()));
		// Load the Field-Editors
		super.doLoad();
		updateGroupDisabledState();
	}

	@Override
	protected void doLoadDefault() {
		// Load default checkbox
		checkboxEnable.setSelection(getPreferenceStore().getDefaultBoolean(getPreferenceName()));
		// Load the Field-Editors (default)
		super.doLoadDefault();
		updateGroupDisabledState();
	}
	
	@Override
	protected void doStore() {
		// Load default checkbox
		getPreferenceStore().setValue(getPreferenceName(), checkboxEnable.getSelection());
		// Load the Field-Editors (default)
		super.doStore();
	}
	
	@Override
	public int getNumberOfControls() {
		return 2;
	}
	
	@Override
	public void setPreferenceStore(IPreferenceStore store) {
		super.setPreferenceStore(store);
	}
}