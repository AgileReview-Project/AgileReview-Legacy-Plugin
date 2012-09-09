package de.tukl.cs.softech.agilereview.annotations;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
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
     * Supported files mapping to the corresponding comment tags
     */
    private static final HashMap<String, String[]> supportedFiles = PropertiesManager.getParserFileendingsMappingTags();
    
    /**
     * Removes the comment tags from the file given by the path
     * @param path the path relative to the workspaceroot
     * @return true if tags were removed successfully, else false
     */
    public static boolean removeAllTags(IPath path) {
        return removeTag(path, null);
    }
    
    /**
     * @param path the path of the file which will be modified
     * @param identifier the identifier of the comment to be removed. Can be set to null in order to remove all tags
     * @return whether tags were removed successful
     */
    public static boolean removeTag(IPath path, String identifier) {
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
                
                // get input from file
                InputStream is = file.getContents();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                
                // read file line by line, replace tags
                String input = "";
                String line = br.readLine();
                boolean fileChanged = false;
                while (line != null) {
                    
                    String searchRegex = Pattern.quote(beginTag) + AnnotationParser.RAW_TAG_REGEX + Pattern.quote(endTag);
                    Pattern p = Pattern.compile(searchRegex);
                    Matcher m = p.matcher(line);
                    boolean deleteLine = false;
                    StringBuffer sb = new StringBuffer();
                    while (m.find()) {
                        fileChanged = true;
                        if (identifier == null || m.group(2).equals(identifier)) {
                            if (m.group(4).equals("-")) {
                                deleteLine = true;
                            }
                            m.appendReplacement(sb, "");
                        }
                    }
                    m.appendTail(sb);
                    
                    line = br.readLine();
                    if (!deleteLine || !sb.toString().matches("\\s*")) {
                        input += sb.toString();
                        input += line != null ? System.getProperty("line.separator") : "";
                    }
                }
                br.close();
                isr.close();
                is.close();
                
                if (fileChanged) {
                    // write modified content to file
                    byte[] bytes = input.getBytes();
                    InputStream source = new ByteArrayInputStream(bytes);
                    file.setContents(source, false, true, null);
                    source.close();
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