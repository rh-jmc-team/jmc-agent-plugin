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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent.Location;

public class EventEditingWizardConfigPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Event Editing";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_CONFIG_PAGE_TITLE = "Editing Event Configurations";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_CONFIG_PAGE_DESCRIPTION = "Edit basic information of an event on how it should be instrumented and injected.";
	private static final String EVENT_ID_LABEL = "ID: ";
	private static final String EVENT_ID_DESCRIPTION = "Event ID";
	private static final String EVENT_NAME_LABEL = "Name: ";
	private static final String EVENT_NAME_DESCRIPTION = "Name of the Event";
	private static final String EVENT_DESCRIPTION_LABEL = "Description: ";
	private static final String EVENT_DESCRIPTION_DESCRIPTION = "Description of this event";
	private static final String EVENT_CLAZZ_LABEL = "Class: ";
	private static final String EVENT_CLAZZ_DESCRIPTION = "com.company.project.MyClass"; // $NON-NLS-1$
	private static final String EVENT_METHOD_LABEL = "Method: ";
	private static final String EVENT_METHOD_NAME_DESCRIPTION = "Method Name";
	private static final String EVENT_DESCRIPTOR_DESCRIPTION = "Descriptor";
	private static final String EVENT_PATH_LABEL = "Path: ";
	private static final String EVENT_PATH_DESCRIPTION = "path/to/event"; // $NON-NLS-1$
	private static final String EVENT_LOCATION_LABEL = "Location: ";
	private static final String EVENT_RETHROW_LABEL = "Catch any expression and rethrow";
	private static final String EVENT_STACKTRACE_LABEL = "Record Stack Trace";

	private static final int NUM_COLUMNS = 3;

	private Text idText;
	private Text nameText;
	private Text descriptionText;
	private Text clazzText;
	private Text methodNameText;
	private Text methodDescriptorText;
	private Text pathText;
	private Combo locationCombo;
	private Button useRethrowBtn;
	private Button recordStackTraceBtn;

	private final IEvent event;

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

		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite gridSection = new Composite(container, SWT.NONE);
		GridLayout gLayout = new GridLayout(NUM_COLUMNS, false);
		gridSection.setLayout(gLayout);

		createGlobalConfigSection(gridSection);
		createMethodSection(gridSection);
		createOtherInfoSection(gridSection);
		gridSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		createCheckBoxSection(container);

		bindListeners();
		populateStoredValues();

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}

	private void createGlobalConfigSection(Composite parent) {
		GridData gdLabel = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gdText.minimumWidth = 0;
		gdText.widthHint = 300;
		gdText.horizontalSpan = NUM_COLUMNS - 1;

		Label label = createLabel(parent, EVENT_ID_LABEL);
		label.setLayoutData(gdLabel);

		idText = createText(parent);
		idText.setMessage(EVENT_ID_DESCRIPTION);
		idText.setLayoutData(gdText);

		label = createLabel(parent, EVENT_NAME_LABEL);
		label.setLayoutData(gdLabel);

		nameText = createText(parent);
		nameText.setMessage(EVENT_NAME_DESCRIPTION);
		nameText.setLayoutData(gdText);

		label = createLabel(parent, EVENT_DESCRIPTION_LABEL);
		label.setLayoutData(gdLabel);

		gdText = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gdText.minimumWidth = 0;
		gdText.widthHint = 300;
		gdText.heightHint = 100;
		gdText.horizontalSpan = NUM_COLUMNS;

		descriptionText = createTextMulti(parent);
		descriptionText.setMessage(EVENT_DESCRIPTION_DESCRIPTION);
		descriptionText.setLayoutData(gdText);

		Control separator = createSeparator(parent);
		GridData gdSeperator = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdSeperator.horizontalSpan = NUM_COLUMNS;
		gdSeperator.heightHint = 20;
		separator.setLayoutData(gdSeperator);

	}

	private void createMethodSection(Composite parent) {
		GridData gdLabel = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gdText.minimumWidth = 0;
		gdText.widthHint = 300;
		gdText.horizontalSpan = NUM_COLUMNS - 1;

		Label label = createLabel(parent, EVENT_CLAZZ_LABEL);
		label.setLayoutData(gdLabel);

		clazzText = createText(parent);
		clazzText.setMessage(EVENT_CLAZZ_DESCRIPTION);
		clazzText.setLayoutData(gdText);

		label = createLabel(parent, EVENT_METHOD_LABEL);
		label.setLayoutData(gdLabel);

		gdText = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gdText.minimumWidth = 0;
		gdText.widthHint = 300;
		gdText.horizontalSpan = NUM_COLUMNS - 2;

		methodNameText = createText(parent);
		methodNameText.setMessage(EVENT_METHOD_NAME_DESCRIPTION);
		methodNameText.setLayoutData(gdText);

		methodDescriptorText = createText(parent);
		methodDescriptorText.setMessage(EVENT_DESCRIPTOR_DESCRIPTION);
		methodDescriptorText.setLayoutData(gdText);

		Control separator = createSeparator(parent);
		GridData gdSeperator = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdSeperator.horizontalSpan = NUM_COLUMNS;
		gdSeperator.heightHint = 20;
		separator.setLayoutData(gdSeperator);
	}

	private void createOtherInfoSection(Composite parent) {
		GridData gdLabel = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gdText.minimumWidth = 0;
		gdText.widthHint = 300;
		gdText.horizontalSpan = NUM_COLUMNS - 1;

		Label label = createLabel(parent, EVENT_PATH_LABEL);
		label.setLayoutData(gdLabel);

		pathText = createText(parent);
		pathText.setMessage(EVENT_PATH_DESCRIPTION);
		pathText.setLayoutData(gdText);

		label = createLabel(parent, EVENT_LOCATION_LABEL);
		label.setLayoutData(gdLabel);

		locationCombo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		List<String> locations = new ArrayList<>();
		for (Location l : Location.values()) {
			locations.add(l.name());
		}
		locationCombo.setItems(locations.toArray(new String[0]));
		locationCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	private void createCheckBoxSection(Composite parent) {
		Composite checkBoxTextContainer = new Composite(parent, SWT.NONE);
		checkBoxTextContainer.setLayout(new GridLayout(2, false));

		useRethrowBtn = new Button(checkBoxTextContainer, SWT.CHECK);
		useRethrowBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		GridData gdLabel = new GridData(SWT.FILL, SWT.CENTER, true, true);

		Label label = createLabel(checkBoxTextContainer, EVENT_RETHROW_LABEL);
		label.setLayoutData(gdLabel);

		recordStackTraceBtn = new Button(checkBoxTextContainer, SWT.CHECK);
		recordStackTraceBtn.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		label = createLabel(checkBoxTextContainer, EVENT_STACKTRACE_LABEL);
		label.setLayoutData(gdLabel);

		checkBoxTextContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
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

	private Text createTextMulti(Composite parent) {
		Text text = new Text(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		text.setEnabled(true);
		return text;
	}

	private Label createSeparator(Composite parent) {
		return new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
	}

	private void bindListeners() {
		idText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				event.setId(idText.getText());
			}
		});

		nameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				event.setName(nameText.getText());
			}
		});

		descriptionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				event.setDescription(descriptionText.getText());
			}
		});

		methodNameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				event.setMethodName(methodNameText.getText());
			}
		});

		methodDescriptorText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				event.setMethodDescriptor(methodDescriptorText.getText());
			}
		});

		pathText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				event.setMethodName(pathText.getText());
			}
		});

		clazzText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				event.setClazz(clazzText.getText());
			}
		});

		useRethrowBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				event.setRethrow(useRethrowBtn.getSelection());
			}
		});

		recordStackTraceBtn.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				event.setStackTrace(recordStackTraceBtn.getSelection());
			}
		});

		locationCombo.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (locationCombo.getSelectionIndex() >= 0) {
					event.setLocation(Location.valueOf(locationCombo.getText()));
				}
			}
		});
	}

	private void populateStoredValues() {
		idText.setText(event.getId());
		nameText.setText(event.getName());
		descriptionText.setText(event.getDescription());
		clazzText.setText(event.getClazz());
		methodNameText.setText(event.getMethodName());
		methodDescriptorText.setText(event.getMethodDescriptor());
		pathText.setText(event.getPath());
		locationCombo.setText(event.getLocation().toString());
		useRethrowBtn.setSelection(event.getRethrow());
		recordStackTraceBtn.setSelection(event.getStackTrace());
	}

}
