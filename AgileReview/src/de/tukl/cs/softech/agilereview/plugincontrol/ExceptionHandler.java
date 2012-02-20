package de.tukl.cs.softech.agilereview.plugincontrol;

import org.eclipse.jface.dialogs.IDialogConstants;
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
     * Handles {@link NoReviewSourceFolderException} in a standardized way
     */
    public static void handleNoReviewSourceFolderException() {
	if (!PropertiesManager.getPreferences().getBoolean(PropertiesManager.EXTERNAL_KEYS.DO_NOT_ASK_FOR_REVIEW_FOLDER)) {/* ?|r108|Peter Reuter|c6| */
	    MessageDialogWithToggle md = MessageDialogWithToggle
		    .openYesNoQuestion(
			    Display.getDefault().getActiveShell(),
			    "No Review Source Project selected",
			    "In order to use AgileReview you need a persistent storage for review and comment data, a so called Review Source Project. "
				    + "Currently there is no active Review Source Project.\nDo you want to activate or create one in order to use AgileReview?",
			    "Do not ask me on Eclipse startup.",
			    PropertiesManager.getPreferences().getBoolean(PropertiesManager.EXTERNAL_KEYS.DO_NOT_ASK_FOR_REVIEW_FOLDER), null, null);
	    PropertiesManager.getPreferences().setValue(PropertiesManager.EXTERNAL_KEYS.DO_NOT_ASK_FOR_REVIEW_FOLDER, md.getToggleState());
	    if (md.getReturnCode() == IDialogConstants.YES_ID) {
		openNewSourceFolderDialog();
	    }
	}/* |r108|Peter Reuter|c6|? */
    }

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
