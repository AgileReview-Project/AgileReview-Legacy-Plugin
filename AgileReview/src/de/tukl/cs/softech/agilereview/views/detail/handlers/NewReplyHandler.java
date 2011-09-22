package de.tukl.cs.softech.agilereview.views.detail.handlers;

import java.util.Calendar;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.detail.DetailView;
import de.tukl.cs.softech.agilereview.views.detail.ReplyDialog;

/**
 * Handler for the new reply command of DetailView
 */
public class NewReplyHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		PluginLogger.log(this.getClass().toString(), "execute", "\"New Reply\" triggered");
		
		Shell shell = new Shell(HandlerUtil.getActiveShell(event));
		shell.setText("New Reply");
		ReplyDialog dialog = new ReplyDialog(shell, SWT.APPLICATION_MODAL);
		dialog.setSize(400, 300);
	    shell.pack();
	    shell.open();
		while (!shell.isDisposed()) {
			if (!Display.getDefault().readAndDispatch()) Display.getDefault().sleep();
	    }
		
		if(dialog.getSaved()) {
			if (ViewControl.isOpen(DetailView.class)) {
				DetailView.getInstance().addReply(dialog.getReplyAuthor(), dialog.getReplyText(), Calendar.getInstance());
				DetailView.getInstance().setFocus();
			}
		}
		return null;
	}

}