package de.tukl.cs.softech.agilereview.views.detail;

import java.util.HashSet;

import org.apache.xmlbeans.XmlObject;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.ISourceProviderService;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.plugincontrol.SourceProvider;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;

/**
 * Abstract class of a Comment or Review representation, which automatically provides IPartListener
 * and FocusListener to save all modified data persistently. Furthermore a listener implementation is provided
 * for a revert action by setting the object Data to "revert" and for a save action by setting the object Data to "save"
 * @param <E> type which would be displayed by this AbstractDetail
 */
public abstract class AbstractDetail<E extends XmlObject> extends Composite implements FocusListener, ModifyListener {
	
	/**
	 * current displayed object which will be modified
	 */
	protected E editedObject;
	/**
	 * backup of the current displayed object
	 */
	protected E backupObject;
	/**
	 * This set represents all components which should adapt the composite background color
	 */
	protected HashSet<Control> bgComponents = new HashSet<Control>();

	/**
	 * Creates a new AbstractDetail Composite onto the given parent with the specified SWT styles
	 * @param parent onto the ReviewDetail Composite will be added
	 * @param style with which this Composite will be styled
	 */
	protected AbstractDetail(Composite parent, int style) {
		super(parent, style);
		initUI();
	}

	/**
	 * this method will be automatically called by the constructor and should
	 * contain the initialization of the UI especially of the saveButton and revertButton
	 */
	protected abstract void initUI();
	
	/**
	 * saveChanges will be called by the IPartListener and FocusListener and should contain
	 * a save routine on object level of the current modified data
	 * @return true, if changes are done and write back is necessary,<br>false, if no changes have been made
	 */
	protected abstract boolean saveChanges();
	
	/**
	 * Returns the current content representation
	 * @return current content representation
	 */
	protected E getContent() {
		return this.editedObject;
	}
	
	/**
	 * fills all contents of the given input into the detail view
	 * @param input which should be displayed
	 */
	protected abstract void fillContents(E input);
	
	/**
	 * Converts all line breaks either \n or \r to \r\n line breaks
	 * @param in the string which line breaks should be converted
	 * @return the converted string
	 */
	protected String convertLineBreaks(String in) {
		return in.replaceAll("\r\n|\r|\n", System.getProperty("line.separator"));
	}
	
	/**
	 * Changes the background color for this AbstractDetail.
	 */
	protected void refreshBackgroundColor() {/*?|r59|Malte|c6|*/
		Color bg = determineBackgroundColor();/*|r59|Malte|c6|?*/
		this.setBackground(bg);
		String osName = System.getProperty("os.name");
		for(Control c : bgComponents) {
			//only paint comboboxes and buttons when the running system is windows
			//as on linux the background is also set for the components itself
			if(c instanceof Combo || c instanceof Button) {
				if(osName.contains("windows")) {
					c.setBackground(bg);
				}
			} else {
				c.setBackground(bg);
			}
		}
	}
	
	/**
	 * Determines the background color of the view. Will always be asked when new a new input will be displayed at the current view.
	 * @return Background color for the view
	 */
	protected abstract Color determineBackgroundColor();/*?|r59|Malte|c5|?*/
	
	/**
	 * saves every changes made in the current Detail View
	 * @param part will be forwarded from the {@link DetailView}
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@SuppressWarnings("unchecked")
	protected void partClosedOrDeactivated(IWorkbenchPart part) {
		if(part instanceof DetailView) {
			saveChanges();
			
			// save the change persistently
			PluginLogger.log(this.getClass().toString(), "partClosedOrDeactivated", "trigger save event");
			ReviewAccess.getInstance().save(this.editedObject);
			ViewControl.refreshViews(ViewControl.COMMMENT_TABLE_VIEW);
						
			this.backupObject = (E)this.editedObject.copy();

			//get SourceProvider for configuration
			ISourceProviderService isps = (ISourceProviderService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISourceProviderService.class);
			SourceProvider sp = (SourceProvider) isps.getSourceProvider(SourceProvider.REVERTABLE);
			sp.setVariable(SourceProvider.REVERTABLE, false);
		}
	}
	
	/**
	 * Reverts all unsaved changes
	 */
	public void revert() {
		@SuppressWarnings("unchecked")
		E copy = (E)backupObject.copy();
		this.editedObject.set(copy);
		fillContents(backupObject);
		
		//get SourceProvider for configuration
		ISourceProviderService isps = (ISourceProviderService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISourceProviderService.class);
		SourceProvider sp = (SourceProvider) isps.getSourceProvider(SourceProvider.REVERTABLE);
		sp.setVariable(SourceProvider.REVERTABLE, false);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.swt.widgets.Composite#setFocus()
	 */
	public abstract boolean setFocus();
	
	/**
	 * not in use
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
	public void focusGained(FocusEvent e) {
		
	}

	/**
	 * save current changes in objects
	 * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
	 */
	@Override
	public void focusLost(FocusEvent e) {
		if(saveChanges()) {
			ISourceProviderService isps = (ISourceProviderService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISourceProviderService.class);
			SourceProvider sp = (SourceProvider) isps.getSourceProvider(SourceProvider.REVERTABLE);
			sp.setVariable(SourceProvider.REVERTABLE, true);
		}
	}
	
	@Override
	public void modifyText(ModifyEvent e) {
		ISourceProviderService isps = (ISourceProviderService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISourceProviderService.class);
		SourceProvider sp = (SourceProvider) isps.getSourceProvider(SourceProvider.REVERTABLE);
		sp.setVariable(SourceProvider.REVERTABLE, true);
	}
}