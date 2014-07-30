/*******************************************************************************
 * Copyright (c) 2007, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.summer.sdt.internal.launching;

import java.io.IOException;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.osgi.util.NLS;
import org.summer.jdi.TimeoutException;
import org.summer.sdt.debug.core.JDIDebugModel;
import org.summer.sdt.launching.IJavaLaunchConfigurationConstants;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.ListeningConnector;
import com.sun.jdi.connect.TransportTimeoutException;

/**
 * A process that represents a VM listening connector that is waiting
 * for a VM to remotely connect.  Allows the user to see the status
 * of the connection and terminate it.  If a successful connection 
 * occurs, the debug target is added to the launch and this process
 * is removed.
 * @since 3.4
 * @see SocketListenConnector
 */
public class SocketListenConnectorProcess implements IProcess {
	
	/**
	 * Whether this process has been terminated.
	 */
	private boolean fTerminated = false;
	/**
	 * The launch this process belongs to
	 */
	private ILaunch fLaunch;
	/**
	 * The port this connector will listen on.
	 */
	private String fPort;
	/**
	 * The system job that will wait for incoming VM connections.
	 */
	private WaitForConnectionJob fWaitForConnectionJob;
	
	/**
	 * Creates this process.  The label for this process will state
	 * the port the connector is listening at.
	 * @param launch the launch this process belongs to
	 * @param port the port the connector will wait on
	 */
	public SocketListenConnectorProcess(ILaunch launch, String port){
		fLaunch = launch;
		fPort = port;
	}
	
	/**
	 * Starts a job that will accept a VM remotely connecting to the 
	 * given connector.  The #startListening() method must have been
	 * called on the connector with the same arguments before calling
	 * this method.  The 'port' argument in the map should have the same
	 * value as the port specified in this process' constructor.
	 * 
	 * @param connector the connector that will accept incoming connections
	 * @param arguments map of arguments that are used by the connector
	 * @throws CoreException if a problem occurs trying to accept a connection
	 * @see SocketListenConnector
	 */
	public void waitForConnection(ListeningConnector connector, Map<String, Connector.Argument> arguments) throws CoreException{
		if (isTerminated()){
			throw new CoreException(getStatus(LaunchingMessages.SocketListenConnectorProcess_0, null, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED));
		}
		fWaitForConnectionJob = new WaitForConnectionJob(this,connector,arguments);
		fWaitForConnectionJob.setPriority(Job.SHORT);
		fWaitForConnectionJob.addJobChangeListener(new JobChangeAdapter(){
			@Override
			public void running(IJobChangeEvent event) {
				fireReadyToAcceptEvent();
			}
			@Override
			public void done(IJobChangeEvent event) {
				if (event.getResult().equals(Status.CANCEL_STATUS)){
					try{
						terminate();
					} catch (DebugException e){}
				}
			}
		});
		fWaitForConnectionJob.schedule();
	}

	/**
	 * Returns an error status using the passed parameters.
	 * 
	 * @param message the status message
	 * @param exception lower level exception associated with the
	 *  error, or <code>null</code> if none
	 * @param code error code
	 * @return the new {@link IStatus}
	 */
	protected static IStatus getStatus(String message, Throwable exception, int code) {
		return new Status(IStatus.ERROR, LaunchingPlugin.getUniqueIdentifier(), code, message, exception);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IProcess#getExitValue()
	 */
	public int getExitValue() throws DebugException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IProcess#getLabel()
	 */
	public String getLabel() {
		return NLS.bind(LaunchingMessages.SocketListenConnectorProcess_1, new String[]{fPort});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IProcess#getLaunch()
	 */
	public ILaunch getLaunch() {
		return fLaunch;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#canTerminate()
	 */
	public boolean canTerminate() {
		return !fTerminated;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#isTerminated()
	 */
	public boolean isTerminated() {
		return fTerminated;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ITerminate#terminate()
	 */
	public void terminate() throws DebugException {
		if (!fTerminated){
			fTerminated = true;
			if (fWaitForConnectionJob != null){
				fWaitForConnectionJob.cancel();
				fWaitForConnectionJob.stopListening();
				fWaitForConnectionJob = null;
			}
			fireTerminateEvent();
		}
	}
	
	/**
	 * Fires a terminate event.
	 */
	protected void fireTerminateEvent() {
		DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[]{new DebugEvent(this, DebugEvent.TERMINATE)});
		}
	}
	
	/**
	 * Fires a custom model specific event when this connector is ready to accept incoming
	 * connections from a remote VM.
	 */
	protected void fireReadyToAcceptEvent(){
		DebugPlugin manager= DebugPlugin.getDefault();
		if (manager != null) {
			manager.fireDebugEventSet(new DebugEvent[]{new DebugEvent(this, DebugEvent.MODEL_SPECIFIC, IJavaLaunchConfigurationConstants.DETAIL_CONFIG_READY_TO_ACCEPT_REMOTE_VM_CONNECTION)});
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IProcess#getStreamsProxy()
	 */
	public IStreamsProxy getStreamsProxy() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IProcess#getAttribute(java.lang.String)
	 */
	public String getAttribute(String key) {
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.IProcess#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String key, String value) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * Job that waits for incoming VM connections.  When a remote
	 * VM connection is accepted, a debug target is created and 
	 * the process that created this job is removed.
	 */
	class WaitForConnectionJob extends Job{

		private IProcess fWaitProcess;
		private ListeningConnector fConnector;
		private Map<String, Connector.Argument> fArguments;
		/**
		 * Flag that can be set to tell this job that waiting
		 * for incoming connections has been cancelled.  If true,
		 * IOExceptions will be ignored, allowing other threads
		 * to close the socket without generating an error.
		 */
		private boolean fListeningStopped = false;
		
		public WaitForConnectionJob(IProcess waitProcess, ListeningConnector connector, Map<String, Connector.Argument> arguments) {
			super(getLabel());
			fWaitProcess = waitProcess;
			fConnector = connector;
			fArguments = arguments;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try{
				// The following code sets a timeout (not officially supported in Sun's spec).
				// Allows polling for job cancellation. If the implementation does not support timeout
				// the job cannot be cancelled (but the launch can still be terminated).
				Connector.Argument timeout = fArguments.get("timeout"); //$NON-NLS-1$
				if (timeout != null){
					timeout.setValue("3000"); //$NON-NLS-1$
				}
				
				VirtualMachine vm = null;
				while (vm == null && !monitor.isCanceled()){
					try {
						vm = fConnector.accept(fArguments);
					} catch (TransportTimeoutException e){
					}
				}
				
				if (monitor.isCanceled()){
					fConnector.stopListening(fArguments);
					return Status.CANCEL_STATUS;
				}
				
				ILaunchConfiguration configuration = fLaunch.getLaunchConfiguration();
				boolean allowTerminate = false;
				if (configuration != null) {
					try{
						allowTerminate = configuration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_ALLOW_TERMINATE, false);
					} catch (CoreException e) {
						LaunchingPlugin.log(e);
					}
				}
				Connector.Argument portArg= fArguments.get("port"); //$NON-NLS-1$
				String vmLabel = constructVMLabel(vm, portArg.value(), fLaunch.getLaunchConfiguration());
				IDebugTarget debugTarget= JDIDebugModel.newDebugTarget(fLaunch, vm, vmLabel, null, allowTerminate, true);
				fLaunch.addDebugTarget(debugTarget);
				fLaunch.removeProcess(fWaitProcess);
				return Status.OK_STATUS;
			} catch (IOException e) {
				if (fListeningStopped){
					return Status.CANCEL_STATUS;
				}
				return getStatus(LaunchingMessages.SocketListenConnectorProcess_4, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED);
			} catch (IllegalConnectorArgumentsException e) {
				return getStatus(LaunchingMessages.SocketListenConnectorProcess_4, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED); 
			} finally {
				// Always try to close the socket
				stopListening();
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#canceling()
		 */
		@Override
		protected void canceling() {
			stopListening();
		}
			
		/**
		 * Tells the listening connector to stop listening.  Ensures
		 * that the socket is closed and the port released.  Sets a flag
		 * so that the IOException thrown by the connector's accept method
		 * will be ignored.
		 */
		protected void stopListening() {
			if (!fListeningStopped){
				try{
					fListeningStopped = true;
					fConnector.stopListening(fArguments);
				} catch (IOException e) {
					done(getStatus(LaunchingMessages.SocketListenConnectorProcess_5, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED)); 
				} catch (IllegalConnectorArgumentsException e) {
					done(getStatus(LaunchingMessages.SocketListenConnectorProcess_5, e, IJavaLaunchConfigurationConstants.ERR_REMOTE_VM_CONNECTION_FAILED)); 
				}
			}
		}
		
		/**
		 * Helper method that constructs a human-readable label for a remote VM.
		 * @param vm the VM
		 * @param port the port
		 * @param configuration the configuration 
		 * @return the new VM label
		 */
		protected String constructVMLabel(VirtualMachine vm, String port, ILaunchConfiguration configuration) {
			String name = null;
			try {
				name = vm.name();
			} catch (TimeoutException e) {
				// do nothing
			} catch (VMDisconnectedException e) {
				// do nothing
			}
			if (name == null) {
				if (configuration == null) {
					name = ""; //$NON-NLS-1$
				} else {
					name = configuration.getName();
				}
			}
			StringBuffer buffer = new StringBuffer(name);
			buffer.append('['); 
			buffer.append(port);
			buffer.append(']'); 
			return buffer.toString();
		}
		
	}
}
