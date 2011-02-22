package de.tukl.cs.softech.agilereview.annotations;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * Remove one or more comment tags
 */
public class TagCleaner {

	/**
	 * Key separator for tag creation
	 */
	private static String keySeparator = PropertiesManager.getInstance().getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
	/**
	 * Core Regular Expression to find the core tag structure
	 */
	private static String rawTagRegex = "([^"+Pattern.quote(keySeparator)+"]+"+Pattern.quote(keySeparator)+"[^"+Pattern.quote(keySeparator)+"]+"+Pattern.quote(keySeparator)+"[^\\?"+Pattern.quote(keySeparator)+"]*)";
	
	/**
	 * Removes the comment tags from the file given by the path
	 * @param path the path relative to the workspaceroot
	 * @return true if tags were removed successfully, else false
	 */
	public static boolean removeAllTags(IPath path) {
		return removeTag(path, rawTagRegex, true);	
	}
	
	/**
	 * @param path the path of the file which will be modified
	 * @param identifier the identifier of the comment to be removed
	 * @param regex true if the identifier already a regex
	 * @return whether tags were removed successful
	 */
	public static boolean removeTag(IPath path, String identifier, boolean regex) {
		boolean result = true;
		
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
		try {
			// identifier already quoted?
			if (!regex) {
				identifier = Pattern.quote(identifier);
			}
			String identifierRegex = "\\s*(\\??)\\s*"+Pattern.quote(keySeparator)+"\\s*"+identifier+"\\s*"+Pattern.quote(keySeparator)+"\\s*(\\??)\\s*";
			
			// get input from file
			InputStream is = file.getContents();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));			
			
			// read file line by line, replace tags
			String input = "";						
			String line = br.readLine();
			while (line != null) {								
				if (path.getFileExtension().equals("java")) {
					String javaregex = "/\\*"+identifierRegex+"\\*/";
					input += line.replaceAll(javaregex, "");	
				} else if (path.getFileExtension().equals("xml")) {
					String xmlregex = "<!--"+identifierRegex+"-->";
					input += line.replaceAll(xmlregex, "");
				}
				line = br.readLine();
				// append new line chars if not last line
				input += line!=null ? System.getProperty("line.separator") : "";
			}

			// write modified content to file
			byte[] bytes = input.getBytes();
			InputStream source = new ByteArrayInputStream(bytes);
			file.setContents(source, false, true, null);
		
		} catch (CoreException e) {
			PluginLogger.logError(TagCleaner.class.toString(), "execute", "CoreException while trying to remove tags.", e);
			result = false;
		} catch (IOException e) {
			PluginLogger.logError(TagCleaner.class.toString(), "execute", "IOException while trying to remove tags.", e);
			result = false;
		}
		
		return result;
	}

}
