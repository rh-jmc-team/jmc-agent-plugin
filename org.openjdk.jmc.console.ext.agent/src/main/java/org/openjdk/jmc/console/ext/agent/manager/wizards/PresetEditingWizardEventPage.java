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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;
import org.openjdk.jmc.console.ext.agent.wizards.BaseWizardPage;
import org.openjdk.jmc.ui.misc.AbstractStructuredContentProvider;
import org.openjdk.jmc.ui.misc.DialogToolkit;

public class PresetEditingWizardEventPage extends BaseWizardPage {
	private static final String PAGE_NAME = "Agent Preset Editing";

	private static final String MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_TITLE = "Add or Remove Preset Events";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_EVENT_PAGE_DESCRIPTION = "Add new events to the preset, or remove/edit existing events.";
	private static final String MESSAGE_UNABLE_TO_SAVE_THE_PRESET = "Unable to add the event";

	private static final String LABEL_ID_COLUMN = "ID";
	private static final String LABEL_NAME_COLUMN = "Name";

	private static final String ID_ID_COLUMN = "id";
	private static final String ID_NAME_COLUMN = "name";

	private final IPreset preset;

	private TableInspector tableInspector;

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

		container.setLayout(new FillLayout());

		createEventTableContainer(container);

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);

		populateUi();
	}

	private Composite createEventTableContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		tableInspector = new TableInspector(container,
				TableInspector.MULTI | TableInspector.SHOW_HEADER | TableInspector.ADD_BUTTON
						| TableInspector.EDIT_BUTTON | TableInspector.DUPLICATE_BUTTON | TableInspector.REMOVE_BUTTON) {
			@Override
			protected void addColumns() {
				addColumn(LABEL_ID_COLUMN, ID_ID_COLUMN, new EventTableLabelProvider() {
					@Override
					protected String doGetText(IEvent event) {
						return event.getId();
					}
				});

				addColumn(LABEL_NAME_COLUMN, ID_NAME_COLUMN, new EventTableLabelProvider() {
					@Override
					protected String doGetText(IEvent event) {
						return event.getName();
					}
				});
			}

			@Override
			protected void onAddButtonSelected(IStructuredSelection selection) {
				IEvent event = preset.createEvent();
				while (DialogToolkit.openWizardWithHelp(new EventEditingWizard(event))) {
					try {
						preset.addEvent(event);
					} catch (IllegalArgumentException e) {
						if (DialogToolkit.openConfirmOnUiThread(MESSAGE_UNABLE_TO_SAVE_THE_PRESET, e.getMessage())) {
							continue;
						}
					}

					break;
				}

				tableInspector.getViewer().refresh();
			}

			@Override
			protected void onEditButtonSelected(IStructuredSelection selection) {
				IEvent original = (IEvent) selection.getFirstElement();
				IEvent workingCopy = original.createWorkingCopy();
				while (DialogToolkit.openWizardWithHelp(new EventEditingWizard(workingCopy))) {
					try {
						preset.updateEvent(original, workingCopy);
					} catch (IllegalArgumentException e) {
						if (DialogToolkit.openConfirmOnUiThread(MESSAGE_UNABLE_TO_SAVE_THE_PRESET, e.getMessage())) {
							continue;
						}
					}

					break;
				}
				tableInspector.getViewer().refresh();
			}

			@Override
			protected void onDuplicateButtonSelected(IStructuredSelection selection) {
				IEvent original = (IEvent) selection.getFirstElement();
				IEvent duplicate = original.createDuplicate();
				preset.addEvent(duplicate);

				tableInspector.getViewer().refresh();
			}

			@Override
			protected void onRemoveButtonSelected(IStructuredSelection selection) {
				for (Object event : selection) {
					preset.removeEvent((IEvent) event);
				}

				tableInspector.getViewer().refresh();
			}
		};
		tableInspector.setContentProvider(new EventTableContentProvider());

		return container;
	}

	private void populateUi() {
		tableInspector.setInput(preset);
	}

	private static class EventTableContentProvider extends AbstractStructuredContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (!(inputElement instanceof IPreset)) {
				throw new IllegalArgumentException("input element must be an IPreset"); // $NON-NLS-1$
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
