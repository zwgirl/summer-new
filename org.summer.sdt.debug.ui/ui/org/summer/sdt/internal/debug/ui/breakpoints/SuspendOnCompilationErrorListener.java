/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.ui.breakpoints;

import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.DebugException;
import org.summer.sdt.core.dom.Message;
import org.summer.sdt.debug.core.IJavaBreakpoint;
import org.summer.sdt.debug.core.IJavaBreakpointListener;
import org.summer.sdt.debug.core.IJavaDebugTarget;
import org.summer.sdt.debug.core.IJavaExceptionBreakpoint;
import org.summer.sdt.debug.core.IJavaLineBreakpoint;
import org.summer.sdt.debug.core.IJavaStackFrame;
import org.summer.sdt.debug.core.IJavaThread;
import org.summer.sdt.debug.core.IJavaType;
import org.summer.sdt.internal.debug.ui.JDIDebugUIPlugin;
import org.summer.sdt.internal.debug.ui.JavaDebugOptionsManager;

/**
 * Breakpoint listener extension for the "suspend on compilation error" exception breakpoint.
 * Changed to a breakpoint specific listener in 3.5 when breakpoint specific listeners were
 * introduced.
 * 
 * @since 3.5
 */
public class SuspendOnCompilationErrorListener implements IJavaBreakpointListener {
	
	public static final String ID_COMPILATION_ERROR_LISTENER = JDIDebugUIPlugin.getUniqueIdentifier() + ".compilationErrorListener"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.debug.core.IJavaBreakpointListener#addingBreakpoint(org.summer.sdt.debug.core.IJavaDebugTarget,
	 * org.summer.sdt.debug.core.IJavaBreakpoint)
	 */
	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.debug.core.IJavaBreakpointListener#breakpointHasCompilationErrors(org.summer.sdt.debug.core.IJavaLineBreakpoint,
	 * org.summer.sdt.core.dom.Message[])
	 */
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.debug.core.IJavaBreakpointListener#breakpointHasRuntimeException(org.summer.sdt.debug.core.IJavaLineBreakpoint,
	 * org.eclipse.debug.core.DebugException)
	 */
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.debug.core.IJavaBreakpointListener#breakpointHit(org.summer.sdt.debug.core.IJavaThread,
	 * org.summer.sdt.debug.core.IJavaBreakpoint)
	 */
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		IJavaExceptionBreakpoint exception = (IJavaExceptionBreakpoint) breakpoint;
		if (exception.getExceptionTypeName().equals("java.lang.Error")) { //$NON-NLS-1$
			// only resolve compilation error if the exception actually is a java.lang.Error
			// (which is used to indicate compilation errors by the Eclipse Java compiler).
		    try {
		    	IJavaStackFrame frame = (IJavaStackFrame)thread.getTopStackFrame();
				if (frame != null) {
					IMarker problem = JavaDebugOptionsManager.getDefault().getProblem(frame);
					return problem != null ? SUSPEND : DONT_SUSPEND;
				}
		    } catch (DebugException e) {
		        JDIDebugUIPlugin.log(e);
		        // don't suspend if we can't determine if there is a problem
		        return DONT_SUSPEND;
		    }
		}
		return DONT_SUSPEND;		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.debug.core.IJavaBreakpointListener#breakpointInstalled(org.summer.sdt.debug.core.IJavaDebugTarget,
	 * org.summer.sdt.debug.core.IJavaBreakpoint)
	 */
	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.debug.core.IJavaBreakpointListener#breakpointRemoved(org.summer.sdt.debug.core.IJavaDebugTarget,
	 * org.summer.sdt.debug.core.IJavaBreakpoint)
	 */
	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.debug.core.IJavaBreakpointListener#installingBreakpoint(org.summer.sdt.debug.core.IJavaDebugTarget,
	 * org.summer.sdt.debug.core.IJavaBreakpoint, org.summer.sdt.debug.core.IJavaType)
	 */
	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
		return DONT_CARE;
	}

}
