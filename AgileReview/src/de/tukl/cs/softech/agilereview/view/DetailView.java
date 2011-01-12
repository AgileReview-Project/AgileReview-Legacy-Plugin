package de.tukl.cs.softech.agilereview.view;

import agileReview.softech.tukl.de.CommentDocument.Comment;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.model.wrapper.AbstractMultipleWrapper;
import de.tukl.cs.softech.agilereview.model.wrapper.MultipleReviewWrapper;
import de.tukl.cs.softech.agilereview.view.detail.CommentDetail;
import de.tukl.cs.softech.agilereview.view.detail.ReviewDetail;

/**
 * The DetailView class manages the different UIs which can occur in the detail view
 */
public class DetailView extends ViewPart implements ISelectionListener {

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
	 * Returns the current instance of the CommentDetailView
	 * @return the current instance of the CommentDetailView
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
		switch(type) {
		case EMPTY:
			this.actParent = new Composite(this.parentParent, this.parentStyle);
			this.setPartName("Detail View");
			this.currentDisplay = EMPTY;
			break;
		case COMMENT_DETAIL:
			this.actParent = new CommentDetail(this.parentParent, this.parentStyle);
			this.setPartName("Comment Detail");
			this.currentDisplay = COMMENT_DETAIL;
			break;
		case REVIEW_DETAIL:
			this.actParent = new ReviewDetail(this.parentParent, this.parentStyle);
			this.setPartName("Review Detail");
			this.currentDisplay = REVIEW_DETAIL;
			break;
		}
		this.parentParent.layout(true);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		instance = this;

		getSite().getPage().addSelectionListener(this);
		this.actParent = parent;
		this.parentParent = parent.getParent();
		this.parentStyle = parent.getStyle();
		
		//add help context
		PlatformUI.getWorkbench().getHelpSystem().setHelp(this.parentParent, Activator.PLUGIN_ID+".DetailView");
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
	 * hears for selections inside the review table and from the review explorer
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if(selection != null && part != null) {
			if(part instanceof CommentTableView) {
				if(selection instanceof IStructuredSelection && !selection.isEmpty()) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					if(!(this.actParent instanceof CommentDetail)) {
						this.changeParent(DetailView.COMMENT_DETAIL);
					}
					Object e;
					if((e = sel.getFirstElement()) instanceof Comment) {
						((CommentDetail)this.actParent).fillContents((Comment)e);
					}
				}
			} else if(part instanceof ReviewExplorer) {
				if(selection instanceof IStructuredSelection && !selection.isEmpty()) {
					IStructuredSelection sel = (IStructuredSelection) selection;
					Object e;
					if((e = sel.getFirstElement()) instanceof MultipleReviewWrapper) {
						if(!(this.actParent instanceof ReviewDetail)) {
							this.changeParent(DetailView.REVIEW_DETAIL);
						}
						((ReviewDetail)this.actParent).fillContents((MultipleReviewWrapper)e);
					} else if((e = sel.getFirstElement()) instanceof AbstractMultipleWrapper) {
						this.changeParent(EMPTY);
					}
				}
			}
		}
	}
}
