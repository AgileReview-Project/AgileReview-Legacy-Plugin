package de.tukl.cs.softech.agilereview.annotations;

import java.util.HashMap;

import org.eclipse.ui.texteditor.ITextEditor;

import de.tukl.cs.softech.agilereview.tools.NoDocumentFoundException;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * Parser Factory which creates IParser instances
 */
public class ParserFactory {
	
	/**
	 * Supported files mapping to the corresponding comment tags
	 */
	private static final HashMap<String, String[]> supportedFiles = PropertiesManager.getInstance().getParserFileendingsAndTags();
	
	/**
	 * Parser Factory in order to create an {@link IAnnotationParser} fitting to the given {@link ITextEditor}
	 * @param editor for which an {@link IAnnotationParser} should be created
	 * @return a fitting {@link IAnnotationParser} or the {@link NullParser} if no support was found
	 */
	public static IAnnotationParser createParser(ITextEditor editor) {
		IAnnotationParser parser;
		String fileType = editor.getEditorInput().getName().substring(editor.getEditorInput().getName().lastIndexOf(".")+1);
		String[] tags = supportedFiles.get(fileType);
		
		if(tags != null) {
			try {
				parser = new AnnotationParser(editor, tags[0], tags[1]);
			} catch(NoDocumentFoundException ex) {
				PluginLogger.logWarning(ParserFactory.class.toString(), "createParser", "Error occured while creating an AnnotationParser for: "+editor.getTitle(), ex);
				parser = new NullParser();
			}
		} else {
			parser = new NullParser();
		}
		
		return parser;
	}
}