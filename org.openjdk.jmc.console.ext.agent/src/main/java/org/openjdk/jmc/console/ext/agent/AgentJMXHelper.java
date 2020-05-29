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
package org.openjdk.jmc.console.ext.agent;

import java.io.IOException;
import java.util.logging.Level;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;

public class AgentJMXHelper {
	private static final String AGENT_OBJECT_NAME = "org.openjdk.jmc.jfr.agent:type=AgentController";
	private static final String DEFINE_EVENT_PROBES = "defineEventProbes";
	private static final String RETRIEVE_CURRENT_TRANSFORMS = "retrieveCurrentTransforms";

	private MBeanServerConnection mbsc;

	public AgentJMXHelper(MBeanServerConnection mbsc) {
		if (mbsc == null) {
			AgentPlugin.getDefault().getLogger().log(Level.SEVERE, "The MBeanServerConnection cannot be null");
			return;
		}
		this.mbsc = mbsc;
	}

	public boolean isMXBeanRegistered() {
		try {
			return mbsc.isRegistered(new ObjectName(AGENT_OBJECT_NAME));
		} catch (MalformedObjectNameException | IOException e) {
			AgentPlugin.getDefault().getLogger().log(Level.SEVERE, "Could not check if agent MXBean is registered", e);
		}
		return false;
	}

	public CompositeData[] retrieveCurrentTransforms() {
		try {
			Object result = mbsc.invoke(new ObjectName(AGENT_OBJECT_NAME), RETRIEVE_CURRENT_TRANSFORMS, new Object[0],
					new String[0]);
			return (CompositeData[]) result;
		} catch (InstanceNotFoundException
				| MalformedObjectNameException
				| MBeanException
				| ReflectionException
				| IOException e) {
			AgentPlugin.getDefault().getLogger().log(Level.WARNING, "Could not retrieve current transforms", e);
		}
		return null;
	}

	public void defineEventProbes(String xmlDescription) {
		try {
			Object[] params = {xmlDescription};
			String[] signature = {String.class.getName()};
			mbsc.invoke(new ObjectName(AGENT_OBJECT_NAME), DEFINE_EVENT_PROBES, params, signature);
		} catch (InstanceNotFoundException
				| MalformedObjectNameException
				| MBeanException
				| ReflectionException
				| IOException e) {
			AgentPlugin.getDefault().getLogger().log(Level.WARNING, "Could not define event probes: " + xmlDescription,
					e);
		}
	}
}
