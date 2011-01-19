package de.tukl.cs.softech.agilereview.control.refactoring;

import java.io.IOException;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;

import de.tukl.cs.softech.agilereview.control.ReviewAccess;

/**
 * ExecutionListener which methods are executed, if a command is triggered.
 */
public class ExecutionListener implements IExecutionListener {

	@Override
	public void notHandled(String commandId, NotHandledException exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postExecuteFailure(String commandId, ExecutionException exception) {
		// TODO Auto-generated method stub

	}

	@Override
	public void postExecuteSuccess(String commandId, Object returnValue) {
		if(commandId.equals("org.eclipse.ui.file.save") || commandId.equals("org.eclipse.ui.file.saveAll")) {
			try {
				ReviewAccess.getInstance().save();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	@Override
	public void preExecute(String commandId, ExecutionEvent event) {
		/*if (commandId.equals("org.eclipse.ui.edit.cut"))
		{
			if (event.getApplicationContext() instanceof EvaluationContext)
			{
				EvaluationContext evalContext = (EvaluationContext)event.getApplicationContext();
				if (evalContext.getDefaultVariable() instanceof Set)
				{
					ITextSelection selectedText = (ITextSelection)((Set)evalContext.getDefaultVariable()).toArray()[0];	
				}
			}
		}*/
	}

}
