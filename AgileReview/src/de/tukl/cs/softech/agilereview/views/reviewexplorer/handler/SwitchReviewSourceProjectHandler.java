package de.tukl.cs.softech.agilereview.views.reviewexplorer.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Handler for switching the Review Source Project in the Review Explorer. 
 * This Handler simply opens the preferences dialog.
 */
public class SwitchReviewSourceProjectHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		//TODO get this from Properties (externalize Strings?)
		String prefPageId = "de.tukl.cs.softech.agilereview.preferences.AgileReviewPreferencePage";
		PreferenceDialog prefDialog = PreferencesUtil.createPreferenceDialogOn(HandlerUtil.getActiveShell(event), prefPageId, null, null);
		prefDialog.open();
		return null;
	}

}
