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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.console.ext.agent.manager.internal.MethodParameter;
import org.openjdk.jmc.console.ext.agent.manager.internal.MethodReturnValue;
import org.openjdk.jmc.console.ext.agent.manager.model.ICapturedValue;
import org.openjdk.jmc.ui.wizards.IPerformFinishable;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodParameter;
import org.openjdk.jmc.console.ext.agent.manager.model.INamedCapturedValue;

public class EventMethodParameterEditingPage extends WizardPage implements IPerformFinishable {
	private static final String PAGE_NAME = "Agent Parameter Editing";
	private static final String MESSAGE_PRESET_MANAGER_PAGE_TITLE = "Edit a Parameter or Return Value Capturing";
	private static final String MESSAGE_PRESET_MANAGER_PAGE_DESCRIPTION = "Define the capturing of a parameter or return value by its index.";
	private static final String PARAM_NAME_LABEL = "Name: ";
	private static final String PARAM_NAME_DESCRIPTION = "Parameter or Return Value Name";
	private static final String PARAM_INDEX_LABEL = "Index: ";
	private static final String RETURN_VALUE_LABEL = "This is a return value";
	private static final String PARAM_DESCRIPTION_LABEL = "Description: ";
	private static final String PARAM_DESCRIPTION_DESCRIPTION = "Description of this parameter or return value";
	private static final String PARAM_CONTENT_TYPE_LABEL = "Content Type: ";
	private static final String PARAM_RELATIONAL_KEY_LABEL = "Relational Key: ";
	private static final String PARAM_RELATIONAL_KEY_DESCRIPTION = "schema://some-uri";
	private static final String PARAM_CONVERTER_TYPE_LABEL = "Converter Type: ";
	private static final String PARAM_CONVERTER_TYPE_DESCRIPTION = "com.company.project.MyConverter";

	private ICapturedValue capturedValue;
	private Text name;
	private Spinner index;
	private Button returnValueBtn;
	private Text description;
	private Combo contentType;
	private Text relationalKey;
	private Text converterType;

	public EventMethodParameterEditingPage(ICapturedValue capturedValue) {
		super(PAGE_NAME);

		// The capturedValue could be a IMethodParameter or IMethodReturnValue
		this.capturedValue = capturedValue;
	}

	public EventMethodParameterEditingPage() {
		super(PAGE_NAME);
	}

	public ICapturedValue getCapturedValue() {
		return capturedValue;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_PRESET_MANAGER_PAGE_TITLE);
		setDescription(MESSAGE_PRESET_MANAGER_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite gridSection = new Composite(container, SWT.NONE);
		gridSection.setLayout(new GridLayout(2, false));

		createPageContent(gridSection);
		gridSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		populateStoredValues();

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}

	private void createPageContent(Composite parent) {
		GridData gdLabel = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gdText.minimumWidth = 0;
		gdText.widthHint = 300;

		Label label = createLabel(parent, PARAM_NAME_LABEL);
		label.setLayoutData(gdLabel);

		name = createText(parent);
		name.setMessage(PARAM_NAME_DESCRIPTION);
		name.setLayoutData(gdText);

		label = createLabel(parent, PARAM_INDEX_LABEL);
		label.setLayoutData(gdLabel);

		Composite indexContainer = new Composite(parent, SWT.NONE);
		indexContainer.setLayout(new GridLayout(3, false));
		indexContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		index = new Spinner(indexContainer, SWT.NONE);
		index.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		label = createLabel(indexContainer, RETURN_VALUE_LABEL);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));

		returnValueBtn = new Button(indexContainer, SWT.CHECK);
		returnValueBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		returnValueBtn.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (returnValueBtn.getSelection()) {
					index.setEnabled(false);
				} else {
					index.setEnabled(true);
				}

			}

		});

		label = createLabel(parent, PARAM_DESCRIPTION_LABEL);
		label.setLayoutData(gdLabel);

		GridData largeText = new GridData(SWT.FILL, SWT.CENTER, true, true);
		largeText.minimumWidth = 0;
		largeText.widthHint = 300;
		largeText.heightHint = 100;
		largeText.horizontalSpan = 2;

		description = createTextMulti(parent);
		description.setMessage(PARAM_DESCRIPTION_DESCRIPTION);
		description.setLayoutData(largeText);

		label = createLabel(parent, PARAM_CONTENT_TYPE_LABEL);
		label.setLayoutData(gdLabel);

		contentType = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		List<String> contentTypes = new ArrayList<>();
		for (ICapturedValue.ContentType type : IMethodParameter.ContentType.values()) {
			contentTypes.add(type.name());
		}
		contentType.setItems(contentTypes.toArray(new String[0]));
		contentType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		label = createLabel(parent, PARAM_RELATIONAL_KEY_LABEL);
		label.setLayoutData(gdLabel);

		relationalKey = createText(parent);
		relationalKey.setMessage(PARAM_RELATIONAL_KEY_DESCRIPTION);
		relationalKey.setLayoutData(gdText);

		label = createLabel(parent, PARAM_CONVERTER_TYPE_LABEL);
		label.setLayoutData(gdLabel);

		converterType = createText(parent);
		converterType.setMessage(PARAM_CONVERTER_TYPE_DESCRIPTION);
		converterType.setLayoutData(gdText);
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

	private void populateStoredValues() {
		if (capturedValue == null) {
			return;
		}
		converterType.setText(capturedValue.getConverter());
		description.setText(capturedValue.getDescription());
		relationalKey.setText(capturedValue.getRelationKey());
		name.setText(((INamedCapturedValue) capturedValue).getName());
		contentType.setText(capturedValue.getContentType().toString());
		if (capturedValue instanceof IMethodParameter) {
			index.setSelection(((IMethodParameter) capturedValue).getIndex());
			returnValueBtn.setEnabled(false);
		} else {
			index.setEnabled(false);
			returnValueBtn.setSelection(true);
		}
	}

	@Override
	public boolean performFinish() {
		if (capturedValue == null) {
			capturedValue = returnValueBtn.getSelection() ? new MethodReturnValue() : new MethodParameter();
		}

		capturedValue.setConverter(converterType.getText());
		capturedValue.setDescription(description.getText());
		capturedValue.setRelationKey(relationalKey.getText());
		((INamedCapturedValue) capturedValue).setName(name.getText());
		if (contentType.getSelectionIndex() >= 0) {
			capturedValue.setContentType(ICapturedValue.ContentType.valueOf(contentType.getText()));
		}
		if (!returnValueBtn.getSelection()) {
			((IMethodParameter) capturedValue).setIndex(index.getSelection());
		}

		return true;
	}

}
