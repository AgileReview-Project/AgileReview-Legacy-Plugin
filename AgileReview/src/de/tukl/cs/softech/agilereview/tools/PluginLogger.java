package de.tukl.cs.softech.agilereview.tools;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.tukl.cs.softech.agilereview.Activator;

/**
 * Logger for this plugin
 */
public class PluginLogger {
	
	/**
	 * Log level for the current session
	 */
	private static final int LOG_LEVEL = PropertiesManager.getInstance().getLogLevel();

	/**
	 * Logs an Error message
	 * @param txt error message
	 */
	public static void logError(String txt) {
		if(LOG_LEVEL > 0) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, txt));
		}
	}
	
	/**
	 * Logs an Error message with the given exception
	 * @param txt error message
	 * @param ex exception to be logged with this message
	 */
	public static void logError(String txt, Throwable ex) {
		if(LOG_LEVEL > 0) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, txt, ex));
		}
	}
	
	/**
	 * Logs a Warning message
	 * @param txt error message
	 */
	public static void logWarning(String txt) {
		if(LOG_LEVEL > 1) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, txt));
		}
	}
	
	/**
	 * Logs a Warning message with the given exception
	 * @param txt error message
	 * @param ex exception to be logged with this message
	 */
	public static void logWarning(String txt, Throwable ex) {
		if(LOG_LEVEL > 1) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, txt, ex));
		}
	}
	
	/**
	 * Logs an Info message
	 * @param txt error message
	 */
	public static void log(String txt) {
		if(LOG_LEVEL > 2) {
			Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, txt));
		}
	}
	
	/**
	 * Logs an Info message with the given exception
	 * @param txt error message
	 * @param ex exception to be logged with this message
	 */
	public static void log(String txt, Throwable ex) {
		if(LOG_LEVEL > 2) {
			Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, txt, ex));
		}
	}
}
