/*******************************************************************************
 *  Copyright (c) 2009, 2013 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.ui.breakpoints;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.debug.ui.IDetailPane3;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.summer.sdt.internal.debug.ui.JDIDebugUIPlugin;

/**
 * Common detail pane function.
 * 
 * @since 3.6
 */
public abstract class AbstractDetailPane implements IDetailPane3 {
	
	private String fName;
	private String fDescription;
	private String fId;
	private AbstractJavaBreakpointEditor fEditor;
	private Set<Integer> fAutoSaveProperties = new HashSet<Integer>();
	private IWorkbenchPartSite fSite; 
	
	// property listeners
	private ListenerList fListeners = new ListenerList();
	private Composite fEditorParent;
	
	/**
	 * Constructs a detail pane.
	 * 
	 * @param name detail pane name
	 * @param description detail pane description
	 * @param id detail pane ID
	 */
	public AbstractDetailPane(String name, String description, String id) {
		fName = name;
		fDescription = description;
		fId = id;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane3#addPropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	public void addPropertyListener(IPropertyListener listener) {
		fListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane3#removePropertyListener(org.eclipse.ui.IPropertyListener)
	 */
	public void removePropertyListener(IPropertyListener listener) {
		fListeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane2#getSelectionProvider()
	 */
	public ISelectionProvider getSelectionProvider() {
		return null;
	}
	
	/**
	 * Fires a property change to all listeners.
	 * 
	 * @param property the property
	 */
	protected void firePropertyChange(int property) {
		Object[] listeners = fListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			((IPropertyListener)listeners[i]).propertyChanged(this, property);
		}	
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#init(org.eclipse.ui.IWorkbenchPartSite)
	 */
	public void init(IWorkbenchPartSite partSite) {
		fSite = partSite;
	}

	public String getID() {
		return fId;
	}

	public String getName() {
		return fName;
	}

	public String getDescription() {
		return fDescription;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#dispose()
	 */
	public void dispose() {
		fEditor = null;
		fSite = null;
		fListeners.clear();
		fAutoSaveProperties.clear();
		fEditorParent.dispose();
	}	
	
	/**
	 * Adds the given auto save properties to this detail pain. Whenever one of these properties
	 * changes, the detail pane editor is immediately saved.
	 * 
	 * @param autosave the auto-save properties
	 */
	protected void addAutosaveProperties(int[] autosave) {
		for (int i = 0; i < autosave.length; i++) {
			fAutoSaveProperties.add(new Integer(autosave[i]));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		fEditorParent = SWTFactory.createComposite(parent, 1, 1, GridData.FILL_BOTH);
		fEditorParent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		fEditor = createEditor(fEditorParent);
		fEditor.setMnemonics(false);
		fEditor.addPropertyListener(new IPropertyListener() {
			public void propertyChanged(Object source, int propId) {
				if (fAutoSaveProperties.contains(new Integer(propId))) {
					try {
						fEditor.doSave();
						return;
					} catch (CoreException e) {
					}
				}
				firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
			}
		});
		return fEditor.createControl(fEditorParent);
	}
	
	/**
	 * Creates the detail pane specific editor.
	 * 
	 * @param parent parent composite
	 * @return editor
	 */
	protected abstract AbstractJavaBreakpointEditor createEditor(Composite parent);
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSaveAs()
	 */
	public void doSaveAs() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveAsAllowed()
	 */
	public boolean isSaveAsAllowed() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isSaveOnCloseNeeded()
	 */
	public boolean isSaveOnCloseNeeded() {
		return isDirty() && fEditor.getStatus().isOK();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#setFocus()
	 */
	public boolean setFocus() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void doSave(IProgressMonitor monitor) {
		IStatusLineManager statusLine = getStatusLine();
		if (statusLine != null) {
			statusLine.setErrorMessage(null);
		}
		try {
			fEditor.doSave();
		} catch (CoreException e) {
			if (statusLine != null) {
				statusLine.setErrorMessage(e.getMessage());
			} else {
				JDIDebugUIPlugin.log(e.getStatus());
			}
		}
	}
	
	private IStatusLineManager getStatusLine() {
		// we want to show messages globally hence we
		// have to go through the active part
		if (fSite instanceof IViewSite) {
			IViewSite site= (IViewSite) fSite;
			IWorkbenchPage page= site.getPage();
			IWorkbenchPart activePart= page.getActivePart();

			if (activePart instanceof IViewPart) {
				IViewPart activeViewPart= (IViewPart)activePart;
				IViewSite activeViewSite= activeViewPart.getViewSite();
				return activeViewSite.getActionBars().getStatusLineManager();
			}
	
			if (activePart instanceof IEditorPart) {
				IEditorPart activeEditorPart= (IEditorPart)activePart;
				IEditorActionBarContributor contributor= activeEditorPart.getEditorSite().getActionBarContributor();
				if (contributor instanceof EditorActionBarContributor) {
					return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
				}
			}
			// no active part
			return site.getActionBars().getStatusLineManager();
		}
		return null;
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISaveablePart#isDirty()
	 */
	public boolean isDirty() {
		return fEditor != null && fEditor.isDirty();
	}

	/**
	 * Returns the editor associated with this detail pane.
	 * 
	 * @return editor
	 */
	protected AbstractJavaBreakpointEditor getEditor() {
		return fEditor;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.IDetailPane#display(org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void display(IStructuredSelection selection) {
		// clear status line
		IStatusLineManager statusLine = getStatusLine();
		if (statusLine != null) {
			statusLine.setErrorMessage(null);
		}
		AbstractJavaBreakpointEditor editor = getEditor();
		Object input = null;
		if (selection != null && selection.size() == 1) {
			input = selection.getFirstElement();
			// update even if the same in case attributes have changed
		}
		try {
			editor.setInput(input);
		} catch (CoreException e) {
			JDIDebugUIPlugin.log(e);
		}
	}

}
