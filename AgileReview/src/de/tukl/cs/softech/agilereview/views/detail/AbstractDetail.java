package de.tukl.cs.softech.agilereview.views.detail;

import org.apache.xmlbeans.XmlObject;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.services.ISourceProviderService;

import de.tukl.cs.softech.agilereview.dataaccess.handler.SaveHandler;
import de.tukl.cs.softech.agilereview.plugincontrol.SourceProvider;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;

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
	 * saves every changes made in the current Detail View
	 * @param part will be forwarded from the {@link DetailView}
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@SuppressWarnings("unchecked")
	protected void partClosedOrDeactivated(IWorkbenchPart part) {
		if(part instanceof DetailView) {
			saveChanges();
			
			//fire "save" command for persistent storage
			IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench().getService(IHandlerService.class);
			try {
				handlerService.executeCommand(SaveHandler.SAVE_COMMAND_ID, null);
			} catch (Exception ex) {
				PluginLogger.logError(this.getClass().toString(), "partClosedOrDeactivated", "Error occured while triggering save command", ex);
			}
			this.backupObject = (E)this.editedObject.copy();
			PluginLogger.log(this.getClass().toString(), "partClosedOrDeactivated", "trigger save event");
			
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
