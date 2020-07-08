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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.openjdk.jmc.console.ext.agent.manager.internal.Event;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.ui.column.ColumnBuilder;
import org.openjdk.jmc.ui.column.ColumnManager;
import org.openjdk.jmc.ui.column.IColumn;
import org.openjdk.jmc.ui.misc.AbstractStructuredContentProvider;
import org.openjdk.jmc.ui.misc.DialogToolkit;
import org.openjdk.jmc.ui.misc.OptimisticComparator;

import java.util.ArrayList;
import java.util.List;

public class PresetEditingWizardEventPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Preset Editing";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_TITLE = "Editing Preset Events";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_DESCRIPTION = "Add new events to the preset, or remove/edit existing events.";
	private static final String MESSAGE_UNABLE_TO_SAVE_THE_PRESET = "Unable to add the event";

	private final IPreset preset;

	private EventTableInspector tableInspector;
	private EventTableButtonControls buttonControls;

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
		container.setLayout(new GridLayout(2, false));

		createEventTable(container);
		createEventButtons(container);

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);

		populateUi();
		bindListeners();
	}

	private void createEventTable(Composite parent) {
		tableInspector = new EventTableInspector(parent);
		tableInspector.setInput(preset);
	}

	private void createEventButtons(Composite parent) {
		buttonControls = new EventTableButtonControls(parent, tableInspector.getViewer());
	}

	private void bindListeners() {
		buttonControls.bindListeners();
	}

	private void populateUi() {
		tableInspector.setInput(preset);
	}

	private void openEventEditingWizardFor(IEvent event) {
		while (DialogToolkit.openWizardWithHelp(new EventEditingWizard(event))) {
			try {
				// TODO: save the modified event to the preset
				preset.addEvent(event);
			} catch (IllegalArgumentException e) {
				if (DialogToolkit.openConfirmOnUiThread(MESSAGE_UNABLE_TO_SAVE_THE_PRESET, e.getMessage())) {
					continue;
				}
			}

			break;
		}
	}

	private static class EventTableInspector {
		private static final String LABEL_ID_COLUMN = "ID";
		private static final String LABEL_NAME_COLUMN = "Name";

		private static final String ID_ID_COLUMN = "id";
		private static final String ID_NAME_COLUMN = "name";

		private final TableViewer viewer;

		private final ColumnLabelProvider idLabelProvider = new EventTableLabelProvider() {
			@Override
			protected String doGetText(IEvent event) {
				return event.getId();
			}
		};

		private final ColumnLabelProvider nameLabelProvider = new EventTableLabelProvider() {
			@Override
			protected String doGetText(IEvent event) {
				return event.getName();
			}
		};

		private EventTableInspector(Composite parent) {
			viewer = new TableViewer(parent, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
			viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			viewer.setContentProvider(new EventTableContentProvider());
			viewer.getTable().setHeaderVisible(true);

			List<IColumn> columns = new ArrayList<>();
			columns.add(new ColumnBuilder(LABEL_ID_COLUMN, ID_ID_COLUMN, idLabelProvider)
					.comparator(new OptimisticComparator(idLabelProvider)).build());
			columns.add(new ColumnBuilder(LABEL_NAME_COLUMN, ID_NAME_COLUMN, nameLabelProvider)
					.comparator(new OptimisticComparator(nameLabelProvider)).build());
			ColumnManager.build(viewer, columns, null);
		}

		public void setInput(Object input) {
			viewer.setInput(input);
		}

		public TableViewer getViewer() {
			return viewer;
		}

		private static class EventTableContentProvider extends AbstractStructuredContentProvider
				implements IContentProvider {

			@Override
			public Object[] getElements(Object inputElement) {
				if (!(inputElement instanceof IPreset)) {
					throw new IllegalArgumentException("input element must be a IPreset"); // $NON-NLS-1$
				}

				IPreset preset = (IPreset) inputElement;
				return preset.getEvents();
			}
		}

		private static abstract class EventTableLabelProvider extends ColumnLabelProvider {
			@Override
			public String getText(Object element) {
				if (!(element instanceof IEvent)) {
					throw new IllegalArgumentException("element must be an IEvent"); // $NON-NLS-1$
				}

				return doGetText((IEvent) element);
			}

			protected abstract String doGetText(IEvent event);
		}
	}

	private class EventTableButtonControls extends Composite {
		private static final String LABEL_ADD_BUTTON = "Add...";
		private static final String LABEL_EDIT_BUTTON = "Edit";
		private static final String LABEL_DUPLICATE_BUTTON = "Duplicate";
		private static final String LABEL_REMOVE_BUTTON = "Remove";

		private final TableViewer tableViewer;

		private final Button addButton;
		private final Button editButton;
		private final Button duplicateButton;
		private final Button removeButton;

		public EventTableButtonControls(Composite parent, TableViewer viewer) {
			super(parent, SWT.NONE);
			tableViewer = viewer;

			GridLayout layout = new GridLayout(1, true);
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			setLayout(layout);
			setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, false, true));

			addButton = new Button(this, SWT.PUSH);
			addButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			addButton.setText(LABEL_ADD_BUTTON);

			editButton = new Button(this, SWT.PUSH);
			editButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			editButton.setText(LABEL_EDIT_BUTTON);

			duplicateButton = new Button(this, SWT.PUSH);
			duplicateButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			duplicateButton.setText(LABEL_DUPLICATE_BUTTON);

			removeButton = new Button(this, SWT.PUSH);
			removeButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			removeButton.setText(LABEL_REMOVE_BUTTON);

			bindListeners();
		}

		protected void onAddButtonSelected(IStructuredSelection selection) {
			openEventEditingWizardFor(new Event());
			tableViewer.refresh();
		}

		protected void onEditButtonSelected(IStructuredSelection selection) {
			openEventEditingWizardFor((IEvent) selection.getFirstElement());
			tableViewer.refresh();
		}

		protected void onDuplicateButtonSelected(IStructuredSelection selection) {
			// TODO: create a copy properly
			IEvent original = (IEvent) selection.getFirstElement();
			IEvent duplicate = new Event();
			duplicate.setId(original.getId() + ".copy");
			duplicate.setName("Copy of " + original.getName());
			preset.addEvent(duplicate);
			tableViewer.refresh();
		}

		protected void onRemoveButtonSelected(IStructuredSelection selection) {
			preset.removeEvent((IEvent) selection.getFirstElement());
			tableViewer.refresh();
		}

		protected void toggleButtonAvailabilityBy(IStructuredSelection selection) {
			addButton.setEnabled(true);

			switch (selection.size()) {
			case 0:
				editButton.setEnabled(false);
				removeButton.setEnabled(false);
				duplicateButton.setEnabled(false);
				break;
			case 1:
				editButton.setEnabled(true);
				removeButton.setEnabled(true);
				duplicateButton.setEnabled(true);
				break;
			default: // more than one selected
				editButton.setEnabled(false);
				duplicateButton.setEnabled(false);
				removeButton.setEnabled(true);
			}
		}

		protected void bindListeners() {
			addButton.addListener(SWT.Selection, e -> onAddButtonSelected(tableViewer.getStructuredSelection()));
			editButton.addListener(SWT.Selection, e -> onEditButtonSelected(tableViewer.getStructuredSelection()));
			duplicateButton.addListener(SWT.Selection,
					e -> onDuplicateButtonSelected(tableViewer.getStructuredSelection()));
			removeButton.addListener(SWT.Selection, e -> onRemoveButtonSelected(tableViewer.getStructuredSelection()));

			tableViewer.addSelectionChangedListener(e -> toggleButtonAvailabilityBy(e.getStructuredSelection()));
			toggleButtonAvailabilityBy(tableViewer.getStructuredSelection());
		}
	}
}
