/*******************************************************************************
 *  Copyright (c) 2006, 2012 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.launching.environments;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Constants;
import org.summer.sdt.core.IAccessRule;
import org.summer.sdt.core.IJavaProject;
import org.summer.sdt.core.JavaCore;
import org.summer.sdt.launching.IVMInstall;
import org.summer.sdt.launching.LibraryLocation;
import org.summer.sdt.launching.environments.IAccessRuleParticipant;
import org.summer.sdt.launching.environments.IExecutionEnvironment;

/**
 * Creates default access rules for standard execution environments
 * based on OSGi profiles.
 * 
 * @since 3.3
 */
public class DefaultAccessRuleParticipant implements IAccessRuleParticipant {
	
	/**
	 * Cache of access rules per environment. Re-use rules between projects.
	 */
	private static Map<String, IAccessRule[][]> fgRules = new HashMap<String, IAccessRule[][]>();

	/* (non-Javadoc)
	 * @see org.summer.sdt.launching.environments.IAccessRuleParticipant#getAccessRules(org.summer.sdt.launching.environments.IExecutionEnvironment, org.summer.sdt.launching.IVMInstall, org.summer.sdt.launching.LibraryLocation[], org.summer.sdt.core.IJavaProject)
	 */
	public IAccessRule[][] getAccessRules(IExecutionEnvironment environment, IVMInstall vm, LibraryLocation[] libraries, IJavaProject project) {
		IAccessRule[][] allRules = null;
		allRules = fgRules.get(environment.getId());
		if (allRules == null || allRules.length != libraries.length) {
			// if a different number of libraries, create a new set of rules
			String[] packages = retrieveSystemPackages(environment);
			IAccessRule[] packageRules = null;
			if (packages.length > 0) {
				packageRules = new IAccessRule[packages.length + 1];
				for (int i = 0; i < packages.length; i++) {
					packageRules[i] = JavaCore.newAccessRule(new Path(packages[i].replace('.', IPath.SEPARATOR)), IAccessRule.K_ACCESSIBLE);
				}
				// add IGNORE_IF_BETTER flag in case another explicit entry allows access (see bug 228488)
				packageRules[packages.length] = JavaCore.newAccessRule(new Path("**/*"), IAccessRule.K_NON_ACCESSIBLE | IAccessRule.IGNORE_IF_BETTER); //$NON-NLS-1$
			} else {
				packageRules = new IAccessRule[0];
			}
			allRules = new IAccessRule[libraries.length][];
			for (int i = 0; i < allRules.length; i++) {
				allRules[i] = packageRules;	
			}
			fgRules.put(environment.getId(), allRules);
		}
		return allRules;
	}

	private String[] retrieveSystemPackages(IExecutionEnvironment environment) {
		Properties profile = environment.getProfileProperties();
		if (profile != null) {
			String packages = profile.getProperty(Constants.FRAMEWORK_SYSTEMPACKAGES);
			if (packages != null) {
				StringTokenizer tokenizer = new StringTokenizer(packages, ","); //$NON-NLS-1$
				String[] result = new String[tokenizer.countTokens() + 1];
				result[0] = "java.**"; //$NON-NLS-1$
				for (int i = 1; i < result.length; i++) {
					result[i] = tokenizer.nextToken().trim() + ".*"; //$NON-NLS-1$
				}
				return result;
			}
		}
		return new String[0];
	}
}
