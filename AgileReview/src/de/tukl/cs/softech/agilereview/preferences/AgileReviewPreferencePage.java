package de.tukl.cs.softech.agilereview.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.tukl.cs.softech.agilereview.Activator;
import de.tukl.cs.softech.agilereview.annotations.ColorManager;
import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.preferences.fieldEditors.BorderedFieldEditor;
import de.tukl.cs.softech.agilereview.preferences.fieldEditors.LinkField;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.views.ViewControl;

/**
 * This class represents a preference page that is contributed to the Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page that is small and knows how to save, restore and apply itself. <p> This
 * page is used to modify preferences only. They are stored in the preference store that belongs to the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class AgileReviewPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
    
    /**
     * Textfield for author
     */
    private StringFieldEditor strAuthorField;
    
    /**
     * Creates the PreferencePage.
     */
    public AgileReviewPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("General AgileReview settings");
    }
    
    /**
     * Creates the field editors. Field editors are abstractions of the common GUI blocks needed to manipulate various types of preferences. Each
     * field editor knows how to save and restore itself.
     */
    @Override
    public void createFieldEditors() {
        
        final PropertiesManager pm = PropertiesManager.getInstance();
        
        // Field for author
        strAuthorField = new StringFieldEditor(PropertiesManager.EXTERNAL_KEYS.AUTHOR_NAME, "Author:", getFieldEditorParent()) {
            @Override
            protected boolean doCheckState() {
                String isValidReply = pm.isValid(this.getStringValue());
                this.setErrorMessage(isValidReply);
                return (isValidReply == null);
            }
        };
        strAuthorField.setValidateStrategy(StringFieldEditor.VALIDATE_ON_KEY_STROKE);
        addField(strAuthorField);
        
        // Field for AgileReview-folder
        List<String> list = new ArrayList<String>();
        IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject[] projArr = workspaceRoot.getProjects();
        for (IProject currProj : projArr) {
            try {
                if (currProj.hasNature(pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.AGILEREVIEW_NATURE))) {
                    list.add(currProj.getName());
                }
            } catch (CoreException e) {
                // Is thrown, if currProj is closed or does not exist -> this project is not interesting for us
            }
        }
        
        String[][] vals = new String[list.size()][2];
        for (int i = 0; i < list.size(); i++) {
            vals[i][0] = list.get(i);
            vals[i][1] = list.get(i);
        }
        
        ComboFieldEditor comboReviewProjectField = new ComboFieldEditor(PropertiesManager.EXTERNAL_KEYS.SOURCE_FOLDER, "Review source project:",
                vals, getFieldEditorParent());
        addField(comboReviewProjectField);
        
        // Checkbox for using Smart Suggestion
        BooleanFieldEditor booleanSmartSuggestionsField = new BooleanFieldEditor(PropertiesManager.EXTERNAL_KEYS.SUGGESTIONS_ENABLED,
                "Use smart suggestion", getFieldEditorParent());
        addField(booleanSmartSuggestionsField);
        
        // Grouping FieldEditor for export defaults
        BorderedFieldEditor exportWrapper = new BorderedFieldEditor(getFieldEditorParent(), "Export Defaults");
        Composite container = exportWrapper.getContainer();
        
        // Directory Browser for export folder
        DirectoryFieldEditor directoryExportField = new DirectoryFieldEditor(PropertiesManager.EXTERNAL_KEYS.EXPORT_PATH,
                "Default XLS export location:", container);
        exportWrapper.addField(directoryExportField);
        
        // export template file
        FileFieldEditor fileExportTemplateField = new FileFieldEditor(PropertiesManager.EXTERNAL_KEYS.TEMPLATE_PATH,
                "Default template for XLS export:", container);
        fileExportTemplateField.setFileExtensions(new String[] { "*.xls;*.xlsx" });
        exportWrapper.addField(fileExportTemplateField);
        
        // export templates link
        exportWrapper.addField(new LinkField(container, "Follow this <a>link</a> for downloading an example template.", pm
                .getInternalProperty(PropertiesManager.INTERNAL_KEYS.URL_EXAMPLE_EXPORT_TEMPLATES)));
        
        addField(exportWrapper);
        
        // donate button
        addField(new LinkField(getFieldEditorParent(), pm.getIcon(PropertiesManager.INTERNAL_KEYS.ICONS.DONATE), pm
                .getInternalProperty(PropertiesManager.INTERNAL_KEYS.URL_DONATIONS)));
    }
    
    // performApply() simply calls performOk (by default). As we need no additional behavior, we don't have to override it
    
    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        
        //change IDE user for color management
        ColorManager.changeIDEUser(strAuthorField.getStringValue());
        
        //refresh views
        if (ReviewAccess.getInstance().updateReviewSourceProject()) {
            ViewControl.refreshViews(ViewControl.ALL_VIEWS, true);
        } else {
            ViewControl.refreshViews(ViewControl.DETAIL_VIEW);
        }
        
        return result;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }
}