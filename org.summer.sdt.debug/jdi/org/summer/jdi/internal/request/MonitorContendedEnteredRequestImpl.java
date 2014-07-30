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
package org.summer.jdi.internal.request;

import org.summer.jdi.internal.VirtualMachineImpl;
import org.summer.jdi.internal.event.MonitorContendedEnteredEventImpl;

import com.sun.jdi.request.MonitorContendedEnteredRequest;

/**
 * this class implements the corresponding interfaces declared by the JDI
 * specification. See the com.sun.jdi package for more information.
 * 
 * @since 3.3
 */
public class MonitorContendedEnteredRequestImpl extends EventRequestImpl
		implements MonitorContendedEnteredRequest {

	/**
	 * Creates new MethodExitRequest.
	 */
	public MonitorContendedEnteredRequestImpl(VirtualMachineImpl vmImpl) {
		super("MonitorContendedEnteredRequest", vmImpl); //$NON-NLS-1$
	}

	/**
	 * @return Returns JDWP EventKind.
	 */
	@Override
	protected final byte eventKind() {
		return MonitorContendedEnteredEventImpl.EVENT_KIND;
	}
}
