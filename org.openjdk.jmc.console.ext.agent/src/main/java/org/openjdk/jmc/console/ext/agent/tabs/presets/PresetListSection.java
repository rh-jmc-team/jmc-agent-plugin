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
package org.openjdk.jmc.console.ext.agent.tabs.presets;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.tabs.editor.internal.XmlEditor;
import org.openjdk.jmc.console.ext.agent.tabs.liveconfig.EventTreeLabelProvider;
import org.openjdk.jmc.rjmx.ui.internal.TreeNodeBuilder;
import org.openjdk.jmc.ui.MCPathEditorInput;
import org.openjdk.jmc.ui.UIPlugin;
import org.openjdk.jmc.ui.common.tree.DefaultTreeNode;
import org.openjdk.jmc.ui.common.tree.ITreeNode;
import org.openjdk.jmc.ui.handlers.ActionToolkit;
import org.openjdk.jmc.ui.handlers.MCContextMenuManager;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;
import org.openjdk.jmc.ui.misc.MCSectionPart;
import org.openjdk.jmc.ui.misc.TreeStructureContentProvider;

public class PresetListSection extends MCSectionPart {
	private static final String PRESETS_LIST_NAME = "AgentUi.PresetsList";

	private List<String> presets = new ArrayList<>();
	private final TreeViewer viewer;

	public PresetListSection(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, DEFAULT_TITLE_STYLE);
		getSection().setText("Saved Presets");

		Composite body = createSectionBody(MCLayoutFactory.createMarginFreeFormPageLayout());

		viewer = createViewer(body, toolkit);
		viewer.getControl()
				.setLayoutData(MCLayoutFactory.createFormPageLayoutData(SWT.DEFAULT, SWT.DEFAULT, true, true));

		final ITreeNode[] nodes = buildTreeModel();
		viewer.setInput(nodes);

		IAction addPresetAction = ActionToolkit.action(
				() -> this.addPreset(), "Add Preset",
				UIPlugin.getDefault().getMCImageDescriptor(UIPlugin.ICON_ADD));
		getMCToolBarManager().add(addPresetAction);

	}

	private void addPreset() {
		FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell());
		fd.setFilterExtensions(new String[] {"*.xml"});
		String filename = fd.open();
		if (filename != null) {
			String path = fd.getFilterPath() + File.separator + fd.getFileName();
			if (!presets.contains(path)) {
				presets.add(path);
				refreshViewer();
			}
		}
	}

	private void refreshViewer() {
		final ITreeNode[] nodes = buildTreeModel();
		viewer.getControl().setRedraw(false);
		viewer.setInput(nodes);
		viewer.getControl().setRedraw(true);
		viewer.getControl().redraw();
	}

	private TreeViewer createViewer(Composite parent, FormToolkit formToolkit) {
		Tree tree = formToolkit.createTree(parent, SWT.BORDER);
		tree.setData("name", PRESETS_LIST_NAME); //$NON-NLS-1$
		tree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		TreeViewer viewer = new TreeViewer(tree);
		viewer.setUseHashlookup(true);
		viewer.setContentProvider(new TreeStructureContentProvider());
		viewer.setLabelProvider(new EventTreeLabelProvider());

		MCContextMenuManager mm = MCContextMenuManager.create(tree);

		IAction editPresetAction = ActionToolkit.action(
				() -> this.editPreset(viewer.getStructuredSelection()), "Edit File",
				UIPlugin.getDefault().getMCImageDescriptor(UIPlugin.ICON_CHANGE));
		mm.add(editPresetAction);

		IAction removePresetAction = ActionToolkit.action(
				() -> this.removePreset(viewer.getStructuredSelection()), "Remove",
				UIPlugin.getDefault().getMCImageDescriptor(UIPlugin.ICON_DELETE));
		mm.add(removePresetAction);

		return viewer;
	}

	private void editPreset(IStructuredSelection selectedPreset) {
		Object selected = selectedPreset.getFirstElement();
		String preset = ((DefaultTreeNode) selected).getUserData().toString();

		IEditorInput input = new MCPathEditorInput(new File(preset), false);
		input = XmlEditor.convertInput(input);
		try {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input,
					XmlEditor.EDITOR_ID);
		} catch (PartInitException e) {
			AgentPlugin.getDefault().getLogger().log(Level.WARNING, "Could not open XML editor", e);
		}
	}

	private void removePreset(IStructuredSelection selectedPreset) {
		Object selected = selectedPreset.getFirstElement();
		String preset = ((DefaultTreeNode) selected).getUserData().toString();
		presets.remove(preset);
		refreshViewer();
	}

	private ITreeNode[] buildTreeModel() {
		TreeNodeBuilder root = new TreeNodeBuilder();
		boolean colour = true;
		for (String preset : presets) {
			root.getUniqueChild(preset);
		}
		return root.getChildren(null);
	}

}
