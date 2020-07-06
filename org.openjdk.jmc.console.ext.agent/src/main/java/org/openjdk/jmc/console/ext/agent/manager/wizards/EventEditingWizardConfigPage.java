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
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent.Location;

import java.util.stream.Stream;

public class EventEditingWizardConfigPage extends WizardPage {
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

		createIdInput(container, cols);
		createNameInput(container, cols);
		createDescriptionInput(container, cols);

		return container;
	}

	private void createIdInput(Composite parent, int cols) {
		{
			Label label = createLabel(parent, LABEL_ID);

			GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			gd.horizontalSpan = 2;
			gd.minimumHeight = 0;
			label.setLayoutData(gd);
		}

		{
			idText = createText(parent);
			idText.setMessage(MESSAGE_EVENT_ID);

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
			gd.horizontalSpan = cols - 2;
			gd.minimumWidth = 0;
			gd.widthHint = 400;
			idText.setLayoutData(gd);
		}
	}

	private void createNameInput(Composite parent, int cols) {
		{
			Label label = createLabel(parent, LABEL_NAME);

			GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			gd.horizontalSpan = 2;
			gd.minimumHeight = 0;
			label.setLayoutData(gd);
		}

		{
			nameText = createText(parent);
			nameText.setMessage(MESSAGE_NAME_OF_THE_EVENT);

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
			gd.horizontalSpan = cols - 2;
			gd.minimumWidth = 0;
			gd.widthHint = 400;
			nameText.setLayoutData(gd);
		}
	}

	private void createDescriptionInput(Composite parent, int cols) {
		{
			Label label = createLabel(parent, LABEL_DESCRIPTION);

			GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, true, true);
			gd.horizontalSpan = cols;
			gd.minimumHeight = 0;
			label.setLayoutData(gd);
		}

		{
			descriptionText = createMultiText(parent);
			// FIXME: Multi line Text field (SWT.MULTI) does not support Text.setMessage
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328832
			descriptionText.setMessage(MESSAGE_OPTIONAL_DESCRIPTION_OF_THIS_EVENT);

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
			gd.horizontalSpan = cols;
			gd.minimumWidth = 0;
			gd.widthHint = 300;
			gd.heightHint = 100;
			descriptionText.setLayoutData(gd);
		}
	}

	private Composite createInstrumentationTargetContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 8;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		createClassInput(container, cols);
		createMethodInput(container, cols);

		return container;
	}

	private void createClassInput(Composite parent, int cols) {
		{
			Label label = createLabel(parent, LABEL_CLASS);

			GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			gd.horizontalSpan = 2;
			gd.minimumHeight = 0;
			label.setLayoutData(gd);
		}

		{
			classText = createText(parent);
			classText.setMessage(MESSAGE_FULLY_QUALIFIED_CLASS_NAME);

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
			gd.horizontalSpan = cols - 2;
			gd.minimumWidth = 0;
			gd.widthHint = 400;
			classText.setLayoutData(gd);
		}
	}

	private void createMethodInput(Composite parent, int cols) {
		{
			Label label = createLabel(parent, LABEL_METHOD);

			GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, true, true);
			gd.horizontalSpan = 2;
			gd.minimumHeight = 0;

			label.setLayoutData(gd);
		}

		{
			methodNameText = createText(parent);
			methodNameText.setMessage(MESSAGE_METHOD_NAME);

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, true);
			gd.horizontalSpan = (cols - 2) / 2;
			gd.minimumWidth = 0;
			gd.widthHint = 300;

			methodNameText.setLayoutData(gd);
		}

		{
			methodDescriptorText = createText(parent);
			methodDescriptorText.setMessage(MESSAGE_METHOD_DESCRIPTOR);

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
			gd.horizontalSpan = (cols - 2) / 2;
			gd.minimumWidth = 0;
			gd.widthHint = 300;

			methodDescriptorText.setLayoutData(gd);
		}
	}

	private Composite createMetaInfoContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 8;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		createPathInput(container, cols);
		createLocationInput(container, cols);
		createRecordExceptionInput(container, cols);
		createRecordStackTraceInput(container, cols);

		return container;
	}

	private void createPathInput(Composite parent, int cols) {
		{
			Label label = createLabel(parent, LABEL_LOCATION);

			GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			gd.horizontalSpan = 2;
			gd.minimumHeight = 0;
			label.setLayoutData(gd);
		}

		{
			pathText = createText(parent);
			pathText.setMessage(MESSAGE_PATH_TO_EVENT);

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
			gd.horizontalSpan = cols - 2;
			gd.minimumWidth = 0;
			gd.widthHint = 400;
			pathText.setLayoutData(gd);
		}
	}

	private void createLocationInput(Composite parent, int cols) {
		{
			Label label = createLabel(parent, LABEL_LOCATION);

			GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
			gd.horizontalSpan = 2;
			gd.minimumHeight = 0;
			label.setLayoutData(gd);
		}

		{
			locationCombo = createCombo(parent,
					Stream.of(Location.values()).map(Location::toString).toArray(String[]::new));

			GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, true);
			gd.horizontalSpan = cols - 2;
			gd.minimumWidth = 0;
			gd.widthHint = 400;
			locationCombo.setLayoutData(gd);
		}
	}

	private void createRecordExceptionInput(Composite parent, int cols) {
		recordExceptionsBtn = createCheckbox(parent, LABEL_RECORD_EXCEPTIONS);
		recordExceptionsBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, cols, 1));
	}

	private void createRecordStackTraceInput(Composite parent, int cols) {
		recordStackTraceBtn = createCheckbox(parent, LABEL_RECORD_STACK_TRACE);
		recordStackTraceBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, cols, 1));
	}

	private Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		return label;
	}

	private Text createText(Composite parent) {
		Text text = new Text(parent, SWT.BORDER);
		text.setEnabled(true);
		return text;
	}

	private Text createMultiText(Composite parent) {
		Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		text.setEnabled(true);
		return text;
	}

	private Label createSeparator(Composite parent) {
		return new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
	}

	private Combo createCombo(Composite parent, String[] items) {
		Combo combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setItems(items);
		return combo;
	}

	private Button createCheckbox(Composite parent, String text) {
		Button checkbox = new Button(parent, SWT.CHECK);
		checkbox.setText(text);
		return checkbox;
	}

	private void setTextText(Text receiver, String text) {
		text = text == null ? "" : text;
		receiver.setText(text);
		receiver.setToolTipText(text);
	}

	private void setComboText(Combo receiver, String text) {
		text = text == null ? "" : text;
		receiver.setText(text);
		receiver.setToolTipText(text);
	}

	private void bindListeners() {
		idText.addModifyListener(e -> event.setId(idText.getText()));
		nameText.addModifyListener(e -> event.setName(nameText.getText()));
		descriptionText.addModifyListener(e -> event.setDescription(descriptionText.getText()));
		methodNameText.addModifyListener(e -> event.setMethodName(methodNameText.getText()));
		methodDescriptorText.addModifyListener(e -> event.setMethodDescriptor(methodDescriptorText.getText()));
		pathText.addModifyListener(e -> event.setMethodName(pathText.getText()));
		classText.addModifyListener(e -> event.setClazz(classText.getText()));
		recordExceptionsBtn.addListener(SWT.Selection, e -> event.setRethrow(recordExceptionsBtn.getSelection()));
		recordStackTraceBtn.addListener(SWT.Selection, e -> event.setStackTrace(recordStackTraceBtn.getSelection()));
		locationCombo.addModifyListener(e -> event.setLocation(Location.valueOf(locationCombo.getText())));
	}

	private void populateUi() {
		setTextText(idText, event.getId());
		setTextText(nameText, event.getName());
		setTextText(descriptionText, event.getDescription());
		setTextText(classText, event.getClazz());
		setTextText(methodNameText, event.getMethodName());
		setTextText(methodDescriptorText, event.getMethodDescriptor());
		setTextText(pathText, event.getPath());
		setComboText(locationCombo, event.getLocation().toString());
		recordExceptionsBtn.setSelection(event.getRethrow());
		recordStackTraceBtn.setSelection(event.getStackTrace());
	}
}
