package de.tukl.cs.softech.agilereview.plugincontrol;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import de.tukl.cs.softech.agilereview.dataaccess.ReviewAccess;
import de.tukl.cs.softech.agilereview.plugincontrol.exceptions.NoReviewSourceFolderException;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;
import de.tukl.cs.softech.agilereview.wizards.noreviewsource.NoReviewSourceWizard;

/**
 * @author Malte
 * This class should be statically implemented and reserved for reusable exception handling.
 */
public class ExceptionHandler {/*?|r108|Malte|c22|?*/

	/**
	 * Handles {@link NoReviewSourceFolderException} in a standardized way
	 */
	public static void handleNoReviewSourceFolderException() {
		if(PropertiesManager.getPreferences().getString(PropertiesManager.EXTERNAL_KEYS.ASK_FOR_REVIEW_FOLDER).equals(MessageDialogWithToggle.ALWAYS)) {
			openNewSourceFolderDialog();
		} else {
			if(MessageDialog.openQuestion(Display.getDefault().getActiveShell(), "No ReviewSourceFolder selected", "You triggert an AgileReview functionality." +
					"Therefor you need a persistent storage for review and comment data, a so called ReviewSourceFolder.\n" +
					"Currently there is no active ReviewSourceFolder. Do you want to activate or create one in order to use AgileReview?")) {
				openNewSourceFolderDialog();
			}
		}
	}
	
	/**
	 * Opens the new source folder dialog and loads the new one if one is selected
	 */
	private static void openNewSourceFolderDialog() {
		NoReviewSourceWizard dialog = new NoReviewSourceWizard(false);
		WizardDialog wDialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), dialog);
		wDialog.setBlockOnOpen(true);
		if (wDialog.open() == Window.OK) {
			String chosenProjectName = dialog.getChosenProjectName();
			if (ReviewAccess.createAndOpenReviewProject(chosenProjectName)) {
				ReviewAccess.getInstance().loadReviewSourceProject(chosenProjectName);
			}
		}
	}
}
