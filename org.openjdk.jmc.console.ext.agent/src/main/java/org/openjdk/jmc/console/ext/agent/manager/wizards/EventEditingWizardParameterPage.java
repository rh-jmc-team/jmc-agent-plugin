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
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.openjdk.jmc.console.ext.agent.manager.internal.MethodParameter;
import org.openjdk.jmc.console.ext.agent.manager.model.ICapturedValue;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodParameter;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodReturnValue;
import org.openjdk.jmc.ui.wizards.OnePageWizardDialog;

public class EventEditingWizardParameterPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Event Editing";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_PARAMETER_PAGE_TITLE = "Edit Event Parameters";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_PARAMETER_PAGE_DESCRIPTION = "Function parameters and return values can be recorded when committing an event.";
	private final IEvent event;
	private ParameterTableInspector tableInspector;
	private TableButtonControls tableButtons;

	protected EventEditingWizardParameterPage(IEvent event) {
		super(PAGE_NAME);

		this.event = event;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_EVENT_EDITING_WIZARD_PARAMETER_PAGE_TITLE);
		setDescription(MESSAGE_EVENT_EDITING_WIZARD_PARAMETER_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tableInspector = new ParameterTableInspector(container);
		tableInspector.setInput(populateTable());

		tableButtons = new TableButtonControls(container, tableInspector.getViewer());
		tableButtons.setAddButtonListener(this::onAddBtnPressed);
		tableButtons.setEditButtonListener(this::onEditBtnPressed);
		tableButtons.setRemoveButtonListener(this::onRemoveBtnPressed);

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}

	private void onAddBtnPressed() {
		EventMethodParameterEditingPage page = new EventMethodParameterEditingPage();
		new OnePageWizardDialog(Display.getCurrent().getActiveShell(), page).open();
		ICapturedValue capturedValue = page.getCapturedValue();
		if (capturedValue instanceof MethodParameter) {
			event.addMethodParameter((IMethodParameter) capturedValue);
		} else {
			event.setMethodReturnValue((IMethodReturnValue) capturedValue);
		}
		tableInspector.setInput(populateTable());
	}

	private void onEditBtnPressed() {
		ICapturedValue itemInfo = getSingleSelectedTemplate();
		if (itemInfo == null) {
			return;
		}
		new OnePageWizardDialog(Display.getCurrent().getActiveShell(), new EventMethodParameterEditingPage(itemInfo))
				.open();

		tableInspector.setInput(populateTable());
	}

	private void onRemoveBtnPressed() {
		ICapturedValue itemInfo = getSingleSelectedTemplate();
		if (itemInfo == null) {
			return;
		}
		if (itemInfo instanceof IMethodReturnValue) {
			event.setMethodReturnValue(null);
		} else {
			event.removeMethodParameter((IMethodParameter) itemInfo);
		}
		tableInspector.setInput(populateTable());
	}

	private ICapturedValue getSingleSelectedTemplate() {
		ISelection selection = tableInspector.getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			return (ICapturedValue) ((IStructuredSelection) selection).getFirstElement();
		}
		return null;
	}

	private ICapturedValue[] populateTable() {
		List<ICapturedValue> capturedValues = new ArrayList<>();

		capturedValues.addAll(Arrays.asList(event.getMethodParameters()));
		if (event.getMethodReturnValue() != null) {
			capturedValues.add(event.getMethodReturnValue());
		}
		return capturedValues.toArray(new ICapturedValue[0]);
	}

}
