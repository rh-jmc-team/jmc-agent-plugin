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

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.console.ext.agent.manager.internal.Preset;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.console.ext.agent.manager.model.PresetRepository;
import org.openjdk.jmc.ui.misc.AbstractStructuredContentProvider;
import org.openjdk.jmc.ui.misc.DialogToolkit;

public class PresetManagerPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Preset Manager";
	private static final String MESSAGE_PRESET_MANAGER_PAGE_TITLE = "JMC Agent Configuration Preset Manager";
	private static final String MESSAGE_PRESET_MANAGER_PAGE_DESCRIPTION = "Presets for JMC agent are useful to repeatedly apply configurations to a running JMC agent.";
	private static final String MESSAGE_PRESET_MANAGER_UNABLE_TO_SAVE_THE_PRESET = "Unable to save the preset";
	private static final String MESSAGE_NEW_BUTTON = "New";
	private static final String MESSAGE_EDIT_BUTTON = "Edit";
	private static final String MESSAGE_DUPLICATE_BUTTON = "Duplicate";
	private static final String MESSAGE_REMOVE_BUTTON = "Remove";
	private static final String MESSAGE_IMPORT_FILES_BUTTON = "Import Files...";
	private static final String MESSAGE_EXPORT_FILE_BUTTON = "Export File...";
	private static final String MESSAGE_EVENTS = "event(s)";

	private final PresetRepository repository;
	private TableViewer tableViewer;
	private Button newButton;
	private Button editButton;
	private Button duplicateButton;
	private Button removeButton;
	private Button importButton;
	private Button exportButton;

	public PresetManagerPage(PresetRepository repository) {
		super(PAGE_NAME);

		this.repository = repository;
	}

	@Override
	public void createControl(Composite parent) {
		setTitle(MESSAGE_PRESET_MANAGER_PAGE_TITLE);
		setDescription(MESSAGE_PRESET_MANAGER_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);
		container.setLayout(new GridLayout(2, false));

		createPresetTable(container);
		createButtons(container);

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);

		bindListeners();
	}

	private void createPresetTable(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);
		tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.setContentProvider(new PresetTableContentProvider());
		tableViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new PresetTableLabelProvider()));
		tableViewer.setInput(repository);
	}

	private void createButtons(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, true));
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);

		newButton = createButton(container, MESSAGE_NEW_BUTTON);
		editButton = createButton(container, MESSAGE_EDIT_BUTTON);
		duplicateButton = createButton(container, MESSAGE_DUPLICATE_BUTTON);
		removeButton = createButton(container, MESSAGE_REMOVE_BUTTON);
		importButton = createButton(container, MESSAGE_IMPORT_FILES_BUTTON);
		exportButton = createButton(container, MESSAGE_EXPORT_FILE_BUTTON);
	}

	private Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.NONE);
		button.setText(text);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		return button;
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

	private void bindListeners() {
		// TODO: should the repository be Observable and refresh the view when it changes?
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openPresetEditingWizardFor(new Preset());
				tableViewer.refresh();
			}
		});

		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openPresetEditingWizardFor((IPreset) tableViewer.getStructuredSelection().getFirstElement());
				tableViewer.refresh();
			}
		});
		editButton.setEnabled(false);

		duplicateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO: create a copy properly
				IPreset original = (IPreset) tableViewer.getStructuredSelection().getFirstElement();
				IPreset duplicate = new Preset();
				duplicate.setFileName("Copy of " + original.getFileName());
				repository.add(duplicate);
				tableViewer.refresh();
			}
		});
		duplicateButton.setEnabled(false);

		importButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO: file selection dialog
				super.widgetSelected(e);
				tableViewer.refresh();
			}
		});

		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				repository.remove((IPreset) tableViewer.getStructuredSelection().getFirstElement());
				tableViewer.refresh();
			}
		});
		removeButton.setEnabled(false);

		exportButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO: file selection dialog
				super.widgetSelected(e);
				tableViewer.refresh();
			}
		});
		exportButton.setEnabled(false);

		tableViewer.addSelectionChangedListener(selectionChangedEvent -> {
			IStructuredSelection selection = tableViewer.getStructuredSelection();
			switch (selection.size()) {
			case 0:
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
				duplicateButton.setEnabled(false);
				exportButton.setEnabled(false);
				break;
			case 1:
				editButton.setEnabled(true);
				removeButton.setEnabled(true);
				duplicateButton.setEnabled(true);
				exportButton.setEnabled(true);
				break;
			default: // more than one selected
				editButton.setEnabled(false);
				duplicateButton.setEnabled(false);
				removeButton.setEnabled(true);
				exportButton.setEnabled(false);
			}
		});
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

	private static class PresetTableLabelProvider extends LabelProvider
			implements DelegatingStyledCellLabelProvider.IStyledLabelProvider {
		@Override
		public String getText(Object element) {
			return getStyledText(element).getString();
		}

		@Override
		public StyledString getStyledText(Object element) {
			if (!(element instanceof IPreset)) {
				throw new IllegalArgumentException("element must be an IPreset"); // $NON-NLS-1$
			}

			IPreset preset = (IPreset) element;
			StyledString text = new StyledString(preset.getFileName());
			text.append(" - " + preset.getEvents().length + " " + MESSAGE_EVENTS, StyledString.DECORATIONS_STYLER);
			return text;
		}

		@Override
		public Image getImage(Object element) {
			return AgentPlugin.getDefault().getImage(AgentPlugin.ICON_AGENT); // TODO: replace the icon in the future
		}
	}
}
