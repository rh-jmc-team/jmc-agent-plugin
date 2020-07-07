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

public class PresetEditingWizardConfigPage extends BaseWizardPage {
	private static final String PAGE_NAME = "Agent Preset Editing";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_CONFIG_PAGE_TITLE = "Editing Preset Global Configurations";
	private static final String MESSAGE_PRESET_EDITING_WIZARD_CONFIG_PAGE_DESCRIPTION = "Global configurations are defaults which applies to any event missing a per-even configuration.";

	private static final String LABEL_FILE_NAME = "File Name: ";
	private static final String LABEL_CLASS_PREFIX = "Class Prefix: ";
	private static final String LABEL_ALLOW_TO_STRING = "Allow toString";
	private static final String LABEL_ALLOW_CONVERTER = "Allow Converter";

	private static final String MESSAGE_NAME_OF_THE_SAVED_XML = "Name of the saved XML on file system";
	private static final String MESSAGE_PREFIX_ADDED_TO_GENERATED_EVENT_CLASSES = "Prefix added to generated event classes";

	private final IPreset preset;

	private Text fileNameText;
	private Text classPrefixText;
	private Button allowToStringButton;
	private Button allowConverterButton;

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

		createFileNameContainer(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createSeparator(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createGlobalConfigContainer(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		populateUi();
		bindListeners();

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

		fileNameText = createTextInput(container, cols, LABEL_FILE_NAME, MESSAGE_NAME_OF_THE_SAVED_XML);

		return container;
	}
	
	private Composite createGlobalConfigContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 5;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		classPrefixText = createTextInput(container, cols, LABEL_CLASS_PREFIX, MESSAGE_PREFIX_ADDED_TO_GENERATED_EVENT_CLASSES);
		allowToStringButton = createCheckboxInput(parent, cols, LABEL_ALLOW_TO_STRING);
		allowConverterButton = createCheckboxInput(parent, cols, LABEL_ALLOW_CONVERTER);

		return container;
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

	@Override
	public boolean canFlipToNextPage() {
		return classPrefixError == null && fileNameError == null;
	}

	private void bindListeners() {
		fileNameText.addModifyListener(modifyEvent -> {
			try {
				preset.setFileName(fileNameText.getText());
				fileNameError = null;
			} catch (IllegalArgumentException e) {
				fileNameError = e;
			}

			setErrorMessageIfAny();
		});

		classPrefixText.addModifyListener(modifyEvent -> {
			try {
				preset.setClassPrefix(classPrefixText.getText());
				classPrefixError = null;
			} catch (IllegalArgumentException e) {
				classPrefixError = e;
			}

			setErrorMessageIfAny();
		});

		allowToStringButton.addListener(SWT.Selection, e -> preset.setAllowToString(allowToStringButton.getSelection()));
		allowConverterButton.addListener(SWT.Selection, e -> preset.setAllowConverter(allowConverterButton.getSelection()));
	}

	private void populateUi() {
		setText(fileNameText, preset.getFileName());
		setText(classPrefixText, preset.getFileName());
		allowToStringButton.setSelection(preset.getAllowToString());
		allowToStringButton.setSelection(preset.getAllowConverter());
	}
}
