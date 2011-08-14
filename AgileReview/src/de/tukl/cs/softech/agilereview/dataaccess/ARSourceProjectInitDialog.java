package de.tukl.cs.softech.agilereview.dataaccess;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.actions.ImportResourcesAction;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.wizards.datatransfer.ExternalProjectImportWizard;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.wizards.newreviewsource.NewReviewSourceWizard;

public class ARSourceProjectInitDialog extends Composite implements Listener {

	private Combo comboChooseProject;
	
	private Button btOk;
	
	private Combo comboClosedProjects;
	
	private Button btOpenClosed;
	
	private Button btCreateNew;
	
	private Button btImport;
	
	private List<String> listOpenARProjects;
	
	private List<String> listClosedARProjects;
	
	private String returnText = "";
	
	public ARSourceProjectInitDialog(Composite parent, int style) {
		super(parent, style);
		initUI();
	}
	
	private void updateComboBoxes(String prefProject) {
		// Get the elements
		listOpenARProjects = new ArrayList<String>();
		listClosedARProjects = new ArrayList<String>();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projArr = workspaceRoot.getProjects();
		for (IProject currProj : projArr) {
			try {
				if (currProj.hasNature(PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.AGILEREVIEW_NATURE))) {
					listOpenARProjects.add(currProj.getName());
				}
			} catch (CoreException e) {
				// Is thrown, if currProj is closed or does not exist
				if (currProj.exists()) {
					listClosedARProjects.add(currProj.getName());	
				}
			}
		}
		
		comboChooseProject.setItems(listOpenARProjects.toArray(new String[listOpenARProjects.size()]));
		comboClosedProjects.setItems(listClosedARProjects.toArray(new String[listClosedARProjects.size()]));
		
		// Select the preferred Project
		if (prefProject != null) {
			String [] items = comboChooseProject.getItems();
			for (int i=0;i<items.length;i++) {
				if (items[i].equals(prefProject)) {
					comboChooseProject.select(i);
				}
			}
		} else {
			comboChooseProject.select(0);
		}
		btOk.setFocus();
	}
	
	/**
	 * Creates the UI
	 */
	private void initUI() {
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		this.setLayout(gridLayout);
		
		Label labelChoose = new Label(this, SWT.NONE);
		labelChoose.setText("Please choose a AgileReview Source Folder:");
	
		comboChooseProject = new Combo(this, SWT.READ_ONLY | SWT.DROP_DOWN);
				
		btOk = new Button(this, SWT.PUSH);
		btOk.setText("Ok");
		btOk.addListener(SWT.Selection, this);
		this.getShell().setDefaultButton(btOk);
		
		Composite bottom = new Composite(this, SWT.BORDER);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.horizontalSpan = gridLayout.numColumns;
		bottom.setLayoutData(gridData);
		GridLayout gridLayoutBottom = new GridLayout();
		gridLayoutBottom.numColumns = 3;
		bottom.setLayout(gridLayoutBottom);
		
		Label labelClose = new Label(bottom, SWT.NONE);
		labelClose.setText("or open a closed AgileReview Source Project");
		
		comboClosedProjects = new Combo(bottom, SWT.READ_ONLY | SWT.DROP_DOWN);
		
		btOpenClosed = new Button(bottom, SWT.PUSH);
		btOpenClosed.setText("Open");
		btOpenClosed.addListener(SWT.Selection, this);		
				
		Composite bottom2 = new Composite(bottom, SWT.NONE);
		GridData gridData2 = new GridData();
		gridData2.horizontalAlignment = GridData.FILL;
		gridData2.horizontalSpan = gridLayout.numColumns;
		bottom2.setLayoutData(gridData2);
		GridLayout gridLayoutBottom2 = new GridLayout();
		gridLayoutBottom2.numColumns = 5;
		bottom2.setLayout(gridLayoutBottom2);
		
		Label labelOr1 = new Label(bottom2, SWT.NONE);
		labelOr1.setText("or");
		
		btCreateNew = new Button(bottom2, SWT.PUSH);
		btCreateNew.setText("Create a new");
		btCreateNew.addListener(SWT.Selection, this);
		
		Label labelOr2 = new Label(bottom2, SWT.NONE);
		labelOr2.setText("or");
		
		btImport = new Button(bottom2, SWT.PUSH);
		btImport.setText("Import one");
		btImport.addListener(SWT.Selection, this);
		
		Label labelOr3 = new Label(bottom2, SWT.NONE);
		labelOr3.setText("first.");
		
		updateComboBoxes(null);
	}
	/**
	 * Returns the name of the project chosen to be the current AgileReview Source Project
	 * @return name of the project chosen
	 */
	public String getChosenProjectName(){
		return returnText;
	}

	@Override
	public void handleEvent(Event event) {
		if (event.widget == btOk) {
			if (comboChooseProject.getText() != ""){
				returnText = comboChooseProject.getText();
				dispose();
			}
		} else if (event.widget == btOpenClosed) {
			if (comboClosedProjects.getText() != ""){
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				IProject p = workspaceRoot.getProject(comboClosedProjects.getText());
				try {
					// TODO: use ProgressMonitor here
					p.open(null);
					while (!p.isOpen()) {}
					updateComboBoxes(p.getName());
				} catch
				(CoreException e) {
					// XXX: Do some error handling
					// e.printStackTrace();
				}
			}
			
		} else if (event.widget == btCreateNew) {
			NewReviewSourceWizard revSourceW = new NewReviewSourceWizard(false, true);
			WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), revSourceW);
			if (dialog.open() == Window.OK){
				if (revSourceW.getCreatedProjectName() != null) {
					updateComboBoxes(revSourceW.getCreatedProjectName());
				}
			}						
		} else if (event.widget == btImport) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			ExternalProjectImportWizard wizard = new ExternalProjectImportWizard();
			wizard.init(workbench, null);
			WizardDialog dialog = new WizardDialog(workbench.getActiveWorkbenchWindow().getShell(), wizard);
			if (dialog.open() == Window.OK){
				IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				String newProject = null;
				for (IProject p: workspaceRoot.getProjects()) {
					try {
						// Take the first "new" AgileReview Source Project to find and interpret it as the imported one
						if (!listOpenARProjects.contains(p.getName()) && p.hasNature(PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.AGILEREVIEW_NATURE))) {
							newProject = p.getName();
							break;
						}
					} catch (CoreException e) {
						// Is thrown, if currProj is closed or does not exist
						// XXX do some error handling
					}
				}
				updateComboBoxes(newProject);
			}
		}		
	}

}
