package de.tukl.cs.softech.agilereview.views.detail;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.graphics.Color;
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
	private static final int RELOCATE_DIALOG = 3;/*?|0000020|smokie88|c5|?*/
	
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
	 * changes the ViewPart UI
	 * @param type static Field of class DetailView
	 */
	public void changeParent(int type) {
		//optimization and protection of cachedComment reset for changeParent to relocate dialog twice
		if(this.currentDisplay == type) {
			return;
		}
		
		//reset all variables
		this.currentParent.dispose();
		this.cachedComment = null;
		
		//get SourceProvider for configuration
		ISourceProviderService isps = (ISourceProviderService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(ISourceProviderService.class);
		SourceProvider sp1 = (SourceProvider) isps.getSourceProvider(SourceProvider.REPLY_POSSIBLE);
		SourceProvider sp2 = (SourceProvider) isps.getSourceProvider(SourceProvider.CONTENT_AVAILABLE);		
		
		switch(type) {
		case EMPTY:
			this.currentParent = new Composite(this.parentParent, this.parentStyle);
			this.setPartName("Detail View");
			this.currentDisplay = EMPTY;
			sp1.setVariable(SourceProvider.REPLY_POSSIBLE, false);
			sp2.setVariable(SourceProvider.CONTENT_AVAILABLE, false);
			PluginLogger.log(this.getClass().toString(), "changeParent", "to EMPTY");
			break;
		case COMMENT_DETAIL:
			this.currentParent = new CommentDetail(this.parentParent, this.parentStyle, new Color(PlatformUI.getWorkbench().getDisplay(), 185, 210, 220));/*?|0000020|smokie88|c6|?*/
			this.setPartName("Comment Details");
			this.currentDisplay = COMMENT_DETAIL;
			sp1.setVariable(SourceProvider.REPLY_POSSIBLE, true);
			sp2.setVariable(SourceProvider.CONTENT_AVAILABLE, true);
			PluginLogger.log(this.getClass().toString(), "changeParent", "to COMMENT_DETAIL");
			break;
		case REVIEW_DETAIL:
			this.currentParent = new ReviewDetail(this.parentParent, this.parentStyle, new Color(PlatformUI.getWorkbench().getDisplay(), 205, 230, 170));/*?|0000020|smokie88|c7|?*/
			this.setPartName("Review Details");
			this.currentDisplay = REVIEW_DETAIL;
			sp1.setVariable(SourceProvider.REPLY_POSSIBLE, false);
			sp2.setVariable(SourceProvider.CONTENT_AVAILABLE, true);
			PluginLogger.log(this.getClass().toString(), "changeParent", "to REVIEW_DETAIL");
			break;
		case RELOCATE_DIALOG:
			cachedComment = (Comment) getContent();
			currentParent = new RelocateDialog(this.parentParent, this.parentStyle, cachedComment);
			this.setPartName("Comment Details");
			this.currentDisplay = RELOCATE_DIALOG;
			sp1.setVariable(SourceProvider.REPLY_POSSIBLE, false);
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
	 */
	public void addReply(String author, String text) {
		if(currentDisplay == COMMENT_DETAIL) {
			((CommentDetail) currentParent).saveChanges();
			((CommentDetail) currentParent).addReply(author, text);
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
	public void selectionChanged(SelectionChangedEvent event) {/*?|0000017|Peter Reuter|c0|*/
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
		}
	}/*|0000017|Peter Reuter|c0|?*/
}
