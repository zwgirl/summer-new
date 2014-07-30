/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.ui.variables;

import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Action which opens preference settings for Java variables.
 */
public class VariableOptionsAction implements IViewActionDelegate {
	
    /* (non-Javadoc)
     * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
     */
    public void init(IViewPart view) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
    	SWTFactory.showPreferencePage("org.summer.sdt.debug.ui.JavaDetailFormattersPreferencePage",  //$NON-NLS-1$
    			new String[] {"org.summer.sdt.debug.ui.JavaDetailFormattersPreferencePage", //$NON-NLS-1$
    							"org.summer.sdt.debug.ui.JavaLogicalStructuresPreferencePage",  //$NON-NLS-1$
    							"org.summer.sdt.debug.ui.heapWalking",  //$NON-NLS-1$
    							"org.summer.sdt.debug.ui.JavaPrimitivesPreferencePage"}); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
    }
}
