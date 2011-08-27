package de.tukl.cs.softech.agilereview.preferences.lang;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
/*?|0000044|Peter|c0|*/
/**
 * Not yet a generic class, which currently displays the following text: 
 * "Follow this link for downloading an example template."<br>The link opens
 * 
 *//*|0000044|Peter|c0|?*/
public class LinkField extends FieldEditor implements Listener {/*?|0000044|Malte|c0|?*/
	
	/**
	 * Creates a new instance
	 * @param parent of this link field
	 */
	public LinkField(Composite parent) {
		super("", "", parent);
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {

	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		GridData rlGD = new GridData(GridData.FILL_HORIZONTAL);
		rlGD.horizontalSpan = numColumns;
		rlGD.horizontalAlignment = GridData.BEGINNING;
		
		Link label = new Link(parent, SWT.NONE);
		label.setText("Follow this <a>link</a> for downloading an example template.");
		label.setLayoutData(rlGD);
		label.setData("exampleTemplates");
		label.addListener(SWT.Selection, this);
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

	@Override
	public int getNumberOfControls() {
		return 1;
	}
	
	@Override
	public void handleEvent(Event event) {
		if(event.widget.getData().equals("exampleTemplates") && event.text.equals("link")) {
			try {
				PlatformUI.getWorkbench().getBrowserSupport().createBrowser(null).openURL(
						new URL(PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.URL_EXAMPLE_EXPORT_TEMPLATES)));
			} catch (PartInitException e) {/*?|0000044|Peter|c2|*/
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error occured",
						"An Error occured while opening a new browser window (1)");/*|0000044|Peter|c2|?*/
			} catch (MalformedURLException e) {/*?|0000044|Peter|c1|*/
				MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Error occured",
						"An Error occured while opening a new browser window (2)");
			}/*|0000044|Peter|c1|?*/
		}
	}
}
