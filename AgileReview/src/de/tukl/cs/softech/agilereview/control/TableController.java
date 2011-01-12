package de.tukl.cs.softech.agilereview.control;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.part.Part;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import de.tukl.cs.softech.agilereview.model.wrapper.AbstractMultipleWrapper;
import de.tukl.cs.softech.agilereview.model.wrapper.MultipleReviewWrapper;
import de.tukl.cs.softech.agilereview.view.CommentTableView;
import de.tukl.cs.softech.agilereview.view.ReviewExplorer;
import de.tukl.cs.softech.agilereview.view.commenttable.ExplorerSelectionFilter;

import agileReview.softech.tukl.de.CommentDocument.Comment;

public class TableController implements Listener, ISelectionListener, IPartListener2 {
	
	/**
	 * 
	 */
	private static TableController instance = new TableController();
	/**
	 * 
	 */
	private HashMap<ITextEditor, AnnotationModel> annotationModelMap = new HashMap<ITextEditor, AnnotationModel>();
	/**
	 * 
	 */
	private ArrayList<Comment> comments;
	/**
	 * 
	 */
	private ArrayList<Comment> filteredComments;
	/**
	 * 
	 */
	private boolean linkExplorer = true;
	/**
	 * 
	 */
	private CommentTableView commentTableView = CommentTableView.getInstance();
	private ExplorerSelectionFilter selectionFilter;
	private TableViewer viewer = commentTableView.getViewer();
	
	private TableController() {
		super();
	}
	
	public static TableController getInstance() {
		return instance;
	}
	
	public void setComments(ArrayList<Comment> comments) {
		this.comments = comments;
		// refresh table
		// add annotations
	}
	
	private void filterComments() {
		
	}
	
	public void addComment(Comment comment) {
		
	}
	
	public void deleteComment(Comment comment) {
		
	}
	
	public void refreshTableContent() {
		
	}
	
	private String getEditorPath() {
		IEditorPart currentEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (currentEditor == null) {
			// eclipse is closing
			return "";
		}
		FileEditorInput input = (FileEditorInput) currentEditor.getEditorInput(); 
		String editorPath = ((FileEditorInput)input).getFile().getLocation().toOSString();
		return editorPath;
	}
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (linkExplorer && part != null && part instanceof ReviewExplorer && selection != null && selection instanceof IStructuredSelection && !selection.isEmpty()) {
			//if there is a selectionFilter, remove it
			if (this.selectionFilter != null) {
				// TODO: adapt the following -> viewer.removeFilter(this.selectionFilter);						
			}

			//get selection, selection's iterator, initialize reviewIDs and paths
			IStructuredSelection sel = (IStructuredSelection) selection;
			Iterator<?> it = sel.iterator();
			ArrayList<String> reviewIDs = new ArrayList<String>();
			HashMap<String, HashSet<String>> paths = new HashMap<String, HashSet<String>>(); 
			
			// get all selected reviews and paths
			while (it.hasNext()) {
				Object next = it.next();
				if (next instanceof MultipleReviewWrapper) {
					String reviewID = ((MultipleReviewWrapper)next).getWrappedReview().getId();
					if (!reviewIDs.contains(reviewID)) {
						reviewIDs.add(reviewID);
					}
				} else if (next instanceof AbstractMultipleWrapper) {
					String path = ((AbstractMultipleWrapper)next).getPath();
					String reviewID = ((AbstractMultipleWrapper)next).getReviewId();
					if (paths.containsKey(reviewID)) {
						paths.get(reviewID).add(path);
					} else {
						paths.put(reviewID, new HashSet<String>());
						paths.get(reviewID).add(path);
					}
					System.out.println(path);
				}
			}

			// add a new filter by the given criteria to the viewer
			this.selectionFilter = new ExplorerSelectionFilter(reviewIDs, paths);
			viewer.addFilter(this.selectionFilter);
			
			// refresh annotations, update list of filtered comments
			// TODO: adapt the following -> deleteAnnotations(getEditorPath());
			// TODO: adapt the following -> filterComments();
			// TODO: adapt the following -> addAnnotations(getEditorPath());
		}
	}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleEvent(Event event) {
		// TODO Auto-generated method stub
		
	}
}
