package de.tukl.cs.softech.agilereview.tools;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.texteditor.ITextEditor;

import agileReview.softech.tukl.de.CommentDocument.Comment;

/**
 * The AnnotationParser analyzes the document of the given editor and provides a mapping
 * of comment tags and their {@link Position}s
 */
public class AnnotationParser {
	
	/**
	 * Regular Expression to find and differentiate each comment tag in java files
	 */
	//TODO prove for tag structure including | in order to get more resistance against normal comments
	private static final String javaTagRegex = "/\\*\\s*(\\??)\\s*([^\\?\\r\\n]*)\\s*(\\??)\\s*\\*/";
	/**
	 * Regular Expression to find and differentiate each comment tag in XML files
	 */	
	private static final String xmlTagRegex = "<!--\\s*(\\??)\\s*([^\\?\\r\\n]*)\\s*(\\??)\\s*-->";
	/**
	 * Pattern to identify comment tags in java files
	 */
	private static final Pattern javaTagPattern = Pattern.compile(javaTagRegex);
	/**
	 * Pattern to identify comment tags in XML files
	 */
	private static final Pattern xmlTagPattern = Pattern.compile(xmlTagRegex);
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
	private TreeMap<String, Position[]> idTagPositions = new TreeMap<String, Position[]>();
	/**
	 * Document which provides the contents for this instance
	 */
	private IDocument document;
	private ITextEditor editor;

	/**
	 * Creates a new instance of AnnotationParser with the given input
	 * @param editor the editor which contents should be analyzed
	 * @throws FileTypeNotSupportedException will be thrown, if the file type which this editor represents is not supported
	 */
	public AnnotationParser(ITextEditor editor) throws FileTypeNotSupportedException {
		if(editor.getEditorInput().getName().endsWith("java")) {
			tagRegex = javaTagRegex;
			tagPattern = javaTagPattern;
		} else if(editor.getEditorInput().getName().endsWith("xml")) {
			tagRegex = xmlTagRegex;
			tagPattern = xmlTagPattern;
		} else {
			throw new FileTypeNotSupportedException();
		}
		this.editor = editor;
		this.document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
		parseInput();
	}
	
	/**
	 * Parses all comment tags and saves them with their {@link Position}
	 */
	private void parseInput() {
		idPositionMap.clear();
		idTagPositions.clear();
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
							document.replace(r.getOffset(), r.getLength(), "");
							System.out.println("currupt: <same begin tag already exists>: "+key);
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
								document.replace(r.getOffset(), r.getLength(), "");
								System.out.println("currupt: <same end tag already exists>: "+key);
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
							document.replace(r.getOffset(), r.getLength(), "");
							System.out.println("currupt: <end tag without begin tag>: "+key);
							tagDeleted = true;
						}
					}
				}
				
				//if a tag was deleted, search from begin of the deleted tag again
				if(tagDeleted) {
					startOffset = r.getOffset();
				} else {
					startOffset = r.getOffset()+r.getLength();
				}
			}
			
			//check for begin tags without end tags
			boolean curruptedBeginTagExists = false;
			TreeSet<Position> positionsToDelete = new TreeSet<Position>();
			for(Position[] ps : idTagPositions.values()) {
				if(ps[1] == null) {
					positionsToDelete.add(ps[0]);
					curruptedBeginTagExists = true;
				}
			}
			
			if(curruptedBeginTagExists) {
				//delete all corrupted begin tags in descending order
				Iterator<Position> it = positionsToDelete.descendingIterator();
				while(it.hasNext()) {
					Position tmp = it.next();
					System.out.println("currupt: <begin tag without end tag>");
					document.replace(tmp.getOffset(), tmp.getLength(), "");
				}
				//parse the file another time to get the correct positions for the tags
				parseInput();
			}
			
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds the Comment tags for the given comment in the currently opened file at the currently selected place
	 * @param comment Comment for which the tags should be inserted
	 * @return Position of the added {@link Comment} or null if the selection is no instance of {@link ITextSelection}
	 * @throws BadLocationException Thrown if the selected location is not in the document (Should theoretically never happen)
	 */
	public Position addTagsInDocument(Comment comment) throws BadLocationException {
		Position result = null;

		ISelection selection= editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			int selStartLine = ((ITextSelection)selection).getStartLine();
			int selEndLine = ((ITextSelection)selection).getEndLine();
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			
			String keySeparator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
			String commentTag = comment.getReviewID()+keySeparator+comment.getAuthor()+keySeparator+comment.getId();
			
			if (selStartLine == selEndLine)	{
				// Only one line is selected
				String lineDelimiter = doc.getLineDelimiter(selStartLine);
				int lineDelimiterLength = 0;
				if (lineDelimiter != null) {
					lineDelimiterLength = lineDelimiter.length();
				}
				
				int insertOffset = doc.getLineOffset(selStartLine)+doc.getLineLength(selStartLine)-lineDelimiterLength;
				
				if (editor.getEditorInput().getName().endsWith(".java")) {
					doc.replace(insertOffset, 0, "/*?"+commentTag+"?*/");
				} else if (editor.getEditorInput().getName().endsWith(".xml")) {
					doc.replace(insertOffset, 0, "<!--?"+commentTag+"?-->");
				}
				
				result = new Position(doc.getLineOffset(selStartLine), doc.getLineLength(selStartLine)-lineDelimiterLength);
			} else {
				// Calculate insert position for start line
				String lineDelimiter = doc.getLineDelimiter(selStartLine);
				int lineDelimiterLength = 0;
				if (lineDelimiter != null) {
					lineDelimiterLength = lineDelimiter.length();
				}
				int insertStartOffset = doc.getLineOffset(selStartLine)+doc.getLineLength(selStartLine)-lineDelimiterLength;
				
				// Calculate insert position for end line
				lineDelimiter = doc.getLineDelimiter(selEndLine);
				lineDelimiterLength = 0;
				if (lineDelimiter != null) {
					lineDelimiterLength = lineDelimiter.length();
				}
				int insertEndOffset = doc.getLineOffset(selEndLine)+doc.getLineLength(selEndLine)-lineDelimiterLength;
				
				// Write tags
				if (editor.getEditorInput().getName().endsWith(".java")) {
					doc.replace(insertEndOffset, 0, "/*"+commentTag+"?*/");
					doc.replace(insertStartOffset, 0, "/*?"+commentTag+"*/");
					
				} else if (editor.getEditorInput().getName().endsWith(".xml")) {
					doc.replace(insertEndOffset, 0, "<!--"+commentTag+"?-->");
					doc.replace(insertStartOffset, 0, "<!--?"+commentTag+"-->");
				}
				
				result = new Position(doc.getLineOffset(selStartLine), 
						doc.getLineOffset(selEndLine) - doc.getLineOffset(selStartLine) + doc.getLineLength(selEndLine)-lineDelimiterLength);
			}
		}
		return result;
	}
	
	/**
	 * Removes the tags for one comment. Attention: if you want to delete more then one {@link Comment} in a row
	 * use the {@code removeCommentsTags(Set<Comment> comments} function, because after every deletion the document
	 * will be reparsed
	 * @param comment which should be deleted
	 * @throws BadLocationException if the {@link Position} is corrupted (the document should be reparsed then)
	 */
	public void removeCommentTags(Comment comment) throws BadLocationException {
		String key = comment.getReviewID()+"|"+comment.getAuthor()+"|"+comment.getId();
		Position[] p = idTagPositions.get(key);
		if(p == null) return;
		
		if(p[0].equals(p[1])) {
			//a single line comment
			document.replace(p[0].getOffset(), p[0].getLength(), "");
		} else {
			//begin and end tag (important: delete first end tag, then begin tag)
			document.replace(p[1].getOffset(), p[1].getLength(), "");
			document.replace(p[0].getOffset(), p[0].getLength(), "");
		}
		parseInput();
	}
	
	/**
	 * Removes all tags of the given comments. After this is done the document will be reparsed
	 * @param comments which should be deleted
	 * @throws BadLocationException if the {@link Position} is corrupted (the document should be reparsed then)
	 */
	public void removeCommentsTags(Set<Comment> comments) throws BadLocationException {
		TreeSet<Position> tagPositions = new TreeSet<Position>();
		for(Comment c : comments) {
			Position[] ps = idTagPositions.get(c.getReviewID()+"|"+c.getAuthor()+"|"+c.getId());
			if(ps != null) {
				tagPositions.addAll(Arrays.asList(ps));
			}
		}
		Iterator<Position> it = tagPositions.descendingIterator();
		while(it.hasNext()) {
			Position tmp = it.next();
			document.replace(tmp.getOffset(), tmp.getLength(), "");
		}
		parseInput();
	}
	
	/**
	 * Parses the document another time
	 */
	public void reload() {
		parseInput();
	}
	
	/**
	 * Returns the Map of existing comment tags in this document and their {@link Position}
	 * @return a {@link TreeMap} of existing comment tags in this document and their {@link Position}
	 */
	public TreeMap<String, Position> getIdPositionMap() {
		return idPositionMap;
	}
	
	/**
	 * Returns the pattern used here
	 * @return pattern used in this parser
	 */
	public static Pattern[] getPattern()
	{
		Pattern[] result = {javaTagPattern, xmlTagPattern};
		return result;
	}
}