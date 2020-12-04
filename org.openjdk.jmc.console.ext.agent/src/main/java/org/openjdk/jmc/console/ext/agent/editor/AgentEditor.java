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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.openjdk.jmc.common.io.IOToolkit;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.tabs.editor.EditorTab;
import org.openjdk.jmc.console.ext.agent.tabs.liveconfig.LiveConfigTab;
import org.openjdk.jmc.console.ext.agent.tabs.overview.OverviewTab;
import org.openjdk.jmc.console.ext.agent.tabs.presets.PresetsTab;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.rjmx.IConnectionListener;
import org.openjdk.jmc.rjmx.IServerHandle;
import org.openjdk.jmc.ui.UIPlugin;
import org.openjdk.jmc.ui.WorkbenchToolkit;
import org.openjdk.jmc.ui.misc.CompositeToolkit;
import org.openjdk.jmc.ui.misc.DisplayToolkit;

import javax.management.MBeanServerConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

//TODO: remove the @SuppressWarnings once IWorkbenchActionConstants.FIND_EXT is added to ApplicationActionBarAdvisor in JMC
public class AgentEditor extends FormEditor implements IConnectionListener {

	public static final String EDITOR_ID = "org.openjdk.jmc.console.ext.agent.editor.AgentEditor"; //$NON-NLS-1$

	private static final String CONNECTION_LOST = "Connection Lost";
	private static final String COULD_NOT_CONNECT = "Could not connect";

	private volatile IConnectionHandle connection;

	private StackLayout stackLayout;
	private Composite mainUi;

	public void onConnectionChange(IConnectionHandle connection) {
		boolean serverDisposed = getEditorInput().getServerHandle().getState() == IServerHandle.State.DISPOSED;
		if (serverDisposed) {
			WorkbenchToolkit.asyncCloseEditor(AgentEditor.this);
		} else if (!connection.isConnected()) {
			DisplayToolkit.safeAsyncExec(() -> {
				if (pages != null) {
					for (Object page : pages) {
						if (page instanceof IFormPage) {
							IMessageManager mm = ((IFormPage) page).getManagedForm().getMessageManager();
							mm.addMessage(this, CONNECTION_LOST, null, IMessageProvider.ERROR);
						}
					}
				}
			});
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setPartName("JMC Agent Plugin: " + getEditorInput().getName());

		connection = getEditorInput().getConnectionHandle();
		setUpInjectables();

		getEditorInput().getAgentJmxHelper().addConnectionChangedListener(this);

		// TODO: move the addition of this ContributionItem to ApplicationActionBarAdvisor in JMC when integrating to main repository
		((WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow()).getMenuBarManager().getItems();
		for (IContributionItem m : ((WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow())
				.getMenuBarManager().getItems()) {
			if (m.getId() == IWorkbenchActionConstants.M_EDIT) {
				MenuManager mm = ((MenuManager) m);
				if (mm.indexOf(IWorkbenchActionConstants.FIND_EXT) == -1) {
					mm.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
					mm.update(true);
					break;
				}
			}
		}
	}

	@Override
	protected Composite createPageContainer(Composite parent) {
		parent = super.createPageContainer(parent);
		FormToolkit toolkit = getToolkit();
		mainUi = toolkit.createComposite(parent);
		Composite progress = toolkit.createComposite(parent);
		CompositeToolkit.createWaitIndicator(progress, toolkit);
		stackLayout = new StackLayout();
		parent.setLayout(stackLayout);
		stackLayout.topControl = progress;
		mainUi.setLayout(new FillLayout());
		return mainUi;
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
		doAddPages();

		stackLayout.topControl.dispose();
		stackLayout.topControl = mainUi;
		mainUi.getParent().layout(true, true);
	}

	private void doAddPages() {
		List<AgentFormPage> pages = new ArrayList<>();
		pages.add(new OverviewTab(this));
		pages.add(new LiveConfigTab(this));
		pages.add(new PresetsTab(this));
		pages.add(new EditorTab(this));

		for (AgentFormPage page : pages) {
			try {
				addPage(page);
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

	@Override
	public int addPage(IFormPage page) throws PartInitException {
		int index = super.addPage(page);
		/*
		 * NOTE: Calling setActivePage(index) causes AgentFormPage.createPartControl to be called
		 * which is needed since it creates the IManagedForm which is fetched in doAddPages() (using
		 * page.getManagedForm() page.getBody()). The call to setActivePage can be removed if
		 * fetching the IManagedForm can be delayed until after the page is activated.
		 */
		setActivePage(index);
		setPageImage(index, page.getTitleImage());

		return index;
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
		getEditorInput().getAgentJmxHelper().removeConnectionChangedListener(this);
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
		AgentJmxHelper helper = getEditorInput().getAgentJmxHelper();
		context.set(AgentJmxHelper.class, helper);
		context.set(IConnectionHandle.class, helper.getConnectionHandle());
		context.set(MBeanServerConnection.class, helper.getMBeanServerConnection());
	}

}
