package de.tukl.cs.softech.agilereview.views.reviewexplorer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleFileWrapper;

/**
 * Action to open a file editor out of the Review Explorer
 */
class REOpenAction extends Action {
	
	/**
	 * WorkbenchPage on which the action takes place
	 */
	private IWorkbenchPage page;
	/**
	 * File which will be opened
	 */
	private MultipleFileWrapper data;
	/**
	 * SelectionProvider which should provide the File to be opened
	 */
	private ISelectionProvider provider; 


	/**
	 * Construct the OpenPropertyAction with the given page. 
	 * @param p The page to use as context to open the editor.
	 * @param selectionProvider The selection provider 
	 */
	protected REOpenAction(IWorkbenchPage p, ISelectionProvider selectionProvider) {
		setText("Open File");
		page = p;
		this.provider = selectionProvider;
	}
	
	/**
	 * Converts a given {@link MultipleFileWrapper} to an {@link IFile}
	 * @param file which will be converted
	 * @return the IFile which represents the input
	 */
	private IFile convertToIFile(MultipleFileWrapper file) {
		IWorkspaceRoot wr = ResourcesPlugin.getWorkspace().getRoot();
		return wr.getFile(new Path(file.getPath()));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#isEnabled()
	 */
	public boolean isEnabled() {
		ISelection selection = this.provider.getSelection();
		if(!selection.isEmpty()) {
			IStructuredSelection sSelection = (IStructuredSelection) selection;
			if(sSelection.size() == 1 && sSelection.getFirstElement() instanceof MultipleFileWrapper) {
				data = ((MultipleFileWrapper)sSelection.getFirstElement()); 				
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.action.Action#run()
	 */
	public void run() { 
		if(isEnabled()) {
			IFile file = convertToIFile(data);
			if(file.exists()) {
				try {
					IDE.openEditor(page, file); 
				} catch (PartInitException e) {
					PluginLogger.logError(this.getClass().toString(), "run", "An error occured while initializing the Editor!", e);
					MessageDialog.openError(Display.getDefault().getActiveShell(), "Error while opening file", "An error occured while initializing the editor!");
				}
			} else {
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Error while opening file", "Could not open file!\nFile not existent in workspace!");
			}
		}
	}
}
