package de.tukl.cs.softech.agilereview;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.PLUGIN_ID);
	/**
	 * The shared instance
	 */
	private static Activator plugin;
	/**
	 * ContextActivation for later deactivation
	 */
	private IContextActivation contextActivation;
	
	/**
	 * The constructor
	 */
	public Activator() {

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		// Activate the agilereview loaded context
//		IContextService contextService = (IContextService)PlatformUI.getWorkbench().getService(IContextService.class);
//		this.contextActivation = contextService.activateContext("de.tukl.cs.softech.agilereview.loaded");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
//		if (this.contextActivation != null) {
//			IContextService contextService = (IContextService)PlatformUI.getWorkbench().getService(IContextService.class);
//			contextService.deactivateContext(this.contextActivation);
//			this.contextActivation = null;
//		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
}
