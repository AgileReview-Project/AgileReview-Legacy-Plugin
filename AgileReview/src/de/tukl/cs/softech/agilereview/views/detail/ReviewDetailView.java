package de.tukl.cs.softech.agilereview.views.detail;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.plugincontrol.SourceProvider;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * The DetailView class manages the different UIs which can occur in the detail view
 */
public class ReviewDetailView extends ViewPart {
    
    /** View ID */
    public static final String VIEW_ID = "de.tukl.cs.softech.agilereview.view.reviewdetailview.view";
    
    /**
     * Static Field describing an empty view
     */
    public static final int EMPTY = 0;
    /**
     * Static Field describing a view displaying review details
     */
    private static final int REVIEW_DETAIL = 2;
    
    /**
     * The current shown UI (one of the defined static fields)
     */
    private int currentDisplay;
    /**
     * The parent of the parent with which this view is initialized in the createPartControl
     */
    private Composite parentParent;
    /**
     * The style of the parent of the parent with which this view is initialized in the createPartControl
     */
    private int parentStyle;
    /**
     * The current parent composite, which will change for different view sites
     */
    private Composite currentParent;
    /**
     * The current instance in which the createPartControl procedure was called
     */
    private static ReviewDetailView instance;
    
    /**
     * Returns the current instance of the DetailView
     * @return the current instance of the DetailView
     */
    public static ReviewDetailView getInstance() {
        return instance;
    }
    
    /**
     * Changes the ViewPart UI to {@link #EMPTY}
     */
    public void clearView() {
        changeParent(EMPTY);
    }
    
    /**
     * changes the ViewPart UI
     * @param type static Field of class DetailView
     */
    private void changeParent(int type) {
        //optimization and protection of cachedComment reset for changeParent to relocate dialog twice
        if (this.currentDisplay == type && !currentParent.isDisposed()) {
            return;
        }
        System.out.println("ChangeParent " + parentParent);
        
        //reset all variables
        this.currentParent.dispose();
        
        //get SourceProvider for configuration
        ISourceProviderService isps = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISourceProviderService.class);
        SourceProvider sp2 = (SourceProvider) isps.getSourceProvider(SourceProvider.REVIEW_CONTENT_AVAILABLE);
        
        switch (type) {
        case EMPTY:
            this.currentParent = new Composite(this.parentParent, this.parentStyle);
            this.setPartName("Review Details");
            this.currentDisplay = EMPTY;
            sp2.setVariable(SourceProvider.REVIEW_CONTENT_AVAILABLE, false);
            PluginLogger.log(this.getClass().toString(), "changeParent", "to EMPTY");
            break;
        case REVIEW_DETAIL:
            System.out.println("Old " + currentParent);
            this.currentParent = new ReviewDetail(this.parentParent, this.parentStyle);
            System.out.println("New " + currentParent);
            System.out.println("ParentParent " + parentParent);
            this.setPartName("Review Details");
            this.currentDisplay = REVIEW_DETAIL;
            System.out.println("Switch to Review Detail");
            sp2.setVariable(SourceProvider.REVIEW_CONTENT_AVAILABLE, true);
            PluginLogger.log(this.getClass().toString(), "changeParent", "to REVIEW_DETAIL");
            break;
        }
        this.parentParent.layout(true);
    }
    
    /**
     * Returns whether the current parent is revertable or not
     * @return true, if the current parent is revertable<br>false, otherwise
     */
    public boolean isRevertable() {
        if (currentParent instanceof AbstractDetail<?>) {
            return ((AbstractDetail<?>) currentParent).isReparentable();
        } else {
            return false;
        }
    }
    
    /**
     * Reverts all unsaved changes
     */
    public void revert() {
        if (currentParent instanceof AbstractDetail<?>) {
            ((AbstractDetail<?>) currentParent).revert();
        }
    }
    
    /**
     * Returns the current content representation
     * @return current content representation or null if no content is displayed
     */
    public Object getContent() {
        if (currentParent instanceof AbstractDetail<?>) {
            return ((AbstractDetail<?>) currentParent).getContent();
        } else {
            return null;
        }
    }
    
    /**
     * Should only be called if the intended background of the view was changed by the user
     */
    public void refreshBackgroundColor() {
        if (this.currentParent instanceof CommentDetail) {
            ((CommentDetail) this.currentParent).refreshBackgroundColor();
        }
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent) {
        PluginLogger.log(this.getClass().toString(), "createPartControl", "ReviewDetailView will be created");
        instance = this;
        System.out.println("Set instance " + instance);
        
        this.currentParent = parent;
        this.parentParent = parent.getParent();
        this.parentStyle = parent.getStyle();
        
        //add help context
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this.parentParent, Activator.PLUGIN_ID + ".DetailView");
        
        // register view
        ViewControl.registerView(this.getClass());
        System.out.println("View registered " + this);
    }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus() {
        switch (this.currentDisplay) {
        case EMPTY:
            break;
        case REVIEW_DETAIL:
            currentParent.setFocus();
            break;
        }
    }
    
    /**
     * saves every changes made in the current Detail View
     * @param part will be forwarded from the {@link ViewControl}
     * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partClosedOrDeactivated(IWorkbenchPart part) {
        if (currentParent instanceof AbstractDetail<?> && !currentParent.isDisposed()) {
            ((AbstractDetail<?>) currentParent).partClosedOrDeactivated(part);
        }
    }
    
    /**
     * Reaction of selection changes in {@link CommentTableView} or {@link ReviewExplorer}
     * @param event will be forwarded from the {@link ViewControl}
     */
    public void selectionChanged(SelectionChangedEvent event) {
        if (!event.getSelection().isEmpty() && event.getSelection() instanceof IStructuredSelection) {
            IStructuredSelection sel = (IStructuredSelection) event.getSelection();
            Object e = sel.getFirstElement();
            if (e instanceof MultipleReviewWrapper) {
                if (!(this.currentParent instanceof ReviewDetail)) {
                    this.changeParent(ReviewDetailView.REVIEW_DETAIL);
                }
                System.out.println("Fill contents " + e);
                ((ReviewDetail) this.currentParent).fillContents((MultipleReviewWrapper) e);
            }
            refreshBackgroundColor();
        }
    }
}