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

import org.eclipse.core.resources.IFile;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import agileReview.softech.tukl.de.CommentDocument.Comment;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.tools.NoDocumentFoundException;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;

/**
 * The AnnotationParser analyzes the document of the given editor and provides a mapping of comment tags and their {@link Position}s
 */
public class AnnotationParser implements IAnnotationParser {
    
    /**
     * Instance of PropertiesManager
     */
    private static PropertiesManager pm = PropertiesManager.getInstance();
    /**
     * Supported files mapping to the corresponding comment tags
     */
    private static final HashMap<String, String[]> supportedFiles = PropertiesManager.getParserFileendingsMappingTags();
    /**
     * Key separator for tag creation
     */
    private static String keySeparator = pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
    /**
     * Core Regular Expression to find the core tag structure
     */
    public static String RAW_TAG_REGEX = "-?\\s*(\\??)" + Pattern.quote(keySeparator) + "\\s*([^" + Pattern.quote(keySeparator) + "]+"
            + Pattern.quote(keySeparator) + "[^" + Pattern.quote(keySeparator) + "]+" + Pattern.quote(keySeparator) + "[^\\?"
            + Pattern.quote(keySeparator) + "]*)\\s*" + Pattern.quote(keySeparator) + "(\\??)\\s*(-?)";
    /**
     * Path of the file this parser represents
     */
    private String path;
    /**
     * Regular Expression used by this instance
     */
    private final String tagRegex;
    /**
     * Pattern used by this instance
     */
    private final Pattern tagPattern;
    /**
     * This map lists every comment tag found in the document with its {@link Position}
     */
    private final TreeMap<String, Position> idPositionMap = new TreeMap<String, Position>();
    /**
     * Position map of all tags
     */
    private final TreeMap<String, Position[]> idTagPositions = new TreeMap<String, Position[]>();
    /**
     * The currently displayed comments
     */
    private final TreeSet<String> displayedComments = new TreeSet<String>();
    /**
     * Document which provides the contents for this instance
     */
    private IDocument document;
    /**
     * The document of this parser
     */
    private final ITextEditor editor;
    /**
     * Annotation model for this parser
     */
    private final AgileAnnotationController annotationModel;
    
    /**
     * Creates a new instance of AnnotationParser with the given input
     * @param editor the editor which contents should be analyzed
     * @param commentBeginTag begin tag for comments in this document
     * @param commentEndTag end tag for comments in this document
     * @throws NoDocumentFoundException will be thrown, if the file type which this editor represents is not supported
     */
    AnnotationParser(ITextEditor editor, String commentBeginTag, String commentEndTag) throws NoDocumentFoundException {
        
        tagRegex = Pattern.quote(commentBeginTag) + RAW_TAG_REGEX + Pattern.quote(commentEndTag);
        tagPattern = Pattern.compile(tagRegex);
        
        this.editor = editor;
        
        if (editor.getDocumentProvider() != null) {
            this.document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
            if (this.document == null) {
                throw new NoDocumentFoundException();
            }
        } else {
            throw new NoDocumentFoundException();
        }
        
        // Set the path this Parser stand for
        IEditorInput input = this.editor.getEditorInput();
        IFile file = (IFile) input.getAdapter(IFile.class);
        final String editorTitle = editor.getTitle();
        if (file != null) {
            path = file.getFullPath().toOSString().replaceFirst(Pattern.quote(System.getProperty("file.separator")), "");
        } else {
            Display.getDefault().asyncExec(new Runnable() {
                
                @Override
                public void run() {
                    MessageDialog
                            .openError(
                                    Display.getDefault().getActiveShell(),
                                    "FileNotFoundException",
                                    "The file for editor "
                                            + editorTitle
                                            + " could not be found. Please consider saving the file before adding comments to it. Afterwards, for adding comments close the current editor and re-open it.");
                }
                
            });
            throw new NoDocumentFoundException();
        }
        this.annotationModel = new AgileAnnotationController(editor);
        parseInput();
    }
    
    /**
     * Saves the current document
     * @author Thilo Rauch (06.09.2012)
     */
    private void saveDocument() {
        // Save the current document before parsing, so automatic formatting can take place
        try {
            editor.getDocumentProvider().saveDocument(null, editor.getEditorInput(), document, true);
        } catch (CoreException e) {
            PluginLogger.logError(this.getClass().toString(), "saveDocument", "CoreException occurs while saving document of editor: "
                    + editor.getTitle(), e);
            Display.getDefault().asyncExec(new Runnable() {
                
                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), "CoreException",
                            "An eclipse internal error occured when saving the current document!\n"
                                    + "Please try to do this by hand in order to save the inserted comment tags.");
                }
                
            });
        }
    }
    
    /**
     * Parses all comment tags and saves them with their {@link Position}
     */
    private void parseInput() {
        this.document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
        PluginLogger.log(this.getClass().toString(), "parseInput", "triggered");
        
        saveDocument();
        
        idPositionMap.clear();
        idTagPositions.clear();
        HashSet<String> corruptedCommentKeys = new HashSet<String>();
        IRegion r;
        int startOffset = 0;
        try {
            Matcher matcher;
            FindReplaceDocumentAdapter fra = new FindReplaceDocumentAdapter(document);
            while ((r = fra.find(startOffset, tagRegex, true, false, false, true)) != null) {
                boolean tagDeleted = false;
                matcher = tagPattern.matcher(document.get(r.getOffset(), r.getLength()));
                if (matcher.matches()) {
                    tagDeleted = parseStartTag(corruptedCommentKeys, matcher, r);
                    // rewrite location if the line should be removed after comment deletion
                    if (!tagDeleted) {
                        tagDeleted = parseEndTag(corruptedCommentKeys, matcher, r);
                    }
                }
                
                // if a tag was deleted, search from begin of the deleted tag again
                if (tagDeleted) {
                    startOffset = r.getOffset();
                } else {
                    // -1 in oder to ensure that the startOffset will not be greater then the document length
                    startOffset = r.getOffset() + r.getLength() - 1;
                }
            }
            
            // check for begin tags without end tags
            boolean curruptedBeginTagExists = false;
            TreeSet<Position> positionsToDelete = new TreeSet<Position>();
            for (String key : idTagPositions.keySet()) {
                Position[] ps = idTagPositions.get(key);
                if (ps[1] == null) {
                    PluginLogger.log(this.getClass().toString(), "parseInput", "corrupt: <begin tag without end tag>: " + key + " --> deleting");
                    corruptedCommentKeys.add(key);
                    positionsToDelete.add(ps[0]);
                    curruptedBeginTagExists = true;
                }
            }
            
            if (curruptedBeginTagExists) {
                // delete all corrupted begin tags in descending order
                Iterator<Position> it = positionsToDelete.descendingIterator();
                while (it.hasNext()) {
                    Position tmp = it.next();
                    document.replace(tmp.getOffset(), tmp.getLength(), "");
                }
                // delete corrupted annotations
                this.annotationModel.deleteAnnotations(corruptedCommentKeys);
                // parse the file another time to get the correct positions for the tags
                parseInput();
            }
            
        } catch (BadLocationException e) {
            if (startOffset != 0) {
                PluginLogger.logError(this.getClass().toString(), "parseInput", "BadLocationException occurs while parsing the editor: "
                        + editor.getTitle(), e);
            } else {
                // file out of sync or other reasons, so eclipse cannot open file till refresh --> suppress Exception
                PluginLogger.log(this.getClass().toString(), "parseInput", "BadLocationException suppressed while parsing the editor: "
                        + editor.getTitle());
            }
            
        }
        
        // Save the current document to save the tags
        // TODO only save document if there are changes made by the parser
        saveDocument();
        
        // update annotations in order to recognize moved tags
        TreeMap<String, Position> annotationsToUpdate = new TreeMap<String, Position>();
        for (String key : displayedComments) {
            
            if (idPositionMap.get(key) != null) {
                annotationsToUpdate.put(key, idPositionMap.get(key));
            }
        }
        annotationModel.updateAnnotations(annotationsToUpdate);
    }
    
    /**
     * Parses the given region matched by the given Matcher against AgileReview end tag behavior. If the tag is corrupted, it will be added to the
     * corruptedCommentKeys set passed. Otherwise it will be added as valid tag to the tagPositionMap.
     * @param corruptedCommentKeys set of corrupted comment keys
     * @param matcher for the tagRegex on the given region
     * @param tagRegion region of the tag occurrence
     * @return true, if the tag was deleted<br>false, otherwise
     * @throws BadLocationException
     * @author Malte Brunnlieb (08.09.2012)
     */
    private boolean parseStartTag(HashSet<String> corruptedCommentKeys, Matcher matcher, IRegion tagRegion) throws BadLocationException {
        boolean tagDeleted = false;
        Position[] tagPositions;
        if (matcher.group(1).equals("?")) {
            String key = matcher.group(2).trim();
            tagPositions = idTagPositions.get(key);
            // begin tag
            if (tagPositions != null) {
                // same begin tag already exists
                corruptedCommentKeys.add(key);
                document.replace(tagRegion.getOffset(), tagRegion.getLength(), "");
                PluginLogger.log(this.getClass().toString(), "parseInput", "corrupt: <same begin tag already exists>: " + key + " --> deleting");
                tagDeleted = true;
            } else {
                idPositionMap.put(key, new Position(document.getLineOffset(document.getLineOfOffset(tagRegion.getOffset()))));
            }
            rewriteTagLocationForLineAdaption(matcher, tagRegion, true);
        }
        return tagDeleted;
    }
    
    /**
     * Parses the given region matched by the given Matcher against AgileReview end tag behavior. If the tag is corrupted, it will be added to the
     * corruptedCommentKeys set passed. Otherwise it will be added as valid tag to the tagPositionMap.
     * @param corruptedCommentKeys set of corrupted comment keys
     * @param matcher for the tagRegex on the given region
     * @param tagRegion region of the tag occurrence
     * @return true, if the tag was deleted<br>false, otherwise
     * @throws BadLocationException
     * @author Malte Brunnlieb (08.09.2012)
     */
    private boolean parseEndTag(HashSet<String> corruptedCommentKeys, Matcher matcher, IRegion tagRegion) throws BadLocationException {
        boolean tagDeleted = false;
        Position[] tagPositions;
        if (matcher.group(3).equals("?")) {
            String key = matcher.group(2).trim();
            tagPositions = idTagPositions.get(key);
            // end tag
            if (tagPositions != null) {
                if (tagPositions[1] != null) {
                    // same end tag already exists
                    corruptedCommentKeys.add(key);
                    document.replace(tagRegion.getOffset(), tagRegion.getLength(), "");
                    PluginLogger.log(this.getClass().toString(), "parseInput", "corrupt: <same end tag already exists>: " + key + " --> deleting");
                    tagDeleted = true;
                } else {
                    // end tag not set
                    Position tmp = idPositionMap.get(key);
                    int line = document.getLineOfOffset(tagRegion.getOffset());
                    tmp.setLength(document.getLineOffset(line) - tmp.getOffset() + document.getLineLength(line));
                    idPositionMap.put(key, tmp);
                    
                    Position[] tmp2 = idTagPositions.get(key);
                    tmp2[1] = new Position(tagRegion.getOffset(), tagRegion.getLength());
                    idTagPositions.put(key, tmp2);
                }
            } else {
                // end tag without begin tag
                corruptedCommentKeys.add(key);
                document.replace(tagRegion.getOffset(), tagRegion.getLength(), "");
                PluginLogger.log(this.getClass().toString(), "parseInput", "corrupt: <end tag without begin tag>: " + key + " --> deleting");
                tagDeleted = true;
            }
            rewriteTagLocationForLineAdaption(matcher, tagRegion, false);
        }
        return tagDeleted;
    }
    
    /**
     * If the line was added by AgileReview, this function will rewrite the location of the current tag such that the line delimiter will also be
     * removed.
     * @param matcher Matcher matching the tagRegex against the tag in the current line
     * @param tagRegion Region the tag occurs in
     * @param startLine states whether the startLine or the endLine will be adapted
     * @throws BadLocationException
     * @author Malte Brunnlieb (08.09.2012)
     */
    private void rewriteTagLocationForLineAdaption(Matcher matcher, IRegion tagRegion, boolean startLine) throws BadLocationException {
        String key = matcher.group(2).trim();
        if (matcher.group(4).equals("-")) {
            // set the position such that the line break beforehand will be removed too when replacing this position with the empty string
            int currLine = document.getLineOfOffset(tagRegion.getOffset());
            String lineToDelete = document.get(document.getLineOffset(currLine), document.getLineLength(currLine)
                    - document.getLineDelimiter(currLine).length());
            
            // if there is at least one tag which is not alone in this line, do not delete the whole line!
            Matcher lineMatcher = Pattern.compile("(.*)" + tagRegex + "(.*)").matcher(lineToDelete);
            if (lineMatcher.matches() && lineMatcher.group(1).trim().isEmpty() && lineMatcher.group(6).trim().isEmpty()) {
                setTagPosition(startLine, key, new Position(document.getLineOffset(currLine), document.getLineLength(currLine)));
                return;
            }
        }
        setTagPosition(startLine, key, new Position(tagRegion.getOffset(), tagRegion.getLength()));
    }
    
    /**
     * Sets the tag position newPos either for the start tag or the end tag
     * @param startTag determines whether the start tag should be set or the end tag
     * @param key of the comment the tags are for
     * @param newPos new {@link Position} to be set
     * @author Malte Brunnlieb (09.09.2012)
     */
    private void setTagPosition(boolean startTag, String key, Position newPos) {
        Position[] oldPos = idTagPositions.get(key);
        if (oldPos == null) {
            oldPos = new Position[2];
        }
        oldPos[startTag ? 0 : 1] = newPos;
        idTagPositions.put(key, oldPos);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#filter(java.util.ArrayList)
     */
    public void filter(HashSet<Comment> comments) {
        PluginLogger.log(this.getClass().toString(), "filter", "triggered");
        
        HashMap<String, Position> toDisplay = new HashMap<String, Position>();
        for (Comment c : comments) {
            String commentKey = c.getReviewID() + keySeparator + c.getAuthor() + keySeparator + c.getId();
            if (path.equals(ReviewAccess.computePath(c)) && this.idPositionMap.get(commentKey) != null) {
                toDisplay.put(commentKey, this.idPositionMap.get(commentKey));
                ColorManager.addReservation(c.getAuthor());
            }
        }
        
        displayedComments.clear();
        displayedComments.addAll(toDisplay.keySet());
        
        // Do not prove for open perspective, because annotations will be cleaned by empty comment array
        this.annotationModel.displayAnnotations(toDisplay);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#clearAnnotations()
     */
    public void clearAnnotations() {
        filter(new HashSet<Comment>());
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#addTagsInDocument(agileReview.softech.tukl.de.CommentDocument.Comment)
     */
    public void addTagsInDocument(Comment comment, boolean display) throws BadLocationException {
        // VARIANT(return Position):Position result = null;
        
        ISelection selection = editor.getSelectionProvider().getSelection();
        if (selection instanceof ITextSelection) {
            int selStartLine = ((ITextSelection) selection).getStartLine();
            int selEndLine = ((ITextSelection) selection).getEndLine();
            addTagsInDocument(comment, display, selStartLine, selEndLine);
        }
        // VARIANT(return Position):return result;
    }
    
    /**
     * Adds the Comment tags for the given comment in the currently opened file at the currently selected place
     * @param comment Comment for which the tags should be inserted
     * @param display if true, the new comment will instantly displayed<br> false, otherwise
     * @param selStartLine of the position where the comment should be inserted
     * @param selEndLine of the position where the comment should be inserted
     * @throws BadLocationException Thrown if the selected location is not in the document (Should theoretically never happen)
     */
    private void addTagsInDocument(Comment comment, boolean display, int selStartLine, int selEndLine) throws BadLocationException {
        boolean startLineInserted = false, endLineInserted = false;;
        int origSelStartLine = selStartLine;
        String commentKey = comment.getReviewID() + keySeparator + comment.getAuthor() + keySeparator + comment.getId();
        String commentTag = keySeparator + commentKey + keySeparator;
        boolean[] significantlyChanged = new boolean[] { false, false };
        
        // check if selection needs to be adapted
        int[] newLines = computeSelectionAdapations(selStartLine, selEndLine);
        if (newLines[0] != -1 || newLines[1] != -1) {
            PluginLogger.log(this.getClass().toString(), "addTagsInDocument",
                    "Selection for inserting tags needs to be adapted, performing adaptation.");
            
            // adapt starting line if necessary
            if (newLines[0] != -1) {
                int newStartLineOffset = document.getLineOffset(newLines[0]);
                int newStartLineLength = document.getLineLength(newLines[0]);
                
                // insert new line if code is in front of javadoc / multi line comments
                if (!document.get(newStartLineOffset, newStartLineLength).trim().isEmpty()) {
                    document.replace(newStartLineOffset + newStartLineLength, 0, System.getProperty("line.separator"));
                    selStartLine = newLines[0] + 1;
                    startLineInserted = true;
                } else {
                    selStartLine = newLines[0];
                }
                
                // only inform the user about these adaptations if he did not select the whole javaDoc
                if (origSelStartLine - 1 != selStartLine) {
                    significantlyChanged[0] = true;
                }
            }
            
            // adapt ending line if necessary
            // add a new line if a line was inserted before
            if (newLines[1] != -1) {
                selEndLine = newLines[1] + (startLineInserted ? 1 : 0);
                significantlyChanged[1] = true;
            } else {
                selEndLine += (startLineInserted ? 1 : 0);
            }
        }
        
        // add new line if start line is last line of javaDoc
        int[] adaptionLines = checkForCodeComment(selStartLine - 1, new String[] { "/*", "*/" });
        if (adaptionLines[1] != -1 && lineContains(adaptionLines[0] + 1, "/**")) {
            int newStartLineOffset = document.getLineOffset(selStartLine + 1);
            int newStartLineLength = document.getLineLength(selStartLine + 1);
            if (!document.get(newStartLineOffset, newStartLineLength).trim().isEmpty()) {
                document.replace(newStartLineOffset, 0, System.getProperty("line.separator"));
                selStartLine++;
                selEndLine++;
                startLineInserted = true;
                significantlyChanged[0] = true;
            }
        }
        
        // add new line if end line is last line of javaDoc
        adaptionLines = checkForCodeComment(selEndLine - 1, new String[] { "/*", "*/" });
        if (adaptionLines[1] != -1 && lineContains(adaptionLines[0] + 1, "/**")) {
            int newEndLineOffset = document.getLineOffset(selEndLine + 1);
            int newEndLineLength = document.getLineLength(selEndLine + 1);
            if (!document.get(newEndLineOffset, newEndLineLength).trim().isEmpty()) {
                document.replace(newEndLineOffset, 0, System.getProperty("line.separator"));
                selEndLine++;
                endLineInserted = true;
                significantlyChanged[1] = true;
            }
        }
        
        if (significantlyChanged[0] || significantlyChanged[1]) {
            // inform user
            Display.getDefault().asyncExec(new Runnable() {
                
                @Override
                public void run() {
                    MessageDialog
                            .openWarning(
                                    Display.getDefault().getActiveShell(),
                                    "Warning!",
                                    "Inserting a AgileReview comment at the current selection will destroy one ore more code comments. "
                                            + "AgileReview will adapt the current selection to avoid this.\nIf it is necessary a new line will be inserted above the selection which will be removed on comment deletion.");
                }
                
            });
            
        }
        
        // compute new selection
        int offset = document.getLineOffset(selStartLine);
        int length = document.getLineOffset(selEndLine) - document.getLineOffset(selStartLine) + document.getLineLength(selEndLine);
        // set new selection
        editor.getSelectionProvider().setSelection(new TextSelection(offset, length));
        
        if (selStartLine == selEndLine) {
            // Only one line is selected
            String lineDelimiter = document.getLineDelimiter(selStartLine);
            int lineDelimiterLength = 0;
            if (lineDelimiter != null) {
                lineDelimiterLength = lineDelimiter.length();
            }
            
            int insertOffset = document.getLineOffset(selStartLine) + document.getLineLength(selStartLine) - lineDelimiterLength;
            
            // Write tag -> get start+end-tag for current file-ending, insert into file
            String[] tags = supportedFiles.get(editor.getEditorInput().getName().substring(editor.getEditorInput().getName().lastIndexOf(".") + 1));
            document.replace(insertOffset, 0, tags[0] + "-?" + commentTag + "?" + tags[1]);
            
            // VARIANT(return Position):result = new Position(document.getLineOffset(selStartLine),
            // document.getLineLength(selStartLine)-lineDelimiterLength);
        } else {
            // Calculate insert position for start line
            String lineDelimiter = document.getLineDelimiter(selStartLine);
            int lineDelimiterLength = 0;
            if (lineDelimiter != null) {
                lineDelimiterLength = lineDelimiter.length();
            }
            int insertStartOffset = document.getLineOffset(selStartLine) + document.getLineLength(selStartLine) - lineDelimiterLength;
            
            // Calculate insert position for end line
            lineDelimiter = document.getLineDelimiter(selEndLine);
            lineDelimiterLength = 0;
            if (lineDelimiter != null) {
                lineDelimiterLength = lineDelimiter.length();
            }
            int insertEndOffset = document.getLineOffset(selEndLine) + document.getLineLength(selEndLine) - lineDelimiterLength;
            
            // Write tags -> get tags for current file-ending, insert second tag, insert first tag
            String[] tags = supportedFiles.get(editor.getEditorInput().getName().substring(editor.getEditorInput().getName().lastIndexOf(".") + 1));
            document.replace(insertEndOffset, 0, tags[0] + "-" + commentTag + "?" + (endLineInserted ? "-" : "") + tags[1]);
            document.replace(insertStartOffset, 0, tags[0] + "-?" + commentTag + (startLineInserted ? "-" : "") + tags[1]);
            
            // VARIANT(return Position):result = new Position(document.getLineOffset(selStartLine),
            // VARIANT(return Position): document.getLineOffset(selEndLine) - document.getLineOffset(selStartLine) +
            // document.getLineLength(selEndLine)-lineDelimiterLength);
        }
        
        // Save, so Eclipse save actions can take place before parsing  
        saveDocument();
        
        parseInput();
        if (ViewControl.isPerspectiveOpen() && display) {
            ColorManager.addReservation(comment.getAuthor());
            this.annotationModel.addAnnotation(commentKey, this.idPositionMap.get(commentKey));
        }
    }
    
    /**
     * Checks whether adding an AgileReview comment at the current selection would destroy a code comment and computes adapted line numbers to avoid
     * destruction of code comments.
     * @param startLine the current startLine of the selection
     * @param endLine the current endLine of the selection
     * @return and array containing the new start (position 0) and endline (position 1). If not nothing is to be changed the content is -1 at position
     *         0/1.
     * @throws BadLocationException
     */
    private int[] computeSelectionAdapations(int startLine, int endLine) throws BadLocationException {
        int[] result = { -1, -1 };
        String[] tags = supportedFiles.get(editor.getEditorInput().getName().substring(editor.getEditorInput().getName().lastIndexOf(".") + 1));
        
        int[] startLineAdaptions = checkForCodeComment(startLine, tags);
        int[] endLineAdaptions = checkForCodeComment(endLine, tags);
        
        // check if inserting a AgileReview comment at selected code region destroys a code comment
        if (startLineAdaptions[0] != -1 && startLineAdaptions[1] != -1 && startLineAdaptions[0] != startLine) {
            result[0] = startLineAdaptions[0];
        }
        if (endLineAdaptions[0] != -1 && endLineAdaptions[1] != -1 && endLineAdaptions[1] != endLine) {
            result[1] = endLineAdaptions[1];
        }
        
        return result;
    }
    
    /**
     * Checks whether the given line is within a code comment. If this holds the code comments start and endline is returned, else {-1, -1}.
     * @param line the line to check
     * @param tags the start and endtag of code comments
     * @return [-1, -1] if line is not within a code comment, else [startline, endline] of the code comment
     * @throws BadLocationException
     */
    private int[] checkForCodeComment(int line, String[] tags) throws BadLocationException {
        // TODO: optimize the search for tags
        
        int openTagLine = -1;
        int closeTagLine = -1;
        
        // check for opening non-AgileReview comment tags before the line
        for (int i = 0; i <= line; i++) {
            if (lineContains(i, tags[0])) {
                openTagLine = i;
            }
        }
        
        // check for according closing non-AgileReview comment tag
        if (openTagLine > -1) {
            for (int i = openTagLine; i < document.getNumberOfLines(); i++) {
                if (lineContains(i, tags[1])) {
                    closeTagLine = i;
                    break;
                }
            }
        }
        
        // finally return the results if a comment was found
        int[] result = { -1, -1 };
        if (openTagLine <= line && line <= closeTagLine) {
            // TODO: not checked if line right before starting line of code comment contains also a code comment...
            result[0] = openTagLine - 1;
            if (!(closeTagLine == line)) {
                result[1] = closeTagLine;
            }
        }
        return result;
    }
    
    /**
     * Checks whether the line identified by the lineNumber contains the given string. This function erases all AgileReview related comment tags
     * before searching for the given string.
     * @param lineNumber line number of the document
     * @param string string to be searched for
     * @return true, if the string is contained in the given line ignoring AgileReview tags,<br> false otherwise
     * @throws BadLocationException if the given line could not be found in the current document
     * 
     * @author Malte Brunnlieb (08.09.2012)
     */
    private boolean lineContains(int lineNumber, String string) throws BadLocationException {
        String lineContent = document.get(document.getLineOffset(lineNumber), document.getLineLength(lineNumber)).trim();
        lineContent = lineContent.replaceAll(Pattern.quote(string) + RAW_TAG_REGEX + Pattern.quote(string), "");
        return lineContent.contains(string);
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#removeCommentTags(agileReview.softech.tukl.de.CommentDocument.Comment)
     */
    public void removeCommentTags(Comment comment) throws BadLocationException {
        removeCommentsTags(new HashSet<Comment>(Arrays.asList(new Comment[] { comment })));
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#removeCommentsTags(java.util.Set)
     */
    public void removeCommentsTags(Set<Comment> comments) throws BadLocationException {
        String separator = pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
        TreeSet<Position> tagPositions = new TreeSet<Position>();
        String key;
        HashSet<String> keyList = new HashSet<String>();
        for (Comment c : comments) {
            key = c.getReviewID() + separator + c.getAuthor() + separator + c.getId();
            Position[] ps = idTagPositions.get(key);
            if (ps != null) {
                ArrayList<ComparablePosition> cp = new ArrayList<ComparablePosition>();
                for (int i = 0; i < ps.length; i++) {
                    cp.add(new ComparablePosition(ps[i]));
                }
                tagPositions.addAll(cp);
                keyList.add(key);
            }
        }
        
        // delete annotations and map entries
        this.annotationModel.deleteAnnotations(keyList);
        this.idTagPositions.keySet().removeAll(keyList);
        this.idPositionMap.keySet().removeAll(keyList);
        
        Iterator<Position> it = tagPositions.descendingIterator();
        while (it.hasNext()) {
            Position tmp = it.next();
            document.replace(tmp.getOffset(), tmp.getLength(), "");
        }
        
        parseInput();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#reload()
     */
    public void reload() {
        parseInput();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see de.tukl.cs.softech.agilereview.annotations.IAnnotationParser#revealCommentLocation(java.lang.String)
     */
    public void revealCommentLocation(String commentID) throws BadLocationException {
        if (this.idPositionMap.get(commentID) != null) {
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
    
    /**
     * Computes the next position from the given one on where a comment is located.
     * @param current The current position
     * @return The next position or<br> null if there is no such position.
     */
    public Position getNextCommentsPosition(Position current) {
        Position position;
        TreeSet<ComparablePosition> positions = new TreeSet<ComparablePosition>();
        for (String key : displayedComments) {
            position = idPositionMap.get(key);
            positions.add(new ComparablePosition(position));
        }
        return positions.higher(new ComparablePosition(current));
    }
    
    @Override
    public void relocateComment(Comment comment, boolean display) throws BadLocationException {
        ISelection selection = editor.getSelectionProvider().getSelection();
        if (selection instanceof ITextSelection) {
            int selStartLine = ((ITextSelection) selection).getStartLine();
            int selEndLine = ((ITextSelection) selection).getEndLine();
            removeCommentTags(comment);
            addTagsInDocument(comment, display, selStartLine, selEndLine);
        }
    }
}