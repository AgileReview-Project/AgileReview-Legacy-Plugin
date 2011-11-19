package de.tukl.cs.softech.agilereview.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.tukl.cs.softech.agilereview.annotations.ColorManager;

/**
 * Extension of a ColorFieldEditor, so the author corresponding to that color is shown behind the color
 */
public class AuthorColorFieldEditor extends ColorFieldEditor {/*?|r73|Thilo|c0|?*/
	
	/**
	 * Number of the color this FieldEditor represents
	 */
	private int authorNumber = -1;
	/**
	 * Label displaying the author
	 */
	private Label authorLabel;
	
	/**
	 * Constructor creating a ColorFieldEditor with the name of the author currently corresponding to that color behind it
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param authorNumber the number representing this color
	 * @param parent the parent of the field editor's control
	 */
	public AuthorColorFieldEditor(String name, String labelText, int authorNumber, Composite parent){
		super(name, labelText, parent);
		this.authorNumber = authorNumber;
		// (1) Needed, as you can not be sure which is called first ((1) or (2))
		if (authorLabel != null) {
			authorLabel.setText(ColorManager.getAuthorForNumber(authorNumber));
		}
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		// make place for the additional label
		super.doFillIntoGrid(parent, numColumns-1);
		authorLabel = new Label(parent, SWT.NONE);
		// (2) Needed, as you can not be sure which is called first ((1) or (2))
		if (authorNumber != -1) {
			authorLabel.setText(ColorManager.getAuthorForNumber(authorNumber));
		}
	}

	@Override
	public int getNumberOfControls() {
		// make place for the additional label
		return super.getNumberOfControls()+1;
	}
	
	@Override
	public void setEnabled(boolean enabled, Composite parent) {
		super.setEnabled(enabled, parent);
		if (authorLabel != null) {
			authorLabel.setVisible(enabled);
		}
	}
	
}
