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
package org.openjdk.jmc.console.ext.agent.tabs.liveconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.rjmx.ui.internal.TreeNodeBuilder;
import org.openjdk.jmc.ui.UIPlugin;
import org.openjdk.jmc.ui.common.tree.DefaultTreeNode;
import org.openjdk.jmc.ui.common.tree.ITreeNode;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;
import org.openjdk.jmc.ui.misc.MCSectionPart;
import org.openjdk.jmc.ui.misc.TreeStructureContentProvider;

public class EventTreeSection extends MCSectionPart {
	private static final String EVENTS_TREE_NAME = "AgentUi.EventsTree";

	private final TreeViewer viewer;
	private AgentJmxHelper agentJmxHelper;

	public EventTreeSection(Composite parent, FormToolkit toolkit, AgentJmxHelper helper) {
		super(parent, toolkit, DEFAULT_TITLE_STYLE);
		this.agentJmxHelper = helper;
		getSection().setText("Event List");

		Composite body = createSectionBody(MCLayoutFactory.createMarginFreeFormPageLayout());
		Composite eventsControlContainer = new Composite(body, SWT.NO_BACKGROUND);
		eventsControlContainer.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		eventsControlContainer.setLayoutData(gridData);

		viewer = createViewer(body, toolkit);
		viewer.getControl()
				.setLayoutData(MCLayoutFactory.createFormPageLayoutData(SWT.DEFAULT, SWT.DEFAULT, true, true));
		CompositeData[] cds = null;
		if (agentJmxHelper.isMXBeanRegistered()) {
			cds = agentJmxHelper.retrieveCurrentTransforms();
		}
		final ITreeNode[] nodes = buildTreeModel(cds);
		viewer.setInput(nodes);

		getMCToolBarManager().add(new EventRefreshAction());

	}

	private final class EventRefreshAction extends Action {

		public EventRefreshAction() {
			super("", IAction.AS_PUSH_BUTTON); //$NON-NLS-1$
			setImageDescriptor(UIPlugin.getDefault().getMCImageDescriptor(UIPlugin.ICON_REFRESH));
			setId("refresh"); //$NON-NLS-1$
		}

		@Override
		public void run() {
			refreshTree();
		}

	}

	public void refreshTree() {
		CompositeData[] cds = null;
		if (agentJmxHelper.isMXBeanRegistered()) {
			cds = agentJmxHelper.retrieveCurrentTransforms();
		}
		final ITreeNode[] nodes = buildTreeModel(cds);
		viewer.getControl().setRedraw(false);
		viewer.setInput(nodes);
		selectTopEvent();
		viewer.getControl().setRedraw(true);
		viewer.getControl().redraw();
	}

	public void addEventSelectionListener(final FeatureTableSection infoPart) {
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (selected != null) {
					String eventName = ((DefaultTreeNode) selected).getUserData().toString();
					infoPart.showEvent(eventName);
				} else {
					infoPart.showEvent(null);
				}
			}
		});
	}

	public void selectTopEvent() {
		List<ITreeNode> search = new ArrayList<>();
		search.addAll(Arrays.asList((ITreeNode[]) viewer.getInput()));
		if (!search.isEmpty()) {
			ITreeNode node = search.remove(0);
			viewer.setSelection(new StructuredSelection(node), true);
			return;
		}
	}

	private TreeViewer createViewer(Composite parent, FormToolkit formToolkit) {
		Tree tree = formToolkit.createTree(parent, SWT.NONE);
		tree.setData("name", EVENTS_TREE_NAME); //$NON-NLS-1$
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TreeViewer viewer = new TreeViewer(tree);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new TreeStructureContentProvider());
		viewer.setLabelProvider(new EventTreeLabelProvider());

		return viewer;
	}

	private ITreeNode[] buildTreeModel(CompositeData[] cds) {
		TreeNodeBuilder root = new TreeNodeBuilder();
		if (cds != null && cds.length != 0) {
			for (CompositeData cd : cds) {
				root.getUniqueChild(cd.get("eventName").toString());
			}
		}
		return root.getChildren(null);
	}

}
