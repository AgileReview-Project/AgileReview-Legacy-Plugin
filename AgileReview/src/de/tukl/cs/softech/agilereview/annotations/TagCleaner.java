package de.tukl.cs.softech.agilereview.annotations;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import de.tukl.cs.softech.agilereview.tools.PluginLogger;
import de.tukl.cs.softech.agilereview.tools.PropertiesManager;

/**
 * Remove one or more comment tags
 */
public class TagCleaner {
    
    /**
     * Instance of PropertiesManager
     */
    private static PropertiesManager pm = PropertiesManager.getInstance();
    /**
     * Supported files mapping to the corresponding comment tags
     */
    private static final HashMap<String, String[]> supportedFiles = PropertiesManager.getParserFileendingsMappingTags();
    /**
     * Key separator for tag creation
     */
    private static String keySeparator = pm.getInternalProperty(PropertiesManager.INTERNAL_KEYS.KEY_SEPARATOR);
    /**
     * Core Regular Expression to find the core tag structure
     */
    private static String rawTagRegex = "([^" + Pattern.quote(keySeparator) + "]+" + Pattern.quote(keySeparator) + "[^" + Pattern.quote(keySeparator)
            + "]+" + Pattern.quote(keySeparator) + "[^\\?" + Pattern.quote(keySeparator) + "]*)";
    
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
        final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
        if (file.exists()) {
            try {
                //check whether this file is supported
                String beginTag;
                String endTag;
                if (supportedFiles.containsKey(file.getFileExtension())) {
                    beginTag = supportedFiles.get(file.getFileExtension())[0];
                    endTag = supportedFiles.get(file.getFileExtension())[1];
                } else {
                    return false;
                }
                
                // identifier already quoted?
                if (!regex) {
                    identifier = Pattern.quote(identifier);
                }
                String identifierRegex = "\\s*(\\??)\\s*" + Pattern.quote(keySeparator) + "\\s*" + identifier + "\\s*" + Pattern.quote(keySeparator)
                        + "\\s*(\\??)\\s*";
                
                // get input from file
                InputStream is = file.getContents();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                
                // read file line by line, replace tags
                String input = "";
                String line = br.readLine();
                boolean changedFile = false;
                while (line != null) {
                    String searchRegex = Pattern.quote(beginTag) + identifierRegex + Pattern.quote(endTag);
                    if (line.matches(".*" + searchRegex + ".*")) {
                        changedFile = true;
                    }
                    input += line.replaceAll(searchRegex, "");
                    line = br.readLine();
                    // append new line chars if not last line
                    input += line != null ? System.getProperty("line.separator") : "";
                }
                if (changedFile) {
                    // write modified content to file
                    byte[] bytes = input.getBytes();
                    InputStream source = new ByteArrayInputStream(bytes);
                    file.setContents(source, false, true, null);
                }
                
            } catch (final CoreException e) {
                PluginLogger.logError(TagCleaner.class.toString(), "execute", "CoreException while trying to remove tags.", e);
                Display.getDefault().asyncExec(new Runnable() {
                    
                    @Override
                    public void run() {
                        MessageDialog.openError(Display.getDefault().getActiveShell(), "CoreException",
                                "An error occured while reading/saving the file " + file.getFullPath() + "\n");
                    }
                    
                });
                return false;
            } catch (IOException e) {
                PluginLogger.logError(TagCleaner.class.toString(), "execute", "IOException while trying to remove tags.", e);
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}