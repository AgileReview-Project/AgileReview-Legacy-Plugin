package de.tukl.cs.softech.agilereview.views.commenttable;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.annotations.AnnotationParser;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.plugincontrol.CommentController;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.detail.DetailView;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.ReviewExplorer;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.AbstractMultipleWrapper;
import de.tukl.cs.softech.agilereview.views.reviewexplorer.wrapper.MultipleReviewWrapper;

/**
 * Used to provide an overview for review comments using a table
 */
public class CommentTableView extends ViewPart implements IDoubleClickListener {

	/**
	 * Current Instance used by the ViewPart
	 */
	private static CommentTableView instance;
	/**
	 * The comments to be displayed (model of TableViewer viewer) 
	 */
	private ArrayList<Comment> comments;
	/**
	 * The comments filtered by the viewers text field (and the explorers selection) 
	 */
	private ArrayList<Comment> filteredComments;
	/**
	 * The view that displays the comments
	 */
	private TableViewer viewer;
	/**
	 * Comparator of the view, used to sort columns ascending/descending
	 */
	private AgileViewerComparator comparator;
	/**
	 * Filter of the view, used to filter by a given search string
	 */
	private AgileCommentFilter commentFilter;
	/**
	 * Filter of the view, used to filter by selected entries of the explorer
	 */
	private ExplorerSelectionFilter selectionFilter = new ExplorerSelectionFilter(new ArrayList<String>(), new HashMap<String, HashSet<String>>());
	/**
	 * Should the content of the table be linked to the selections of the explorer?
	 */
	private boolean linkExplorer = PropertiesManager.getInstance().getExternalPreference(PropertiesManager.EXTERNAL_KEYS.LINK_EXPLORER).isEmpty() ? false : Boolean.valueOf(PropertiesManager.getInstance().getExternalPreference(PropertiesManager.EXTERNAL_KEYS.LINK_EXPLORER));
	/**
	 * The number of columns of the parent's GridLayout
	 */
	private static final int layoutCols = 6;
	/**
	 * The titles of the table's columns, also used to fill the filter menu 
	 */
	private String[] titles = { "ReviewID", "CommentID", "Author", "Recipient", "Status", "Priority", "Revision", "Date created", "Date modified", "Replies", "Location" };
	/**
	 * The width of the table's columns
	 */
	private int[] bounds = { 60, 70, 70, 70, 70, 70, 55, 120, 120, 50, 100 };
	/**
	 * indicates whether Eclipse was just started or not
	 */
	private boolean startup = true;
	/**
	 * indicates whether annotations are displayed or not
	 */
	private boolean hideAnnotations = false; 
	/**
	 * map of currently opened editors and their annotation parsers
	 */
	private HashMap<ITextEditor, AnnotationParser> parserMap = new HashMap<ITextEditor, AnnotationParser>();
	
	/**
	 * Provides the current used instance of the CommentTableView
	 * @return instance of CommentTableView
	 */
	public static CommentTableView getInstance() {
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {
		instance = this;
		PluginLogger.log(this.getClass().toString(), "createPartControl", "Creating TableView");
		// get comments from CommentController
		try {
			this.comments = ReviewAccess.getInstance().getAllComments();
			this.filteredComments = this.comments;
		} catch (XmlException e) {
			// TODO
			PluginLogger.logError(this.getClass().toString(), "createPartControl", "XMLException when trying to read comments via ReviewAccess", e);
		} catch (IOException e) {
			//TODO
			PluginLogger.logError(this.getClass().toString(), "createPartControl", "IOException when trying to read comments via ReviewAccess", e);
		}
		
		// set layout of parent
		GridLayout layout = new GridLayout(layoutCols, false);
		parent.setLayout(layout);
		
		// create UI elements (filter, add-/delete-button)
		createToolBar(parent);
		createViewer(parent);
		
		// set comparator (sorting order of columns) and filter
		comparator = new AgileViewerComparator();
		viewer.setComparator(comparator);
		commentFilter = new AgileCommentFilter("ALL");
		viewer.addFilter(commentFilter);
		
		// register this class as a selection provider
		getSite().setSelectionProvider(viewer);
		
		//add help context
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, Activator.PLUGIN_ID+".TableView");
		
		// register view
		ViewControl.registerView(this.getClass());
		PluginLogger.log(this.getClass().toString(), "createPartControl", "Registering TableView with ViewControl");
		
		// get editor that is active when opening eclipse
		if (getActiveEditor() instanceof ITextEditor) {
			PluginLogger.log(this.getClass().toString(), "createPartControl", "Start parsing initially opened editor");
			this.parserMap.put((ITextEditor) getActiveEditor(), new AnnotationParser((ITextEditor) getActiveEditor()));
		}
		
	}
	
	/**
	 * Used to set a new model
	 * @param comments
	 */
	protected void setTableContent(ArrayList<Comment> comments) {
		this.comments = comments;
		viewer.setInput(comments);
		viewer.refresh();
		PluginLogger.log(this.getClass().toString(), "setTableContent", "Setting table content");
	}
	
	/**
	 * Add a comment to an existing model
	 * @param comment the comment
	 */
	public void addComment(Comment comment) {
		// add comment to (un)filtered model
		PluginLogger.log(this.getClass().toString(), "addComment", "Adding comment to table content");
		this.comments.add(comment);
		if (!this.comments.equals(this.filteredComments)) { 
			this.filteredComments.add(comment);
		}
		viewer.setInput(this.comments);
		
		// set selection (to display comment in detail view)
		getSite().getSelectionProvider().setSelection(new StructuredSelection(comment));
		
		try {
			PluginLogger.log(this.getClass().toString(), "addComment", "Add tags for currently added comment");
			parserMap.get(getActiveEditor()).addTagsInDocument(comment);
		} catch (BadLocationException e) {
			PluginLogger.logError(this.getClass().toString(), "addComment", "BadLocationException when trying to add tags", e);
			//e.printStackTrace();
		} catch (CoreException e) {
			PluginLogger.log(this.getClass().toString(), "addComment", "CoreException when trying to add tags", e);
			//e.printStackTrace();
		}
		
		if (ViewControl.isOpen(DetailView.class)) {
			DetailView.getInstance().selectionChanged(this, new StructuredSelection(comment));	
		} else {
			PluginLogger.logWarning(this.getClass().toString(), "addComment", "Could not open added comment in DetailView since DetailView is not registered with ViewController");
		}
		
	}
	
	/**
	 * Delete a comment from the model
	 * @param comment the comment
	 */
	public void deleteComment(Comment comment) {

		PluginLogger.log(this.getClass().toString(), "deleteComment", "Deleting a comment from table content");
		// add comment to (un)filtered model
		this.comments.remove(comment);
		if (this.filteredComments.contains(comment)) {
			this.filteredComments.remove(comment);	
		}		
		viewer.setInput(this.comments);
		
		// remove annotation and tags
		try {
			PluginLogger.log(this.getClass().toString(), "deleteComment", "Starting to remove tags for currently deleted comment");
			openEditor(comment);
			parserMap.get(getActiveEditor()).removeCommentTags(comment);
		} catch (BadLocationException e) {
			PluginLogger.logError(this.getClass().toString(), "deleteComment", "BadLocationException when trying to delete comment", e);
			//e.printStackTrace();
		} catch (CoreException e) {
			PluginLogger.logError(this.getClass().toString(), "deleteComment", "CoreException when trying to delete comment", e);
			//e.printStackTrace();
		}
	}

	/**
	 * @return the currently active editor
	 */
	private IEditorPart getActiveEditor() {
		return getSite().getPage().getActiveEditor();
	}

	/**
	 * Reload current table input
	 */
	public void refreshTable() {
		PluginLogger.log(this.getClass().toString(), "refreshTable", "Reloading current table input");
		viewer.refresh();
	}
	
	/**
	 * Creates the TableViewer component, sets it's model and layout
	 * @param parent The parent of the TableViewer
	 * @return viewer The TableView component of this view
	 */
	private TableViewer createViewer(Composite parent) {

		PluginLogger.log(this.getClass().toString(), "createViewer", "Starting to create viewer");
		// create viewer
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns(parent, viewer);

		// set attributes of viewer's table
		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		// set input for viewer
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setInput(this.comments);

		// provide access to selections of table rows
		viewer.addSelectionChangedListener(CommentController.getInstance()); // TODO umstellen auf ISelectionListener und dann über SelectionProvider!!!
		PluginLogger.log(this.getClass().toString(), "createViewer", "Registering as SelectionProvider");
		getSite().setSelectionProvider(viewer);
		
		viewer.addDoubleClickListener(this);

		// set layout of the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = layoutCols;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		viewer.getControl().setLayoutData(gridData);
		
		// set properties of columns to titles
		viewer.setColumnProperties(this.titles);
		
		return viewer;
	}
	
	/**
	 * Creates the columns of the viewer and adds label providers to fill cells
	 * @param parent The parent object of the viewer
	 * @param viewer The viewer who's columns are to be created
	 */
	private void createColumns(Composite parent, TableViewer viewer) {
		// ReviewID
		TableViewerColumn col = createColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				return c.getReviewID();
			}
		});
		
		// CommentID
		col = createColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				return c.getId();
			}
		});
		
		// Author
		col = createColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				return c.getAuthor();
			}
		});

		// Recipient
		col = createColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				return c.getRecipient();
			}
		});
		
		// Status
		col = createColumn(titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				String status = PropertiesManager.getInstance().getCommentStatusByID(c.getStatus());			
				return status;
			}
		});
		
		// Priority
		col = createColumn(titles[5], bounds[5], 5);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				String prio = PropertiesManager.getInstance().getCommentPriorityByID(c.getPriority());
				return prio;
			}
		});
		
		// Revision
		col = createColumn(titles[6], bounds[6], 6);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				return String.valueOf(c.getRevision());
			}
		});
		
		// Date created
		col = createColumn(titles[7], bounds[7], 7);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				DateFormat df = new SimpleDateFormat( "dd.M.yyyy', 'HH:mm:ss" );				
				return df.format(c.getCreationDate().getTime());
			}
		});
		
		// Date modified
		col = createColumn(titles[8], bounds[8], 8);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				DateFormat df = new SimpleDateFormat( "dd.M.yyyy', 'HH:mm:ss" );
				return df.format(c.getLastModified().getTime());
			}
		});
		
		// Number of relplies
		col = createColumn(titles[9], bounds[9], 9);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				return String.valueOf(c.getReplies().getReplyArray().length);
			}
		});
		
		// Location
		col = createColumn(titles[10], bounds[10], 10);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				Comment c = (Comment) element;
				return ReviewAccess.computePath(c);
			}
		});
	}
	
	/**
	 * Creates a single column of the viewer with given parameters 
	 * @param title The title to be set
	 * @param bound The width of the column
	 * @param colNumber The columns number
	 * @return The column with given parameters
	 */
	private TableViewerColumn createColumn(String title, int bound, int colNumber) {
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		column.addSelectionListener(getSelectionAdapter(column, colNumber));
		return viewerColumn;
	}
	
	/**
	 * Get the selection adapter of a given column
	 * @param column the column 
	 * @param index the column's index
	 * @return the columns selection adapter
	 */
	private SelectionAdapter getSelectionAdapter(final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				int dir = viewer.getTable().getSortDirection();
				if (viewer.getTable().getSortColumn() == column) {
					dir = dir == SWT.UP ? SWT.DOWN : SWT.UP;
				} else {

					dir = SWT.DOWN;
				}
				viewer.getTable().setSortDirection(dir);
				viewer.getTable().setSortColumn(column);
				viewer.refresh();
			}
		};
		return selectionAdapter;
	}

	/**
	 * create the toolbar containing filter and add/delete buttons
	 * @param parent the toolsbar's parent
	 * @return the toolbar
	 */
	private ToolBar createToolBar(Composite parent) {
		// create toolbar
		final ToolBar toolBar = new ToolBar(parent, SWT.FLAT | SWT.WRAP | SWT.RIGHT);

		// add dropdown box to toolbar to select category to filter
		final ToolItem itemDropDown = new ToolItem(toolBar, SWT.DROP_DOWN);
	    itemDropDown.setText("Search for ALL");
	    itemDropDown.setToolTipText("Click here to select the filter option");

	    // create listener to submit category changes to dropdown box and filter
	    Listener selectionListener = new Listener() {
	    	public void handleEvent(Event event) {
	    		MenuItem item = (MenuItem)event.widget;
	    		viewer.removeFilter(commentFilter);
	    		commentFilter = new AgileCommentFilter(item.getText());
	    		viewer.addFilter(commentFilter);
		    	itemDropDown.setText("Search for "+item.getText());
		    	toolBar.pack();
		    }
		};
		
	    // create menu for dropdown box
	    final Menu menu = new Menu(parent.getShell(), SWT.POP_UP);
	    
	    // add menu items
	    MenuItem item = new MenuItem(menu, SWT.PUSH);
    	item.setText("ALL");
    	item.addListener(SWT.Selection, selectionListener);
    	item = new MenuItem(menu, SWT.SEPARATOR);
	    for (int i=0; i<titles.length; i++) {
	    	item = new MenuItem(menu, SWT.PUSH);
	    	item.setText(titles[i]);
		    item.addListener(SWT.Selection, selectionListener);
	    }
	    
	    // add text field for filter to toolbar
	    ToolItem itemSeparator = new ToolItem(toolBar, SWT.SEPARATOR);
	    final Text text = new Text(toolBar, SWT.BORDER | SWT.SINGLE);
	    text.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent ke) {
				commentFilter.setSearchText(text.getText());
				filterComments();
				viewer.refresh();		
			}

		});
	    text.pack();
	    itemSeparator.setWidth(text.getBounds().width);
	    itemSeparator.setControl(text);	   
	    
	    // add seperator to toolbar
	    itemSeparator = new ToolItem(toolBar, SWT.SEPARATOR);
	    
	    // add "add comment" button to toolbar
	    ToolItem itemAddComment = new ToolItem(toolBar, SWT.PUSH);
	    itemAddComment.setText("Add Comment");
	    itemAddComment.setData("add");
	    itemAddComment.setImage(createImageDescriptor(PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.ICONS.COMMENT_ADD)).createImage());
	    itemAddComment.addListener(SWT.Selection, CommentController.getInstance());
	    
	    // add "delete comment" button to toolbar
	    ToolItem itemDelComment = new ToolItem(toolBar, SWT.PUSH);
	    itemDelComment.setText("Delete Comment");
	    itemDelComment.setData("delete");
	    itemDelComment.setImage(createImageDescriptor(PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.ICONS.COMMENT_DELETE)).createImage());
	    itemDelComment.addListener(SWT.Selection, CommentController.getInstance());
	    
	    // add "show all comments" button to toolbar
	    ToolItem itemAllComments = new ToolItem(toolBar, SWT.CHECK);
	    itemAllComments.setImage(createImageDescriptor(PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.ICONS.SYNCED)).createImage());
	    itemAllComments.setText("Link Explorer");
	    itemAllComments.setSelection(this.linkExplorer);
	    // add listener to connect and disconnect explorer and table
	    itemAllComments.addListener(SWT.Selection, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				linkExplorer = !linkExplorer;
				if (!linkExplorer) {
					viewer.removeFilter(selectionFilter);
					PropertiesManager.getInstance().setExternalPreference(PropertiesManager.EXTERNAL_KEYS.LINK_EXPLORER,"false");
				} else {
					viewer.addFilter(selectionFilter);
					PropertiesManager.getInstance().setExternalPreference(PropertiesManager.EXTERNAL_KEYS.LINK_EXPLORER,"true");
				}
				filterComments();
				viewer.refresh();
			}
		});
	    
	    // add listener to dropdown box to show menu
	    itemDropDown.addListener(SWT.Selection, new Listener() {
		      public void handleEvent(Event event) {
		        if(event.detail == SWT.ARROW || event.detail == 0) {
		          Rectangle bounds = itemDropDown.getBounds();
		          Point point = toolBar.toDisplay(bounds.x, bounds.y + bounds.height);
		          menu.setLocation(point);
		          menu.setVisible(true);
		          text.setFocus();
		        }
		      }
		    });
	    
	    toolBar.pack();
	    
		return toolBar;
	}
	
	/**
	 * Resets the comments (reloading from model)
	 * @throws IOException 
	 * @throws XmlException 
	 */
	public void resetComments() throws XmlException, IOException {
		PluginLogger.log(this.getClass().toString(), "resetComments", "Reloading comments from model");
		// TODO: check if annotations need to be removed!!!
		this.comments = ReviewAccess.getInstance().getAllComments();
		this.viewer.setInput(this.comments);
		filterComments();
		IEditorPart editor;
		if((editor = getActiveEditor()) != null) {
			if(this.parserMap.containsKey(editor)) {
				this.parserMap.get(editor).reload();
			}
		}
		this.refreshTable();
	}
	
	/**
	 * Resets the current parserMap and adds the active editor if some is active.
	 * This can be done in order to avoid corrupt editors, for example
	 * after refactoring was done.
	 */
	public void resetEditors() {
		PluginLogger.log(this.getClass().toString(), "resetEditor", "Droping old editors, reparsing active editor");
		for(AnnotationParser p : this.parserMap.values()) {
			p.filter(new String[]{});
		}
		this.parserMap.clear();
		System.gc();
		IEditorPart editor;
		if((editor = this.getActiveEditor()) != null) {
			if(editor instanceof ITextEditor) {
				this.parserMap.put((ITextEditor) editor, new AnnotationParser((ITextEditor) editor));
			}
		}
	}
	
	/** Used to add images to add/delete button
	 * @param path path to the image
	 * @return an imagedescriptor of the given path
	 */
	private static ImageDescriptor createImageDescriptor(String path) {
	    return AbstractUIPlugin.imageDescriptorFromPlugin(Activator.PLUGIN_ID, path);
	}

	/**
	 * Editor has been brought to top, add annotations
	 * @param partRef will be forwarded from the {@link ViewControl}
	 * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) partRef.getPart(false);
			if (!this.parserMap.containsKey(editor) && !this.hideAnnotations) {
				this.parserMap.put(editor, new AnnotationParser(editor));
			}
		}
	}
	
	/**
	 * Editor has been closed, remove from parserMap
	 * @param partRef will be forwarded from the {@link ViewControl}
	 * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
	 */
	public void partClosed(IWorkbenchPartReference partRef) {
		if (partRef.getPart(false) instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) partRef.getPart(false);
			if (this.parserMap.containsKey(editor)) {
				this.parserMap.remove(editor);
			}
		}
	}
	
	/** Selection of ReviewExplorer changed, filter comments
	 * @param part will be forwarded from the {@link ViewControl}
	 * @param selection will be forwarded from the {@link ViewControl}
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (linkExplorer && part instanceof ReviewExplorer) {
			if (selection instanceof IStructuredSelection && !selection.isEmpty()) {

				PluginLogger.log(this.getClass().toString(), "selectionChanged", "Selection of ReviewExplorer changed");
				// if there is a selectionFilter, remove it
				if (this.selectionFilter != null) {
					viewer.removeFilter(this.selectionFilter);
				}

				// get selection, selection's iterator, initialize reviewIDs and
				// paths
				IStructuredSelection sel = (IStructuredSelection) selection;
				Iterator<?> it = sel.iterator();
				ArrayList<String> reviewIDs = new ArrayList<String>();
				HashMap<String, HashSet<String>> paths = new HashMap<String, HashSet<String>>();

				// get all selected reviews and paths
				while (it.hasNext()) {
					Object next = it.next();
					if (next instanceof MultipleReviewWrapper) {
						String reviewID = ((MultipleReviewWrapper) next)
								.getWrappedReview().getId();
						if (!reviewIDs.contains(reviewID)) {
							reviewIDs.add(reviewID);
						}
					} else if (next instanceof AbstractMultipleWrapper) {
						String path = ((AbstractMultipleWrapper) next)
								.getPath();
						String reviewID = ((AbstractMultipleWrapper) next)
								.getReviewId();
						if (paths.containsKey(reviewID)) {
							paths.get(reviewID).add(path);
						} else {
							paths.put(reviewID, new HashSet<String>());
							paths.get(reviewID).add(path);
						}
					}
				}

				PluginLogger.log(this.getClass().toString(), "selectionChanged", "Adding new filter regarding selection of ReviewExplorer");
				// add a new filter by the given criteria to the viewer
				this.selectionFilter = new ExplorerSelectionFilter(reviewIDs,
						paths);
				viewer.addFilter(this.selectionFilter);

				// refresh annotations, update list of filtered comments
				filterComments();
				//AnnotationController.getInstance().refreshAnnotations((ITextEditor) getActiveEditor(), CommentTableView.this.filteredComments);
			}
		}
	}
	
	/**
	 * Filter comments by criteria received from explorer and search field
	 */
	private void filterComments() {
		PluginLogger.log(this.getClass().toString(), "filterComments", "Starting to filter comments");
		List<Object> filteredCommentObjects;
		if (linkExplorer) {
			// filter by selections from explorer and search word from text field
			PluginLogger.log(this.getClass().toString(), "filterComments", "Filter by textfield and ReviewExplorer selection");
			filteredCommentObjects = Arrays.asList(selectionFilter.filter(viewer, this, commentFilter.filter(viewer, this, this.comments.toArray())));
		} else {
			// filter by search word from text field
			PluginLogger.log(this.getClass().toString(), "filterComments", "Filter by textfield");
			filteredCommentObjects = Arrays.asList(commentFilter.filter(viewer, this, this.comments.toArray()));	
		}		
		
		//fill filteredComments and filter annotations
		this.filteredComments = new ArrayList<Comment>();
		String[] commentKeys = new String[filteredCommentObjects.size()];
		String keySeparator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
		
		int i = 0;
		for (Object o : filteredCommentObjects) {
			filteredComments.add((Comment) o);
			commentKeys[i] = ((Comment) o).getReviewID()+keySeparator+((Comment) o).getAuthor()+keySeparator+((Comment) o).getId();
			i++;
		}
		PluginLogger.log(this.getClass().toString(), "filterComments", "Starting to filter annotations");
		this.parserMap.get(this.getActiveEditor()).filter(commentKeys);
		PluginLogger.log("CommentTableView","filterComments",commentKeys.toString());
	}

	/* not used
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() { /* TODO Auto-generated method stub */}
	
	/**
	 * Removes all annotations if the AgileReview perspective is closed. The method is invoke by {@link:ViewControl} 
	 * @param page the workbench page
	 * @param perspective the activated perspective
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor perspective) {
		if (!perspective.getLabel().equals("AgileReview")) {
			PluginLogger.log(this.getClass().toString(), "perspectiveActivated", "Hiding annotations since current perspective is not 'AgileReview'");
			for (AnnotationParser parser: this.parserMap.values()) {
				parser.filter(new String[0]);
			}
			this.parserMap = new HashMap<ITextEditor, AnnotationParser>();
			this.startup = false;
			this.hideAnnotations = true;
		}
		if (perspective.getLabel().equals("AgileReview") && !this.startup) {
			PluginLogger.log(this.getClass().toString(), "perspectiveActivated", "Adding annotations since AgileReview perspective has been activated");
			this.parserMap.put((ITextEditor) getActiveEditor(), new AnnotationParser((ITextEditor) getActiveEditor()));
			this.hideAnnotations = false;
		}
	}

	/**
	 * Opens an editor for a given comment
	 * @param comment the comment
	 */
	private void openEditor(Comment comment) {
		PluginLogger.log(this.getClass().toString(), "openEditor", "Opening editor for the given comment");
		IPath path = new Path(ReviewAccess.computePath(comment));
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		try {
			getSite().getPage().openEditor((IEditorInput) new FileEditorInput(file), desc.getId());
		} catch (PartInitException e) {
			PluginLogger.logError(this.getClass().toString(), "openEditor", "PartInitException occured when opening editor", e);
			//e.printStackTrace();
		}
	}
	
	@Override
	public void doubleClick(DoubleClickEvent event) {
		Comment comment = (Comment) ((IStructuredSelection)event.getSelection()).getFirstElement();
		openEditor(comment);
		//jump to comment in opened editor
		String keySeparator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
		String commentTag = comment.getReviewID()+keySeparator+comment.getAuthor()+keySeparator+comment.getId();
		try {
			PluginLogger.log(this.getClass().toString(), "doubleClick", "Revealing comment in it's editor");
			this.parserMap.get(getActiveEditor()).revealCommentLocation(commentTag);
		} catch (BadLocationException e) {
			PluginLogger.logError(this.getClass().toString(), "openEditor", "BadLocationException when revealing comment in it's editor", e);
			e.printStackTrace();
		}
	}
}
