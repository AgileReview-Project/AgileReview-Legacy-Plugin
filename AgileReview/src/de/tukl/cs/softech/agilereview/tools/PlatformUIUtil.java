/**
 * Copyright (c) 2011, 2012 AgileReview Development Team and others.
 * All rights reserved. This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License - v 1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 * Contributors: Malte Brunnlieb, Philipp Diebold, Peter Reuter, Thilo Rauch
 */
package de.tukl.cs.softech.agilereview.tools;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * @author Malte Brunnlieb (26.04.2018)
 */
public class PlatformUIUtil {
    
    public static IWorkbenchPage getActivePage() {
        //wait until the active page is created, then register all listeners
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        IWorkbenchPage page;
        while ((page = window.getActivePage()) == null) {
        }
        return page;
    }
    
    /**
     * @param workbench
     * @return
     * @author Malte Brunnlieb (26.04.2018)
     */
    public static IWorkbenchWindow getActiveWorkbenchWindow() {
        IWorkbench workbench = getWorkbench();
        IWorkbenchWindow window;
        while ((window = workbench.getActiveWorkbenchWindow()) == null) {
        }
        return window;
    }
    
    /**
     * @return
     * @author Malte Brunnlieb (26.04.2018)
     */
    public static IWorkbench getWorkbench() {
        IWorkbench workbench;
        while ((workbench = PlatformUI.getWorkbench()) == null) {
        }
        return workbench;
    }
}
