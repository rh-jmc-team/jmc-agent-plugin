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
package org.openjdk.jmc.console.ext.agent.manager.model.impl;

import java.util.ArrayList;
import java.util.List;

import org.openjdk.jmc.console.ext.agent.manager.model.IEvent;
import org.openjdk.jmc.console.ext.agent.manager.model.IPreset;

public class Preset implements IPreset {
	private static final String DEFAULT_FILE_NAME = "newFile";
	private static final String DEFAULT_CLASS_PREFIX = "__JFREvent";
	private static final boolean DEFAULT_BOOLEAN_FIELD = true;
	private static final String ERROR_CANNOT_BE_EMPTY = "Field cannot be empty";
	private static final String ERROR_MUST_HAVE_UNIQUE_ID = "Event must have a unique Id";
	private static final String ERROR_MUST_HAVE_UNIQUE_EVENT_CLASS_NAME = "Event must have a unique event name per class";

	private String fileName;
	private String classPrefix;
	private boolean allowToString;
	private boolean allowConverter;
	private List<IEvent> events = new ArrayList<>();

	public Preset() {
		fileName = DEFAULT_FILE_NAME;
		classPrefix = DEFAULT_CLASS_PREFIX;
		allowToString = DEFAULT_BOOLEAN_FIELD;
		allowConverter = DEFAULT_BOOLEAN_FIELD;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public void setFileName(String fileName) {
		fileName = removeWhiteSpaces(fileName);
		if (fileName.isEmpty()) {
			throw new IllegalArgumentException(ERROR_CANNOT_BE_EMPTY);
		}
		this.fileName = fileName;
	}

	@Override
	public void setClassPrefix(String prefix) {
		this.classPrefix = prefix;
	}

	@Override
	public String getClassPrefix() {
		return classPrefix;
	}

	@Override
	public void setAllowToString(boolean allowed) {
		allowToString = allowed;
	}

	@Override
	public boolean getAllowToString() {
		return allowToString;
	}

	@Override
	public void setAllowConverter(boolean allowed) {
		allowConverter = allowed;
	}

	@Override
	public boolean getAllowConverter() {
		return allowConverter;
	}

	@Override
	public IEvent[] getEvents() {
		return events.toArray(new IEvent[0]);
	}

	@Override
	public void addEvent(IEvent event) {
		if (!hasUniqueId(event.getId())) {
			throw new IllegalArgumentException(ERROR_MUST_HAVE_UNIQUE_ID);
		}
		if (!hasUniqueEventClassName(event)) {
			throw new IllegalArgumentException(ERROR_MUST_HAVE_UNIQUE_EVENT_CLASS_NAME);
		}
		events.add(event);
	}

	@Override
	public void removeEvent(IEvent event) {
		events.remove(event);
	}

	@Override
	public boolean containEvent(IEvent event) {
		return events.contains(event);
	}

	private String removeWhiteSpaces(String stringWithSpaces) {
		return stringWithSpaces.replaceAll("\\s+", "");
	}

	private boolean hasUniqueId(String id) {
		for (IEvent e : events) {
			if (e.getId().equals(id)) {
				return false;
			}
		}
		return true;
	}

	private boolean hasUniqueEventClassName(IEvent event) {
		for (IEvent e : events) {
			if (e.getClazz().equals(event.getClazz()) && e.getName().equals(event.getName())) {
				return false;
			}
		}
		return true;
	}

}
