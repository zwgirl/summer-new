/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jacob Saoumi - Bug 434722 error in ConditionalJump Instruction
 *******************************************************************************/
package org.summer.sdt.internal.debug.eval.ast.instructions;

import org.eclipse.core.runtime.CoreException;
import org.summer.sdt.debug.core.IJavaObject;
import org.summer.sdt.debug.core.IJavaPrimitiveValue;
import org.summer.sdt.debug.core.IJavaValue;

public class ConditionalJump extends Jump {
	private boolean fJumpOnTrue;

	public ConditionalJump(boolean jumpOnTrue) {
		fJumpOnTrue = jumpOnTrue;
	}

	/*
	 * @see Instruction#execute()
	 */
	@Override
	public void execute() throws CoreException {
		IJavaValue conditionValue = popValue();
		IJavaPrimitiveValue condition = null;
		if (conditionValue instanceof IJavaPrimitiveValue) {
			condition = (IJavaPrimitiveValue) conditionValue;
		} else if (conditionValue instanceof IJavaObject) {
			if (((IJavaObject) conditionValue).getJavaType().getName().equals("java.lang.Boolean")) { //$NON-NLS-1$
				condition = (IJavaPrimitiveValue) ((IJavaObject) conditionValue).getField("value", false).getValue(); //$NON-NLS-1$
			}
		}

		if (!(fJumpOnTrue ^ condition.getBooleanValue())) {
			jump(fOffset);
		}
	}

	/*
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return InstructionsEvaluationMessages.ConditionalJump_conditional_jump_1;
	}

}
