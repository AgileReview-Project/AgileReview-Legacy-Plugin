package de.tukl.cs.softech.agilereview.plugincontrol;

import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import de.tukl.cs.softech.agilereview.plugincontrol.refactoring.ExecutionListener;
import de.tukl.cs.softech.agilereview.plugincontrol.refactoring.ResourceChangeListener;

/**
 * Startup class in order to start this plug-in on eclipse startup
 */
public class Startup  implements IStartup {
	
	/**
	 * {@link IExecutionListener} for listening relevant Commands
	 */
	private ExecutionListener executionListener = new ExecutionListener();
	/**
	 * {@link IResourceChangeListener} for listening refactorings like renaming and movement
	 */
	private ResourceChangeListener resourceChangeListener = new ResourceChangeListener();
	
	@Override
	public void earlyStartup() {
		// add executionlistener to listen to interesting commands
		System.out.println("register executionListener");
		((ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class)).addExecutionListener(executionListener);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(resourceChangeListener, IResourceChangeEvent.POST_CHANGE);
	}
}
