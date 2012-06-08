package de.tukl.cs.softech.agilereview.views;

import java.util.HashSet;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener3;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.detail.DetailView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;

/**
 * The ViewControl provides information about the current displayed ViewPart of this plugin. Furthermore the ViewControl provides and forwards events
 * of the following Listener: {@link ISelectionListener}, {@link IPartListener2}, {@link IPerspectiveListener3}
 */
public class ViewControl implements ISelectionChangedListener, IPartListener2, IPerspectiveListener3, IPropertyChangeListener {
	
	/**
	 * Public static field representing the detail view
	 */
	public static final int DETAIL_VIEW = 1;
	/**
	 * Public static field representing the comment summary
	 */
	public static final int COMMMENT_TABLE_VIEW = 2;
	/**
	 * Public static field representing the review explorer
	 */
	public static final int REVIEW_EXPLORER = 4;
	/**
	 * Public static field representing all existing views of this plugin
	 */
	public static final int ALL_VIEWS = 7;
	
	/**
	 * Set of all active Views
	 */
	private static HashSet<Class<? extends IWorkbenchPart>> activeViews = new HashSet<Class<? extends IWorkbenchPart>>();
	/**
	 * Indicates whether the AgileReview perspective is currently open
	 */
	private static boolean perspectiveIsOpen = false;
	/**
	 * contextActivation for later deactivating
	 */
	private static IContextActivation contextActivation;
	/**
	 * Instance of ViewControl in order to add all listeners
	 */
	private static ViewControl instance = new ViewControl();
	
	/**
	 * Singleton Pattern
	 * @return the only instance of ViewControl
	 */
	public static ViewControl getInstance() {
		return instance;
	}
	
	/**
	 * Creates a new instance of ViewControl
	 */
	private ViewControl() {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				//wait until the active page is created, then register all listeners
				while (PlatformUI.getWorkbench() == null) {
				}
				while (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
				}
				while (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() == null) {
				}
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(ViewControl.this);
				// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addSelectionListener(ViewControl.this);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().addPerspectiveListener(ViewControl.this);
				// If our perspective is loaded, activate its context
				if (contextActivation == null
						&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective().getId().equals(
								"de.tukl.cs.softech.agilereview.view.AgileReviewPerspective")) {
					IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
					contextActivation = contextService.activateContext("de.tukl.cs.softech.agilereview.perspective.open");
					PluginLogger.log(ViewControl.class.toString(), "Constructor",
							"Context \"de.tukl.cs.softech.agilereview.perspective.open\" activated");
					
					//reparse all open files, because otherwise the annotations will not be shown initially
					perspectiveIsOpen = true;
					if (isOpen(CommentTableView.class)) {
						CommentTableView.getInstance().reparseAllEditors();
					}
				}
				Activator.getDefault().getPreferenceStore().addPropertyChangeListener(ViewControl.this);
			}
		});
	}
	
	/**
	 * Registers a new {@link ViewPart} for the plugin
	 * @param c Class of the {@link ViewPart} to register
	 * @return true if the registration was successful
	 */
	public static boolean registerView(Class<? extends IWorkbenchPart> c) {
		PluginLogger.log(ViewControl.class.toString(), "registerView", c.getName());
		return activeViews.add(c);
		
	}
	
	/**
	 * Unregisters a given {@link ViewPart} for the plugin. Views will be unregistered automatically when closed
	 * @param c Class of the {@link ViewPart} to unregister
	 * @return true if the given {@link ViewPart} was unregistered successfully
	 */
	private static boolean unregisterView(Class<? extends IWorkbenchPart> c) {
		PluginLogger.log(ViewControl.class.toString(), "unregisterView", c.getName());
		return activeViews.remove(c);
	}
	
	/**
	 * Checks whether the given {@link ViewPart} has been registered
	 * @param c Class of the {@link ViewPart} to be checked
	 * @return true if the given {@link ViewPart} has been registered
	 */
	public static boolean isOpen(Class<? extends ViewPart> c) {
		return activeViews.contains(c);
	}
	
	/**
	 * Checks whether the AgileReview Perspective is currently open
	 * @return true, if the AgileReview perspective is currently open <br> false, otherwise
	 */
	public static boolean isPerspectiveOpen() {
		return perspectiveIsOpen;
	}
	
	/**
	 * Calls the refreshViews(int, boolean, boolean) function with the given flags, false, false.
	 * @param flags
	 */
	public static void refreshViews(int flags) {
		refreshViews(flags, false, false);
	}
	
	/**
	 * Calls the refreshViews(int, boolean, boolean) function with the given flags, false and the given value of refreshInputs.
	 * @param flags
	 * @param refreshInputs
	 */
	public static void refreshViews(int flags, boolean refreshInputs) {
		refreshViews(flags, false, refreshInputs);
	}
	
	/**
	 * This function refreshes the views specified in the flags parameter. Therefore use the public fields delivered by this class and combine them
	 * with the bitwise or operator.<br> If the parameter validateExplorerSelection is set to true, the ReviewExplorers selection will be validated.
	 * For example this is necessary when changing the open status of reviews.
	 * @param flags views which should be refreshed
	 * @param validateExplorerSelection if true, the ReviewExplorers selection will be validated
	 * @param refreshInputs if true, data of ReviewExplorer and CommentTableView will be freshly loaded
	 */
	public static void refreshViews(int flags, boolean validateExplorerSelection, boolean refreshInputs) {
		if ((flags % 2 == 1) && isOpen(DetailView.class)) {
			DetailView.getInstance().refreshBackgroundColor();
		}
		if (((flags >> 1) % 2 == 1) && isOpen(CommentTableView.class)) {
			if (refreshInputs) {
				CommentTableView.getInstance().resetComments();
			} else {
				CommentTableView.getInstance().refreshTable();
			}
		}
		if (((flags >> 2) % 2 == 1) && isOpen(ReviewExplorer.class)) {
			if (validateExplorerSelection) {
				ReviewExplorer.getInstance().validateExplorerSelection();
			}
			if (refreshInputs) {
				ReviewExplorer.getInstance().refreshInput();
			} else {
				ReviewExplorer.getInstance().refresh();
			}
		}
	}
	
	/**
	 * Indicates whether the perspective should be switched (user preferences or actual user decision)
	 * @return true if perspective should be switched to AgileReview perspective, false if not
	 */
	public boolean shouldSwitchPerspective() {
		boolean answer = false;
		if (!isPerspectiveOpen()) {
			String switchPerspective = PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.AUTO_OPEN_PERSPECTIVE);
			if (MessageDialogWithToggle.ALWAYS.equals(switchPerspective)) {
				return true;
			} else if (MessageDialogWithToggle.NEVER.equals(switchPerspective)) { return false; }
			MessageDialogWithToggle dialog = MessageDialogWithToggle
					.openYesNoQuestion(
							Display.getDefault().getActiveShell(),
							"AgileReview",
							"This command belongs to the AgileReview Perspective. Do you want to switch to this perspective to fully enable the AgileReview Plugin?",
							null, false, PropertiesManager.getPreferences(), "autoOpenPerspective");
			answer = (dialog.getReturnCode() == IDialogConstants.YES_ID);
		}
		return answer;
	}
	
	/**
	 * Switches the perspective to the AgileReview perspective
	 */
	public void switchPerspective() {
		Display.getDefault().syncExec(new Runnable() {
			
			@Override
			public void run() {
				while (PlatformUI.getWorkbench() == null) {
				}
				while (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
				}
				try {
					PlatformUI.getWorkbench().showPerspective("de.tukl.cs.softech.agilereview.view.AgileReviewPerspective",
							PlatformUI.getWorkbench().getActiveWorkbenchWindow());
				} catch (WorkbenchException e) {
					PluginLogger.logError(this.getClass().toString(), "partOpened", "WorkbenchException while opening perspective", e);
				}
			}
		});
	}
	
	/**
	 * Opens the views specified by the parameter. If the view is already open, the specified views will be brought to top.
	 * @param views public static fields specifying the views (xor for more than one)
	 */
	public static void openView(final int views) {
		Display.getDefault().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				while (PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {
				}
				while (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() == null) {
				}
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				
				if (views % 2 == 1) {
					if (isOpen(DetailView.class)) {
						page.bringToTop(DetailView.getInstance());
					} else {
						try {
							page.showView("de.tukl.cs.softech.agilereview.view.commentdetailview.view");
						} catch (PartInitException e) {
							MessageDialog.openError(Display.getDefault().getActiveShell(), "Error while opening View",
									"Could not open the Comment Detail View. Error during initialization!");
							PluginLogger.logError(ViewControl.class.toString(), "openView", "Could not open the DetailView", e);
						}
					}
				}
				if ((views >> 1) % 2 == 1) {
					if (isOpen(CommentTableView.class)) {
						page.bringToTop(DetailView.getInstance());
					} else {
						try {
							page.showView("de.tukl.cs.softech.agilereview.view.commenttableview.view");
						} catch (PartInitException e) {
							MessageDialog.openError(Display.getDefault().getActiveShell(), "Error while opening View",
									"Could not open the Comment Summary View. Error during initialization!");
							PluginLogger.logError(ViewControl.class.toString(), "openView", "Could not open the DetailView", e);
						}
					}
				}
				if ((views >> 2) % 2 == 1) {
					if (isOpen(ReviewExplorer.class)) {
						page.bringToTop(DetailView.getInstance());
					} else {
						try {
							page.showView("de.tukl.cs.softech.agilereview.view.reviewnavigator.view");
						} catch (PartInitException e) {
							MessageDialog.openError(Display.getDefault().getActiveShell(), "Error while opening View",
									"Could not open the Review Explorer. Error during initialization!");
							PluginLogger.logError(ViewControl.class.toString(), "openView", "Could not open the DetailView", e);
						}
					}
				}
			}
		});
	}
	
	//****************************************
	//****** ISelectionChangedListener *******
	//****************************************
	
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		if (isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().selectionChanged(event);
		}
		if (isOpen(DetailView.class)) {
			DetailView.getInstance().selectionChanged(event);
		}
		if (isOpen(ReviewExplorer.class)) {
			ReviewExplorer.getInstance().selectionChanged(event);
		}
	}
	
	//****************************************
	//********** IPartListener2 **************
	//****************************************
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		
	}
	
	/**
	 * listens whether a part was brought to top and forwards this to the following methods:<br>
	 * {@link CommentTableView#partBroughtToTop(IWorkbenchPartReference)}
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		PluginLogger.log(this.getClass().toString(), "partBroughtToTop", partRef.getPart(false).getTitle());
		if (isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().partBroughtToTop(partRef);
		}
	}
	
	/**
	 * listens whether a part was closed and forwards this to the following methods:<br> {@link CommentTableView#partClosed(IWorkbenchPartReference)}
	 * <br> {@link DetailView#partClosedOrDeactivated(IWorkbenchPart)}
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		if (ViewControl.unregisterView(partRef.getPart(false).getClass())) {
			PluginLogger.log(this.getClass().toString(), "partClosed", "unregister: " + partRef.getPart(false).getTitle());
		}
		
		if (isOpen(DetailView.class)) {
			DetailView.getInstance().partClosedOrDeactivated(partRef.getPart(false));
		}
		if (isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().partClosed(partRef);
		}
	}
	
	/**
	 * listens whether a part was deactivated and forwards this to the following methods:<br>
	 * {@link DetailView#partClosedOrDeactivated(IWorkbenchPart)}
	 * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		PluginLogger.log(this.getClass().toString(), "partDeactivated", partRef.getPart(false).getTitle());
		if (isOpen(DetailView.class)) {
			DetailView.getInstance().partClosedOrDeactivated(partRef.getPart(false));
		}
	}
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
	}
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
	}
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
	}
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
	 */
	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		PluginLogger.log(this.getClass().toString(), "partInputChanged", partRef.getPart(false).getTitle());
		if (isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().partInputChanged(partRef);
		}
	}
	
	//****************************************
	//****** IPerspectiveListener3 ***********
	//****************************************
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPerspectiveListener2#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor,
	 *      org.eclipse.ui.IWorkbenchPartReference, java.lang.String)
	 */
	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, IWorkbenchPartReference partRef, String changeId) {
		// PluginLogger.log(this.getClass().toString(), "perspectiveChanged1", perspective.getLabel());
	}
	
	/**
	 * listens whether a perspective was activated and forwards this to the following methods:<br>
	 * {@link CommentTableView#perspectiveActivated(IWorkbenchPage, IPerspectiveDescriptor)}
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveActivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		PluginLogger.log(this.getClass().toString(), "perspectiveActivated", perspective.getLabel());
		if (perspective.getId().equals("de.tukl.cs.softech.agilereview.view.AgileReviewPerspective")) {
			if (contextActivation == null) {
				IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
				contextActivation = contextService.activateContext("de.tukl.cs.softech.agilereview.perspective.open");
				PluginLogger.log(ViewControl.class.toString(), "perspectiveActivated",
						"Context \"de.tukl.cs.softech.agilereview.perspective.open\" activated");
				
			}
			perspectiveIsOpen = true;
		} else {
			if (contextActivation != null) {
				IContextService contextService = (IContextService) PlatformUI.getWorkbench().getService(IContextService.class);
				contextService.deactivateContext(contextActivation);
				contextActivation = null;
				PluginLogger.log(ViewControl.class.toString(), "perspectiveActivated",
						"Context \"de.tukl.cs.softech.agilereview.perspective.open\" deactivated");
			}
			perspectiveIsOpen = false;
		}
		
		if (isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().perspectiveActivated(page, perspective);
		}
	}
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPerspectiveListener#perspectiveChanged(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor,
	 *      java.lang.String)
	 */
	@Override
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		// PluginLogger.log(this.getClass().toString(), "perspectiveChanged2", perspective.getLabel());
	}
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveOpened(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveOpened(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		
	}
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveClosed(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveClosed(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		
	}
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveDeactivated(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveDeactivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		// PluginLogger.log(this.getClass().toString(), "perspectiveDeactivated", perspective.getLabel());
	}
	
	/**
	 * not yet used
	 * @see org.eclipse.ui.IPerspectiveListener3#perspectiveSavedAs(org.eclipse.ui.IWorkbenchPage, org.eclipse.ui.IPerspectiveDescriptor,
	 *      org.eclipse.ui.IPerspectiveDescriptor)
	 */
	@Override
	public void perspectiveSavedAs(IWorkbenchPage page, IPerspectiveDescriptor oldPerspective, IPerspectiveDescriptor newPerspective) {
		// PluginLogger.log(this.getClass().toString(), "perspectiveSavedAs", oldPerspective.getLabel()+"-->"+newPerspective.getLabel());
	}
	
	//****************************************
	//****** IPropertyListener ***********
	//****************************************
	
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PropertiesManager.EXTERNAL_KEYS.ANNOTATION_COLOR_ENABLED)) {
			if (isOpen(CommentTableView.class)) {
				CommentTableView.getInstance().cleanEditorReferences();
				CommentTableView.getInstance().resetEditorReferences();
			}
		}
	}
}