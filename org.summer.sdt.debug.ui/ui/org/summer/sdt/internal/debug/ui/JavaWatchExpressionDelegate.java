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
package org.summer.sdt.internal.debug.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IWatchExpressionDelegate;
import org.eclipse.debug.core.model.IWatchExpressionListener;
import org.eclipse.debug.core.model.IWatchExpressionResult;
import org.summer.sdt.core.IJavaProject;
import org.summer.sdt.debug.core.IJavaDebugTarget;
import org.summer.sdt.debug.core.IJavaStackFrame;
import org.summer.sdt.debug.core.IJavaThread;
import org.summer.sdt.debug.eval.IAstEvaluationEngine;
import org.summer.sdt.debug.eval.IEvaluationListener;
import org.summer.sdt.debug.eval.IEvaluationResult;
import org.summer.sdt.internal.debug.core.JDIDebugPlugin;
import org.summer.sdt.internal.debug.core.JavaDebugUtils;
import org.summer.sdt.internal.debug.core.model.JDIThread;
import org.summer.sdt.internal.debug.ui.display.JavaInspectExpression;

/**
 * 
 */
public class JavaWatchExpressionDelegate implements IWatchExpressionDelegate {
	
	private String fExpressionText;
	private IWatchExpressionListener fListener;

	/**
	 * @see org.eclipse.debug.core.model.IWatchExpressionDelegate#getValue(java.lang.String, org.eclipse.debug.core.model.IDebugElement)
	 */
	public void evaluateExpression(String expression, IDebugElement context, IWatchExpressionListener listener) {
		fExpressionText= expression;
		fListener= listener;
		// find a stack frame context if possible.
		IStackFrame frame = null;
		if (context instanceof IStackFrame) {
			frame = (IStackFrame)context;
		} else if (context instanceof IThread) {
			try {
				frame = ((IThread)context).getTopStackFrame();
			} catch (DebugException e) {
			}
		}
		if (frame == null) {
			fListener.watchEvaluationFinished(null);	
		} else {
			// consult the adapter in case of a wrappered debug model
			final IJavaStackFrame javaStackFrame =(IJavaStackFrame) ((IAdaptable)frame).getAdapter(IJavaStackFrame.class);
			if (javaStackFrame != null) {
				doEvaluation(javaStackFrame);
			} else {
				fListener.watchEvaluationFinished(null);
			}	
		}
	}
	
	/**
	 * Ask to evaluate the expression in the context of the given stack frame.
	 * 
	 * The evaluation is performed asynchronously. A change debug event, with
	 * this as the source, is fired when the evaluation is completed.
	 * 
	 * @param javaStackFrame the stack frame in the context of which performed
	 * the evaluation.
	 */
	protected void doEvaluation(IJavaStackFrame javaStackFrame) {
		IJavaThread thread = (IJavaThread)javaStackFrame.getThread();
		if (preEvaluationCheck(thread)) {
			thread.queueRunnable(new EvaluationRunnable(javaStackFrame));
		} else {
			fListener.watchEvaluationFinished(null);
		}
	}
	
	private boolean preEvaluationCheck(IJavaThread javaThread) {
		if (javaThread == null) {
			return false;
		}
		if (javaThread.isSuspended() && ((JDIThread)javaThread).isInvokingMethod()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Runnable used to evaluate the expression.
	 */
	private final class EvaluationRunnable implements Runnable {
		
		private final IJavaStackFrame fStackFrame;
		
		private EvaluationRunnable(IJavaStackFrame frame) {
			fStackFrame= frame;
		}
		
		public void run() {
			IJavaProject project = JavaDebugUtils.resolveJavaProject(fStackFrame);
			if (project == null) {
				fListener.watchEvaluationFinished(null);
				return;
			}
			IAstEvaluationEngine evaluationEngine= JDIDebugPlugin.getDefault().getEvaluationEngine(project, (IJavaDebugTarget) fStackFrame.getDebugTarget());
			// the evaluation listener
			IEvaluationListener listener= new IEvaluationListener() {
				public void evaluationComplete(final IEvaluationResult result) {
					IWatchExpressionResult watchResult= new IWatchExpressionResult() {
						public IValue getValue() {
							return result.getValue();
						}
						public boolean hasErrors() {
							return result.hasErrors();
						}
						public String[] getErrorMessages() {
							return JavaInspectExpression.getErrorMessages(result);
						}
						public String getExpressionText() {
							return result.getSnippet();
						}
						public DebugException getException() {
							return result.getException();
						}
					};
					fListener.watchEvaluationFinished(watchResult);
				}
			};
			try {
				evaluationEngine.evaluate(fExpressionText, fStackFrame, listener, DebugEvent.EVALUATION_IMPLICIT, false);
			} catch (DebugException e) {
				JDIDebugPlugin.log(e);
				fListener.watchEvaluationFinished(null);
			}
		}
	}
}
