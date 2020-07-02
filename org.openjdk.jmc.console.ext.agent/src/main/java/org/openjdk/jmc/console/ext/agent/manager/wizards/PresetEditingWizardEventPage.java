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

import com.sun.tools.javac.util.List;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openjdk.jmc.console.ext.agent.manager.internal.Event;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.ui.misc.AbstractStructuredContentProvider;
import org.openjdk.jmc.ui.misc.DialogToolkit;

public class PresetEditingWizardEventPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Preset Editing";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_TITLE = "Editing Preset Events";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_DESCRIPTION = "Add new events to the preset, or remove/edit existing events.";

	private static final String LABEL_EVENTS = "Events: ";
	private static final String LABEL_ADD_BUTTON = "Add...";
	private static final String LABEL_EDIT_BUTTON = "Edit";
	private static final String LABEL_DUPLICATE_BUTTON = "Duplicate";
	private static final String LABEL_REMOVE_BUTTON = "Remove";

	private final IPreset preset;

	private TableViewer tableViewer;
	private Button addButton;
	private Button editButton;
	private Button duplicateButton;
	private Button removeButton;

	protected PresetEditingWizardEventPage(IPreset preset) {
		super(PAGE_NAME);

		this.preset = preset;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_TITLE);
		setDescription(MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		{
			Composite eventContainer = createEventContainer(container);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			eventContainer.setLayoutData(gd);
		}

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);

		bindListeners();
	}

	private Composite createEventContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 2;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		Label eventLabel = new Label(container, SWT.NONE);
		eventLabel.setText(LABEL_EVENTS);
		eventLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, cols, 1));

		createEventTable(container);
		createEventButtons(container);

		return container;
	}

	private void createEventTable(Composite parent) {
		tableViewer = new TableViewer(parent, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.setContentProvider(new EventTableContentProvider());
		tableViewer.getTable().setHeaderVisible(true);

		TableViewerColumn idColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		idColumn.getColumn().setText("ID");
		idColumn.getColumn().setWidth(200);
		idColumn.getColumn().setMoveable(true);
		idColumn.setLabelProvider(new EventTableLabelProvider() {
			@Override
			protected String doGetText(IEvent event) {
				return event.getId();
			}
		});

		TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		nameColumn.getColumn().setText("Name");
		nameColumn.getColumn().setWidth(200);
		nameColumn.getColumn().setMoveable(true);
		nameColumn.setLabelProvider(new EventTableLabelProvider() {
			@Override
			protected String doGetText(IEvent event) {
				return event.getName();
			}
		});
		
		tableViewer.getTable().setSortColumn(idColumn.getColumn());
		tableViewer.getTable().setSortDirection(SWT.DOWN);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(tableViewer); // TODO:???

		tableViewer.setInput(preset);
	}

	private Composite createEventButtons(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, true));
		GridLayout layout = new GridLayout(1, true);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		container.setLayout(layout);

		addButton = createButton(container, LABEL_ADD_BUTTON);
		editButton = createButton(container, LABEL_EDIT_BUTTON);
		duplicateButton = createButton(container, LABEL_DUPLICATE_BUTTON);
		removeButton = createButton(container, LABEL_REMOVE_BUTTON);
		return container;
	}

	private Button createButton(Composite parent, String text) {
		Button button = new Button(parent, SWT.NONE);
		button.setText(text);
		button.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		return button;
	}

	private void bindListeners() {
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openEventEditingWizardFor(new Event());
				tableViewer.refresh();
			}
		});

		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openEventEditingWizardFor((IEvent) tableViewer.getStructuredSelection().getFirstElement());
				tableViewer.refresh();
			}
		});
		editButton.setEnabled(false);

		duplicateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// TODO: create a copy properly
				IEvent original = (IEvent) tableViewer.getStructuredSelection().getFirstElement();
				IEvent duplicate = new Event();
				// duplicate.setId(original.getId() + ".copy");
				duplicate.setName("Copy of " + original.getName());
				preset.addEvent(duplicate);
				tableViewer.refresh();
			}
		});
		duplicateButton.setEnabled(false);

		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				preset.removeEvent((IEvent) tableViewer.getStructuredSelection().getFirstElement());
				tableViewer.refresh();
			}
		});
		removeButton.setEnabled(false);

		tableViewer.addSelectionChangedListener(
				selectionChangedEvent -> List.of(editButton, duplicateButton, removeButton)
						.forEach(button -> button.setEnabled(!tableViewer.getStructuredSelection().isEmpty())));
	}

	private void openEventEditingWizardFor(IEvent event) {
		if (!(DialogToolkit.openWizardWithHelp(new EventEditingWizard(event)))) {
			return;
		}

		// TODO: save the modified event to the preset
		preset.addEvent(event);
	}

	private static class EventTableContentProvider extends AbstractStructuredContentProvider
			implements IContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (!(inputElement instanceof IPreset)) {
				throw new IllegalArgumentException("input element must be a IPreset");
			}

			IPreset preset = (IPreset) inputElement;
			return preset.getEvents();
		}
	}

	private static abstract class EventTableLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (!(element instanceof IEvent)) {
				throw new IllegalArgumentException("element must be an IEvent");
			}

			return doGetText((IEvent) element);
		}

		protected abstract String doGetText(IEvent event);
	}
}
