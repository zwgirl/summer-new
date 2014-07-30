/*******************************************************************************
 * Copyright (c) 2006, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.debug.ui.variables;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.model.elements.ExpressionLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementMementoProvider;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;
import org.summer.sdt.debug.core.IJavaStackFrame;
import org.summer.sdt.debug.core.IJavaValue;
import org.summer.sdt.debug.core.IJavaVariable;
import org.summer.sdt.internal.debug.ui.display.JavaInspectExpression;
import org.summer.sdt.internal.debug.ui.heapwalking.JavaWatchExpressionFilter;

/**
 * Provides provider adapters for IJavaVariables and IJavaStackFrames
 * 
 * @see IJavaVariable
 * @see JavaVariableLabelProvider
 * @see JavaVariableContentProvider
 * @see ExpressionLabelProvider
 * @see JavaExpressionContentProvider
 * @see JavaWatchExpressionFilter
 * @see JavaStackFrameMementoProvider
 * @since 3.3
 */
public class JavaDebugElementAdapterFactory implements IAdapterFactory {
	
	private static final IElementLabelProvider fgLPVariable = new JavaVariableLabelProvider();
	private static final IElementContentProvider fgCPVariable = new JavaVariableContentProvider();
	private static final IElementLabelProvider fgLPExpression = new ExpressionLabelProvider();
	private static final IElementContentProvider fgCPExpression = new JavaExpressionContentProvider();
	private static final IWatchExpressionFactoryAdapter fgWEVariable = new JavaWatchExpressionFilter();
	private static final IElementMementoProvider fgMPStackFrame = new JavaStackFrameMementoProvider();
	private static final IElementLabelProvider fgLPFrame = new JavaStackFrameLabelProvider();

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IElementLabelProvider.class.equals(adapterType)) {
			if (adaptableObject instanceof IJavaVariable) {
				return fgLPVariable; 
			}
			if (adaptableObject instanceof IJavaStackFrame) {
				return fgLPFrame;
			}
			if (adaptableObject instanceof JavaInspectExpression) {
				return fgLPExpression;
			}
		}
		if (IElementContentProvider.class.equals(adapterType)) {
			if (adaptableObject instanceof IJavaVariable) {
				return fgCPVariable;
			}
			if (adaptableObject instanceof JavaInspectExpression) {
				return fgCPExpression;
			}
			if (adaptableObject instanceof IJavaValue) {
				return fgCPExpression;
			}
		}
		if (IWatchExpressionFactoryAdapter.class.equals(adapterType)) {
			if (adaptableObject instanceof IJavaVariable) {
				return fgWEVariable;
			}
			if (adaptableObject instanceof JavaInspectExpression) {
				return fgWEVariable;
			}
		}
		if (IElementMementoProvider.class.equals(adapterType)) {
			if (adaptableObject instanceof IJavaStackFrame) {
				return fgMPStackFrame;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	public Class[] getAdapterList() {
		return new Class[] {IElementLabelProvider.class, IElementContentProvider.class, IWatchExpressionFactoryAdapter.class, IElementMementoProvider.class};
	}
}
