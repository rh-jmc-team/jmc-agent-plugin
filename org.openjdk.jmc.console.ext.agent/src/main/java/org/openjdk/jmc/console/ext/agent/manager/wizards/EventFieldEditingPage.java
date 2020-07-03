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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.console.ext.agent.manager.model.ICapturedValue;
import org.openjdk.jmc.console.ext.agent.manager.model.IField;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodParameter;

public class EventFieldEditingPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Parameter Editing";
	private static final String MESSAGE_FIELD_EDITING_PAGE_TITLE = "Editing a Field";
	private static final String MESSAGE_FIELD_EDITING_PAGE_DESCRIPTION = "Define a custom evaluation and capturing with an expression";
	private static final String FIELD_NAME_LABEL = "Name: ";
	private static final String FIELD_NAME_DESCRIPTION = "Field Name";
	private static final String FIELD_EXPRESSION_LABEL = "Expression: ";
	private static final String FIELD_EXPRESSION_DESCRIPTION = "Expression to capture";
	private static final String FIELD_DESCRIPTION_LABEL = "Description: ";
	private static final String FIELD_DESCRIPTION_DESCRIPTION = "Description of field";
	private static final String FIELD_CONTENT_TYPE_LABEL = "Content Type: ";
	private static final String FIELD_RELATIONAL_KEY_LABEL = "Relational Key: ";
	private static final String FIELD_RELATIONAL_KEY_DESCRIPTION = "schema://some-uri"; // $NON-NLS-1$
	private static final String FIELD_CONVERTER_TYPE_LABEL = "Converter Type: ";
	private static final String FIELD_CONVERTER_TYPE_DESCRIPTION = "com.company.project.MyConverter"; // $NON-NLS-1$

	private Text name;
	private Text expression;
	private Text description;
	private Combo contentType;
	private Text relationalKey;
	private Text converterType;
	private final IField field;

	public EventFieldEditingPage(IField field) {
		super(PAGE_NAME);

		// The capturedValue could be a IMethodParameter or IMethodReturnValue
		this.field = field;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_FIELD_EDITING_PAGE_TITLE);
		setDescription(MESSAGE_FIELD_EDITING_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		container.setLayout(new GridLayout());
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite gridSection = new Composite(container, SWT.NONE);
		gridSection.setLayout(new GridLayout(2, false));

		createPageContent(gridSection);
		gridSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		popuateStoredValues();
		bindListeners();

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

		Label label = createLabel(parent, FIELD_NAME_LABEL);
		label.setLayoutData(gdLabel);

		name = createText(parent);
		name.setMessage(FIELD_NAME_DESCRIPTION);
		name.setLayoutData(gdText);

		label = createLabel(parent, FIELD_EXPRESSION_LABEL);
		label.setLayoutData(gdLabel);

		expression = createText(parent);
		expression.setMessage(FIELD_EXPRESSION_DESCRIPTION);
		expression.setLayoutData(gdText);

		label = createLabel(parent, FIELD_DESCRIPTION_LABEL);
		label.setLayoutData(gdLabel);

		GridData largeText = new GridData(SWT.FILL, SWT.CENTER, true, true);
		largeText.minimumWidth = 0;
		largeText.widthHint = 300;
		largeText.heightHint = 100;
		largeText.horizontalSpan = 2;

		description = createTextMulti(parent);
		description.setMessage(FIELD_DESCRIPTION_DESCRIPTION);
		description.setLayoutData(largeText);

		label = createLabel(parent, FIELD_CONTENT_TYPE_LABEL);
		label.setLayoutData(gdLabel);

		contentType = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		List<String> contentTypes = new ArrayList<>();
		for (ICapturedValue.ContentType type : IMethodParameter.ContentType.values()) {
			contentTypes.add(type.name());
		}
		contentType.setItems(contentTypes.toArray(new String[0]));
		contentType.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		label = createLabel(parent, FIELD_RELATIONAL_KEY_LABEL);
		label.setLayoutData(gdLabel);

		relationalKey = createText(parent);
		relationalKey.setMessage(FIELD_RELATIONAL_KEY_DESCRIPTION);
		relationalKey.setLayoutData(gdText);

		label = createLabel(parent, FIELD_CONVERTER_TYPE_LABEL);
		label.setLayoutData(gdLabel);

		converterType = createText(parent);
		converterType.setMessage(FIELD_CONVERTER_TYPE_DESCRIPTION);
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

	private void popuateStoredValues() {
		converterType.setText(field.getConverter());
		description.setText(field.getDescription());
		relationalKey.setText(field.getRelationKey());
		name.setText(field.getName());
		expression.setText(field.getExpression());
		contentType.setText(field.getContentType().toString());
	}

	private void bindListeners() {
		converterType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				field.setConverter(converterType.getText());
			}
		});

		description.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				field.setDescription(description.getText());
			}
		});

		relationalKey.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				field.setRelationKey(relationalKey.getText());
			}
		});

		name.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				field.setName(name.getText());

			}
		});

		expression.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				field.setExpression(expression.getText());
			}
		});

		contentType.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (contentType.getSelectionIndex() >= 0) {
					field.setContentType(ICapturedValue.ContentType.valueOf(contentType.getText()));
				}
			}
		});

	}
}
