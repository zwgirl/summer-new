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
package org.summer.sdt.internal.launching;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.summer.sdt.core.IClasspathEntry;
import org.summer.sdt.core.IJavaProject;
import org.summer.sdt.launching.IRuntimeClasspathEntry;
import org.summer.sdt.launching.IRuntimeClasspathEntry2;
import org.summer.sdt.launching.IRuntimeClasspathEntryResolver;
import org.summer.sdt.launching.IVMInstall;
import org.summer.sdt.launching.JavaRuntime;

/**
 * Default resolver for a contributed classpath entry
 */
public class DefaultEntryResolver implements IRuntimeClasspathEntryResolver {
	/* (non-Javadoc)
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.summer.sdt.launching.IRuntimeClasspathEntry, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration) throws CoreException {
		IRuntimeClasspathEntry2 entry2 = (IRuntimeClasspathEntry2)entry;
		IRuntimeClasspathEntry[] entries = entry2.getRuntimeClasspathEntries(configuration);
		List<IRuntimeClasspathEntry> resolved = new ArrayList<IRuntimeClasspathEntry>();
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry[] temp = JavaRuntime.resolveRuntimeClasspathEntry(entries[i], configuration);
			for (int j = 0; j < temp.length; j++) {
				resolved.add(temp[j]);
			}
		}
		return resolved.toArray(new IRuntimeClasspathEntry[resolved.size()]);
	}
	/* (non-Javadoc)
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.summer.sdt.launching.IRuntimeClasspathEntry, org.summer.sdt.core.IJavaProject)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project) throws CoreException {
		IRuntimeClasspathEntry2 entry2 = (IRuntimeClasspathEntry2)entry;
		IRuntimeClasspathEntry[] entries = entry2.getRuntimeClasspathEntries(null);
		List<IRuntimeClasspathEntry> resolved = new ArrayList<IRuntimeClasspathEntry>();
		for (int i = 0; i < entries.length; i++) {
			IRuntimeClasspathEntry[] temp = JavaRuntime.resolveRuntimeClasspathEntry(entries[i], project);
			for (int j = 0; j < temp.length; j++) {
				resolved.add(temp[j]);
			}
		}
		return resolved.toArray(new IRuntimeClasspathEntry[resolved.size()]);
	}
		
	/* (non-Javadoc)
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntryResolver#resolveVMInstall(org.summer.sdt.core.IClasspathEntry)
	 */
	public IVMInstall resolveVMInstall(IClasspathEntry entry) throws CoreException {
		return null;
	}
}
