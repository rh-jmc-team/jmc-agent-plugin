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

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.rjmx.IServerHandle;

import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

public class AgentUi extends Composite {

    private static final String CONNECTOR_ADDRESS = "com.sun.management.jmxremote.localConnectorAddress";
    private static final String CONNECTOR_ARGS = "sun.jvm.args";
    private VirtualMachine vm;
    private FormToolkit toolkit;
    private AgentJMXHelper agentJMXHelper;
    private EventTreeSection eventTree;
    private LoadAgentSection loadAgentSection;

	public AgentUi(Composite parent, int style, IServerHandle handle, FormToolkit toolkit) {
		super(parent, style);
		this.setLayout(new GridLayout());
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		this.toolkit = toolkit;

		String pid = handle.getServerDescriptor().getJvmInfo().getPid().toString();
		vm = initVM(pid);
		if (isAgentLoaded()) {
			setUpJMXRelatedComponents();
		} else {
			loadAgentSection = new LoadAgentSection(this, vm);
			loadAgentSection.setLoadAgentListener(() -> loadAgentListener());
		}
	}

	public static Logger getLogger() {
		return Logger.getLogger(AgentUi.class.getName());
	}

	private VirtualMachine initVM(String pid) {
		VirtualMachine vm = null;
		try {
			vm = VirtualMachine.attach(pid);
		} catch (AttachNotSupportedException | IOException e) {
			getLogger().log(Level.SEVERE, "Could not attatch process with pid " + pid + " and create a VirtualMachine", e);
		}
		return vm;
	}

	private boolean isAgentLoaded() {
		String connectorAddress = null;
		try {
			connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
		} catch (IOException e) {
			getLogger().log(Level.SEVERE, "Could not check if agent has been loaded dynamically", e);
			return false;
		}
		if (connectorAddress == null) {
			String vmArgs = null;
			try {
				vmArgs = vm.getAgentProperties().getProperty(CONNECTOR_ARGS);
			} catch (IOException e) {
				getLogger().log(Level.SEVERE, "Could not check if agent has been loaded statically", e);
				return false;
			}
			return vmArgs.contains("-javaagent:");
		}
		return true;
	}

	private void setUpJMXRelatedComponents() {
		MBeanServerConnection mbsc = initMBeanServerConnection();
		agentJMXHelper = new AgentJMXHelper(mbsc);
		eventTree = new EventTreeSection(this, toolkit);
		eventTree.setAgentJMXHelper(agentJMXHelper);
	}

	private MBeanServerConnection initMBeanServerConnection() {
		MBeanServerConnection mbsc = null;
		try {
			String connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
			if (connectorAddress == null) {
			     vm.startLocalManagementAgent();
			     connectorAddress = vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);
			}
			JMXServiceURL url = new JMXServiceURL(connectorAddress);
			JMXConnector jmxConnector = JMXConnectorFactory.connect(url);

			mbsc = jmxConnector.getMBeanServerConnection();
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, "Could not create a MBeanServerConnection", e);
		}
		return mbsc;
	}

	private void loadAgentListener() {
		loadAgentSection.dispose();
		setUpJMXRelatedComponents();
		this.layout();
	}

}
