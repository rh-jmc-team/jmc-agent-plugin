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

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.openjdk.jmc.console.ext.agent.manager.internal.Field;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IField;
import org.openjdk.jmc.ui.wizards.OnePageWizardDialog;

public class EventEditingWizardFieldPage extends WizardPage {
	private static final String PAGE_NAME = "Agent Event Editing";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_FIELD_PAGE_TITLE = "Editing Event Fields";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_FIELD_PAGE_DESCRIPTION = "Fields are a subset of Java primary expressions which can be evaluated and recorded when committing an event.";

	private final IEvent event;
	private TableButtonControls tableButtons;
	private FieldTableInspector tableInspector;

	protected EventEditingWizardFieldPage(IEvent event) {
		super(PAGE_NAME);

		this.event = event;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_EVENT_EDITING_WIZARD_FIELD_PAGE_TITLE);
		setDescription(MESSAGE_EVENT_EDITING_WIZARD_FIELD_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tableInspector = new FieldTableInspector(container);
		tableInspector.setInput(event.getFields());

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
		IField field = new Field();
		EventFieldEditingPage page = new EventFieldEditingPage(field);
		new OnePageWizardDialog(Display.getCurrent().getActiveShell(), page).open();
		event.addField(field);
		tableInspector.setInput(event.getFields());
	}

	private void onEditBtnPressed() {
		IField itemInfo = getSingleSelectedTemplate();
		if (itemInfo == null) {
			return;
		}
		new OnePageWizardDialog(Display.getCurrent().getActiveShell(), new EventFieldEditingPage(itemInfo)).open();
		tableInspector.setInput(event.getFields());
	}

	private void onRemoveBtnPressed() {
		IField itemInfo = getSingleSelectedTemplate();
		if (itemInfo == null) {
			return;
		}
		event.removeField(itemInfo);
		tableInspector.setInput(event.getFields());
	}

	private IField getSingleSelectedTemplate() {
		ISelection selection = tableInspector.getViewer().getSelection();
		if (selection instanceof IStructuredSelection) {
			return (IField) ((IStructuredSelection) selection).getFirstElement();
		}
		return null;
	}

}
