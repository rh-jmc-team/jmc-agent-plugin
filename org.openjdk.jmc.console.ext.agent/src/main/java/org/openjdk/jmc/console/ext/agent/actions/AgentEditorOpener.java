/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020, Red Hat Inc. All rights reserved.
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The contents of this file are subject to the terms of either the Universal Permissive License
 * v 1.0 as shown at http://oss.oracle.com/licenses/upl
 *
 * or the following license:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openjdk.jmc.console.ext.agent.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.openjdk.jmc.common.io.IOToolkit;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.editor.AgentEditor;
import org.openjdk.jmc.console.ext.agent.editor.AgentEditorInput;
import org.openjdk.jmc.console.ext.agent.wizards.StartAgentWizard;
import org.openjdk.jmc.rjmx.ConnectionException;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.rjmx.IConnectionListener;
import org.openjdk.jmc.rjmx.IServerHandle;
import org.openjdk.jmc.rjmx.JVMSupportToolkit;
import org.openjdk.jmc.rjmx.actionprovider.IActionFactory;
import org.openjdk.jmc.ui.common.action.Executable;
import org.openjdk.jmc.ui.misc.DialogToolkit;
import org.openjdk.jmc.ui.misc.DisplayToolkit;

import java.util.Objects;

// TODO: Export IActionFactory to this plug-in and remove @SuppressWarnings once it's official in JMC   
@SuppressWarnings("restriction")
public class AgentEditorOpener implements IActionFactory {
	private final static String JOB_NAME = "Connecting to RJMX service";
	private final static String MESSAGE_COULD_NOT_CONNECT = "Could not connect";
	private final static String MESSAGE_STARTING_AGENT_ON_REMOTE_JVM_NOT_SUPPORTED = "Starting an agent on remote JVM is not supported";
	private final static String MESSAGE_START_AGENT_MANUALLY = "Start the agent manually and try again";
	private final static String MESSAGE_FAILED_TO_OPEN_AGENT_EDITOR = "Failed to open the JMC Agent Editor";

	@Override
	public Executable createAction(IServerHandle serverHandle) {
		return () -> new ConnectJob(serverHandle).schedule();
	}

	private final static class ConnectJob extends Job implements IConnectionListener {

		private final IServerHandle serverHandle;
		private IConnectionHandle connectionHandle;
		private AgentJmxHelper helper;

		private ConnectJob(IServerHandle serverHandle) {
			super(JOB_NAME);

			this.serverHandle = Objects.requireNonNull(serverHandle);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				helper = new AgentJmxHelper(serverHandle);
				helper.addConnectionChangedListener(this);
				connectionHandle = helper.getConnectionHandle();

				IStatus ret = doRun(monitor);

				helper.removeConnectionChangedListener(this);
				return ret;
			} catch (ConnectionException e) {
				// FIXME: Show stacktrace? (Need to show our own ExceptionDialog in that case, or maybe create our own DetailsAreaProvider, see WorkbenchStatusDialogManager.setDetailsAreaProvider)
				return new Status(IStatus.ERROR, AgentPlugin.PLUGIN_ID, IStatus.ERROR,
						NLS.bind(MESSAGE_COULD_NOT_CONNECT, serverHandle.getServerDescriptor().getDisplayName(),
								e.getMessage()), e);
			}
		}

		private IStatus doRun(IProgressMonitor monitor) {
			String[] error = JVMSupportToolkit.checkConsoleSupport(connectionHandle);
			if (error.length == 2 && !DialogToolkit.openConfirmOnUiThread(error[0], error[1])) {
				return Status.CANCEL_STATUS;
			}

			// local JVM but agent not running
			if (!helper.isMXBeanRegistered() && helper.isLocalJvm()) {
				DisplayToolkit.safeAsyncExec(Display.getDefault(),
						() -> DialogToolkit.openWizardWithHelp(new StartAgentWizard(helper)));
				return Status.OK_STATUS;
			}

			// remote JVM and agent not running
			if (!helper.isMXBeanRegistered() && !helper.isLocalJvm()) {
				DisplayToolkit.safeAsyncExec(Display.getDefault(), () -> {
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					DialogToolkit.showError(window.getShell(), MESSAGE_STARTING_AGENT_ON_REMOTE_JVM_NOT_SUPPORTED,
							MESSAGE_START_AGENT_MANUALLY);
				});
				return Status.OK_STATUS;
			}

			// agent already running
			DisplayToolkit.safeAsyncExec(Display.getDefault(), () -> {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				try {
					IEditorInput ei = new AgentEditorInput(serverHandle, helper.getConnectionHandle(), helper);
					window.getActivePage().openEditor(ei, AgentEditor.EDITOR_ID, true);
				} catch (PartInitException e) {
					DialogToolkit.showException(window.getShell(), MESSAGE_FAILED_TO_OPEN_AGENT_EDITOR, e);
				}
			});
			
			return Status.OK_STATUS;
		}

		@Override
		protected void canceling() {
			IOToolkit.closeSilently(connectionHandle);
		}

		@Override
		public void onConnectionChange(IConnectionHandle connection) {
			if (serverHandle.getState() == IServerHandle.State.DISPOSED) {
				cancel();
				return;
			}
		}
	}
}
