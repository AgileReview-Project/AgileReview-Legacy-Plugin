package de.tukl.cs.softech.agilereview.views.reviewexplorer;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.xmlbeans.XmlException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE.SharedImages;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import agileReview.softech.tukl.de.ReviewDocument.Review;
import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.AbstractMultipleWrapper;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * The Review Explorer is the view which shows all reviews as well as 
 * the files and folder which are commented in the corresponding reviews.
 */
public class ReviewExplorer extends ViewPart implements IDoubleClickListener {

	/**
	 * {@link ReviewAccess} for accessing xml data
	 */
	private ReviewAccess RA = ReviewAccess.getInstance();
	/**
	 * The root node of the treeviewer
	 */
	private RERootNode root = new RERootNode();
	
	/**
	 * The tree for showing the reviews
	 */
	private TreeViewer treeViewer;
	
	/**
	 * Action for adding a review
	 */
	private Action addReviewAction;
	/**
	 * Action for deleting a review
	 */
	private Action deleteReviewAction;
	/**
	 * Action for activating a review
	 */
	private Action setActiveReviewAction;
	/**
	 * Action for Opening/Closing a review
	 */
	private Action openCloseReviewAction;
	
	/**
	 * Action for opening the files displayed in the tree viewer on double-click
	 */
	private REOpenAction openFileAction;
	/**
	 * Properties manager for repeated access 
	 */
	private PropertiesManager props = PropertiesManager.getInstance();
	
	/**
	 * Current Instance used by the ViewPart
	 */
	private static ReviewExplorer instance;
	
	/**
	 * Returns the current instance of the ReviewExplorer
	 * @return current instance
	 */
	public static ReviewExplorer getInstance()
	{
		return instance;
	}
	
	@Override
	public void createPartControl(Composite parent) 
	{
		instance = this;
		
		// Create the treeview
		treeViewer = new TreeViewer(parent);
		treeViewer.setContentProvider(new REContentProvider());
		treeViewer.setLabelProvider(new RELabelProvider());
		treeViewer.setComparator(new REViewerComparator());
		treeViewer.setInput(this.root);
		refreshInput();
		
		// Define the actions for toolbar
		addReviewAction = new Action("Add review"){
			public void run() {
				addNewReview();
			}
		};
		addReviewAction.setToolTipText("Add a new review");
		addReviewAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, this.props.getInternalProperty(PropertiesManager.INTERNAL_KEYS.ICONS.REVIEW_ADD)));
		
		deleteReviewAction = new Action("Delete review"){
			public void run() {
				deleteSelectedReviews();
			}
		};
		deleteReviewAction.setToolTipText("Delete the selected review");
		deleteReviewAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, this.props.getInternalProperty(PropertiesManager.INTERNAL_KEYS.ICONS.REVIEW_DELETE)));
		
		setActiveReviewAction = new Action("Activate review"){
			public void run() {
				activateSelectedReview();
			}
		};
		setActiveReviewAction.setToolTipText("Activates the selected review. This means all newly created comments will be saved in this review.");
		setActiveReviewAction.setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, this.props.getInternalProperty(PropertiesManager.INTERNAL_KEYS.ICONS.REVIEW_OK)));
		
		openCloseReviewAction = new Action("Open/Close Review"){
			public void run() {
				openCloseReview();
			}
		};
		openCloseReviewAction.setToolTipText("Opens or Closes a review. Closed reviews are not loaded.");
		openCloseReviewAction.setImageDescriptor(ImageDescriptor.createFromImage(PlatformUI.getWorkbench().getSharedImages().getImage(SharedImages.IMG_OBJ_PROJECT_CLOSED)));
		
		//TODO export Action
		/*Action tmp = new Action("export"){
			public void run() {
				List<Review> reviews = new ArrayList<Review>();
				for(MultipleReviewWrapper r : root.getReviews()) {
					reviews.add(r.getWrappedReview());
				}
				XSLExport.exportReviews(reviews);
			}
		};*/
		
		
		openFileAction = new REOpenAction(this.getSite().getPage(), treeViewer);
		
		
		// Create toolbar
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
		toolbarManager.add(addReviewAction);
		toolbarManager.add(deleteReviewAction);
		toolbarManager.add(setActiveReviewAction);
		toolbarManager.add(openCloseReviewAction);
		
		//TODO export Action
		//toolbarManager.add(tmp);
		
		treeViewer.addDoubleClickListener(this);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, Activator.PLUGIN_ID+".ReviewExplorer");
		getSite().setSelectionProvider(treeViewer);
		
		
		// register view
		ViewControl.registerView(this.getClass());
	}
	
	/**
	 * Creates a new review and displays it in the tree
	 */
	private void addNewReview() 
	{
		InputDialog in = new InputDialog(null ,"Add new Review", "Please enter a name for the new Review", "", PropertiesManager.getInstance());
		int input = in.open();
		if (input == Window.OK)
		{
			try 
			{
				Review newRev = RA.createNewReview(in.getValue().trim());
				MultipleReviewWrapper rWrap = new MultipleReviewWrapper(newRev, newRev.getId());
				// When a new review is created this should be open an activated
				rWrap.setOpen(true);
				this.props.addToOpenReviews(newRev.getId());
				this.props.setExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW, newRev.getId());
				
				root.addReview(rWrap);
				this.refresh();
				this.treeViewer.setSelection(new StructuredSelection(rWrap), true);
			} catch (IOException e) 
			{
				// TODO: Auto-generated method stub
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Deletes the selected reviews
	 */
	private void deleteSelectedReviews()
	{
		if (treeViewer.getSelection().isEmpty()) {
			return;
		}
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		Iterator<?> iterator = selection.iterator();
		HashSet<MultipleReviewWrapper> hsMRW = new HashSet<MultipleReviewWrapper>(); 
		while (iterator.hasNext()) {
			Object item = iterator.next();
			if (item instanceof MultipleReviewWrapper)
			{
				hsMRW.add((MultipleReviewWrapper)item);
			}	
		}
		if (!hsMRW.isEmpty() && !MessageDialog.openConfirm(null, "Delete review", "Are you sure you want to delete all selected reviews? All comments of this review will alse be deleted"))
		{
			return;
		}
		treeViewer.getTree().setRedraw(false);
		for (MultipleReviewWrapper wrap : hsMRW)
		{
			// Delete comments of this review from TableView
			for (Comment c : RA.getComments(wrap.getReviewId()))
			{
				CommentTableView.getInstance().deleteComment(c);
			}
			
			// Delete the selected review
			RA.deleteReview(wrap.getReviewId());
			root.deleteReview(wrap);
			// Check if this was the active review
			if (props.getExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW).equals(wrap.getReviewId()))
			{
				props.setExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW, "");
			}
			// Remove this review from the list of open reviews (regardless if it was open or not)
			props.removeFromOpenReviews(wrap.getReviewId());

			this.refresh();
		}
		treeViewer.getTree().setRedraw(true);
	}
	
	/**
	 * Activates the selected review
	 */
	private void activateSelectedReview()
	{
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		if (!selection.isEmpty())
		{	
			@SuppressWarnings("unchecked")
			Iterator<Object> it = selection.iterator();
			boolean sameReview = true;
			String referenceRevId = "";
			while (it.hasNext() && sameReview)
			{
				Object o = it.next();
				if (o instanceof AbstractMultipleWrapper)
				{
					AbstractMultipleWrapper wrap = (AbstractMultipleWrapper)o;
					if (referenceRevId.isEmpty())
					{
						referenceRevId = wrap.getReviewId();
					}
					sameReview = referenceRevId.equals(wrap.getReviewId());
				}
			}
			
			// If not all selected items belong to the same review, give a warning
			if (!sameReview)
			{
				MessageDialog.openWarning(null, "Error occured: Could not activate review", "Only one review can be \"active\" at a time");
			}
			else if (!props.isReviewOpen(referenceRevId))
			{
				MessageDialog.openWarning(null, "Error occured: Could not activate review", "Only open reviews can be activated");
			}
			else
			{
				props.setExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW, referenceRevId);
				this.refresh();
			}
		}
	}
	
	
	/**
	 * Method for opening or closing the currently selected review
	 */
	private void openCloseReview()
	{
		IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
		if (selection.size()==1)
		{	
			Object o = selection.getFirstElement();
			if (o instanceof MultipleReviewWrapper)
			{
				MultipleReviewWrapper selectedWrap = (MultipleReviewWrapper)o;
				String reviewId = selectedWrap.getReviewId();
				if (selectedWrap.isOpen())
				{	
					// Review is open --> close it
					selectedWrap.setOpen(false);
					ReviewAccess.getInstance().unloadReviewComments(reviewId);
					this.props.removeFromOpenReviews(reviewId);
					
					// Test if active review may have vanished
					String activeReview = props.getExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW);
					if (!activeReview.isEmpty())
					{
						if (!ReviewAccess.getInstance().isReviewLoaded(activeReview))
						{
							// Active review has vanished --> deactivate it
							props.setExternalPreference(PropertiesManager.EXTERNAL_KEYS.ACTIVE_REVIEW, "");
						}
					}
				}
				else
				{
					// Review is closed --> open it
					selectedWrap.setOpen(true);
					try 
					{
						ReviewAccess.getInstance().loadReviewComments(reviewId);
					} catch (XmlException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					this.props.addToOpenReviews(reviewId);
				}	
				this.refresh();
				try {
					if(ViewControl.isOpen(CommentTableView.class)) {
						CommentTableView.getInstance().resetComments();
					}
				} catch (XmlException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Refreshes the tree viewer. Also expands all previously expanded nodes afterwards.
	 */
	public void refresh()
	{
		this.treeViewer.getControl().setRedraw(false);
		Object[] expandedElements = this.treeViewer.getExpandedElements();
		this.treeViewer.refresh();
		
		for (Object o : expandedElements)
		{
			this.treeViewer.expandToLevel(o, 1);
		}
		this.treeViewer.getControl().setRedraw(true);
		this.treeViewer.getControl().redraw();
	}
	
	
	/**
	 * Sets the input of the ReviewExplorer completely new
	 */
	public void refreshInput()
	{
		// Save expansion state
		this.treeViewer.getControl().setRedraw(false);
		TreePath[] expandedElements = this.treeViewer.getExpandedTreePaths();
		
		// Refresh the input
		this.root.clear();
		for (Review r : RA.getAllReviews())
		{
			MultipleReviewWrapper currWrap = new MultipleReviewWrapper(r, r.getId());
			// Check if review is "open"
			currWrap.setOpen(props.isReviewOpen(r.getId()));
			root.addReview(currWrap);
		}
		
		// Expand nodes again
		this.treeViewer.refresh();
		for (Object o : expandedElements)
		{
			this.treeViewer.expandToLevel(o, 1);
		}
		this.treeViewer.getControl().setRedraw(true);
		this.treeViewer.getControl().redraw();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	@Override
	public void doubleClick(DoubleClickEvent event) {
		if(openFileAction.isEnabled()){
			openFileAction.run();
		}
	}

	@Override
	public void setFocus() {}
	
}
