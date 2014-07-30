/*******************************************************************************
 *  Copyright (c) 2005, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.core.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.osgi.util.NLS;
import org.summer.sdt.core.IField;
import org.summer.sdt.core.IType;
import org.summer.sdt.debug.core.IJavaWatchpoint;
import org.summer.sdt.debug.core.JDIDebugModel;
import org.summer.sdt.internal.debug.ui.BreakpointUtils;

/**
 * @since 3.2
 *
 */
public class WatchpointTypeChange extends WatchpointChange {
	
	private IType fDestType, fOriginalType;
	
	public WatchpointTypeChange(IJavaWatchpoint watchpoint, IType destType, IType originalType) throws CoreException {
		super(watchpoint);
		fDestType = destType;
		fOriginalType = originalType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#getName()
	 */
	@Override
	public String getName() {
		String msg = NLS.bind(RefactoringMessages.WatchpointTypeChange_1, new String[] {getBreakpointLabel(getOriginalBreakpoint())});
		if(!"".equals(fDestType.getElementName())) { //$NON-NLS-1$
			msg = NLS.bind(RefactoringMessages.WatchpointTypeChange_0,
				new String[] {getBreakpointLabel(getOriginalBreakpoint()), fDestType.getElementName()});
		}
		return msg;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ltk.core.refactoring.Change#perform(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public Change perform(IProgressMonitor pm) throws CoreException {
		IField destField = fDestType.getField(getFieldName());
		Map<String, Object> map = new HashMap<String, Object>();
		BreakpointUtils.addJavaBreakpointAttributes(map, destField);
		IResource resource = BreakpointUtils.getBreakpointResource(destField);
		int[] range = getNewLineNumberAndRange(destField);
		IJavaWatchpoint breakpoint = JDIDebugModel.createWatchpoint(
				resource,
				fDestType.getFullyQualifiedName(),
				getFieldName(),
				NO_LINE_NUMBER,
				range[1],
				range[2],
				getHitCount(),
				true,
				map);
		apply(breakpoint);
		getOriginalBreakpoint().delete();
		return new DeleteBreakpointChange(breakpoint);
	}

	public IType getDestinationType() {
		return fDestType;
	}

	public IType getOriginalType() {
		return fOriginalType;
	}

}
