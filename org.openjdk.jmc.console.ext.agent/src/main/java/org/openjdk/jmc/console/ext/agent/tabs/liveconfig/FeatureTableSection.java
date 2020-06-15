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
package org.openjdk.jmc.console.ext.agent.tabs.liveconfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import javax.management.openmbean.CompositeData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openjdk.jmc.console.ext.agent.AgentJmxHelper;
import org.openjdk.jmc.console.ext.agent.AgentPlugin;
import org.openjdk.jmc.rjmx.IConnectionHandle;
import org.openjdk.jmc.ui.misc.MCLayoutFactory;
import org.openjdk.jmc.ui.misc.MCSectionPart;

public class FeatureTableSection extends MCSectionPart {

	private volatile String lastName;
	private AgentJmxHelper helper;
	private EventTableInspector propertyInspector;
	private EventTableInspector attributeInspector;

	public FeatureTableSection(Composite parent, FormToolkit toolkit, IConnectionHandle ch, AgentJmxHelper helper) {
		super(parent, toolkit, DEFAULT_TITLE_STYLE);
		this.helper = helper;
		getSection().setText("Event Table");

		Composite body = createSectionBody(MCLayoutFactory.createMarginFreeFormPageLayout());
		CTabFolder tabFolder = new CTabFolder(body, SWT.NONE);
		toolkit.adapt(tabFolder);
		tabFolder.setLayoutData(MCLayoutFactory.createFormPageLayoutData());

		CTabItem propertiesTab = new CTabItem(tabFolder, SWT.NONE);
		propertyInspector = new EventTableInspector(tabFolder);
		propertiesTab.setControl(propertyInspector.getViewer().getTree());
		propertiesTab.setText("Properties");

		CTabItem attributeTab = new CTabItem(tabFolder, SWT.NONE);
		attributeInspector = new EventTableInspector(tabFolder);
		attributeTab.setControl(attributeInspector.getViewer().getTree());
		attributeTab.setText("Attributes");

		tabFolder.setSelection(0);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void showEvent(String eventName) {
		if (lastName != null && lastName.equals(eventName)) {
			return;
		}
		lastName = eventName;
		CompositeData[] cds = helper.retrieveCurrentTransforms();
		CompositeData cdEvent = getEventCompositeData(cds, eventName);
		try {
			propertyInspector.setInput(createEventProperties(cdEvent));
			attributeInspector.setInput(createEventAttributes(cdEvent));
		} catch (Exception e) {
			AgentPlugin.getDefault().getLogger().log(Level.SEVERE, "Failed to update feature table", e);
		}
	}

	private CompositeData getEventCompositeData(CompositeData[] cds, String eventName) {
		for (CompositeData cd : cds) {
			if (eventName.equals(cd.get("eventName").toString())) {
				return cd;
			}
		}
		return null;
	}

	private List<Object> createEventProperties(CompositeData cd) throws Exception {
		List<Object> attributes = new ArrayList<>();
		if (cd != null) {
			Set<String> keys = cd.getCompositeType().keySet();
			for (String key : keys) {
				if (!isEmptyCompositeData(cd, key) && !EventAttributes.ATTRIBUTE_LIST.contains(key)) {
					attributes.add(new EventReadOnlyAttribute(key, cd.getCompositeType().getType(key).getClassName(),
							cd.get(key)));
				}
			}
		}
		return attributes;
	}

	private List<Object> createEventAttributes(CompositeData cd) throws Exception {
		List<Object> attributes = new ArrayList<>();
		if (cd != null) {
			for (String key : EventAttributes.ATTRIBUTE_LIST) {
				if (!isEmptyCompositeData(cd, key)) {
					attributes.add(new EventReadOnlyAttribute(key, cd.getCompositeType().getType(key).getClassName(),
							cd.get(key)));
				}
			}
		}
		return attributes;
	}

	private boolean isEmptyCompositeData(CompositeData cd, String key) {
		if (cd.get(key) == null) {
			return true;
		}
		try {
			CompositeData[] cds = (CompositeData[]) cd.get(key);
			return cds.length == 0;
		} catch (ClassCastException e) {
			return false;
		}
	}

}
