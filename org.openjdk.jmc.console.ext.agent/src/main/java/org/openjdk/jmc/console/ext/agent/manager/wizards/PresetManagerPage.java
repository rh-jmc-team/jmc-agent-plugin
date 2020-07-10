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
package org.openjdk.jmc.console.ext.agent.manager.wizards;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.manager.internal.Preset;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepository;
import org.openjdk.jmc.ui.misc.AbstractStructuredContentProvider;
import org.openjdk.jmc.ui.misc.DialogToolkit;

public class PresetManagerPage extends BaseWizardPage {
	private static final String PAGE_NAME = "Agent Preset Manager";

	private static final String MESSAGE_PRESET_MANAGER_PAGE_TITLE = "JMC Agent Configuration Preset Manager";
	private static final String MESSAGE_PRESET_MANAGER_PAGE_DESCRIPTION = "Presets for JMC agent are useful to repeatedly apply configurations to a running JMC agent.";
	private static final String MESSAGE_PRESET_MANAGER_UNABLE_TO_SAVE_THE_PRESET = "Unable to save the preset";
	private static final String MESSAGE_EVENTS = "event(s)";

	private static final String ID_PRESET = "preset";

	private final PresetRepository repository;

	private TableInspector tableInspector;

	public PresetManagerPage(PresetRepository repository) {
		super(PAGE_NAME);

		this.repository = repository;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_PRESET_MANAGER_PAGE_TITLE);
		setDescription(MESSAGE_PRESET_MANAGER_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		container.setLayout(new FillLayout());

		createPresetTableContainer(container);

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);

		populateUi();
	}

	private Composite createPresetTableContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		tableInspector = new TableInspector(container,
				TableInspector.ADD_BUTTON | TableInspector.EDIT_BUTTON | TableInspector.DUPLICATE_BUTTON
						| TableInspector.REMOVE_BUTTON | TableInspector.IMPORT_FILES_BUTTON
						| TableInspector.EXPORT_FILE_BUTTON) {
			@Override
			protected void addColumns() {
				addColumn(ID_PRESET, new ColumnLabelProvider() {
					@Override
					public String getText(Object element) {
						if (!(element instanceof IPreset)) {
							throw new IllegalArgumentException("element must be an IPreset"); // $NON-NLS-1$
						}

						IPreset preset = (IPreset) element;
						return preset.getFileName() + " - " + preset.getEvents().length + " " + MESSAGE_EVENTS;
					}

					@Override
					public Image getImage(Object element) {
						return AgentPlugin.getDefault().getImage(AgentPlugin.ICON_AGENT); // TODO: replace the icon in the future
					}
				});
			}

			@Override
			protected void onAddButtonSelected(IStructuredSelection selection) {
				openPresetEditingWizardFor(new Preset());

				tableInspector.getViewer().refresh();
			}

			@Override
			protected void onEditButtonSelected(IStructuredSelection selection) {
				openPresetEditingWizardFor((IPreset) selection.getFirstElement());

				tableInspector.getViewer().refresh();
			}

			@Override
			protected void onDuplicateButtonSelected(IStructuredSelection selection) {
				IPreset original = (IPreset) selection.getFirstElement();
				IPreset duplicate = new Preset();
				duplicate.setFileName("Copy of " + original.getFileName());
				repository.add(duplicate);

				tableInspector.getViewer().refresh();
			}

			@Override
			protected void onRemoveButtonSelected(IStructuredSelection selection) {
				repository.remove((IPreset) selection.getFirstElement());

				tableInspector.getViewer().refresh();
			}

			@Override
			protected void onImportFilesButtonSelected(IStructuredSelection selection) {
				// TODO: file selection dialog
				tableInspector.getViewer().refresh();
			}

			@Override
			protected void onExportFileButtonSelected(IStructuredSelection selection) {
				// TODO: file selection dialog
			}
		};
		tableInspector.setContentProvider(new PresetTableContentProvider());

		return container;
	}

	private void populateUi() {
		tableInspector.setInput(repository);
	}

	private void openPresetEditingWizardFor(IPreset preset) {
		while (DialogToolkit.openWizardWithHelp(new PresetEditingWizard(preset))) {
			try {
				// TODO: properly save to repository
				repository.add(preset);
			} catch (IllegalArgumentException e) {
				if (DialogToolkit.openConfirmOnUiThread(MESSAGE_PRESET_MANAGER_UNABLE_TO_SAVE_THE_PRESET,
						e.getMessage())) {
					continue;
				}
			}

			break;
		}
	}

	private static class PresetTableContentProvider extends AbstractStructuredContentProvider
			implements IContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			if (!(inputElement instanceof PresetRepository)) {
				throw new IllegalArgumentException("input element must be a PresetRepository"); // $NON-NLS-1$
			}

			PresetRepository repository = (PresetRepository) inputElement;
			return repository.list();
		}
	}
}
