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
package org.openjdk.jmc.console.ext.agent.ui;

import java.util.logging.Level;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.VirtualMachine;

public class LoadAgentSection extends Composite {

    private static final String ENTER_PATH_MSG = "Enter Path...";
    private VirtualMachine vm;
	private Runnable loadAgentListener;

	public LoadAgentSection(Composite parent, VirtualMachine vm) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout());
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		this.vm = vm;

		Composite chartLabelContainer = new Composite(this, SWT.NO_BACKGROUND);
		chartLabelContainer.setLayout(new GridLayout(3, false));

		Label label = new Label(chartLabelContainer, SWT.NULL);
		label.setText("Agent jar Path: ");
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, false, false);
		gridData.widthHint = 100;
		label.setLayoutData(gridData);
		Text agentJarPath = new Text(chartLabelContainer, SWT.LEFT | SWT.BORDER);
		agentJarPath.setLayoutData(gridData);
		agentJarPath.setText(ENTER_PATH_MSG);
		Button browseJarButton = new Button(chartLabelContainer, SWT.PUSH);
		browseJarButton.setText("Browse");
		browseJarButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell());
				fd.setFilterExtensions(new String[] {"*.jar;*.JAR"});
				String filename = fd.open();
				if (filename != null) {
					agentJarPath.setText(new StringBuilder().append(fd.getFilterPath()).append("/").append(fd.getFileName()).toString());
				}
			}
		});
		agentJarPath.setText(ENTER_PATH_MSG);

		label = new Label(chartLabelContainer, SWT.NULL);
		label.setText("XML Path: ");
		label.setLayoutData(gridData);
		Text xmlPath = new Text(chartLabelContainer, SWT.LEFT | SWT.BORDER);
		xmlPath.setLayoutData(gridData);
		xmlPath.setText(ENTER_PATH_MSG);
		Button browseXmlButton = new Button(chartLabelContainer, SWT.PUSH);
		browseXmlButton.setText("Browse");
		browseXmlButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell());
				fd.setFilterExtensions(new String[] {"*.xml;*.XML"});
				String filename = fd.open();
				if (filename != null) {
					xmlPath.setText(new StringBuilder().append(fd.getFilterPath()).append("/").append(fd.getFileName()).toString());
				}
			}
		});

		Button button = new Button(this, SWT.PUSH);
		button.setText("Load agent");
		button.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (loadAgent(agentJarPath.getText(), xmlPath.getText())) {
					loadAgentListener.run();
				}
			}
		});
	}

	public void setLoadAgentListener(Runnable listener) {
		this.loadAgentListener = listener;
	}

	private boolean loadAgent(String agentJar, String xmlPath) {
		try {
			if (xmlPath == null || xmlPath.equals(ENTER_PATH_MSG)) {
				vm.loadAgent(agentJar);
			} else {
				vm.loadAgent(agentJar, xmlPath);
			}
		} catch (AgentInitializationException e) {
			AgentUi.getLogger().log(Level.SEVERE,
					"Could not access jdk.internal.misc.Unsafe! Rerun your application with '--add-opens java.base/jdk.internal.misc=ALL-UNNAMED'.", e);
			return false;
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
		return true;
	}

}
