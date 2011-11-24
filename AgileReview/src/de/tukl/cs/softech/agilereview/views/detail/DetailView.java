package de.tukl.cs.softech.agilereview.views.detail;

import java.util.Calendar;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.services.ISourceProviderService;

import agileReview.softech.tukl.de.CommentDocument.Comment;
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
public class DetailView extends ViewPart {

	/**
	 * Static Field describing an empty view
	 */
	public static final int EMPTY = 0;
	/**
	 * Static Field describing a view displaying comment details
	 */
	private static final int COMMENT_DETAIL = 1;
	/**
	 * Static Field describing a view displaying review details
	 */
	private static final int REVIEW_DETAIL = 2;
	/**
	 * Static Field describing a view displaying the relocate dialog
	 */
	private static final int RELOCATE_DIALOG = 3;
	
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
	 * Cached comment when temporary showing the relocate dialog
	 */
	private Comment cachedComment;
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
		if(this.currentDisplay == type) {
			return;
		}
		
		//reset all variables
		this.currentParent.dispose();
		this.cachedComment = null;
		
		//get SourceProvider for configuration
		ISourceProviderService isps = (ISourceProviderService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISourceProviderService.class);
		SourceProvider sp1 = (SourceProvider) isps.getSourceProvider(SourceProvider.COMMENT_SHOWN);
		SourceProvider sp2 = (SourceProvider) isps.getSourceProvider(SourceProvider.CONTENT_AVAILABLE);
		
		switch(type) {
		case EMPTY:
			this.currentParent = new Composite(this.parentParent, this.parentStyle);
			this.setPartName("Detail View");
			this.currentDisplay = EMPTY;
			sp1.setVariable(SourceProvider.COMMENT_SHOWN, false);
			sp2.setVariable(SourceProvider.CONTENT_AVAILABLE, false);
			PluginLogger.log(this.getClass().toString(), "changeParent", "to EMPTY");
			break;
		case COMMENT_DETAIL:
			this.currentParent = new CommentDetail(this.parentParent, this.parentStyle);
			this.setPartName("Comment Details");
			this.currentDisplay = COMMENT_DETAIL;
			sp1.setVariable(SourceProvider.COMMENT_SHOWN, true);
			sp2.setVariable(SourceProvider.CONTENT_AVAILABLE, true);
			PluginLogger.log(this.getClass().toString(), "changeParent", "to COMMENT_DETAIL");
			break;
		case REVIEW_DETAIL:
			this.currentParent = new ReviewDetail(this.parentParent, this.parentStyle);
			this.setPartName("Review Details");
			this.currentDisplay = REVIEW_DETAIL;
			sp1.setVariable(SourceProvider.COMMENT_SHOWN, false);
			sp2.setVariable(SourceProvider.CONTENT_AVAILABLE, true);
			PluginLogger.log(this.getClass().toString(), "changeParent", "to REVIEW_DETAIL");
			break;
		case RELOCATE_DIALOG:
			cachedComment = (Comment) getContent();
			currentParent = new RelocateDialog(this.parentParent, this.parentStyle, cachedComment);
			this.setPartName("Comment Details");
			this.currentDisplay = RELOCATE_DIALOG;
			sp1.setVariable(SourceProvider.COMMENT_SHOWN, false);
			sp2.setVariable(SourceProvider.CONTENT_AVAILABLE, false);
			PluginLogger.log(this.getClass().toString(), "changeParent", "to RELOCATE_DIALOG");
			break;
		}
		this.parentParent.layout(true);
	}
	
	/**
	 * Returns whether the current parent is revertable or not
	 * @return true, if the current parent is revertable<br>false, otherwise
	 */
	public boolean isRevertable() {
		if(currentParent instanceof AbstractDetail<?>) {
			return ((AbstractDetail<?>) currentParent).isReparentable();
		} else {
			return false;
		}
	}
	
	/**
	 * Add Reply if and only if the comment detail part is opened
	 * @param author author of the reply
	 * @param text text of the reply
	 * @param creationDate of the reply
	 */
	public void addReply(String author, String text, Calendar creationDate) {
		if(currentDisplay == COMMENT_DETAIL) {
			((CommentDetail) currentParent).addReply(author, text, creationDate);
			//save the current comment in order to save the reply creation time
			((CommentDetail) currentParent).partClosedOrDeactivated(this);/*?|r93|Malte|c7|?*/
		}
	}

	/**
	 * Reverts all unsaved changes
	 */
	public void revert() {
		if(currentParent instanceof AbstractDetail<?>) {
			((AbstractDetail<?>) currentParent).revert();
		}
	}
	
	/**
	 * Returns the current content representation
	 * @return current content representation or null if no content is displayed
	 */
	public Object getContent() {
		if(cachedComment != null) {
			return cachedComment;
		} else if(currentParent instanceof AbstractDetail<?>) {
			return ((AbstractDetail<?>) currentParent).getContent();
		} else {
			return null;
		}
	}
	
	/**
	 * Triggers the relocation process for the current shown comment 
	 * and checks some security issues
	 */
	public void relocateComment() {
		//for security reasons, but should not occur
		if(!(getContent() instanceof Comment)) {
			return; 
		}
		
		//initiate or finish the relocation with the same command
		if(currentParent instanceof RelocateDialog) {
			((RelocateDialog)currentParent).performCommentRelocation();
		} else {
			changeParent(RELOCATE_DIALOG);
		}
	}
	
	/**
	 * Should only be called if the intended background of the view was changed by the user
	 */
	public void refreshBackgroundColor() {
		if(this.currentParent instanceof CommentDetail) {/*?|r59|Malte|c8|*/
			((CommentDetail)this.currentParent).refreshBackgroundColor();/*?|r88|Peter|c0|?*/
		}/*|r59|Malte|c8|?*/
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		PluginLogger.log(this.getClass().toString(), "createPartControl", "DetailView will be created");
		instance = this;

		this.currentParent = parent;
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
			currentParent.setFocus();
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
		if(currentParent instanceof AbstractDetail<?> && !currentParent.isDisposed()) {
			((AbstractDetail<?>)currentParent).partClosedOrDeactivated(part);
		}
	}

	/**
	 * Reaction of selection changes in {@link CommentTableView} or {@link ReviewExplorer}
	 * @param event will be forwarded from the {@link ViewControl}
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		if(event.getSelection().isEmpty()) {
			this.changeParent(DetailView.EMPTY);
		} else if(event.getSelection() instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) event.getSelection();
			Object e = sel.getFirstElement();
			if(e instanceof MultipleReviewWrapper) {
				if(!(this.currentParent instanceof ReviewDetail)) {
					this.changeParent(DetailView.REVIEW_DETAIL);
				}
				((ReviewDetail)this.currentParent).fillContents((MultipleReviewWrapper)e);
			} else if(e instanceof Comment) {
				if(!(this.currentParent instanceof CommentDetail)) {
					this.changeParent(DetailView.COMMENT_DETAIL);
				}
				((CommentDetail)this.currentParent).fillContents((Comment)e);
			}
			refreshBackgroundColor();/*?|r59|Malte|c9|?*/
		}
	}
}