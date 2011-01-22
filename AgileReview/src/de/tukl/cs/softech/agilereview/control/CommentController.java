package de.tukl.cs.softech.agilereview.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.view.CommentTableView;
import de.tukl.cs.softech.agilereview.view.DetailView;
import de.tukl.cs.softech.agilereview.view.ReviewExplorer;
import de.tukl.cs.softech.agilereview.view.ViewControl;
import de.tukl.cs.softech.agilereview.view.detail.CommentDetail;
import de.tukl.cs.softech.agilereview.view.detail.ReviewDetail;

/**
 * The CommentController describes an interface for some complex interactions of the different ViewParts.
 * For example the CommentController observes all selections of the CommentTableView and the current active
 * ReviewID of the ReviewExplorer.<br>
 * Besides this the CommentController provides Listener for any save action according the persistent storage.
 */
public class CommentController extends Observable implements Listener, ISelectionChangedListener {
	
	/**
	 * unique instance of CommentController
	 */
	private static final CommentController instance = new CommentController();
	/**
	 * unique instance of ReviewAccess
	 */
	private ReviewAccess ra = ReviewAccess.getInstance();
	/**
	 * current Selection of CommentTable
	 */
	private ArrayList<Comment> currentSelection;
	
	/**
	 * returns the unique instance of CommentController
	 * @return the unique instance of CommentController
	 */
	public static CommentController getInstance() {
		return instance;
	}
	
	/**
	 * Creates a new instance of the CommentController
	 */
	private CommentController() {

	}
	
	/**
	 * Adds a new comment
	 */
	private void addNewComment()  {
		if (!PropertiesManager.getInstance().getActiveReview().isEmpty())
		{
			try
			{
				String pathToFile = "";
				IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if ( part instanceof ITextEditor ) {
					IEditorInput input = part.getEditorInput();
					if (input != null && input instanceof FileEditorInput) {
						pathToFile = ((FileEditorInput)input).getFile().getFullPath().toOSString().replaceFirst(Pattern.quote(System.getProperty("file.separator")), "");
					}
				}
				String activeReview = PropertiesManager.getInstance().getActiveReview();
				String user  = PropertiesManager.getInstance().getUser();
				// TODO: What to do, if user is not valid
				Comment newComment = ra.createNewComment(activeReview , user, pathToFile);
				if(ViewControl.isOpen(CommentTableView.class)) {
					CommentTableView.getInstance().addComment(newComment);
				}
				// Refresh the Review Explorer
				if(ViewControl.isOpen(ReviewExplorer.class)) {
					ReviewExplorer.getInstance().refresh();
				}
			}
			catch (IOException e)
			{
				//TODO Auto-generated
				e.printStackTrace();
			}

		}
		else
		{
			MessageDialog.openWarning(null, "Warning: No active review", "Please activate a review before adding comments!");
		}
	}

	/**
	 * Deletes the selected comment
	 */
	private void deleteComment() {
		if(ViewControl.isOpen(CommentTableView.class)) {
			ArrayList<Comment> copy = new ArrayList<Comment>(currentSelection);
			Iterator<Comment> it = copy.iterator();
			CommentTableView ctv = CommentTableView.getInstance();
			while (it.hasNext()) {
				Comment c = it.next();
				ctv.deleteComment(c);
				try {
					ra.deleteComment(c);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		if(ViewControl.isOpen(DetailView.class)) {
			DetailView.getInstance().changeParent(DetailView.EMPTY);
		}
		// Refresh the Review Explorer
		if(ViewControl.isOpen(ReviewExplorer.class)) {
			ReviewExplorer.getInstance().refresh();
		}
	}

	/**
	 * manages:<br>
	 * "save" events of {@link CommentDetail} or {@link ReviewDetail}<br>
	 * "add" events of {@link CommentTableView} or {@link CommentDetail}<br>
	 * "delete" events of {@link CommentTableView} or {@link CommentDetail}
	 * @see org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		if((event.widget.getData()) instanceof String) {
			if(((String)event.widget.getData()).equals("save")) {
				if(ViewControl.isOpen(CommentTableView.class)) {
					CommentTableView.getInstance().refreshComments();
				}
			} else if(((String)event.widget.getData()).equals("delete")) {
				deleteComment();
			} else if(((String)event.widget.getData()).equals("add")) {
				addNewComment();
			}
			
			// Save the changes to XML
			try {
				ReviewAccess.getInstance().save();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * hears for the current selection of the CommentTableView
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		currentSelection = new ArrayList<Comment>();
		ISelection selection = event.getSelection();		
		if (selection != null && selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection) selection;			
			for (Iterator<?> iterator = sel.iterator(); iterator.hasNext();) {
				Comment selComment = (Comment)iterator.next();
				currentSelection.add(selComment);
			}
		}
	}
}
