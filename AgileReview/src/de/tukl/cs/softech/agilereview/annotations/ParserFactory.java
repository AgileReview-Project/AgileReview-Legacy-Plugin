package de.tukl.cs.softech.agilereview.annotations;


import java.util.HashMap;

import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.tools.NoDocumentFoundException;
import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.commenttable.CommentTableView;

/**
 * Parser Factory which creates IParser instances
 */
public class ParserFactory implements IPropertyChangeListener {
	
	/**
	 * Supported files mapping to the corresponding comment tags
	 */
	private static HashMap<String, String[]> supportedFiles = PropertiesManager.getInstance().getParserFileendingsMappingTags();
	
	static {
		new ParserFactory();
	}
	
	/**
	 * Creates a new ParserFactory in order to add itself as PropertyChangeListener to the {@link PreferenceStore}
	 */
	private ParserFactory() {
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener((IPropertyChangeListener) this);
	}
	
	/**
	 * Parser Factory in order to create an {@link IAnnotationParser} fitting to the given {@link ITextEditor}
	 * @param editor for which an {@link IAnnotationParser} should be created
	 * @return a fitting {@link IAnnotationParser} or the {@link NullParser} if no support was found
	 */
	public static IAnnotationParser createParser(IEditorPart editor) {
		IAnnotationParser parser;
		if (editor instanceof ITextEditor) {
			String fileType = editor.getEditorInput().getName().substring(editor.getEditorInput().getName().lastIndexOf(".")+1);
			String[] tags = supportedFiles.get(fileType);

			if(tags != null) {
				try {
					parser = new AnnotationParser((ITextEditor)editor, tags[0], tags[1]);
				} catch(NoDocumentFoundException ex) {
					PluginLogger.logWarning(ParserFactory.class.toString(), "createParser", "Error occured while creating an AnnotationParser for: "+editor.getTitle(), ex);
					parser = new NullParser();
				}
			} else {
				parser = new NullParser();
			}
		} else {
			parser = new NullParser();
		}
		return parser;
		
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getProperty().equals(PropertiesManager.EXTERNAL_KEYS.PARSER_FILEENDINGS)) {
			//get supported files list anew as something might has changed
			supportedFiles = PropertiesManager.getInstance().getParserFileendingsMappingTags();
			
			//create all parser anew in order to react on changed supported files list
			if(ViewControl.isOpen(CommentTableView.class)) {
				CommentTableView.getInstance().cleanEditorReferences();
				CommentTableView.getInstance().resetEditorReferences();
			}
		}
	}
}