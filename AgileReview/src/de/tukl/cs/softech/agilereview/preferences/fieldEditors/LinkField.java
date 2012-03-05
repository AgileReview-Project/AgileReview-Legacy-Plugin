package de.tukl.cs.softech.agilereview.preferences.fieldEditors;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

/**
 * Generic FieldEditor which only displays a link which can be opened in the system browser
 */
public class LinkField extends FieldEditor implements Listener {/*?|r110|Malte|c2|?*/
	
	/**
	 * The text which contains a link tag
	 */
	private String text;
	/**
	 * The link where this field should point to
	 */
	private String link;
	/**
	 * The image on which the link will be bound
	 */
	private Image icon;
	/**
	 * Label which contains the text and the link
	 */
	private Link linkLabel;
	/**
	 * The label containing the image which will be bound to the given link
	 */
	private Button label;
	
	/**
	 * Creates a new instance with the given text bound to the given link
	 * @param parent of this link field
	 * @param text containing a \<a\>\</a\> tag in order to indicate the link
	 * @param link to which the text should point to
	 */
	public LinkField(Composite parent, String text, String link) {
		this.text = text;
		this.link = link;
		doFillIntoGrid(parent, getNumberOfControls());
		adjustForNumColumns(getNumberOfControls());
	}
	
	/**
	 * Creates a new instance with the given icon bound to the given link
	 * @param parent of this link field
	 * @param image on which the link should be bound the link
	 * @param link link to which the icon should point to
	 */
	public LinkField(Composite parent, Image image, String link) {
		this.icon = image;
		this.link = link;
		doFillIntoGrid(parent, getNumberOfControls());
		adjustForNumColumns(getNumberOfControls());
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
		if(linkLabel != null) {
			GridData rlGD = (GridData) linkLabel.getLayoutData();
			rlGD.horizontalSpan = numColumns;
			linkLabel.setLayoutData(rlGD);
		} else if(label != null) {
			GridData rlGD = (GridData) label.getLayoutData();
			rlGD.horizontalSpan = numColumns;
			label.setLayoutData(rlGD);
		}
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		GridData rlGD = new GridData();
		rlGD.horizontalSpan = numColumns;
		rlGD.horizontalAlignment = GridData.END;
		rlGD.verticalIndent = 10;
		
		if(text != null) {
			linkLabel = new Link(parent, SWT.NONE);
			linkLabel.setText(text);
			linkLabel.setLayoutData(rlGD);
			linkLabel.addListener(SWT.Selection, this);
		} else if(icon != null) {
			label = new Button(parent, SWT.NONE);
			label.setImage(icon);
			label.setLayoutData(rlGD);
			label.addListener(SWT.Selection, this);
		}
	}

	@Override
	protected void doLoad() {
	}

	@Override
	protected void doLoadDefault() {
	}

	@Override
	protected void doStore() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.FieldEditor#store()
	 * @author Peter Reuter (05.03.2012)
	 */
	public void store() {
		// do nothing, nothing needs to be stored
	}

	@Override
	public int getNumberOfControls() {
		return 2;
	}
	
	@Override
	public void handleEvent(Event event) {
		Program.launch(link);
	}
}