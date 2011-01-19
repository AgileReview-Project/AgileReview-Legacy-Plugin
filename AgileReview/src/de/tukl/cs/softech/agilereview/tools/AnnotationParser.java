package de.tukl.cs.softech.agilereview.tools;

import java.util.TreeMap;
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
	private static final String javaTagRegex = "/\\*\\s*(\\??)\\s*([^\\?]*)\\s*(\\??)\\s*\\*/";
	/**
	 * Regular Expression to find and differentiate each comment tag in XML files
	 */	
	private static final String xmlTagRegex = "<!--\\s*(\\??)\\s*([^\\?]*)\\s*(\\??)\\s*-->";
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
		Matcher matcher;
		FindReplaceDocumentAdapter fra = new FindReplaceDocumentAdapter(document);
		IRegion r;
		int startOffset = 0;
		try {
			while((r = fra.find(startOffset, tagRegex, true, false, false, true)) != null) {
				int line = document.getLineOfOffset(r.getOffset());
				matcher = tagPattern.matcher(document.get(r.getOffset(), r.getLength()));
				if(matcher.matches()) {
					if(matcher.group(1).equals("?")) {
						//begin tag
						idPositionMap.put(matcher.group(2).trim(), new Position(document.getLineOffset(line)));
					}
					if(matcher.group(3).equals("?") && idPositionMap.get(matcher.group(2).trim()) != null) {
						//end tag
						Position tmp = idPositionMap.get(matcher.group(2).trim());
						tmp.setLength(document.getLineOffset(line) - tmp.getOffset() + document.getLineLength(line));
						idPositionMap.put(matcher.group(2).trim(), tmp);
					}
				}
				startOffset = r.getOffset()+r.getLength();
			}
			
			//TODO DEBUG
			for(String s : idPositionMap.keySet()) {
				System.out.println(s+":\n"+document.get(idPositionMap.get(s).getOffset(), idPositionMap.get(s).getLength())+"\n-----");
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void cleanCurruptedTags() {
		//TODO remove inconsistent tags
	}
	
	/**
	 * Adds the Comment tags for the given comment in the currently opened file at the currently selected place
	 * @param c Comment for which the tags should be inserted
	 * @return Position of the added {@link Comment} or null if the selection is no instance of {@link ITextSelection}
	 * @throws BadLocationException Thrown if the selected location is not in the document (Should theoretically never happen)
	 */
	public Position addTagsInDocument(Comment c) throws BadLocationException {
		Position result = null;

		ISelection selection= editor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			int selStartLine = ((ITextSelection)selection).getStartLine();
			int selEndLine = ((ITextSelection)selection).getEndLine();
			IDocument doc = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			
			String keySeparator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
			String commentTag = c.getReviewID()+keySeparator+c.getAuthor()+keySeparator+c.getId();
			
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
					doc.replace(insertStartOffset, 0, "/*?"+commentTag+"*/");
					doc.replace(insertEndOffset, 0, "/*"+commentTag+"?*/");
				} else if (editor.getEditorInput().getName().endsWith(".xml")) {
					doc.replace(insertStartOffset, 0, "<!--?"+commentTag+"-->");
					doc.replace(insertEndOffset, 0, "<!--"+commentTag+"?-->");
				}
				
				result = new Position(doc.getLineOffset(selStartLine), 
						doc.getLineOffset(selEndLine) - doc.getLineOffset(selStartLine) + doc.getLineLength(selEndLine)-lineDelimiterLength);
			}
		}
		return result;
	}
	
	public void removeCommentTags(Comment c) {
		
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