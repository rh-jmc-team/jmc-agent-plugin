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
import java.util.Collection;
import java.util.List;

import javax.management.openmbean.CompositeData;

import org.openjdk.jmc.rjmx.services.IReadOnlyAttribute;
import org.openjdk.jmc.rjmx.util.internal.AbstractReadOnlyAttribute;
import org.openjdk.jmc.rjmx.util.internal.PartitionedList;
import org.openjdk.jmc.rjmx.util.internal.SimpleAttributeInfo;

public class EventReadOnlyAttribute extends AbstractReadOnlyAttribute {

	private Object value;
	private EventReadOnlyAttribute parent = null;

	public EventReadOnlyAttribute(String name, String type, Object value) {
		super(new SimpleAttributeInfo(name, type));
		this.value = value;
	}

	public EventReadOnlyAttribute(EventReadOnlyAttribute parent, String name, String type, Object value) {
		this(name, type, value);
		this.parent = parent;
	}

	@Override
	public Object getValue() {
		return value;
	}

	@Override
	public boolean hasChildren() {
		if (getValue() instanceof CompositeData) {
			return !((CompositeData) getValue()).getCompositeType().keySet().isEmpty();
		}
		return super.hasChildren();
	}

	@Override
	public Collection<?> getChildren() {
		if (value instanceof CompositeData) {
			return getCompositeChildren((CompositeData) getValue());
		}
		return super.getChildren();
	}

	private Collection<?> getCompositeChildren(CompositeData cd) {
		List<IReadOnlyAttribute> elements = new ArrayList<>();
		for (String key : cd.getCompositeType().keySet()) {
			if (cd.get(key) != null) {
				elements.add(new EventReadOnlyAttribute(parent, key, cd.getCompositeType().getType(key).getClassName(),
						cd.get(key)));
			}
		}
		return PartitionedList.create(elements);
	}

}
