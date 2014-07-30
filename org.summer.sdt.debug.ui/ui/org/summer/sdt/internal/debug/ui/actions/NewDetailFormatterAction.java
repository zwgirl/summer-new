/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.ui.actions;


import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.summer.sdt.debug.core.IJavaType;
import org.summer.sdt.debug.core.IJavaValue;
import org.summer.sdt.debug.core.IJavaVariable;
import org.summer.sdt.internal.debug.ui.DetailFormatter;
import org.summer.sdt.internal.debug.ui.DetailFormatterDialog;
import org.summer.sdt.internal.debug.ui.JDIDebugUIPlugin;
import org.summer.sdt.internal.debug.ui.JavaDetailFormattersManager;
import org.summer.sdt.internal.debug.ui.display.JavaInspectExpression;

public class NewDetailFormatterAction extends ObjectActionDelegate {

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		IStructuredSelection selection= getCurrentSelection();
		if (selection == null || selection.size() != 1) {
			return;
		}
		Object element= selection.getFirstElement();
		String typeName;
		try {
			IJavaType type;
			if (element instanceof IJavaVariable) {
				type = ((IJavaValue)((IJavaVariable) element).getValue()).getJavaType();
			} else if (element instanceof JavaInspectExpression) {
				type = ((IJavaValue)((JavaInspectExpression) element).getValue()).getJavaType();
			} else {
				return;
			}
			if (type == null) {
				return;
			}
			typeName= type.getName();
		} catch (DebugException e) {
			return;
		}
		JavaDetailFormattersManager detailFormattersManager= JavaDetailFormattersManager.getDefault();
		DetailFormatter detailFormatter= new DetailFormatter(typeName, "", true); //$NON-NLS-1$
		if (new DetailFormatterDialog(JDIDebugUIPlugin.getActivePage().getWorkbenchWindow().getShell(), detailFormatter, null, true, false).open() == Window.OK) {
			detailFormattersManager.setAssociatedDetailFormatter(detailFormatter);
		}
	}

}
