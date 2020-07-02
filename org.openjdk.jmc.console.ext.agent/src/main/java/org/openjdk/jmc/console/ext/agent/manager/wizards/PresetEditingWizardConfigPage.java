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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;

public class PresetEditingWizardConfigPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Preset Editing";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_CONFIG_PAGE_TITLE = "Editing Preset Global Configurations";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_CONFIG_PAGE_DESCRIPTION = "Global configurations are defaults which applies to any event missing a per-even configuration.";

	private static final String LABEL_FILE_NAME = "File Name: ";
	private static final String LABEL_GLOBAL_CONFIG = "Global Configurations: ";
	private static final String LABEL_CLASS_PREFIX = "Class Prefix: ";
	private static final String BUTTON_ALLOW_TO_STRING = "Allow toString";
	private static final String BUTTON_ALLOW_CONVERTER = "Allow Converter";

	private static final String DEFAULT_CLASS_PREFIX = "__JFR_Event";

	private final IPreset preset;

	private Text fileNameText;
	private Text classPrefixText;
	private Button allowToString;
	private Button allowConverter;

	private Exception fileNameError;
	private Exception classPrefixError;

	protected PresetEditingWizardConfigPage(IPreset preset) {
		super(PAGE_NAME);

		this.preset = preset;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_PRESET_EDITING_WIZARD_CONFIG_PAGE_TITLE);
		setDescription(MESSAGE_PRESET_EDITING_WIZARD_CONFIG_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		{
			Composite fileNameContainer = createFileNameContainer(container);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			fileNameContainer.setLayoutData(gd);
		}

		{
			Control separator = createSeparator(container);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			separator.setLayoutData(gd);
		}

		{
			Composite globalConfigContainer = createGlobalConfigContainer(container);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
			globalConfigContainer.setLayoutData(gd);
		}

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}

	private Composite createFileNameContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 5;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		createFileNameInput(container, cols);

		return container;
	}

	private void createFileNameInput(Composite parent, int cols) {
		GridData gd1 = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		Label label = createLabel(parent, LABEL_FILE_NAME);
		label.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gd2.horizontalSpan = cols - 2;
		fileNameText = createText(parent);
		gd2.minimumWidth = 0;
		gd2.widthHint = 400;
		fileNameText.setLayoutData(gd2);

		fileNameText.addModifyListener(modifyEvent -> {
			try {
				preset.setFileName(fileNameText.getText());
				fileNameError = null;
			} catch (IllegalArgumentException e) {
				fileNameError = e;
			}

			setErrorMessageIfAny();
		});

		// TODO: come up with a proper naming scheme, to avoid collision in names
		// TODO: defaults should be set in model, not view
		setTextText(fileNameText, "new_preset.xml");
	}

	private void setErrorMessageIfAny() {
		if (fileNameError != null) {
			setErrorMessage(fileNameError.getMessage());
			return;
		}

		if (classPrefixError != null) {
			setErrorMessage(classPrefixError.getMessage());
			return;
		}

		setErrorMessage(null);
	}

	private Composite createGlobalConfigContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 5;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		Label globalConfigLabel = new Label(container, SWT.NONE);
		globalConfigLabel.setText(LABEL_GLOBAL_CONFIG);
		globalConfigLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true, cols, 1));

		createClassPrefixInput(container, cols);
		createAllowToStringCheckbox(container, cols);
		createAllowConverterCheckbox(container, cols);

		return container;
	}

	private void createClassPrefixInput(Composite parent, int cols) {
		GridData gd1 = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		Label label = createLabel(parent, LABEL_CLASS_PREFIX);
		label.setLayoutData(gd1);

		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, true);
		gd2.horizontalSpan = cols - 2;
		classPrefixText = createText(parent);
		gd2.minimumWidth = 0;
		gd2.widthHint = 400;
		classPrefixText.setLayoutData(gd2);

		classPrefixText.addModifyListener(modifyEvent -> {
			try {
				preset.setClassPrefix(classPrefixText.getText());
				classPrefixError = null;
			} catch (IllegalArgumentException e) {
				classPrefixError = e;
			}

			setErrorMessageIfAny();
		});

		setTextText(classPrefixText, DEFAULT_CLASS_PREFIX);
	}

	private void createAllowToStringCheckbox(Composite parent, int cols) {
		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, false, true, cols, 1);
		allowToString = createCheckbox(parent, BUTTON_ALLOW_TO_STRING);
		allowToString.setLayoutData(gd2);

		allowToString.addSelectionListener(SelectionListener
				.widgetSelectedAdapter(selectionEvent -> preset.setAllowToString(allowConverter.getSelection())));
	}

	private void createAllowConverterCheckbox(Composite parent, int cols) {
		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, false, true, cols, 1);
		allowConverter = createCheckbox(parent, BUTTON_ALLOW_CONVERTER);
		allowConverter.setLayoutData(gd2);

		allowConverter.addSelectionListener(SelectionListener
				.widgetSelectedAdapter(selectionEvent -> preset.setAllowConverter(allowConverter.getSelection())));
	}

	protected Label createLabel(Composite parent, String text) {
		Label label = new Label(parent, SWT.NONE);
		label.setText(text);
		return label;
	}

	protected Text createText(Composite parent) {
		Text text = new Text(parent, SWT.READ_ONLY | SWT.BORDER);
		text.setEnabled(true);
		text.setEditable(true);
		return text;
	}

	protected Label createSeparator(Composite parent) {
		return new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
	}

	protected Button createCheckbox(Composite parent, String text) {
		Button checkbox = new Button(parent, SWT.CHECK);
		checkbox.setText(text);
		return checkbox;
	}

	private void setTextText(Text receiver, String text) {
		text = text == null ? "" : text;
		receiver.setText(text);
		receiver.setToolTipText(text);
	}
}
