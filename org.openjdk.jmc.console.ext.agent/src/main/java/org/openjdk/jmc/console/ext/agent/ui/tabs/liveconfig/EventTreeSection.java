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
package org.openjdk.jmc.console.ext.agent.ui.tabs.liveconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.management.openmbean.CompositeData;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.console.ext.agent.ui.AgentJMXHelper;
import org.openjdk.jmc.rjmx.ui.internal.TreeNodeBuilder;
import org.openjdk.jmc.ui.common.tree.ITreeNode;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;
import org.openjdk.jmc.ui.misc.TreeStructureContentProvider;


public class EventTreeSection extends Composite {
	private static final String EVENTS_TREE_NAME = "AgentUi.EventsTree";
	private static final String NO_TRANSFORMED_EVENTS_MSG = "No events are currently transformed";
	private static final String SECTION_LABEL = "Current Transfromed Events";
	private static final String GET_EVENTS = "Get Events";
	private static final List<String> COMPOSITE_DATA_TYPES = new ArrayList<>(Arrays.asList("returnValue", "method"));
	private static final List<String> COMPOSITE_DATA_ARRAY_TYPES = new ArrayList<>(Arrays.asList("fields", "parameters"));
	private final TreeViewer viewer;
	private AgentJMXHelper agentJMXHelper;

	public EventTreeSection(Composite parent, FormToolkit toolkit) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout());
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite eventsControlContainer = new Composite(this, SWT.NO_BACKGROUND);
		eventsControlContainer.setLayout(new GridLayout(2, false));
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		eventsControlContainer.setLayoutData(gridData);

		Label label = new Label(eventsControlContainer, SWT.NULL);
		label.setText(SECTION_LABEL);
		Button eventsButton = new Button(eventsControlContainer, SWT.PUSH);
		eventsButton.setText(GET_EVENTS);
		eventsButton.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				CompositeData[] cds = agentJMXHelper.retrieveCurrentTransforms();
				final ITreeNode[] nodes = buildTreeModel(cds);
				viewer.getControl().setRedraw(false);
				viewer.setInput(nodes);
				viewer.getControl().setRedraw(true);
				viewer.getControl().redraw();
			}
		});

		viewer = createViewer(this, toolkit);
		viewer.getControl()
				.setLayoutData(MCLayoutFactory.createFormPageLayoutData(SWT.DEFAULT, SWT.DEFAULT, true, true));
	}

	public void setAgentJMXHelper(AgentJMXHelper agentJMXHelper) {
		this.agentJMXHelper = agentJMXHelper;
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
		if (cds == null || cds.length == 0) {
			root.getUniqueChild(NO_TRANSFORMED_EVENTS_MSG);
		} else {
			for (CompositeData cd : cds) {
				TreeNodeBuilder node = root.getUniqueChild(cd.get("eventName").toString());
				buildChildNodes(cd, node);
			}
		}
		return root.getChildren(null);
	}

	private void buildChildNodes(CompositeData cd, TreeNodeBuilder rootNode) {
		Set<String> keys = cd.getCompositeType().keySet();
		for (String key : keys) {
			if (!isEmptyCompositeData(cd, key)) {
				TreeNodeBuilder parent = rootNode.get(key);
				parent.setValue(key);
				if (COMPOSITE_DATA_TYPES.contains(key)) {
					buildChildNodes((CompositeData) cd.get(key), parent);
				} else if (COMPOSITE_DATA_ARRAY_TYPES.contains(key)) {
					CompositeData[] childCds = (CompositeData[]) cd.get(key);
					for (int i = 0; i < childCds.length; i++) {
						String childKey = key + " " + i;
						TreeNodeBuilder childNode = parent.get(childKey);
						childNode.setValue(childKey);
						buildChildNodes(childCds[i], childNode);
					}
				} else {
					String value = cd.get(key).toString();
					TreeNodeBuilder child = parent.get(value);
					child.setValue(value);
				}
			}
		}
	}

	private boolean isEmptyCompositeData(CompositeData cd, String key) {
		if (cd.get(key) == null) {
			return true;
		}
		try {
			CompositeData[] cds = (CompositeData[]) cd.get(key);
			return cds.length == 0;
		} catch (ClassCastException e) {
			return false;
		}
	}
}
