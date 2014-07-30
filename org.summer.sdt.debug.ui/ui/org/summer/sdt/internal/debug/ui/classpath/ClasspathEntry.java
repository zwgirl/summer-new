/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.summer.sdt.internal.debug.ui.classpath;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.summer.sdt.core.IJavaProject;
import org.summer.sdt.launching.IRuntimeClasspathEntry;
import org.summer.sdt.launching.IRuntimeClasspathEntry2;

public class ClasspathEntry extends AbstractClasspathEntry implements IRuntimeClasspathEntry, IAdaptable {

	private IRuntimeClasspathEntry entry= null;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getJavaProject()
	 */
	public IJavaProject getJavaProject() {
		return entry.getJavaProject();
	}
	public ClasspathEntry(IRuntimeClasspathEntry entry, IClasspathEntry parent) {
		this.parent= parent;
		this.entry= entry;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ClasspathEntry) {
			ClasspathEntry other= (ClasspathEntry)obj;
			if (entry != null) {
				return entry.equals(other.entry);
			}
		} else if (obj instanceof IRuntimeClasspathEntry) {
			return entry.equals(obj);
		}
		return false;
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return entry.hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return entry.getPath().toOSString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getType()
	 */
	public int getType() {
		return entry.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getMemento()
	 */
	public String getMemento() throws CoreException {
		return entry.getMemento(); 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getPath()
	 */
	public IPath getPath() {
		return entry.getPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getResource()
	 */
	public IResource getResource() {
		return entry.getResource();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getSourceAttachmentPath()
	 */
	public IPath getSourceAttachmentPath() {
		return entry.getSourceAttachmentPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#setSourceAttachmentPath(org.eclipse.core.runtime.IPath)
	 */
	public void setSourceAttachmentPath(IPath path) {
		entry.setSourceAttachmentPath(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getSourceAttachmentRootPath()
	 */
	public IPath getSourceAttachmentRootPath() {
		return entry.getSourceAttachmentRootPath();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#setSourceAttachmentRootPath(org.eclipse.core.runtime.IPath)
	 */
	public void setSourceAttachmentRootPath(IPath path) {
		entry.setSourceAttachmentRootPath(path);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getClasspathProperty()
	 */
	public int getClasspathProperty() {
		return entry.getClasspathProperty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#setClasspathProperty(int)
	 */
	public void setClasspathProperty(int location) {
		entry.setClasspathProperty(location);
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getLocation()
	 */
	public String getLocation() {
		return entry.getLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getSourceAttachmentLocation()
	 */
	public String getSourceAttachmentLocation() {
		return entry.getSourceAttachmentLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getSourceAttachmentRootLocation()
	 */
	public String getSourceAttachmentRootLocation() {
		return entry.getSourceAttachmentRootLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getVariableName()
	 */
	public String getVariableName() {
		return entry.getVariableName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.launching.IRuntimeClasspathEntry#getClasspathEntry()
	 */
	public org.summer.sdt.core.IClasspathEntry getClasspathEntry() {
		return entry.getClasspathEntry();
	}
	
	public IRuntimeClasspathEntry getDelegate() {
		return entry;
	}
	
	public boolean hasChildren() {
		IRuntimeClasspathEntry rpe = getDelegate();
		return rpe instanceof IRuntimeClasspathEntry2 &&
		 ((IRuntimeClasspathEntry2)rpe).isComposite();
	}
	
	public IClasspathEntry[] getChildren(ILaunchConfiguration configuration) {
		IRuntimeClasspathEntry rpe = getDelegate();
		if (rpe instanceof IRuntimeClasspathEntry2) {
			IRuntimeClasspathEntry2 r2 = (IRuntimeClasspathEntry2) rpe;
			try {
				IRuntimeClasspathEntry[] entries = r2.getRuntimeClasspathEntries(configuration);
				IClasspathEntry[] cps = new IClasspathEntry[entries.length];
				for (int i = 0; i < entries.length; i++) {
					IRuntimeClasspathEntry childEntry = entries[i];
					cps[i] = new ClasspathEntry(childEntry, this);
				}
				return cps;
			} catch (CoreException e) {
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.summer.sdt.internal.debug.ui.classpath.IClasspathEntry#isEditable()
	 */
	public boolean isEditable() {
		return getParent() instanceof ClasspathGroup;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (getDelegate() instanceof IAdaptable) {
			return ((IAdaptable)getDelegate()).getAdapter(adapter);
		}
		return null;
	}
}
