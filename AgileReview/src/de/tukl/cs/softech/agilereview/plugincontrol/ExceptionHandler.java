package de.tukl.cs.softech.agilereview.plugincontrol;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import de.tukl.cs.softech.agilereview.plugincontrol.exceptions.NoReviewSourceFolderException;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.wizards.noreviewsource.NoReviewSourceWizard;

/**
 * This class should be statically implemented and reserved for reusable exception handling.
 * @author Malte
 */
public class ExceptionHandler {/* ?|r108|Malte|c22|? */

	/**
	 *  Title for the message dialogs
	 */
	private static String title = "No Review Source Project selected";
	/**
	 * Message of the message dialogs
	 */
	private static String message = "In order to use AgileReview you need a persistent storage for review and comment data, a so called Review Source Project. "
		    + "Currently there is no active Review Source Project.\nDo you want to activate or create one in order to use AgileReview?";	
	
    /**
     * Handles {@link NoReviewSourceFolderException} in a standardized way
     */
    public static void handleNoReviewSourceFolderException() {/*?|r108|Thilo|c1|*/
	    if (MessageDialog.openQuestion(Display.getDefault().getActiveShell(), title, message)) {
	    	openNewSourceFolderDialog();
	    }
    }/*|r108|Thilo|c1|?*/
    
    /**
     * Handles {@link NoReviewSourceFolderException} for Eclipse startup
     */
    public static void handleNoReviewSourceFolderExceptionOnStartUp() {/*?|r108|Thilo|c0|*/
		MessageDialogWithToggle md = MessageDialogWithToggle
			    .openYesNoQuestion(
				    Display.getDefault().getActiveShell(),
				    title,
				    message,
				    "Do not ask me on Eclipse startup.",
				    PropertiesManager.getPreferences().getBoolean(PropertiesManager.EXTERNAL_KEYS.DO_NOT_ASK_FOR_REVIEW_FOLDER), 
				    null, null);
		 PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.DO_NOT_ASK_FOR_REVIEW_FOLDER, md.getToggleState());
	    // Eventually show the NewSourceFolderDialog
	    if ((md.getReturnCode() == IDialogConstants.YES_ID)) {
	    	openNewSourceFolderDialog();
	    }
    }/*|r108|Thilo|c0|?*/
    
    /**
     * Opens the new source folder dialog and loads the new one if one is selected
     */
    private static void openNewSourceFolderDialog() {
	NoReviewSourceWizard wizard = new NoReviewSourceWizard(true);/* ?|r113|Malte|c0|? */
	WizardDialog wDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);
	wDialog.setBlockOnOpen(true);
	wDialog.open();/* ?|r108|Peter Reuter|c7|? */
    }
}
