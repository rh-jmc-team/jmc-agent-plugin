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
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.openjdk.jmc.console.ext.agent.manager.internal.MethodParameter;
import org.openjdk.jmc.console.ext.agent.manager.internal.MethodReturnValue;
import org.openjdk.jmc.console.ext.agent.manager.model.ICapturedValue;
import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodParameter;
import org.openjdk.jmc.console.ext.agent.manager.model.IMethodReturnValue;
import org.openjdk.jmc.console.ext.agent.manager.model.INamedCapturedValue;
import org.openjdk.jmc.ui.misc.AbstractStructuredContentProvider;
import org.openjdk.jmc.ui.wizards.OnePageWizardDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EventEditingWizardParameterPage extends BaseWizardPage {
	private static final String PAGE_NAME = "Agent Event Editing";

	private static final String MESSAGE_EVENT_EDITING_WIZARD_PARAMETER_PAGE_TITLE = "Add or Remove Event Parameters";
	private static final String MESSAGE_EVENT_EDITING_WIZARD_PARAMETER_PAGE_DESCRIPTION = "Function parameters and return values can be recorded when committing an event.";
	private static final String MESSAGE_RETURN_VALUE = "(Return value)";

	private static final String LABEL_INDEX = "Index";
	private static final String LABEL_NAME = "Name";
	private static final String LABEL_DESCRIPTION = "Description";

	private static final String ID_INDEX = "index";
	private static final String ID_NAME = "name";
	private static final String ID_DESCRIPTION = "description";

	private final IEvent event;

	private TableInspector tableInspector;

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

		container.setLayout(new FillLayout());

		createFieldTableContainer(container);

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);

		populateUi();
	}

	private Composite createFieldTableContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());

		tableInspector = new TableInspector(container, TableInspector.MULTI | TableInspector.SHOW_HEADER
				| TableInspector.ADD_BUTTON | TableInspector.EDIT_BUTTON | TableInspector.REMOVE_BUTTON) {
			@Override
			protected void addColumns() {
				addColumn(LABEL_INDEX, ID_INDEX, new ParameterTableLabelProvider() {
					@Override
					protected String doGetText(INamedCapturedValue parameter) {
						if (parameter instanceof IMethodReturnValue) {
							return MESSAGE_RETURN_VALUE;
						}

						if (parameter instanceof IMethodParameter) {
							return ((IMethodParameter) parameter).getIndex() + "";
						}

						throw new IllegalArgumentException(
								"element must be a an IMethodParameter or IMethodReturnValue"); // $NON-NLS-1$
					}
				});

				addColumn(LABEL_NAME, ID_NAME, new ParameterTableLabelProvider() {
					@Override
					protected String doGetText(INamedCapturedValue parameter) {
						return parameter.getName();
					}
				});

				addColumn(LABEL_DESCRIPTION, ID_DESCRIPTION, new ParameterTableLabelProvider() {
					@Override
					protected String doGetText(INamedCapturedValue parameter) {
						return parameter.getDescription();
					}
				});
			}

			@Override
			protected void onAddButtonSelected(IStructuredSelection selection) {
//				EventMethodParameterEditingPage page = new EventMethodParameterEditingPage();
				CapturedValueEditingPage page = new CapturedValueEditingPage(new MethodParameter());
				if (new OnePageWizardDialog(Display.getCurrent().getActiveShell(), page).open() != Window.OK) {
					return;
				}
				ICapturedValue capturedValue = page.getResult();
				if (capturedValue instanceof MethodParameter) {
					event.addMethodParameter((IMethodParameter) capturedValue);
				} else {
					event.setMethodReturnValue((IMethodReturnValue) capturedValue);
				}

				tableInspector.getViewer().refresh();
			}

			@Override
			protected void onEditButtonSelected(IStructuredSelection selection) {
				INamedCapturedValue original = (INamedCapturedValue) selection.getFirstElement();
				CapturedValueEditingPage page = new CapturedValueEditingPage(original);
				if (new OnePageWizardDialog(Display.getCurrent().getActiveShell(), page).open() == Window.OK) {
					INamedCapturedValue modified = page.getResult();
					// TODO: save the field
					if (original instanceof MethodParameter) {
						event.removeMethodParameter((IMethodParameter) original);
					} else {
						event.setMethodReturnValue(null);
					}

					if (modified instanceof MethodParameter) {
						event.addMethodParameter((IMethodParameter) modified);
					} else {
						event.setMethodReturnValue((IMethodReturnValue) modified);
					}
				}

				tableInspector.getViewer().refresh();
			}

			@Override
			protected void onRemoveButtonSelected(IStructuredSelection selection) {
				INamedCapturedValue namedCapturedValue = (INamedCapturedValue) selection.getFirstElement();
				if (namedCapturedValue instanceof MethodParameter) {
					event.removeMethodParameter((MethodParameter) namedCapturedValue);
				} else if (namedCapturedValue instanceof MethodReturnValue) {
					event.setMethodReturnValue(null);
				} else {
					throw new IllegalArgumentException("element must be a an IMethodParameter or IMethodReturnValue"); // $NON-NLS-1$
				}

				tableInspector.getViewer().refresh();
			}
		};

		tableInspector.setContentProvider(new ParameterTableContentProvider());

		return container;
	}

	private void populateUi() {
		tableInspector.setInput(event);
	}

	private static class ParameterTableContentProvider extends AbstractStructuredContentProvider
			implements IContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			if (!(inputElement instanceof IEvent)) {
				throw new IllegalArgumentException("input element must be an IEvent"); // $NON-NLS-1$
			}

			IEvent event = (IEvent) inputElement;
			IMethodParameter[] parameters = event.getMethodParameters();
			if (event.getMethodReturnValue() == null) {
				return parameters;
			}

			List<INamedCapturedValue> capturedValues = new ArrayList<>(Arrays.asList(parameters));
			capturedValues.add(event.getMethodReturnValue());
			return capturedValues.toArray(new INamedCapturedValue[0]);
		}
	}

	private static abstract class ParameterTableLabelProvider extends ColumnLabelProvider {
		@Override
		public String getText(Object element) {
			if (!(element instanceof IMethodParameter) && !(element instanceof IMethodReturnValue)) {
				throw new IllegalArgumentException("element must be a an IMethodParameter or IMethodReturnValue"); // $NON-NLS-1$
			}

			return doGetText((INamedCapturedValue) element);
		}

		protected abstract String doGetText(INamedCapturedValue field);
	}
}
