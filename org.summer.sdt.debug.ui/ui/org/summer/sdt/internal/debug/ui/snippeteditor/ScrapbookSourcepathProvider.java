/*******************************************************************************
 * Copyright (c) 2004, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.ui.snippeteditor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.summer.sdt.core.IJavaProject;
import org.summer.sdt.launching.IJavaLaunchConfigurationConstants;
import org.summer.sdt.launching.IRuntimeClasspathEntry;
import org.summer.sdt.launching.JavaRuntime;
import org.summer.sdt.launching.StandardSourcePathProvider;

/**
 * Sourcepath provider for a snippet editor
 */
public class ScrapbookSourcepathProvider extends StandardSourcePathProvider {

	@Override
	public IRuntimeClasspathEntry[] computeUnresolvedClasspath(ILaunchConfiguration configuration) throws CoreException {
		boolean useDefault = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, true);
		IRuntimeClasspathEntry[] entries = null;
		if (useDefault) {
			// the default source lookup path is the classpath of the associated project
			IJavaProject project = JavaRuntime.getJavaProject(configuration);
			entries = JavaRuntime.computeUnresolvedRuntimeClasspath(project);
		} else {
			// recover persisted source path
			entries = recoverRuntimePath(configuration, IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH);
		}
		return entries;
	}
}
