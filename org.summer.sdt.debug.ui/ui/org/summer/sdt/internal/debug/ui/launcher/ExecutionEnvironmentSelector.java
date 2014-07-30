/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.ui.launcher;

import org.eclipse.core.variables.IStringVariable;
import org.eclipse.debug.ui.stringsubstitution.IArgumentSelector;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.summer.sdt.internal.debug.ui.jres.ExecutionEnvironmentsLabelProvider;
import org.summer.sdt.launching.JavaRuntime;
import org.summer.sdt.launching.environments.IExecutionEnvironment;

/**
 * Used to select from available execution environments.
 */
public class ExecutionEnvironmentSelector implements IArgumentSelector {

	/**
	 * Constructs a new selector
	 */
	public ExecutionEnvironmentSelector() {
	}

	public String selectArgument(IStringVariable variable, Shell shell) {
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new ExecutionEnvironmentsLabelProvider());
		dialog.setTitle(LauncherMessages.ExecutionEnvironmentSelector_0);
		dialog.setMultipleSelection(false);
		dialog.setMessage(LauncherMessages.ExecutionEnvironmentSelector_1);
		dialog.setElements(JavaRuntime.getExecutionEnvironmentsManager().getExecutionEnvironments()); 
		if (dialog.open() == Window.OK) {
			return (((IExecutionEnvironment)dialog.getResult()[0]).getId());
		}
		return null;
	}

}
