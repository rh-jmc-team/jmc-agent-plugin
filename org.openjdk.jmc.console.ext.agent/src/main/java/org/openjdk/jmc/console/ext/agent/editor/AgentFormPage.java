/*
 * Copyright (c) 2018, Oracle and/or its affiliates. All rights reserved.
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
package org.openjdk.jmc.console.ext.agent.editor;

import javax.inject.Inject;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.openjdk.jmc.rjmx.IConnectionHandle;

/**
 * Extension point class that agent console tabs can subclass. The ConsoleTab uses the FormPage .
 */
//TODO: remove the @SuppressWarnings once IWorkbenchActionConstants.FIND_EXT is added to ApplicationActionBarAdvisor in JMC
@SuppressWarnings("restriction")
public class AgentFormPage extends FormPage /* implements IConsolePageContainer */ {

	private String id;
	private Image icon;

	@Inject
	private IConnectionHandle connectionHandle;

	public AgentFormPage(AgentEditor editor, String id, String title) {
		super(editor, id, title); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public AgentEditor getEditor() {
		return (AgentEditor) super.getEditor();
	}

	public Composite getBody() {
		return getManagedForm().getForm().getBody();
	}

	/**
	 * @return the connection handle associated with this tab.
	 */
	public IConnectionHandle getConnectionHandle() {
		return connectionHandle;
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		Form form = managedForm.getForm().getForm();
		managedForm.getToolkit().decorateFormHeading(form);
		form.setText(getTitle());
		form.setImage(getTitleImage());

		validateDependencies();

		IToolBarManager toolBar = managedForm.getForm().getToolBarManager();
		toolBar.add(new GroupMarker("first"));
		toolBar.update(true);

		// TODO: move the addition of this ContributionItem to ApplicationActionBarAdvisor in JMC when integrating to main repository
		((WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow()).getMenuBarManager().getItems();
		for (IContributionItem m : ((WorkbenchWindow) PlatformUI.getWorkbench().getActiveWorkbenchWindow())
				.getMenuBarManager().getItems()) {
			if (m.getId() == IWorkbenchActionConstants.M_EDIT) {
				MenuManager mm = ((MenuManager) m);
				if (mm.indexOf(IWorkbenchActionConstants.FIND_EXT) == -1) {
					mm.add(new GroupMarker(IWorkbenchActionConstants.FIND_EXT));
					mm.update(true);
					break;
				}
			}
		}

	}

	protected void validateDependencies() {
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public Image getTitleImage() {
		return icon;
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}
