/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.ui.launcher;


import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;
import org.summer.sdt.core.IClasspathEntry;
import org.summer.sdt.core.IJavaProject;
import org.summer.sdt.core.JavaCore;
import org.summer.sdt.debug.ui.IJavaDebugUIConstants;
import org.summer.sdt.internal.debug.ui.JDIDebugUIPlugin;
import org.summer.sdt.launching.JavaRuntime;
import org.summer.sdt.ui.wizards.BuildPathDialogAccess;

/**
 * Quick fix to select an alternate JRE for a project. 
 */
public class SelectSystemLibraryQuickFix extends JREResolution {
	
	private IPath fOldPath;
	private IJavaProject fProject;
	
	public SelectSystemLibraryQuickFix(IPath oldPath, IJavaProject project) {
		fOldPath = oldPath;
		fProject = project;	
	}

	/**
	 * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
	 */
	public void run(IMarker marker) {
		try {
			handleContainerResolutionError(fOldPath, fProject);
		} catch (CoreException e) {
			JDIDebugUIPlugin.statusDialog(LauncherMessages.JREContainerResolution_Unable_to_update_classpath_1, e.getStatus());  
		}
	}
	
	protected void handleContainerResolutionError(final IPath oldPath, final IJavaProject project) throws CoreException {			
		
		String lib = oldPath.segment(0);
		IPath initialPath = null;
		if (JavaRuntime.JRELIB_VARIABLE.equals(lib)) {
			initialPath = JavaRuntime.newDefaultJREContainerPath();
		} else if (JavaRuntime.JRE_CONTAINER.equals(lib)) {
			initialPath = oldPath;
		}
		IClasspathEntry initialEntry = JavaCore.newContainerEntry(initialPath);
		final IClasspathEntry containerEntry = BuildPathDialogAccess.configureContainerEntry(JDIDebugUIPlugin.getActiveWorkbenchShell(), initialEntry, project, new IClasspathEntry[]{});
		if (containerEntry == null || containerEntry.getPath().equals(oldPath)) {
			return;
		}

		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					IPath newPath = containerEntry.getPath();
					IClasspathEntry[] classpath = project.getRawClasspath();
					for (int i = 0; i < classpath.length; i++) {
						if (classpath[i].getPath().equals(oldPath)) {
							classpath[i] = JavaCore.newContainerEntry(newPath, classpath[i].isExported());
							break;
						}
					}
					project.setRawClasspath(classpath, monitor);
				//JavaCore.setClasspathContainer(oldPath, new IJavaProject[] {project}, new IClasspathContainer[] {new JREContainer(vm, newBinding, project)}, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				}
			}
		};
		
		try {
			PlatformUI.getWorkbench().getProgressService().busyCursorWhile(runnable);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() instanceof CoreException) {
				throw (CoreException)e.getTargetException();
			}
			throw new CoreException(new Status(IStatus.ERROR, JDIDebugUIPlugin.getUniqueIdentifier(), IJavaDebugUIConstants.INTERNAL_ERROR, "An exception occurred while updating the classpath.", e.getTargetException()));  //$NON-NLS-1$
		} catch (InterruptedException e) {
			// cancelled
		}
	}		
	/**
	 * @see org.eclipse.ui.IMarkerResolution#getLabel()
	 */
	public String getLabel() {
		return NLS.bind(LauncherMessages.JREContainerResolution_Select_a_system_library_to_use_when_building__0__2, new String[]{fProject.getElementName()}); 
	}

}
