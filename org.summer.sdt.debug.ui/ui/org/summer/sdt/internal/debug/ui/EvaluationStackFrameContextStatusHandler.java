/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.ui.IWorkbenchWindow;
import org.summer.sdt.debug.core.IJavaDebugTarget;
import org.summer.sdt.debug.core.IJavaStackFrame;

/**
 * Supplies the current stack frame context. Currently use for logical object structure
 * computations.
 * 
 * @since 3.1
 */
public class EvaluationStackFrameContextStatusHandler implements IStatusHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, java.lang.Object)
	 */
	public Object handleStatus(IStatus status, Object source) {
		if (source instanceof IDebugElement) {
			IDebugElement element = (IDebugElement) source;
			IJavaDebugTarget target = (IJavaDebugTarget) element.getDebugTarget().getAdapter(IJavaDebugTarget.class);
			if (target != null) {
				IJavaStackFrame frame = EvaluationContextManager.getEvaluationContext((IWorkbenchWindow)null);
				if (frame != null && frame.getDebugTarget().equals(target)) {
					return frame;
				}
			}
		}
		return null;
	}

}
