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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.texteditor.ITextEditor;

import agileReview.softech.tukl.de.CommentDocument.Comment;
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
	 * Key separator for tag creation
	 */
	private static String keySeparator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
	/**
	 * Core Regular Expression to find the core tag structure
	 */
	private static String rawTagRegex = "\\s*(\\??)"+Pattern.quote(keySeparator)+"\\s*([^"+Pattern.quote(keySeparator)+"]+"+Pattern.quote(keySeparator)+"[^"+Pattern.quote(keySeparator)+"]+"+Pattern.quote(keySeparator)+"[^\\?"+Pattern.quote(keySeparator)+"]*)\\s*"+Pattern.quote(keySeparator)+"(\\??)\\s*";
	
	/**
	 * Regular Expression used by this instance
	 */
	protected String tagRegex;
	/**
	 * Pattern used by this instance
	 */
	protected Pattern tagPattern;
	/**
	 * This map lists every comment tag found in the document with its {@link Position}
	 */
	protected TreeMap<String, Position> idPositionMap = new TreeMap<String, Position>();
	/**
	 * Position map of all tags
	 */
	protected TreeMap<String, Position[]> idTagPositions = new TreeMap<String, Position[]>();
	/**
	 * Document which provides the contents for this instance
	 */
	protected IDocument document;
	/**
	 * The document of this parser
	 */
	protected ITextEditor editor;
	/**
	 * Annotation model for this parser
	 */
	protected AgileAnnotationController annotationModel;

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
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#filter(java.util.ArrayList)
	 */
	public void filter(ArrayList<Comment> comments) {
		PluginLogger.log(this.getClass().toString(), "filter", "triggered");
		String[] commentKeys = new String[comments.size()];
		for(int i = 0; i < comments.size(); i++) {
			commentKeys[i] = comments.get(i).getReviewID()+keySeparator+comments.get(i).getAuthor()+keySeparator+comments.get(i).getId();
		}

		HashMap<String, Position> display = new HashMap<String, Position>();
		for(String s : commentKeys) {
			if(this.idPositionMap.get(s) != null) {
				display.put(s, this.idPositionMap.get(s));
			}
		}
		if(ViewControl.isPerspectiveOpen()) {
			this.annotationModel.displayAnnotations(display);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#clearAnnotations()
	 */
	public void clearAnnotations() {
		filter(new ArrayList<Comment>());
	}
	
	/*
	 * (non-Javadoc)
	 * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#addTagsInDocument(agileReview.softech.tukl.de.CommentDocument.Comment)
	 */
	public void addTagsInDocument(Comment comment) throws BadLocationException, CoreException {
		//Position result = null;

		ISelection selection= editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			int selStartLine = ((ITextSelection)selection).getStartLine();
			int selEndLine = ((ITextSelection)selection).getEndLine();
			
			String commentKey = comment.getReviewID()+keySeparator+comment.getAuthor()+keySeparator+comment.getId();
			String commentTag = keySeparator+commentKey+keySeparator;
			
			if (selStartLine == selEndLine)	{
				// Only one line is selected
				String lineDelimiter = document.getLineDelimiter(selStartLine);
				int lineDelimiterLength = 0;
				if (lineDelimiter != null) {
					lineDelimiterLength = lineDelimiter.length();
				}
				
				int insertOffset = document.getLineOffset(selStartLine)+document.getLineLength(selStartLine)-lineDelimiterLength;
				
				if (editor.getEditorInput().getName().endsWith(".java")) {
					document.replace(insertOffset, 0, "/*?"+commentTag+"?*/");
				} else if (editor.getEditorInput().getName().endsWith(".xml")) {
					document.replace(insertOffset, 0, "<!--?"+commentTag+"?-->");
				}
				
				//result = new Position(document.getLineOffset(selStartLine), document.getLineLength(selStartLine)-lineDelimiterLength);
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
				
				// Write tags
				if (editor.getEditorInput().getName().endsWith(".java")) {
					document.replace(insertEndOffset, 0, "/*"+commentTag+"?*/");
					document.replace(insertStartOffset, 0, "/*?"+commentTag+"*/");
					
				} else if (editor.getEditorInput().getName().endsWith(".xml")) {
					document.replace(insertEndOffset, 0, "<!--"+commentTag+"?-->");
					document.replace(insertStartOffset, 0, "<!--?"+commentTag+"-->");
				}
				
				//result = new Position(document.getLineOffset(selStartLine), 
				//		document.getLineOffset(selEndLine) - document.getLineOffset(selStartLine) + document.getLineLength(selEndLine)-lineDelimiterLength);
			}
			parseInput();
			if(ViewControl.isPerspectiveOpen()) {
				this.annotationModel.addAnnotation(commentKey, this.idPositionMap.get(commentKey));
			}
		}
		//return result;
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
		String separator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
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
}