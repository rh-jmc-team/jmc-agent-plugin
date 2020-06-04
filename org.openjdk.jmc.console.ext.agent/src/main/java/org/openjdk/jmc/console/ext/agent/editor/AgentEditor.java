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
package org.openjdk.jmc.console.ext.agent.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import javax.management.MBeanServerConnection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.common.io.IOToolkit;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.tabs.editor.EditorTab;
import org.openjdk.jmc.console.ext.agent.tabs.liveconfig.LiveConfigTab;
import org.openjdk.jmc.console.ext.agent.tabs.overview.OverviewTab;
import org.openjdk.jmc.console.ext.agent.tabs.presets.PresetsTab;
import org.openjdk.jmc.rjmx.ConnectionException;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.rjmx.IServerHandle;
import org.openjdk.jmc.rjmx.JVMSupportToolkit;
import org.openjdk.jmc.ui.UIPlugin;
import org.openjdk.jmc.ui.WorkbenchToolkit;
import org.openjdk.jmc.ui.misc.CompositeToolkit;
import org.openjdk.jmc.ui.misc.DialogToolkit;
import org.openjdk.jmc.ui.misc.DisplayToolkit;

public class AgentEditor extends FormEditor {

	public static final String EDITOR_ID = "org.openjdk.jmc.console.ext.agent.editor.AgentEditor"; //$NON-NLS-1$

	private static final String CONNECTION_LOST = "Connection Lost";
	private static final String COULD_NOT_CONNECT = "Could not connect";

	private final class ConnectJob extends Job {
		private final StackLayout stackLayout;
		private final Composite mainUi;

		private ConnectJob(StackLayout stackLayout, Composite mainUi) {
			super("Opening Agent Plugin");
			this.stackLayout = stackLayout;
			this.mainUi = mainUi;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				connection = getEditorInput().getServerHandle().connect("Agent MBean",
						AgentEditor.this::onConnectionChange);
				String[] error = JVMSupportToolkit.checkConsoleSupport(connection);
				if (error.length == 2 && !DialogToolkit.openConfirmOnUiThread(error[0], error[1])) {
					WorkbenchToolkit.asyncCloseEditor(AgentEditor.this);
					return Status.CANCEL_STATUS;
				}
				DisplayToolkit.safeAsyncExec(() -> {
					if (!mainUi.isDisposed()) {
						setUpInjectables();
						doAddPages();
						stackLayout.topControl.dispose();
						stackLayout.topControl = mainUi;
						mainUi.getParent().layout(true, true);
					}
				});
				return Status.OK_STATUS;
			} catch (ConnectionException e) {
				WorkbenchToolkit.asyncCloseEditor(AgentEditor.this);
				// FIXME: Show stacktrace? (Need to show our own ExceptionDialog in that case, or maybe create our own DetailsAreaProvider, see WorkbenchStatusDialogManager.setDetailsAreaProvider)
				return new Status(IStatus.ERROR, AgentPlugin.PLUGIN_ID, IStatus.ERROR,
						NLS.bind(COULD_NOT_CONNECT, getEditorInput().getName(), e.getMessage()), e);
			}
		}
	}

	private void onConnectionChange(IConnectionHandle connection) {
		boolean serverDisposed = getEditorInput().getServerHandle().getState() == IServerHandle.State.DISPOSED;
		if (serverDisposed) {
			WorkbenchToolkit.asyncCloseEditor(AgentEditor.this);
		} else if (!connection.isConnected()) {
			DisplayToolkit.safeAsyncExec(new Runnable() {
				@Override
				public void run() {
					if (pages != null) {
						for (Object page : pages) {
							if (page instanceof IFormPage) {
								IMessageManager mm = ((IFormPage) page).getManagedForm().getMessageManager();
								mm.addMessage(this, CONNECTION_LOST, null, IMessageProvider.ERROR);
							}
						}
					}
				}
			});
		}
	}

	private volatile IConnectionHandle connection;

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setPartName("JMC Agent Plugin: " + getEditorInput().getName());
	}

	@Override
	protected Composite createPageContainer(Composite parent) {
		parent = super.createPageContainer(parent);
		FormToolkit toolkit = getToolkit();
		Composite container = toolkit.createComposite(parent);
		Composite progress = toolkit.createComposite(parent);
		CompositeToolkit.createWaitIndicator(progress, toolkit);
		StackLayout stackLayout = new StackLayout();
		parent.setLayout(stackLayout);
		stackLayout.topControl = progress;
		new ConnectJob(stackLayout, container).schedule();
		container.setLayout(new FillLayout());
		return container;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	protected void addPages() {
		// TODO Auto-generated method stub
	}

	private void doAddPages() {
		List<AgentFormPage> pages = new ArrayList<>();
		pages.add(new OverviewTab(this));
		pages.add(new LiveConfigTab(this));
		pages.add(new PresetsTab(this));
		pages.add(new EditorTab(this));

		for (AgentFormPage page : pages) {
			try {
				add(page);
				final IEclipseContext eclipseContext = this.getSite().getService(IEclipseContext.class);
				IEclipseContext childContext = eclipseContext.createChild();
				childContext.set(IManagedForm.class, page.getManagedForm());
				childContext.set(Composite.class, page.getBody());
				ContextInjectionFactory.inject(page, childContext);
			} catch (Exception e) {
				AgentPlugin.getDefault().getLogger().log(Level.SEVERE, "Error when creating page", e); //$NON-NLS-1$
			}
		}
		setActivePage(0);
	}

	private void add(IFormPage page) throws PartInitException {
		int index = addPage(page);
		/*
		 * NOTE: Calling setActivePage(index) causes AgentFormPage.createPartControl to be called
		 * which is needed since it creates the IManagedForm which is fetched in doAddPages() (using
		 * page.getManagedForm() page.getBody()). The call to setActivePage can be removed if
		 * fetching the IManagedForm can be delayed until after the page is activated.
		 */
		setActivePage(index);
		setPageImage(index, page.getTitleImage());
	}

	@Override
	protected void setActivePage(int pageIndex) {
		// Range check since MultiPageEditorPart.createPartControl calls
		// setActivePage(0) even though there are no pages
		if (pageIndex >= 0 && pageIndex < getPageCount()) {
			super.setActivePage(pageIndex);
		}
	}

	@Override
	protected IEditorPart getEditor(int pageIndex) {
		// Range check since MultiPageEditorPart.createPartControl calls
		// getEditor(0) even though there are no pages
		if (pageIndex >= 0 && pageIndex < getPageCount()) {
			return super.getEditor(pageIndex);
		}
		return null;
	}

	/**
	 * Creates a {@link FormToolkit}
	 */
	@Override
	protected FormToolkit createToolkit(Display display) {
		return new FormToolkit(UIPlugin.getDefault().getFormColors(display));
	}

	@Override
	public AgentEditorInput getEditorInput() {
		return (AgentEditorInput) super.getEditorInput();
	}

	@Override
	public void dispose() {
		super.dispose();
		IOToolkit.closeSilently(connection);
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (adapter == IConnectionHandle.class && connection != null) {
			return adapter.cast(connection);
		}
		return super.getAdapter(adapter);
	}

	private void setUpInjectables() {
		IEclipseContext context = this.getSite().getService(IEclipseContext.class);

		// TODO: Consider carefully which services we want to support.
		AgentJmxHelper helper = new AgentJmxHelper(connection);
		context.set(AgentJmxHelper.class, helper);
		context.set(IConnectionHandle.class, helper.getConnectionHandle());
		context.set(MBeanServerConnection.class, helper.getMBeanServerConnection());
	}

}
