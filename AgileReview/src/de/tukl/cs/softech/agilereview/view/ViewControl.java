package de.tukl.cs.softech.agilereview.view;

import java.util.HashSet;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener3;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.ViewPart;

/**
 * The ViewControl provides information about the current displayed ViewPart of
 * this plugin. Furthermore the ViewControl provides and forwards events of the
 * following Listener: {@link ISelectionListener}, {@link IPartListener2}
 */
public class ViewControl implements ISelectionListener, IPartListener2, IPerspectiveListener3 {
	
	/**
	 * Set of all active Views
	 */
	private static HashSet<Class<? extends ViewPart>> activeViews = new HashSet<Class<? extends ViewPart>>();
	/**
	 * Instance of ViewControl in order to add all listeners
	 */
	@SuppressWarnings("unused")
	private static ViewControl instance = new ViewControl();
	
	/**
	 * Creates a new instance of ViewControl
	 */
	private ViewControl() {
		//wait until the active page is created, then register all listeners
		Display.getCurrent().asyncExec(new Runnable() {
			
			@Override
			public void run() {
				while(PlatformUI.getWorkbench() == null) {}
				while(PlatformUI.getWorkbench().getActiveWorkbenchWindow() == null) {}
				while(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() == null) {}
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addPartListener(ViewControl.this);
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().addSelectionListener(ViewControl.this);
				// register this class as a perspective listener
				IPageService service = (IPageService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IPageService.class);
				service.addPerspectiveListener(ViewControl.this);
			}
		});
	}
	
	/**
	 * Registers a new {@link ViewPart} for the plugin
	 * @param c Class of the {@link ViewPart} to register
	 * @return true if the registration was successful
	 */
	protected static boolean registerView(Class<? extends ViewPart> c) {
		System.out.println("registered: "+c);
		return activeViews.add(c);
	}
	
	/**
	 * Unregisters a given {@link ViewPart} for the plugin
	 * @param c Class of the {@link ViewPart} to unregister
	 * @return true if the given {@link ViewPart} was unregistered successfully
	 */
	protected static boolean unregisterView(Class<? extends ViewPart> c) {
		System.out.println("unregistered: "+c);
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
	
	//****************************************
	//****** ISelectionListener **************
	//****************************************
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(selection != null && part != null) {
			if(isOpen(CommentTableView.class)) {
				CommentTableView.getInstance().selectionChanged(part, selection);
			}
			if(isOpen(DetailView.class)) {
				DetailView.getInstance().selectionChanged(part, selection);
			}
		}
	}
	
	//****************************************
	//********** IPartListener2 **************
	//****************************************

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if(isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().partBroughtToTop(partRef);
		}
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		activeViews.remove(partRef.getPart(false));
		
		if(isOpen(DetailView.class)) {
			DetailView.getInstance().partClosedOrDeactivated(partRef.getPart(false));
		}
		
		if(isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().partClosed(partRef);
		}
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		if(isOpen(DetailView.class)) {
			DetailView.getInstance().partClosedOrDeactivated(partRef.getPart(false));
		}		
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}
	
	//****************************************
	//******* IPerspectiveListener3 **********
	//****************************************

	@Override
	public void perspectiveClosed(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void perspectiveDeactivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void perspectiveOpened(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void perspectiveSavedAs(IWorkbenchPage page,
			IPerspectiveDescriptor oldPerspective,
			IPerspectiveDescriptor newPerspective) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective,
			IWorkbenchPartReference partRef, String changeId) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void perspectiveActivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) {
		if(isOpen(CommentTableView.class)) {
			CommentTableView.getInstance().perspectiveActivated(page, perspective);
		}
		
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective, String changeId) {
		// TODO Auto-generated method stub
		
	}
}
