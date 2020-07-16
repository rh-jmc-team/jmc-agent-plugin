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
package org.openjdk.jmc.console.ext.agent.wizards;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.manager.wizards.BaseWizardPage;
import org.openjdk.jmc.flightrecorder.ui.FlightRecorderUI;
import org.openjdk.jmc.ui.common.jvm.JVMDescriptor;

import java.nio.file.Files;
import java.nio.file.Paths;

public class StartAgentWizardPage extends BaseWizardPage {
	private static final String PAGE_NAME = "Start Agent Wizard Page";
	private static final String MESSAGE_START_AGENT_WIZARD_PAGE_TITLE = "Start JMC Agent";
	private static final String MESSAGE_START_AGENT_WIZARD_PAGE_DESCRIPTION = "Enter the JMC Agent configuration details and then click Finish to start the agent.";
	private static final String MESSAGE_PATH_TO_AN_AGENT_JAR = "Path to an agent JAR";
	private static final String MESSAGE_PATH_TO_AN_AGENT_CONFIG = "(Optional) Path to an agent configuration";

	private static final String LABEL_TARGET_JVM = "Target JVM: ";
	private static final String LABEL_AGENT_JAR = "Agent JAR: ";
	private static final String LABEL_AGENT_XML = "Agent XML: ";
	private static final String LABEL_BROWSE = "Browse...";

	private static final String DIALOG_BROWSER_FOR_AGENT_JAR = "Browser for JMC Agent JAR";
	private static final String DIALOG_BROWSER_FOR_AGENT_CONFIG = "Browser for JMC Agent Configuration";

	private static final String FILE_OPEN_FILTER_PATH = "file.open.filter.path"; // $NON-NLS-1$
	private static final String FILE_OPEN_JAR_EXTENSION = "*.jar"; // $NON-NLS-1$
	private static final String FILE_OPEN_XML_EXTENSION = "*.xml"; // $NON-NLS-1$

	private final AgentJmxHelper helper;

	private Text targetJvmText;
	private Text agentJarText;
	private Button agentJarBrowseButton;
	private Text agentXmlText;
	private Button agentXmlBrowseButton;

	protected StartAgentWizardPage(AgentJmxHelper helper) {
		super(PAGE_NAME);

		this.helper = helper;
	}

	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);

		setTitle(MESSAGE_START_AGENT_WIZARD_PAGE_TITLE);
		setDescription(MESSAGE_START_AGENT_WIZARD_PAGE_DESCRIPTION);

		ScrolledComposite sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		Composite container = new Composite(sc, SWT.NONE);
		sc.setContent(container);

		GridLayout layout = new GridLayout();
		container.setLayout(layout);

		createTargetJvmContainer(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createSeparator(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		createAgentBrowserContainer(container).setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		bindListeners();
		populateUi();

		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setMinSize(container.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		setControl(sc);
	}

	public JVMDescriptor getTargetJvm() {
		return helper.getConnectionHandle().getServerDescriptor().getJvmInfo();
	}

	public String getAgentJarPath() {
		return agentJarText.getText();
	}

	public String getAgentXmlPath() {
		return agentXmlText.getText();
	}

	private Composite createTargetJvmContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 5;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		targetJvmText = createTextInput(container, cols, LABEL_TARGET_JVM, ""); // $NON-NLS-1$
		targetJvmText.setEnabled(false);

		return container;
	}

	private Composite createAgentBrowserContainer(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		int cols = 8;
		GridLayout layout = new GridLayout(cols, false);
		layout.horizontalSpacing = 8;
		container.setLayout(layout);

		agentJarText = createTextInput(container, cols - 2, LABEL_AGENT_JAR, MESSAGE_PATH_TO_AN_AGENT_JAR);
		agentJarText.setEditable(false);
		agentJarBrowseButton = createButton(container, LABEL_BROWSE);
		agentJarBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 0));

		agentXmlText = createTextInput(container, cols - 2, LABEL_AGENT_XML, MESSAGE_PATH_TO_AN_AGENT_CONFIG);
		agentXmlText.setEditable(false);
		agentXmlBrowseButton = createButton(container, LABEL_BROWSE);
		agentXmlBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 0));

		return container;
	}

	private void bindListeners() {
		agentJarBrowseButton.addListener(SWT.Selection, e -> {
			String path = openFileOpenerBrowser(DIALOG_BROWSER_FOR_AGENT_JAR, new String[] {FILE_OPEN_JAR_EXTENSION});
			if (path != null && !path.isEmpty()) {
				setText(agentJarText, path);
			}
		});
		agentXmlBrowseButton.addListener(SWT.Selection, e -> {
			String path = openFileOpenerBrowser(DIALOG_BROWSER_FOR_AGENT_CONFIG,
					new String[] {FILE_OPEN_XML_EXTENSION});
			if (path != null && !path.isEmpty()) {
				setText(agentXmlText, path);
			}
		});
		agentJarText.addModifyListener(e -> setPageComplete(!agentJarText.getText().isEmpty()));
		getWizard().getContainer().updateButtons();
	}

	private void populateUi() {
		setText(targetJvmText, helper.getConnectionHandle().getServerDescriptor().getDisplayName());
	}

	protected String openFileOpenerBrowser(String title, String[] extensions) {
		String filterPath = FlightRecorderUI.getDefault().getDialogSettings().get(FILE_OPEN_FILTER_PATH);
		if (filterPath != null && Files.notExists(Paths.get(filterPath))) {
			filterPath = System.getProperty("user.home", "./"); // $NON-NLS-1$ $NON-NLS-2$
		}

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		FileDialog dialog = new FileDialog(window.getShell(), SWT.OPEN | SWT.SINGLE);
		dialog.setFilterPath(filterPath);
		dialog.setText(title);
		dialog.setFilterExtensions(extensions);

		return dialog.open();
	}
}
