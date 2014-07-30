/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.summer.sdt.core.IMember;
import org.summer.sdt.debug.core.IJavaBreakpoint;
import org.summer.sdt.debug.core.IJavaLineBreakpoint;
import org.summer.sdt.debug.core.IJavaPatternBreakpoint;
import org.summer.sdt.debug.core.IJavaStratumLineBreakpoint;
import org.summer.sdt.debug.core.IJavaTargetPatternBreakpoint;
import org.summer.sdt.ui.JavaElementLabelProvider;

/**
 * Adapter factory for java breakpoints.
 */
public class JavaBreakpointWorkbenchAdapterFactory implements IAdapterFactory {
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType != IWorkbenchAdapter.class || !(adaptableObject instanceof IJavaBreakpoint)) {
			return null;
		}
		return new IWorkbenchAdapter() {
			private JavaElementLabelProvider fJavaLabelProvider;

			public Object[] getChildren(Object o) {
				return null;
			}

			public ImageDescriptor getImageDescriptor(Object object) {
				return null;
			}

			/**
			 * Returns a label for breakpoints that doesn't include information
			 * which the user can edit. This assures that this label can be used
			 * in situations where the user is changing values (like the title bar
			 * of the property dialog).
			 */
			public String getLabel(Object o) {
				if (!(o instanceof IJavaBreakpoint)) {
					return null;
				}
				IJavaBreakpoint breakpoint = (IJavaBreakpoint) o;
				StringBuffer label = new StringBuffer();
				try {
					String type= breakpoint.getTypeName();
					if (type != null) {
						label.append(type);
					}
				} catch (CoreException e) {
					JDIDebugUIPlugin.log(e);
				}
				if (breakpoint instanceof IJavaPatternBreakpoint) {
					try {
						label.append(((IJavaPatternBreakpoint) breakpoint).getSourceName());
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
					}
				} else if (breakpoint instanceof IJavaTargetPatternBreakpoint) {
					try {
						label.append(((IJavaTargetPatternBreakpoint) breakpoint).getSourceName());
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
					}
				} else if (breakpoint instanceof IJavaStratumLineBreakpoint) {
					try {
						label.append(((IJavaStratumLineBreakpoint) breakpoint).getSourceName());
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
					}
				}
				if (breakpoint instanceof IJavaLineBreakpoint) {
					IJavaLineBreakpoint lineBreakpoint= ((IJavaLineBreakpoint) breakpoint);
					try {
						int lineNumber= lineBreakpoint.getLineNumber();
						if (lineNumber != -1) {
							label.append(DebugUIMessages.JavaBreakpointWorkbenchAdapterFactory_1); 
							label.append(lineNumber);
							label.append(']');
						}
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
					}
					try {
						IMember member= BreakpointUtils.getMember(lineBreakpoint);
						if (member != null) {
							label.append(" - "); //$NON-NLS-1$
							label.append(getJavaLabelProvider().getText(member));
						}
					} catch (CoreException e) {
						JDIDebugUIPlugin.log(e);
					}
				}
				return label.toString();
			}

			public Object getParent(Object o) {
				return null;
			}
			
			protected JavaElementLabelProvider getJavaLabelProvider() {
				if (fJavaLabelProvider == null) {
					fJavaLabelProvider = new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT);		
				}
				return fJavaLabelProvider;
			}
		};
	}

	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class };
	}
}
