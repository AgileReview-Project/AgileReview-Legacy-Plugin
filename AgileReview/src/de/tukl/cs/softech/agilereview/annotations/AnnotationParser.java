package de.tukl.cs.softech.agilereview.annotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.NoDocumentFoundException;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;

/**
 * The AnnotationParser analyzes the document of the given editor and provides a mapping
 * of comment tags and their {@link Position}s
 */
public class AnnotationParser implements IAnnotationParser {
	
	/**
	 * Instance of PropertiesManager
	 */
	private static PropertiesManager pm = PropertiesManager.getInstance();
	/**
	 * Supported files mapping to the corresponding comment tags
	 */
	private static final HashMap<String, String[]> supportedFiles = pm.getParserFileendingsAndTags();
	/**
	 * Key separator for tag creation
	 */
	private static String keySeparator = pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
	/**
	 * Core Regular Expression to find the core tag structure
	 */
	private static String rawTagRegex = "\\s*(\\??)"+Pattern.quote(keySeparator)+"\\s*([^"+Pattern.quote(keySeparator)+"]+"+Pattern.quote(keySeparator)+"[^"+Pattern.quote(keySeparator)+"]+"+Pattern.quote(keySeparator)+"[^\\?"+Pattern.quote(keySeparator)+"]*)\\s*"+Pattern.quote(keySeparator)+"(\\??)\\s*";
	/**
	 * Path of the file this parser represents
	 */
	private String path;
	
	/**
	 * Regular Expression used by this instance
	 */
	private String tagRegex;
	/**
	 * Pattern used by this instance
	 */
	private Pattern tagPattern;
	/**
	 * This map lists every comment tag found in the document with its {@link Position}
	 */
	private TreeMap<String, Position> idPositionMap = new TreeMap<String, Position>();
	/**
	 * Position map of all tags
	 */
	private TreeMap<String, Position[]> idTagPositions = new TreeMap<String, Position[]>();
	/**
	 * The currently displayed comments
	 */
	private TreeSet<String> displayedComments = new TreeSet<String>();
	/**
	 * Document which provides the contents for this instance
	 */
	private IDocument document;
	/**
	 * The document of this parser
	 */
	private ITextEditor editor;
	/**
	 * Annotation model for this parser
	 */
	private AgileAnnotationController annotationModel;

	/**
	 * Creates a new instance of AnnotationParser with the given input
	 * @param editor the editor which contents should be analyzed
	 * @param commentBeginTag begin tag for comments in this document
	 * @param commentEndTag end tag for comments in this document
	 * @throws NoDocumentFoundException will be thrown, if the file type which this editor represents is not supported
	 */
	protected AnnotationParser(ITextEditor editor, String commentBeginTag, String commentEndTag) throws NoDocumentFoundException {
		
		tagRegex = Pattern.quote(commentBeginTag)+rawTagRegex+Pattern.quote(commentEndTag);
		tagPattern = Pattern.compile(tagRegex);
		
		this.editor = editor;

		if(editor.getDocumentProvider() != null) {
			this.document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			if(this.document == null) {
				throw new NoDocumentFoundException();
			}
		} else {
			throw new NoDocumentFoundException();
		}
		
		// Set the path this Parser stand for
		IEditorInput input = this.editor.getEditorInput();
		if (input != null && input instanceof FileEditorInput) {
			path = ((FileEditorInput)input).getFile().getFullPath().toOSString().replaceFirst(Pattern.quote(System.getProperty("file.separator")), "");
		}
		
		this.annotationModel = new AgileAnnotationController(editor);
		parseInput();
	}
	
	/**
	 * Parses all comment tags and saves them with their {@link Position}
	 */
	private void parseInput() {
		this.document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		PluginLogger.log(this.getClass().toString(), "parseInput", "triggered");
		idPositionMap.clear();
		idTagPositions.clear();
		HashSet<String> corruptedCommentKeys = new HashSet<String>();
		Matcher matcher;
		FindReplaceDocumentAdapter fra = new FindReplaceDocumentAdapter(document);
		IRegion r;
		int startOffset = 0;
		try {
			while((r = fra.find(startOffset, tagRegex, true, false, false, true)) != null) {
				boolean tagDeleted = false;
				int line = document.getLineOfOffset(r.getOffset());
				matcher = tagPattern.matcher(document.get(r.getOffset(), r.getLength()));
				if(matcher.matches()) {
					String key = matcher.group(2).trim();
					Position[] tagPositions;
					if(matcher.group(1).equals("?")) {
						tagPositions = idTagPositions.get(key);
						//begin tag
						if(tagPositions != null) {
							//same begin tag already exists
							corruptedCommentKeys.add(key);
							document.replace(r.getOffset(), r.getLength(), "");
							PluginLogger.log(this.getClass().toString(), "parseInput", "currupt: <same begin tag already exists>: "+key+" --> deleting");
							tagDeleted = true;
						} else {
							idPositionMap.put(key, new Position(document.getLineOffset(line)));
							idTagPositions.put(key, new Position[]{new Position(r.getOffset(), r.getLength()), null});
						}
					}
					
					if(matcher.group(3).equals("?") && !tagDeleted) {
						tagPositions = idTagPositions.get(key);
						//end tag
						if(tagPositions != null) {
							if(tagPositions[1] != null) {
								//same end tag already exists
								corruptedCommentKeys.add(key);
								document.replace(r.getOffset(), r.getLength(), "");
								PluginLogger.log(this.getClass().toString(), "parseInput", "currupt: <same end tag already exists>: "+key+" --> deleting");
								tagDeleted = true;
							} else {
								//end tag not set
								Position tmp = idPositionMap.get(key);
								tmp.setLength(document.getLineOffset(line) - tmp.getOffset() + document.getLineLength(line));
								idPositionMap.put(key, tmp);
								
								Position[] tmp2 = idTagPositions.get(key);
								tmp2[1] = new Position(r.getOffset(), r.getLength());
								idTagPositions.put(key, tmp2);
							}
						} else {
							//end tag without begin tag
							corruptedCommentKeys.add(key);
							document.replace(r.getOffset(), r.getLength(), "");
							PluginLogger.log(this.getClass().toString(), "parseInput", "currupt: <end tag without begin tag>: "+key+" --> deleting");
							tagDeleted = true;
						}
					}
				}
				
				//if a tag was deleted, search from begin of the deleted tag again
				if(tagDeleted) {
					startOffset = r.getOffset();
				} else {
					//-1 in oder to ensure that the startOffset will not be greater then the document length
					startOffset = r.getOffset()+r.getLength()-1;
				}
			}
			
			//check for begin tags without end tags
			boolean curruptedBeginTagExists = false;
			TreeSet<Position> positionsToDelete = new TreeSet<Position>();
			for(String key : idTagPositions.keySet()) {
				Position[] ps = idTagPositions.get(key);
				if(ps[1] == null) {
					PluginLogger.log(this.getClass().toString(), "parseInput", "currupt: <begin tag without end tag>: "+key+" --> deleting");
					corruptedCommentKeys.add(key);
					positionsToDelete.add(ps[0]);
					curruptedBeginTagExists = true;
				}
			}
			
			if(curruptedBeginTagExists) {
				//delete all corrupted begin tags in descending order
				Iterator<Position> it = positionsToDelete.descendingIterator();
				while(it.hasNext()) {
					Position tmp = it.next();
					document.replace(tmp.getOffset(), tmp.getLength(), "");
				}
				//delete corrupted annotations
				this.annotationModel.deleteAnnotations(corruptedCommentKeys);
				//parse the file another time to get the correct positions for the tags
				parseInput();
			}
			
		} catch (BadLocationException e) {
			if(startOffset != 0) {
				PluginLogger.logError(this.getClass().toString(), "parseInput", "BadLocationException occurs while parsing the editor: "+editor.getTitle(), e);
			} else {
				//file out of sync or other reasons, so eclipse cannot open file till refresh --> suppress Exception
				PluginLogger.log(this.getClass().toString(), "parseInput", "BadLocationException suppressed while parsing the editor: "+editor.getTitle());
			}
			
		}
		
		HashMap<Position, String> toDisplay = new HashMap<Position, String>();
		for(String s : idPositionMap.keySet()) {
			toDisplay.put(idPositionMap.get(s), s);
		}

		// Save the current document to save the tags
		try {
			editor.getDocumentProvider().saveDocument(null, editor.getEditorInput(), document, true);
		} catch (CoreException e) {
			PluginLogger.logError(this.getClass().toString(), "parseInput", "CoreException occurs while saving document of editor: "+editor.getTitle(), e);
		}
		
		//update annotations in order to recognize moved tags
		TreeMap<String, Position> annotationsToUpdate = new TreeMap<String, Position>();
		for(String key : displayedComments) {
			if(idPositionMap.get(key) != null) {
				annotationsToUpdate.put(key, idPositionMap.get(key));
			}
		}
		annotationModel.updateAnnotations(annotationsToUpdate);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#filter(java.util.ArrayList)
	 */
	public void filter(HashSet<Comment> comments) {
		PluginLogger.log(this.getClass().toString(), "filter", "triggered");

		HashMap<String, Position> toDisplay = new HashMap<String, Position>();
		for(Comment c : comments) {
			String commentKey = c.getReviewID()+keySeparator+c.getAuthor()+keySeparator+c.getId();
			
			if(path.equals(ReviewAccess.computePath(c)) && this.idPositionMap.get(commentKey) != null) {
				toDisplay.put(commentKey, this.idPositionMap.get(commentKey));
			}
		}
		
		displayedComments.clear();
		displayedComments.addAll(toDisplay.keySet());
		
		//Do not prove for open perspective, because annotations will be cleaned by empty comment array
		this.annotationModel.displayAnnotations(toDisplay);
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#clearAnnotations()
	 */
	public void clearAnnotations() {
		filter(new HashSet<Comment>());
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#addTagsInDocument(agileReview.softech.tukl.de.CommentDocument.Comment)
	 */
	public void addTagsInDocument(Comment comment, boolean display) throws BadLocationException, CoreException {
		//VARIANT(return Position):Position result = null;

		ISelection selection= editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			int selStartLine = ((ITextSelection)selection).getStartLine();
			int selEndLine = ((ITextSelection)selection).getEndLine();
			
			String commentKey = comment.getReviewID()+keySeparator+comment.getAuthor()+keySeparator+comment.getId();
			String commentTag = keySeparator+commentKey+keySeparator;
			
			int[] newLines = checkForComment(document, selStartLine, selEndLine);
			if (newLines[0] != -1 && newLines[1] != -1  && (newLines[0] != selStartLine || newLines[1] != selEndLine)) {
				int offset = document.getLineOffset(newLines[0]);
				int length = document.getLineOffset(newLines[1])-document.getLineOffset(newLines[0])+document.getLineLength(newLines[1]);
				editor.getSelectionProvider().setSelection(new TextSelection(offset, length));
				addTagsInDocument(comment, display);
				return;
			}
			
			if (selStartLine == selEndLine)	{
				// Only one line is selected
				String lineDelimiter = document.getLineDelimiter(selStartLine);
				int lineDelimiterLength = 0;
				if (lineDelimiter != null) {
					lineDelimiterLength = lineDelimiter.length();
				}
				
				int insertOffset = document.getLineOffset(selStartLine)+document.getLineLength(selStartLine)-lineDelimiterLength;
				
				// Write tag -> get start+end-tag for current file-ending, insert into file				
				String[] tags = supportedFiles.get(editor.getEditorInput().getName().substring(editor.getEditorInput().getName().lastIndexOf(".")+1));
				document.replace(insertOffset, 0, tags[0]+"?"+commentTag+"?"+tags[1]);
				
				//VARIANT(return Position):result = new Position(document.getLineOffset(selStartLine), document.getLineLength(selStartLine)-lineDelimiterLength);
			} else {
				// Calculate insert position for start line
				String lineDelimiter = document.getLineDelimiter(selStartLine);
				int lineDelimiterLength = 0;
				if (lineDelimiter != null) {
					lineDelimiterLength = lineDelimiter.length();
				}
				int insertStartOffset = document.getLineOffset(selStartLine)+document.getLineLength(selStartLine)-lineDelimiterLength;
				
				// Calculate insert position for end line
				lineDelimiter = document.getLineDelimiter(selEndLine);
				lineDelimiterLength = 0;
				if (lineDelimiter != null) {
					lineDelimiterLength = lineDelimiter.length();
				}
				int insertEndOffset = document.getLineOffset(selEndLine)+document.getLineLength(selEndLine)-lineDelimiterLength;
				
				// Write tags -> get tags for current file-ending, insert second tag, insert first tag
				String[] tags = supportedFiles.get(editor.getEditorInput().getName().substring(editor.getEditorInput().getName().lastIndexOf(".")+1));
				document.replace(insertEndOffset, 0, tags[0]+commentTag+"?"+tags[1]);
				document.replace(insertStartOffset, 0, tags[0]+"?"+commentTag+tags[1]);

				
				//VARIANT(return Position):result = new Position(document.getLineOffset(selStartLine), 
				//VARIANT(return Position):		document.getLineOffset(selEndLine) - document.getLineOffset(selStartLine) + document.getLineLength(selEndLine)-lineDelimiterLength);
			}
			parseInput();
			if(ViewControl.isPerspectiveOpen() && display) {
				this.annotationModel.addAnnotation(commentKey, this.idPositionMap.get(commentKey));
			}
		}
		//VARIANT(return Position):return result;
	}
	
	/**
	 * Checks whether adding an AgileReview comment at the current selection<br>
	 * would destroy a code comment and computes adapted line numbers to avoid<br>
	 * destruction of code comments.
	 * 
	 * @param document the document in which the comment will be added
	 * @param startLine the current startLine of the selection
	 * @param endLine the current endLine of the selection
	 * @return and array containing the new start (position 0) and endline (position 1)
	 * @throws BadLocationException
	 */
	public int[] checkForComment(IDocument document, int startLine, int endLine) throws BadLocationException {
		int[] result = {-1, -1};
		int openTagLineNr = -1;
		int closeTagLineNr = -1;
		String[] tags = supportedFiles.get(editor.getEditorInput().getName().substring(editor.getEditorInput().getName().lastIndexOf(".")+1));
		
		// check for opening non-AgileReview comment tags
		for (int i=0; i<=endLine;i++) {
			// check if line contains comment and comment is no AgileReview tag
			String line = document.get(document.getLineOffset(i), document.getLineLength(i)).trim();
			boolean containsStartTag = line.contains(tags[0]);
			boolean isNotAgileReviewTag = !line.matches(".*"+Pattern.quote(tags[0])+rawTagRegex+Pattern.quote(tags[1])+".*");
			if (containsStartTag && isNotAgileReviewTag) {
				openTagLineNr = i;
			}
		}
		// if an opening non-AgileReview comment tag was found check for its closing tag
		if (openTagLineNr>-1) {
			boolean found = false;
			int actLine = openTagLineNr;
			while (actLine<document.getNumberOfLines() && !found) {
				// check if line contains comment and comment is no AgileReview tag
				String line = document.get(document.getLineOffset(actLine), document.getLineLength(actLine)).trim();
				boolean containsEndTag = line.contains(tags[1]);
				boolean isNotAgileReviewTag = !line.matches(".*"+Pattern.quote(tags[0])+rawTagRegex+Pattern.quote(tags[1])+".*");
				if (containsEndTag && isNotAgileReviewTag) {
					closeTagLineNr = actLine;
					found = true;
				}
				actLine++;
			}
		}
		
		// check if inserting a AgileReview comment at selected code region destroys a code comment
		if (!(closeTagLineNr <= startLine || (startLine < openTagLineNr && closeTagLineNr < endLine) || endLine < openTagLineNr)) {
			// check if startLine needs to be adapted
			if (startLine >= openTagLineNr) {
				result[0] = openTagLineNr-1;
			} else {
				result[0] = startLine;
			}
			// check if endLine needs to be adapted
			if (closeTagLineNr > endLine) {
				result[1] = closeTagLineNr;	
			} else {
				result[1] = endLine;
			}
		}
		
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#removeCommentTags(agileReview.softech.tukl.de.CommentDocument.Comment)
	 */
	public void removeCommentTags(Comment comment) throws BadLocationException, CoreException {
		removeCommentsTags(new HashSet<Comment>(Arrays.asList(new Comment[]{comment})));
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#removeCommentsTags(java.util.Set)
	 */
	public void removeCommentsTags(Set<Comment> comments) throws BadLocationException, CoreException {		
		String separator = pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
		TreeSet<Position> tagPositions = new TreeSet<Position>();
		String key;
		HashSet<String> keyList = new HashSet<String>();
		for(Comment c : comments) {
			key = c.getReviewID()+separator+c.getAuthor()+separator+c.getId();
			Position[] ps = idTagPositions.get(key);
			if(ps != null) {
				ArrayList<ComparablePosition> cp = new ArrayList<ComparablePosition>();
				for(int i = 0; i < ps.length; i++) {
					cp.add(new ComparablePosition(ps[i]));
				}
				tagPositions.addAll(cp);
				keyList.add(key);
			}
		}
		
		//delete annotations and map entries
		this.annotationModel.deleteAnnotations(keyList);
		this.idTagPositions.keySet().removeAll(keyList);
		this.idPositionMap.keySet().removeAll(keyList);
		
		Iterator<Position> it = tagPositions.descendingIterator();
		while(it.hasNext()) {
			Position tmp = it.next();
			document.replace(tmp.getOffset(), tmp.getLength(), "");
		}
		
		parseInput();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#reload()
	 */
	public void reload() {
		parseInput();
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#revealCommentLocation(java.lang.String)
	 */
	public void revealCommentLocation(String commentID) throws BadLocationException {
		if(this.idPositionMap.get(commentID) != null) {
			editor.selectAndReveal(this.idPositionMap.get(commentID).offset, 0);
		} else {
			throw new BadLocationException();
		}
	}
	
	/**
	 * Returns all comments which are overlapping with the given {@link Position}
	 * @param p position
	 * @return all comments which are overlapping with the given {@link Position}
	 */
	public String[] getCommentsByPosition(Position p) {
		return this.annotationModel.getCommentsByPosition(p);
	}
}