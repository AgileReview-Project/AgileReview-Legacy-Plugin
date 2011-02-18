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
	 * Formats the three parameter to one output String
	 * @param className from which this logging was called
	 * @param methodName from which this logging was called
	 * @param msg which should be displayed
	 * @return formatted String
	 */
	private static String formatMessage(String className, String methodName, String msg) {
		return className+" -> "+methodName+": "+msg;
	}

	/**
	 * Logs an Error message
	 * @param className from which this logging was called
	 * @param methodName from which this logging was called
	 * @param msg error message
	 */
	public static void logError(String className, String methodName, String msg) {
		if(LOG_LEVEL > 0) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, formatMessage(className, methodName, msg)));
		}
	}
	
	/**
	 * Logs an Error message with the given exception
	 * @param className from which this logging was called
	 * @param methodName from which this logging was called
	 * @param msg error message
	 * @param ex exception to be logged with this message
	 */
	public static void logError(String className, String methodName, String msg, Throwable ex) {
		if(LOG_LEVEL > 0) {
			Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, formatMessage(className, methodName, msg), ex));
		}
	}
	
	/**
	 * Logs a Warning message
	 * @param className from which this logging was called
	 * @param methodName from which this logging was called
	 * @param msg error message
	 */
	public static void logWarning(String className, String methodName, String msg) {
		if(LOG_LEVEL > 1) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, formatMessage(className, methodName, msg)));
		}
	}
	
	/**
	 * Logs a Warning message with the given exception
	 * @param className from which this logging was called
	 * @param methodName from which this logging was called
	 * @param msg error message
	 * @param ex exception to be logged with this message
	 */
	public static void logWarning(String className, String methodName, String msg, Throwable ex) {
		if(LOG_LEVEL > 1) {
			Activator.getDefault().getLog().log(new Status(IStatus.WARNING, Activator.PLUGIN_ID, formatMessage(className, methodName, msg), ex));
		}
	}
	
	/**
	 * Logs an Info message
	 * @param className from which this logging was called
	 * @param methodName from which this logging was called
	 * @param msg error message
	 */
	public static void log(String className, String methodName, String msg) {
		if(LOG_LEVEL > 2) {
			Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, formatMessage(className, methodName, msg)));
		}
	}
	
	/**
	 * Logs an Info message with the given exception
	 * @param className from which this logging was called
	 * @param methodName from which this logging was called
	 * @param msg error message
	 * @param ex exception to be logged with this message
	 */
	public static void log(String className, String methodName, String msg, Throwable ex) {
		if(LOG_LEVEL > 2) {
			Activator.getDefault().getLog().log(new Status(IStatus.INFO, Activator.PLUGIN_ID, formatMessage(className, methodName, msg), ex));
		}
	}
}
