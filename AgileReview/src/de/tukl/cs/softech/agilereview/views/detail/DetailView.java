package de.tukl.cs.softech.agilereview.views.detail;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.detail.handlers.SourceProvider;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.AbstractMultipleWrapper;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * The DetailView class manages the different UIs which can occur in the detail view
 */
public class DetailView extends ViewPart {

	/**
	 * Static Field for describing an empty view
	 */
	public static final int EMPTY = 0;
	/**
	 * Static Field for describing a view displaying comment details
	 */
	public static final int COMMENT_DETAIL = 1;
	/**
	 * Static Field for describing a view displaying review details
	 */
	public static final int REVIEW_DETAIL = 2;
	
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
	private Composite actParent;
	 /**
	  * The current instance in which the createPartControl procedure was called
	  */
	private static DetailView instance;

	/**
	 * Returns the current instance of the DetailView
	 * @return the current instance of the DetailView
	 */
	public static DetailView getInstance() {
		return instance;
	}
	
	/**
	 * changes the ViewPart UI
	 * @param type static Field of class DetailView
	 */
	public void changeParent(int type) {
		this.actParent.dispose();
		
		//get SourceProvider for configuration
		ISourceProviderService isps = (ISourceProviderService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISourceProviderService.class);
		SourceProvider sp1 = (SourceProvider) isps.getSourceProvider(SourceProvider.REPLY_POSSIBLE);
		SourceProvider sp2 = (SourceProvider) isps.getSourceProvider(SourceProvider.CONTENT_AVAILABLE);		
		
		switch(type) {
		case EMPTY:
			this.actParent = new Composite(this.parentParent, this.parentStyle);
			this.setPartName("Detail View");
			this.currentDisplay = EMPTY;
			sp1.setReplyPossible(false);
			sp2.setContentAvailable(false);
			PluginLogger.log(this.getClass().toString(), "changeParent", "to EMPTY");
			break;
		case COMMENT_DETAIL:
			this.actParent = new CommentDetail(this.parentParent, this.parentStyle);
			this.setPartName("Comment Details");
			this.currentDisplay = COMMENT_DETAIL;
			sp1.setReplyPossible(true);
			sp2.setContentAvailable(true);
			PluginLogger.log(this.getClass().toString(), "changeParent", "to COMMENT_DETAIL");
			break;
		case REVIEW_DETAIL:
			this.actParent = new ReviewDetail(this.parentParent, this.parentStyle);
			this.setPartName("Review Details");
			this.currentDisplay = REVIEW_DETAIL;
			sp1.setReplyPossible(false);
			sp2.setContentAvailable(true);
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
		if(actParent instanceof AbstractDetail) {
			return ((AbstractDetail<?>) actParent).isReparentable();
		} else {
			return false;
		}
	}
	
	/**
	 * Add Reply if and only if the comment detail part is opened
	 */
	public void addReply() {
		if(currentDisplay == COMMENT_DETAIL) {
			((CommentDetail) actParent).addReply();
		}
	}

	/**
	 * Reverts all unsaved changes
	 */
	public void revert() {
		if(actParent instanceof AbstractDetail) {
			((AbstractDetail<?>) actParent).revert();
		}
	}
	
	/**
	 * Returns the current content representation
	 * @return current content representation or null if no content is displayed
	 */
	public Object getContent() {
		if(actParent instanceof AbstractDetail) {
			return ((AbstractDetail<?>) actParent).getContent();
		} else {
			return null;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		PluginLogger.log(this.getClass().toString(), "createPartControl", "DetailView will be created");
		instance = this;

		this.actParent = parent;
		this.parentParent = parent.getParent();
		this.parentStyle = parent.getStyle();
		
		//add help context
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.parentParent, Activator.PLUGIN_ID+".DetailView");
		
		// register view
		ViewControl.registerView(this.getClass());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		switch(this.currentDisplay) {
		case EMPTY:
			break;
		case COMMENT_DETAIL:
			actParent.setFocus();
			break;
		case REVIEW_DETAIL:
			actParent.setFocus();
			break;
		}
	}
	
	/**
	 * saves every changes made in the current Detail View
	 * @param part will be forwarded from the {@link ViewControl}
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partClosedOrDeactivated(IWorkbenchPart part) {
		if(actParent instanceof AbstractDetail && !actParent.isDisposed()) {
			((AbstractDetail<?>)actParent).partClosedOrDeactivated(part);
		}
	}

	/**
	 * Reaction of selection changes in {@link CommentTableView} or {@link ReviewExplorer}
	 * @param event will be forwarded from the {@link ViewControl}
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		if(event.getSelection() instanceof IStructuredSelection && !event.getSelection().isEmpty()) {
			IStructuredSelection sel = (IStructuredSelection) event.getSelection();
			Object e = sel.getFirstElement();
			
			if(e instanceof MultipleReviewWrapper) {
				if(!(this.actParent instanceof ReviewDetail)) {
					this.changeParent(DetailView.REVIEW_DETAIL);
				}
				((ReviewDetail)this.actParent).fillContents((MultipleReviewWrapper)e);
			} else if(e instanceof AbstractMultipleWrapper) {
				this.changeParent(EMPTY);
			} else if(e instanceof Comment) {
				if(!(this.actParent instanceof CommentDetail)) {
					this.changeParent(DetailView.COMMENT_DETAIL);
					((CommentDetail)this.actParent).fillContents((Comment)e);
				}
			}
		}
	}
}
