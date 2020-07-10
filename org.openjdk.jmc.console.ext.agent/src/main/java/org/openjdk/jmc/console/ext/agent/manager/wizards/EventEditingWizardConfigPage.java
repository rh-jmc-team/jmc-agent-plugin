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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent.Location;

import java.util.stream.Stream;

public class EventEditingWizardConfigPage extends BaseWizardPage {
	private static final String PAGE_NAME = "Agent Event Editing";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_CONFIG_PAGE_TITLE = "Editing Event Configurations";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_CONFIG_PAGE_DESCRIPTION = "Edit basic information of an event on how it should be instrumented and injected.";

	private static final String LABEL_ID = "ID: ";
	private static final String LABEL_NAME = "Name: ";
	private static final String LABEL_DESCRIPTION = "Description: ";
	private static final String LABEL_CLASS = "Class: ";
	private static final String LABEL_METHOD = "Method: ";
	private static final String LABEL_PATH = "Path: ";
	private static final String LABEL_LOCATION = "Location: ";
	private static final String LABEL_CLEAR = "Clear";
	private static final String LABEL_RECORD_EXCEPTIONS = "Record exceptions";
	private static final String LABEL_RECORD_STACK_TRACE = "Record stack trace";

	private static final String MESSAGE_EVENT_ID = "Event ID";
	private static final String MESSAGE_NAME_OF_THE_EVENT = "Name of the event";
	private static final String MESSAGE_FULLY_QUALIFIED_CLASS_NAME = "Fully qualified class name"; // $NON-NLS-1$
	private static final String MESSAGE_METHOD_NAME = "Method name";
	private static final String MESSAGE_METHOD_DESCRIPTOR = "Method descriptor";
	private static final String MESSAGE_OPTIONAL_DESCRIPTION_OF_THIS_EVENT = "(Optional) Description of this event";
	private static final String MESSAGE_PATH_TO_EVENT = "Path to event in event browser"; // $NON-NLS-1$

	private final IEvent event;

	private Text idText;
	private Text nameText;
	private Text descriptionText;
	private Text classText;
	private Text methodNameText;
	private Text methodDescriptorText;
	private Text pathText;
	private Combo locationCombo;
	private Button locationClearBtn;
	private Button recordExceptionsBtn;
	private Button recordStackTraceBtn;

	protected EventEditingWizardConfigPage(IEvent event) {
		super(PAGE_NAME);

		this.event = event;
	}

	@Override
	public IWizardPage getNextPage() {
		return super.getNextPage();
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_EVENT_EDITING_WIZARD_CONFIG_PAGE_TITLE);
		setDescription(MESSAGE_EVENT_EDITING_WIZARD_CONFIG_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		createConfigContainer(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createSeparator(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createInstrumentationTargetContainer(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createSeparator(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createMetaInfoContainer(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		bindListeners();
		populateUi();

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}

	private Composite createConfigContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 5;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		idText = createTextInput(container, cols, LABEL_ID, MESSAGE_EVENT_ID);
		nameText = createTextInput(container, cols, LABEL_NAME, MESSAGE_NAME_OF_THE_EVENT);
		descriptionText = createMultiTextInput(container, cols, LABEL_DESCRIPTION,
				MESSAGE_OPTIONAL_DESCRIPTION_OF_THIS_EVENT);

		return container;
	}

	private Composite createInstrumentationTargetContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 8;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		classText = createTextInput(container, cols, LABEL_CLASS, MESSAGE_FULLY_QUALIFIED_CLASS_NAME);
		Text[] receivers = createMultiInputTextInput(container, cols, LABEL_METHOD,
				new String[] {MESSAGE_METHOD_NAME, MESSAGE_METHOD_DESCRIPTOR});

		methodNameText = receivers[0];
		methodDescriptorText = receivers[1];

		return container;
	}

	private Composite createMetaInfoContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 8;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		pathText = createTextInput(container, cols, LABEL_PATH, MESSAGE_PATH_TO_EVENT);
		locationCombo = createComboInput(container, cols - 2, LABEL_LOCATION,
				Stream.of(Location.values()).map(Location::toString).toArray(String[]::new));
		locationClearBtn = createButton(container, LABEL_CLEAR);
		locationClearBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 0));

		recordExceptionsBtn = createCheckboxInput(container, cols, LABEL_RECORD_EXCEPTIONS);
		recordStackTraceBtn = createCheckboxInput(container, cols, LABEL_RECORD_STACK_TRACE);

		return container;
	}

	private void bindListeners() {
		idText.addModifyListener(e -> event.setId(idText.getText()));
		nameText.addModifyListener(e -> event.setName(nameText.getText()));
		descriptionText.addModifyListener(e -> event.setDescription(descriptionText.getText()));
		methodNameText.addModifyListener(e -> event.setMethodName(methodNameText.getText()));
		methodDescriptorText.addModifyListener(e -> event.setMethodDescriptor(methodDescriptorText.getText()));
		pathText.addModifyListener(e -> event.setMethodName(pathText.getText()));
		classText.addModifyListener(e -> event.setClazz(classText.getText()));
		locationCombo.addModifyListener(e -> event.setLocation(
				locationCombo.getSelectionIndex() == -1 ? null : Location.valueOf(locationCombo.getText())));
		locationClearBtn.addListener(SWT.Selection, e -> locationCombo.deselectAll());
		recordExceptionsBtn.addListener(SWT.Selection, e -> event.setRethrow(recordExceptionsBtn.getSelection()));
		recordStackTraceBtn.addListener(SWT.Selection, e -> event.setStackTrace(recordStackTraceBtn.getSelection()));
	}

	private void populateUi() {
		setText(idText, event.getId());
		setText(nameText, event.getName());
		setText(descriptionText, event.getDescription());
		setText(classText, event.getClazz());
		setText(methodNameText, event.getMethodName());
		setText(methodDescriptorText, event.getMethodDescriptor());
		setText(pathText, event.getPath());
		setText(locationCombo, event.getLocation() == null ? null : event.getLocation().toString());
		recordExceptionsBtn.setSelection(event.getRethrow());
		recordStackTraceBtn.setSelection(event.getStackTrace());
	}
}
