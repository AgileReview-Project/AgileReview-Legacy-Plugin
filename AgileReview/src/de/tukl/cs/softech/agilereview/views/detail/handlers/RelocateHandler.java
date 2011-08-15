package de.tukl.cs.softech.agilereview.views.detail.handlers;/*?|0000008|Peter|c2|*/

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import de.tukl.cs.softech.agilereview.views.ViewControl;
import de.tukl.cs.softech.agilereview.views.detail.DetailView;

/**
 * This handler handles the repositioning action of the comment detail view
 */
public class RelocateHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if(ViewControl.isOpen(DetailView.class)) {
			DetailView.getInstance().relocateComment();
		}
		return null;
	}
}/*|0000008|Peter|c2|?*/
